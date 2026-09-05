package dam.moviles.tareas_app_front.data.session

import android.content.Context

/**
 * Almacenamiento simple de credenciales de sesión.
 *
 * Desacopla el acceso a tokens del almacenamiento real para poder
 * testear el interceptor y el authenticator sin instrumentación.
 */
interface TokenStore {
    fun obtenerAccessToken(): String?
    fun obtenerRefreshToken(): String?
    fun obtenerPersistirSesion(): Boolean
    fun guardarTokens(accessToken: String, refreshToken: String, persistir: Boolean)
    fun borrarTokens()
}

/**
 * Guarda los tokens de acceso y refresh.
 *
 * El refresh token rota en el backend: cada renovación devuelve un refresh
 * nuevo e invalida el anterior. Por eso access + refresh se guardan SIEMPRE
 * juntos, en una única transacción atómica sobre SharedPreferences.
 *
 * Semántica de "Mantener sesión iniciada":
 *  - persistir = true  -> ambos tokens se escriben en SharedPreferences y la
 *    sesión se restaura en futuras aperturas.
 *  - persistir = false -> los tokens viven solo en memoria del proceso. La
 *    sesión funciona mientras el proceso siga vivo, pero no se restaura tras
 *    cerrar/abrir la app. El refresh generado durante la sesión tampoco se
 *    persiste.
 *
 * RIESGO CONOCIDO (migración pendiente, no bloqueante):
 *  - Los tokens se almacenan en SharedPreferences (no cifrados con Keystore).
 *    Hacerlo bien requeriría Android Keystore o encrypción en capa propia;
 *    `androidx.security:security-crypto` está deprecado y su introducción aquí
 *    sería una migración arriesgada. Por ahora se mantiene SharedPreferences
 *    y no se loguea ningún token en ninguna parte.
 */
class TokenManager(
    context: Context
) : TokenStore {

    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        "sesion_usuario",
        Context.MODE_PRIVATE
    )

    // Caché en memoria compartida por todas las instancias del proceso.
    // Permite que "Mantener sesión" desactivado funcione durante la vida del
    // proceso aunque MainActivity, los workers y los interceptores usen
    // instancias distintas de TokenManager.
    companion object {
        const val CLAVE_ACCESS_TOKEN = "access_token"
        const val CLAVE_REFRESH_TOKEN = "refresh_token"
        const val CLAVE_PERSISTIR_SESION = "mantener_sesion"

        @Volatile
        private var memoriaAccess: String? = null

        @Volatile
        private var memoriaRefresh: String? = null

        @Volatile
        private var memoriaPersistir: Boolean? = null
    }

    override fun obtenerAccessToken(): String? {
        return memoriaAccess
            ?: sharedPreferences.getString(CLAVE_ACCESS_TOKEN, null)
    }

    override fun obtenerRefreshToken(): String? {
        return memoriaRefresh
            ?: sharedPreferences.getString(CLAVE_REFRESH_TOKEN, null)
    }

    override fun obtenerPersistirSesion(): Boolean {
        return memoriaPersistir
            ?: sharedPreferences.getBoolean(CLAVE_PERSISTIR_SESION, false)
    }

    /**
     * Guarda access + refresh de forma atómica.
     *
     * Si [persistir] es true se escriben ambos en disco en la misma
     * transacción (`commit` síncrono para que una rotación nunca quede a medias
     * si el proceso muere justo después). Si es false se limpia el disco y solo
     * queda la sesión en memoria del proceso.
     */
    override fun guardarTokens(
        accessToken: String,
        refreshToken: String,
        persistir: Boolean
    ) {
        memoriaAccess = accessToken
        memoriaRefresh = refreshToken
        memoriaPersistir = persistir

        val editor = sharedPreferences.edit()
            .putBoolean(CLAVE_PERSISTIR_SESION, persistir)

        if (persistir) {
            editor
                .putString(CLAVE_ACCESS_TOKEN, accessToken)
                .putString(CLAVE_REFRESH_TOKEN, refreshToken)
        } else {
            editor
                .remove(CLAVE_ACCESS_TOKEN)
                .remove(CLAVE_REFRESH_TOKEN)
        }

        // Limpieza de la clave legada del antiguo sistema de un solo token JWT.
        editor.remove("jwt_token")

        editor.commit()
    }

    override fun borrarTokens() {
        memoriaAccess = null
        memoriaRefresh = null
        memoriaPersistir = null

        sharedPreferences.edit()
            .remove(CLAVE_ACCESS_TOKEN)
            .remove(CLAVE_REFRESH_TOKEN)
            .remove(CLAVE_PERSISTIR_SESION)
            .remove("jwt_token")
            .commit()
    }
}