package dam.moviles.tareas_app_front

import dam.moviles.tareas_app_front.data.session.TokenStore

/**
 * TokenStore en memoria para tests de la capa de autenticación.
 */
class TokenStoreFake(
    accessToken: String? = null,
    refreshToken: String? = null,
    private var persistir: Boolean = true
) : TokenStore {

    var accessToken = accessToken
        private set
    var refreshToken = refreshToken
        private set
    var llamadasGuardar = 0
        private set
    var llamadasBorrar = 0
        private set

    override fun obtenerAccessToken(): String? = accessToken

    override fun obtenerRefreshToken(): String? = refreshToken

    override fun obtenerPersistirSesion(): Boolean = persistir

    override fun guardarTokens(
        accessToken: String,
        refreshToken: String,
        persistir: Boolean
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.persistir = persistir
        llamadasGuardar++
    }

    override fun borrarTokens() {
        accessToken = null
        refreshToken = null
        llamadasBorrar++
    }
}