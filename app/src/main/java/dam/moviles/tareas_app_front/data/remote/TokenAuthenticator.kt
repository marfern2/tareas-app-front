package dam.moviles.tareas_app_front.data.remote

import dam.moviles.tareas_app_front.data.remote.dto.RefreshRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.RefreshResponseDto
import dam.moviles.tareas_app_front.data.session.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Define cómo refrescar el access token cuando una petición protegida recibe 401.
 */
fun interface AuthRefresher {
    fun refrescar(refreshToken: String): RefreshResponseDto
}

/**
 * Responde a los 401 de peticiones protegidas renovando la sesión de forma
 * transparente para el usuario.
 *
 * Flujo: 401 protegido -> refresh con el refresh token guardado -> guardar
 * access + refresh nuevos (atómico) -> reintentar la petición original con el
 * access nuevo.
 *
 * - Nunca reintenta un 401 del propio /auth/refresh.
 * - Nunca hace más de un reintento por petición (priorResponse != null).
 * - Solo hay un refresh simultáneo: el resto de hilos reutilizan el token
 *   recién renovado sin volver a llamar al servidor.
 * - Un fallo de red (IOException/timeout) durante el refresh NO borra la
 *   sesión: se propaga para que la UI muestre SIN_CONEXION/TIMEOUT.
 * - Un 401/403 (o rechazo 4xx) del refresh demuestra que la sesión ya no es
 *   renovable: se limpian los tokens y se avisa a la UI.
 *
 * La llamada a /auth/refresh DEBE ejecutarse a través de un cliente OkHttp
 * separado, sin este authenticator ni el AuthInterceptor (ver RetrofitClient),
 * para evitar recursión RetrofitClient -> Authenticator -> RetrofitClient.
 */
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refresher: AuthRefresher,
    private val notificarSesionInvalida: () -> Unit = {}
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val peticionOriginal = response.request

        // Un 401 del propio endpoint de refresh no se reintenta jamás.
        if (esPeticionRefresh(peticionOriginal)) {
            return null
        }

        // Evita retry infinito: como mucho conectamos con un reintento.
        if (response.priorResponse != null) {
            return null
        }

        val tokenUsado = extraerAccessToken(peticionOriginal)

        // Se serializa el proceso completo para que solo un hilo refresque.
        synchronized(lock) {
            val accessActual = tokenStore.obtenerAccessToken()

            // Si otro request ya renovó el token mientras esperábamos el lock,
            // reutilizamos el nuevo sin volver a llamar a /auth/refresh.
            if (accessActual != null && accessActual != tokenUsado) {
                return reconstruirConToken(peticionOriginal, accessActual)
            }

            val refreshToken = tokenStore.obtenerRefreshToken()

            if (refreshToken.isNullOrBlank()) {
                // No hay forma de renovar la sesión.
                tokenStore.borrarTokens()
                notificarSesionInvalida()
                return null
            }

            val respuestaRefresh = try {
                runBlocking {
                    refresher.refrescar(refreshToken)
                }
            } catch (e: HttpException) {
                if (esSesionNoRenovable(e.code())) {
                    tokenStore.borrarTokens()
                    notificarSesionInvalida()
                    return null
                }
                // 5xx/429/timeouts del servidor: transitorios, no se borra nada.
                throw e
            } catch (e: IOException) {
                // Falta de red o timeout durante el refresh: NO se borra la sesión.
                throw e
            }

            val nuevoAccess = respuestaRefresh.token
            val nuevoRefresh = respuestaRefresh.refreshToken

            if (nuevoAccess.isNullOrBlank() || nuevoRefresh.isNullOrBlank()) {
                // Respuesta inválida pero sin prueba de que el refresh esté muerto.
                return null
            }

            // Rotación atómica: access + refresh nuevo se guardan juntos.
            tokenStore.guardarTokens(
                accessToken = nuevoAccess,
                refreshToken = nuevoRefresh,
                persistir = tokenStore.obtenerPersistirSesion()
            )

            return reconstruirConToken(peticionOriginal, nuevoAccess)
        }
    }

    private fun esPeticionRefresh(peticion: Request): Boolean {
        return peticion.url.encodedPath.startsWith("/auth/refresh")
    }

    private fun esSesionNoRenovable(codigo: Int): Boolean {
        // 401: refresh expirado/revocado. 403 u otros 4xx (excepto 408 y 429
        // que son transitorios): el servidor rechaza activamente la sesión.
        return codigo == 401 ||
            codigo == 403 ||
            (codigo in 400..499 && codigo != 408 && codigo != 429)
    }

    private fun extraerAccessToken(peticion: Request): String? {
        val auth = peticion.header("Authorization")
            ?: return null
        val prefijo = "Bearer "
        return if (auth.startsWith(prefijo)) {
            auth.substring(prefijo.length)
        } else {
            null
        }
    }

    private fun reconstruirConToken(peticion: Request, accessToken: String): Request {
        return peticion.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }
}