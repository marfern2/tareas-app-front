package dam.moviles.tareas_app_front.data.remote

import dam.moviles.tareas_app_front.TokenStoreFake
import dam.moviles.tareas_app_front.data.remote.api.AuthApi
import dam.moviles.tareas_app_front.data.remote.dto.RefreshRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.RefreshResponseDto
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.util.concurrent.TimeUnit

class TokenAuthenticatorTest {

    private interface ApiDePrueba {
        @GET("tareas")
        suspend fun tareas()

        @GET("tipos")
        suspend fun tipos()

        @GET("auth/refresh")
        suspend fun refreshManual()
    }

    private lateinit var servidor: MockWebServer
    private lateinit var store: TokenStoreFake
    private lateinit var refresher: RefresherContador
    private var notificadaSesionInvalida = false

    private inner class RefresherContador(
        private val authApi: AuthApi
    ) : AuthRefresher {
        var llamadas = 0
            private set

        override fun refrescar(refreshToken: String): RefreshResponseDto {
            llamadas++
            return runBlocking {
                authApi.refresh(RefreshRequestDto(refreshToken))
            }
        }
    }

    @Before
    fun setUp() {
        servidor = MockWebServer()
        servidor.start()
        store = TokenStoreFake(
            accessToken = "ACCESS_ANTIGUO",
            refreshToken = "REFRESH_ANTIGUO"
        )
        refresher = RefresherContador(authApiRenovacion())
        notificadaSesionInvalida = false
    }

    @After
    fun tearDown() {
        servidor.shutdown()
    }

    // Cliente "limpio" exclusivo para /auth/refresh, igual que en producción.
    private fun authApiRenovacion(): AuthApi {
        return Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    private fun clientePrincipal(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(store))
            .authenticator(
                TokenAuthenticator(
                    tokenStore = store,
                    refresher = refresher,
                    notificarSesionInvalida = {
                        notificadaSesionInvalida = true
                    }
                )
            )
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    private fun api(): ApiDePrueba {
        return Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .client(clientePrincipal())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiDePrueba::class.java)
    }

    private fun respuestaRefresh(
        access: String,
        refresh: String
    ): MockResponse {
        return MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(
                """{"token":"$access","refreshToken":"$refresh","type":"Bearer"}"""
            )
    }

    // 3. 401 protegido -> refresh -> reintento con access nuevo.
    @Test
    fun tareasProt_401_refresca_yReintenta_conAccessNuevo() {
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(respuestaRefresh("ACCESS_NUEVO", "REFRESH_NUEVO"))
        servidor.enqueue(MockResponse().setResponseCode(200))

        // No lanza excepción: el refresco transparente completó la petición.
        runBlocking { api().tareas() }

        assertEquals(1, refresher.llamadas)

        // La sesión se renueva y se guardan LOS DOS tokens rotados.
        assertEquals("ACCESS_NUEVO", store.accessToken)
        assertEquals("REFRESH_NUEVO", store.refreshToken)

        // Peticiones observadas en el servidor.
        val primera = servidor.takeRequest()
        val refresh = servidor.takeRequest()
        val reintento = servidor.takeRequest()

        assertEquals("Bearer ACCESS_ANTIGUO", primera.getHeader("Authorization"))
        assertNull(refresh.getHeader("Authorization"))
        assertEquals("Bearer ACCESS_NUEVO", reintento.getHeader("Authorization"))
        assertTrue(!notificadaSesionInvalida)
    }

    // Los reintentos reutilizan el lock: primero el header ya es el nuevo.
    @Test
    fun refreshOk_peroReintentoVuelveA401_noSeReintentaOtraVez() {
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(respuestaRefresh("ACCESS_NUEVO", "REFRESH_NUEVO"))
        servidor.enqueue(MockResponse().setResponseCode(401))

        try {
            runBlocking { api().tareas() }
            fail("Debería lanzar HttpException 401")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }

        // 401 protegido + refresh + 401 del reintento. Nada más.
        assertEquals(3, servidor.requestCount)
        assertEquals(1, refresher.llamadas)
        // El refresh tuvo éxito, así que los tokens NO se borran.
        assertEquals("ACCESS_NUEVO", store.accessToken)
        assertEquals("REFRESH_NUEVO", store.refreshToken)
        assertTrue(!notificadaSesionInvalida)
    }

