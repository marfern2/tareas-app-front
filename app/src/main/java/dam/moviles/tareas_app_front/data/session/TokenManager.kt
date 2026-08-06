package dam.moviles.tareas_app_front.data.session

import android.content.Context

class TokenManager(
    private val context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        "sesion_usuario",
        Context.MODE_PRIVATE
    )

    fun guardarToken(token: String) {
        sharedPreferences.edit()
            .putString("jwt_token", token)
            .apply()
    }

    fun obtenerToken(): String? {
        return sharedPreferences.getString("jwt_token", null)
    }

    fun borrarToken() {
        sharedPreferences.edit()
            .remove("jwt_token")
            .apply()
    }
}