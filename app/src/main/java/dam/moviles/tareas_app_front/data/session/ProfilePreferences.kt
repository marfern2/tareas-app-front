package dam.moviles.tareas_app_front.data.session

import android.content.Context

class ProfilePreferences(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "perfil_usuario",
        Context.MODE_PRIVATE
    )

    fun guardarUsuario(
        id: Long,
        username: String,
        email: String
    ) {
        sharedPreferences.edit()
            .putLong(CLAVE_USUARIO_ID, id)
            .putString(CLAVE_USERNAME, username)
            .putString(CLAVE_EMAIL, email)
            .apply()
    }

    fun obtenerUsuarioId(): Long? {
        val id = sharedPreferences.getLong(CLAVE_USUARIO_ID, -1L)

        return if (id == -1L) {
            null
        } else {
            id
        }
    }

    fun obtenerUsername(): String {
        return sharedPreferences.getString(
            CLAVE_USERNAME,
            ""
        ).orEmpty()
    }

    fun obtenerEmail(): String {
        return sharedPreferences.getString(
            CLAVE_EMAIL,
            ""
        ).orEmpty()
    }

    fun guardarFotoUri(
        usuarioId: Long,
        fotoUri: String
    ) {
        sharedPreferences.edit()
            .putString(claveFoto(usuarioId), fotoUri)
            .apply()
    }

    fun obtenerFotoUri(usuarioId: Long?): String? {
        if (usuarioId == null) {
            return null
        }

        return sharedPreferences.getString(
            claveFoto(usuarioId),
            null
        )
    }

    /*
     * Borra los datos del usuario que tiene la sesión abierta.
     *
     * No borra su foto. La foto queda asociada al ID del usuario,
     * por lo que volverá a aparecer cuando ese mismo usuario
     * vuelva a iniciar sesión.
     */
    fun borrarUsuarioActual() {
        sharedPreferences.edit()
            .remove(CLAVE_USUARIO_ID)
            .remove(CLAVE_USERNAME)
            .remove(CLAVE_EMAIL)
            .apply()
    }

    private fun claveFoto(usuarioId: Long): String {
        return "foto_perfil_$usuarioId"
    }

    companion object {
        private const val CLAVE_USUARIO_ID = "usuario_id"
        private const val CLAVE_USERNAME = "username"
        private const val CLAVE_EMAIL = "email"
    }
}