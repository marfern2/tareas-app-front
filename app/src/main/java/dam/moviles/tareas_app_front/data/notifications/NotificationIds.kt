package dam.moviles.tareas_app_front.data.notifications

/*
 * IDs de notificación estables y sin colisiones evidentes.
 *
 * Una misma tarea usa siempre el mismo ID dentro del mismo usuario, de forma
 * que una notificación nueva reemplaza a la anterior en lugar de acumularse.
 * Usuarios distintos obtienen IDs distintos para la misma tarea.
 */
object NotificationIds {

    /** Desplazamiento para el código de solicitud de la acción "Ver tarea". */
    const val OFFSET_ACCION = 5_000_000

    fun idRecordatorio(usuarioId: Long, tareaId: Long): Int {
        return hashPositivo(usuarioId, tareaId)
    }

    fun idResumen(usuarioId: Long): Int {
        return hashPositivo(usuarioId, SEMILLA_RESUMEN)
    }

    fun idPrueba(): Int {
        return 1_000_000
    }

    fun codigoAccion(idNotificacion: Int): Int {
        return idNotificacion + OFFSET_ACCION
    }

    private fun hashPositivo(a: Long, b: Long): Int {
        val combinado = a * 31L + b
        return ((combinado xor (combinado ushr 32)) and Int.MAX_VALUE.toLong()).toInt()
    }

    private const val SEMILLA_RESUMEN = 987_654_321L
}
