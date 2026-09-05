package dam.moviles.tareas_app_front.data.remote

import dam.moviles.tareas_app_front.data.session.TokenStore
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Añade automáticamente `Authorization: Bearer <accessToken>` a las peticiones
 * protegidas. Los endpoints de autenticación (login, registro, refresh,
 * logout) no llevan el token de forma innecesaria.
 */
class AuthInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val peticion = chain.request()

        if (esEndpointSinAutenticacion(peticion)) {
            return chain.proceed(peticion)
        }

        val accessToken = tokenStore.obtenerAccessToken()

        if (accessToken.isNullOrBlank()) {
            return chain.proceed(peticion)
        }

        val peticionConAuth = peticion.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(peticionConAuth)
    }

    private fun esEndpointSinAutenticacion(peticion: Request): Boolean {
        return peticion.url.encodedPath.startsWith("/auth/")
    }
}