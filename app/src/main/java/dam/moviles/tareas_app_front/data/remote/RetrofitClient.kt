package dam.moviles.tareas_app_front.data.remote

import android.content.Context
import dam.moviles.tareas_app_front.BuildConfig
import dam.moviles.tareas_app_front.data.remote.api.AuthApi
import dam.moviles.tareas_app_front.data.remote.api.TareasApi
import dam.moviles.tareas_app_front.data.remote.api.TiposTareaApi
import dam.moviles.tareas_app_front.data.remote.dto.RefreshRequestDto
import dam.moviles.tareas_app_front.data.session.SessionManager
import dam.moviles.tareas_app_front.data.session.TokenManager
import dam.moviles.tareas_app_front.data.session.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    @Volatile
    private var tokenStore: TokenStore? = null

    /**
     * Debe llamarse una sola vez al arrancar la app (DonitApplication),
     * antes de cualquier uso de las APIs. También cubre los procesos que
     * WorkManager arranca sin Activity.
     */
    fun iniciar(context: Context) {
        if (tokenStore == null) {
            tokenStore = TokenManager(context.applicationContext)
        }
    }

    private fun obtenerTokenStore(): TokenStore {
        val store = tokenStore
        if (store != null) {
            return store
        }
        error("RetrofitClient.iniciar(Context) debe llamarse antes de usar las APIs.")
    }

    // Interceptor de logs para desarrollo: nivel BASIC, no imprime bodies ni
    // cabeceras para no exponer access/refresh tokens ni contraseñas en Logcat.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Cliente principal con autenticación automática: añade el token de acceso
    // a las peticiones protegidas y renueva la sesión ante un 401.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(
                AuthInterceptor(obtenerTokenStore())
            )
            .authenticator(
                TokenAuthenticator(
                    tokenStore = obtenerTokenStore(),
                    refresher = AuthRefresher { refreshToken ->
                        runBlocking {
                            authApiRefresh.refresh(
                                RefreshRequestDto(refreshToken)
                            )
                        }
                    },
                    notificarSesionInvalida = {
                        SessionManager.marcarSesionInvalida()
                    }
                )
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Cliente mínimo y separado, SIN TokenAuthenticator ni AuthInterceptor,
    // usado exclusivamente para /auth/refresh (dentro del authenticator) y
    // /auth/logout. Evita la recursión RetrofitClient -> Authenticator ->
    // RetrofitClient: un 401 del propio refresh no puede volver a entrar en
    // este authenticator.
    private val refreshOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitRefresh: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(refreshOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    private val authApiRefresh: AuthApi by lazy {
        retrofitRefresh.create(AuthApi::class.java)
    }

    val tareasApi: TareasApi by lazy {
        retrofit.create(TareasApi::class.java)
    }

    val tiposTareaApi: TiposTareaApi by lazy {
        retrofit.create(TiposTareaApi::class.java)
    }

    /**
     * Cierre de sesión en el servidor. Tolera cualquier fallo (red, 401, 5xx):
     * la sesión local se limpia siempre por parte de la UI.
     */
    suspend fun cerrarSesion(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) {
            return
        }

        try {
            authApiRefresh.logout(RefreshRequestDto(refreshToken))
        } catch (_: Exception) {
            // La sesión local se limpia igualmente, aunque el backend falle.
        }
    }
}