    // 4. El refresh rota TAMBIÉN el refresh token (assert explícito).
    @Test
    fun refreshGuarda_tambienElRefreshTokenRotado() {
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(respuestaRefresh("ACCESS_ROTADO", "REFRESH_ROTADO"))
        servidor.enqueue(MockResponse().setResponseCode(200))

        runBlocking { api().tareas() }

        assertEquals("ACCESS_ROTADO", store.accessToken)
        assertEquals("REFRESH_ROTADO", store.refreshToken)
        assertEquals(1, refresher.llamadas)
    }

    // 5. Refresh 401 -> sesión inválida: borra tokens, avisa, sin reintentos.
    @Test
    fun refresh_devuelve401_limpiaSesionSinRetryInfinito() {
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(MockResponse().setResponseCode(401))

        try {
            runBlocking { api().tareas() }
            fail("Debería lanzar HttpException 401")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }

        assertNull(store.accessToken)
        assertNull(store.refreshToken)
        assertTrue(notificadaSesionInvalida)
        // Solo 401 + refresh 401. No hay reintentos infinitos.
        assertEquals(2, servidor.requestCount)
    }

    // 6. IOException durante el refresh -> NO se borran tokens.
    @Test
    fun ioExceptionDuranteRefresh_noBorraTokens() {
        servidor.enqueue(MockResponse().setResponseCode(401))

        val storeExtra = TokenStoreFake(
            accessToken = "ACCESS_ANTIGUO",
            refreshToken = "REFRESH_ANTIGUO"
        )

        val authenticator = TokenAuthenticator(
            tokenStore = storeExtra,
            refresher = AuthRefresher {
                throw IOException("sin conexión")
            }
        )

        val cliente = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(storeExtra))
            .authenticator(authenticator)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiDePrueba::class.java)

        try {
            runBlocking { retrofit.tareas() }
            fail("Debería lanzar IOException")
        } catch (e: IOException) {
            // Propagado: la UI lo mapea como SIN_CONEXION/TIMEOUT.
        }

        assertEquals("ACCESS_ANTIGUO", storeExtra.accessToken)
        assertEquals("REFRESH_ANTIGUO", storeExtra.refreshToken)
    }

    // 7. Dos 401 simultáneos -> solo un refresh.
    @Test
    fun dos401Simultaneos_soloGeneranUnRefresh() {
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(MockResponse().setResponseCode(401))
        servidor.enqueue(respuestaRefresh("ACCESS_NUEVO", "REFRESH_NUEVO"))
        servidor.enqueue(MockResponse().setResponseCode(200))
        servidor.enqueue(MockResponse().setResponseCode(200))

        val servicio = api()

        runBlocking {
            val a = async { servicio.tareas() }
            val b = async { servicio.tipos() }

            a.await()
            b.await()
        }

        assertEquals(1, refresher.llamadas)
        assertEquals("ACCESS_NUEVO", store.accessToken)
        assertEquals("REFRESH_NUEVO", store.refreshToken)
    }

    // Un 401 del propio endpoint /auth/refresh no se reintenta jamás.
    @Test
    fun refreshEndpoint401_noIntentaRefrescar() {
        servidor.enqueue(MockResponse().setResponseCode(401))

        try {
            runBlocking { api().refreshManual() }
            fail("Debería lanzar HttpException 401")
        } catch (e: HttpException) {
            assertEquals(401, e.code())
        }

        assertEquals(0, refresher.llamadas)
        assertEquals(1, servidor.requestCount)
    }
}