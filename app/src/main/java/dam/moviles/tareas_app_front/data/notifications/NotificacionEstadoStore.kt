package dam.moviles.tareas_app_front.data.notifications

import android.content.Context

/**
 * Pequeño almacén local que recuerda qué avisos de tareas vencidas ya se han
 * enviado cada día, para no repetir varias notificaciones de la misma tarea.
 */
class NotificacionEstadoStore(
    context: Context
) {
    private val prefs = context.getSharedPreferences(
        "notificaciones_estado",
        Context.MODE_PRIVATE
    )

    fun fueVencidaNotificadaHoy(
        usuarioId: Long,
        tareaId: Long,
        dia: String
    ): Boolean {
        return prefs.getString(clave(usuarioId, tareaId), null) == dia
    }

    fun marcarVencidaNotificada(
        usuarioId: Long,
        tareaId: Long,
        dia: String
    ) {
        prefs.edit()
            .putString(clave(usuarioId, tareaId), dia)
            .apply()
    }

    fun limpiarUsuario(usuarioId: Long) {
        val prefijo = prefijoUsuario(usuarioId)

        val clavesUsuario = prefs.all.keys.filter { clave ->
            clave.startsWith(prefijo)
        }

        if (clavesUsuario.isEmpty()) {
            return
        }

        prefs.edit().apply {
            clavesUsuario.forEach { clave ->
                remove(clave)
            }
        }.apply()
    }

    private fun clave(usuarioId: Long, tareaId: Long): String {
        return "${prefijoUsuario(usuarioId)}_$tareaId"
    }

    private fun prefijoUsuario(usuarioId: Long): String {
        return "vencida_$usuarioId"
    }
}
