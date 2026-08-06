package dam.moviles.tareas_app_front.data.notifications

import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto

/*
 * Modelo mínimo que viaja en el WorkRequest de un recordatorio.
 *
 * No se guardan objetos Retrofit completos: solo los campos necesarios para
 * mostrar la notificación en segundo plano sin necesidad de red.
 */
data class TareaRecordatorio(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val fecha: String,
    val urgencia: Int,
    val tipoNombre: String,
    val completada: Boolean
) {
    companion object {
        fun desdeDto(tarea: TareaResponseDto): TareaRecordatorio {
            return TareaRecordatorio(
                id = tarea.id,
                titulo = tarea.titulo,
                descripcion = tarea.descripcion,
                fecha = tarea.fecha,
                urgencia = tarea.urgencia,
                tipoNombre = tarea.tipoTareaNombre,
                completada = tarea.completada
            )
        }
    }
}

/** Claves del Data de WorkManager. */
object ClavesWorkData {
    const val USUARIO_ID = "usuario_id"
    const val TAREA_ID = "tarea_id"
    const val TITULO = "titulo"
    const val DESCRIPCION = "descripcion"
    const val FECHA = "fecha"
    const val URGENCIA = "urgencia"
    const val TIPO_NOMBRE = "tipo_nombre"
    const val COMPLETADA = "completada"
    const val NOTIFICACIONES_ACTIVADAS = "notificaciones_activadas"
    const val RESUMEN_DIARIO_ACTIVADO = "resumen_diario_activado"
    const val AVISAR_VENCIDAS = "avisar_vencidas"
    const val AVISAR_URGENCIA = "avisar_urgencia"
    const val SONIDO = "sonido"
    const val VIBRACION = "vibracion"
}

/** Extras usados en el PendingIntent que abre la aplicación. */
object ExtrasNotificacion {
    const val DESTINO = "notificacion_destino"
    const val TAREA_ID = "notificacion_tarea_id"
    const val FECHA = "notificacion_fecha"

    const val DESTINO_CALENDARIO = "calendario"
    const val DESTINO_AJUSTES = "ajustes"
}
