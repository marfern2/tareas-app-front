package dam.moviles.tareas_app_front.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters

/**
 * Worker de un recordatorio individual.
 *
 * Recibe únicamente los datos mínimos de la tarea en el Data del WorkRequest,
 * por lo que no necesita red ni token persistente.
 */
class TaskReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val data = inputData
        val usuarioId = data.getLong(ClavesWorkData.USUARIO_ID, -1L)

        if (usuarioId <= 0) {
            return Result.success()
        }

        val tarea = leerTarea(data) ?: return Result.success()

        if (tarea.completada) {
            return Result.success()
        }

        val ajustes = leerAjustes(data)

        if (!ajustes.notificacionesActivadas) {
            return Result.success()
        }

        // Comprobación de seguridad: si los avisos urgentes se desactivaron
        // después de programar, no se envía la notificación.
        if (esUrgenciaAlta(tarea.urgencia) && !ajustes.avisarUrgencia) {
            return Result.success()
        }

        NotificationHelper.enviarRecordatorio(
            context = applicationContext,
            tarea = tarea,
            usuarioId = usuarioId,
            sonido = ajustes.sonido,
            vibracion = ajustes.vibracion
        )

        return Result.success()
    }

    private fun leerTarea(data: Data): TareaRecordatorio? {
        val id = data.getLong(ClavesWorkData.TAREA_ID, -1L)
        val titulo = data.getString(ClavesWorkData.TITULO)
        val fecha = data.getString(ClavesWorkData.FECHA)

        if (id <= 0 || titulo.isNullOrBlank() || fecha.isNullOrBlank()) {
            return null
        }

        return TareaRecordatorio(
            id = id,
            titulo = titulo,
            descripcion = data.getString(ClavesWorkData.DESCRIPCION),
            fecha = fecha,
            urgencia = data.getInt(ClavesWorkData.URGENCIA, 0),
            tipoNombre = data.getString(ClavesWorkData.TIPO_NOMBRE).orEmpty(),
            completada = data.getBoolean(ClavesWorkData.COMPLETADA, false)
        )
    }

    private data class AjustesSnapshot(
        val notificacionesActivadas: Boolean,
        val avisarUrgencia: Boolean,
        val sonido: Boolean,
        val vibracion: Boolean
    )

    private fun leerAjustes(data: Data): AjustesSnapshot {
        return AjustesSnapshot(
            notificacionesActivadas = data.getBoolean(
                ClavesWorkData.NOTIFICACIONES_ACTIVADAS,
                false
            ),
            avisarUrgencia = data.getBoolean(
                ClavesWorkData.AVISAR_URGENCIA,
                false
            ),
            sonido = data.getBoolean(ClavesWorkData.SONIDO, true),
            vibracion = data.getBoolean(ClavesWorkData.VIBRACION, true)
        )
    }
}
