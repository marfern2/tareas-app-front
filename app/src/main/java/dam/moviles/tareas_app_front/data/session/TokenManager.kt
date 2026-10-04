package dam.moviles.tareas_app_front.data.session

import android.content.Context

interface TokenStore {
    fun obtenerAccessToken(): String?
    fun obtenerRefreshToken(): String?
    fun obtenerPersistirSesion(): Boolean
    fun guardarTokens(accessToken: String, refreshToken: String, persistir: Boolean)
    fun borrarTokens()
}

/** Los tokens persistentes se leen únicamente del almacén cifrado tras migrar. */
class TokenManager internal constructor(
    context: Context,
    private val secure: SecureTokenStorage
) : TokenStore {
    constructor(context: Context) : this(context, KeystoreTokenStorage(context))

    private val legacy = context.getSharedPreferences(
        "sesion_usuario", Context.MODE_PRIVATE
    )

    companion object {
        const val CLAVE_ACCESS_TOKEN = "access_token"
        const val CLAVE_REFRESH_TOKEN = "refresh_token"
        const val CLAVE_PERSISTIR_SESION = "mantener_sesion"
        private const val CLAVE_JWT_LEGACY = "jwt_token"
        private val lock = Any()
        private var memoria: SessionTokens? = null
        private var memoriaPersistir: Boolean? = null
    }

    override fun obtenerAccessToken(): String? = synchronized(lock) {
        memoria?.access ?: tokensPersistentes()?.access
    }

    override fun obtenerRefreshToken(): String? = synchronized(lock) {
        memoria?.refresh ?: tokensPersistentes()?.refresh
    }

    override fun obtenerPersistirSesion(): Boolean = synchronized(lock) {
        memoriaPersistir ?: legacy.getBoolean(CLAVE_PERSISTIR_SESION, false)
    }

    override fun guardarTokens(accessToken: String, refreshToken: String, persistir: Boolean) {
        synchronized(lock) {
            if (persistir) {
                secure.write(SessionTokens(accessToken, refreshToken))
                borrarLegacyTokens(persistir = true)
                memoria = null
            } else {
                secure.clear()
                borrarLegacyTokens(persistir = false)
                memoria = SessionTokens(accessToken, refreshToken)
            }
            memoriaPersistir = persistir
        }
    }

    override fun borrarTokens() {
        synchronized(lock) {
            memoria = null
            memoriaPersistir = null
            var error: RuntimeException? = null
            try {
                secure.clear()
            } catch (e: RuntimeException) {
                error = e
            }
            try {
                comprobar(legacy.edit()
                    .remove(CLAVE_ACCESS_TOKEN)
                    .remove(CLAVE_REFRESH_TOKEN)
                    .remove(CLAVE_JWT_LEGACY)
                    .remove(CLAVE_PERSISTIR_SESION)
                    .commit())
            } catch (e: RuntimeException) {
                if (error == null) error = e else error.addSuppressed(e)
            }
            if (error != null) throw error
        }
    }

    private fun tokensPersistentes(): SessionTokens? {
        val guardados = secure.read()
        val accessLegacy = legacy.getString(CLAVE_ACCESS_TOKEN, null)
        val refreshLegacy = legacy.getString(CLAVE_REFRESH_TOKEN, null)
        val hayLegacy = accessLegacy != null || refreshLegacy != null ||
            legacy.contains(CLAVE_JWT_LEGACY)

        if (!hayLegacy) return guardados
        if (guardados != null) {
            borrarLegacyTokens()
            return guardados
        }
        if (accessLegacy == null || refreshLegacy == null) {
            borrarLegacyTokens()
            return null
        }

        val migrados = SessionTokens(accessLegacy, refreshLegacy)
        secure.write(migrados)
        borrarLegacyTokens()
        return migrados
    }

    private fun borrarLegacyTokens(persistir: Boolean? = null) {
        val editor = legacy.edit()
            .remove(CLAVE_ACCESS_TOKEN)
            .remove(CLAVE_REFRESH_TOKEN)
            .remove(CLAVE_JWT_LEGACY)
        if (persistir != null) editor.putBoolean(CLAVE_PERSISTIR_SESION, persistir)
        comprobar(editor.commit())
    }

    private fun comprobar(ok: Boolean) {
        if (!ok) throw IllegalStateException("No se pudo guardar la sesión")
    }
}
