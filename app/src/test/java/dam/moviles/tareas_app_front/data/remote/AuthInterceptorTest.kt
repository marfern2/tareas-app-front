package dam.moviles.tareas_app_front.data.remote

import dam.moviles.tareas_app_front.TokenStoreFake
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var servidor: MockWebServer
    private lateinit var store: TokenStoreFake

    @Before
    fun setUp() {
        servidor = MockWebServer()
        servidor.start()
        store = TokenStoreFake(accessToken = "ACCESS_1", refreshToken = "REFRESH_1")
    }

    @After
    fun tearDown() {
        servidor.shutdown()
    }

    private fun clienteConInterceptor(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(store))
            .build()
    }

    private fun ejecutar(path: String) {
        servidor.enqueue(MockResponse().setResponseCode(200))
        clienteConInterceptor().newCall(
            Request.Builder()
                .url(servidor.url(path))
                .build()
        ).execute().close()
    }

    // 1. Interceptor añade Bearer access token a peticiones protegidas.
    @Test
    fun peticionProtegida_recibeAuthorizationBearer() {
        ejecutar("tareas")

        val peticion = servidor.takeRequest()

        assertEquals("Bearer ACCESS_1", peticion.getHeader("Authorization"))
    }

    @Test
    fun peticionProtegidaOtroPath_tambienRecibeBearer() {
        ejecutar("tipos-tarea")

        val peticion = servidor.takeRequest()

        assertEquals("Bearer ACCESS_1", peticion.getHeader("Authorization"))
    }

    // 2. Endpoints de autenticación no reciben Bearer innecesariamente.
    @Test
    fun login_noRecibeBearer() {
        ejecutar("auth/login")

        val peticion = servidor.takeRequest()

        assertNull(peticion.getHeader("Authorization"))
    }

    @Test
    fun refresh_noRecibeBearer() {
        ejecutar("auth/refresh")

        val peticion = servidor.takeRequest()

        assertNull(peticion.getHeader("Authorization"))
    }

    @Test
    fun logout_noRecibeBearer() {
        ejecutar("auth/logout")

        val peticion = servidor.takeRequest()

        assertNull(peticion.getHeader("Authorization"))
    }

    @Test
    fun registro_noRecibeBearer() {
        ejecutar("auth/registro")

        val peticion = servidor.takeRequest()

        assertNull(peticion.getHeader("Authorization"))
    }

    // 3. Sin access token no se añade la cabecera.
    @Test
    fun sinAccessToken_noAnadeCabecera() {
        store = TokenStoreFake(accessToken = null, refreshToken = "REFRESH_1")

        ejecutar("tareas")

        val peticion = servidor.takeRequest()

        assertNull(peticion.getHeader("Authorization"))
    }
}