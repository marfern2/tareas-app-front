package dam.moviles.tareas_app_front.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dam.moviles.tareas_app_front.data.repository.TareasRepository
import dam.moviles.tareas_app_front.data.session.TokenManager
import dam.moviles.tareas_app_front.ui.calendar.esFechaAnterior
import dam.moviles.tareas_app_front.ui.calendar.hoy

/**
 * Worker del resumen diario y de los avisos de tareas vencidas.
 *
 * Consulta las tareas actualizadas en el servidor usando la autenticación
 * automática (el interceptor añade el access token y lo renueva ante un 401).
 * Si no hay sesión persistente (sin "mantener sesión iniciada") no envía nada:
 * nunca usa información antigua de otra cuenta.
 */
class DailySummaryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val data = inputData
        val usuarioId = data.getLong(ClavesWorkData.USUARIO_ID, -1L)

        if (usuarioId <= 0) {
            return Result.success()
        }

        if (!data.getBoolean(ClavesWorkData.NOTIFICACIONES_ACTIVADAS, false)) {
            return Result.success()
        }

        // Sin refresh token no se puede renovar la sesión: mejor no consultar
        // tareas que mostrar datos de otra cuenta o de una sesión muerta.
        if (TokenManager(applicationContext).obtenerRefreshToken() == null) {
            return Result.success()
        }

        val tareas = try {
            TareasRepository().obtenerTareas()
        } catch (_: Exception) {
            // Un 401 no renovable ya limpió la sesión; un fallo de red se
            // ignora para no lanzar notificaciones erróneas.
            return Result.success()
        }

        val sonido = data.getBoolean(ClavesWorkData.SONIDO, true)
        val vibracion = data.getBoolean(ClavesWorkData.VIBRACION, true)
        val hoyTexto = hoy()

        if (data.getBoolean(ClavesWorkData.RESUMEN_DIARIO_ACTIVADO, true)) {
            enviarResumen(tareas, usuarioId, hoyTexto, sonido, vibracion)
        }

        if (data.getBoolean(ClavesWorkData.AVISAR_VENCIDAS, true)) {
            enviarVencidas(tareas, usuarioId, hoyTexto, sonido, vibracion)
        }

        return Result.success()
    }

    private fun enviarResumen(
        tareas: List<dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto>,
        usuarioId: Long,
        hoyTexto: String,
        sonido: Boolean,
        vibracion: Boolean
    ) {
        val pendientes = tareas.count { tarea ->
            !tarea.completada && !esFechaAnterior(tarea.fecha, hoyTexto)
        }

        val urgentes = tareas.count { tarea ->
            !tarea.completada && esUrgenciaAlta(tarea.urgencia)
        }

        val vencidas = tareas.count { tarea ->
            !tarea.completada && esFechaAnterior(tarea.fecha, hoyTexto)
        }

        // No molestar: no se envía el resumen si no hay nada que reportar.
        if (pendientes + urgentes + vencidas == 0) {
            return
        }

        NotificationHelper.enviarResumen(
            context = applicationContext,
            pendientes = pendientes,
            urgentes = urgentes,
            vencidas = vencidas,
            usuarioId = usuarioId,
            sonido = sonido,
            vibracion = vibracion
        )
    }

    private fun enviarVencidas(
        tareas: List<dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto>,
        usuarioId: Long,
        hoyTexto: String,
        sonido: Boolean,
        vibracion: Boolean
    ) {
        val estadoStore = NotificacionEstadoStore(applicationContext)

        tareas
            .filter { tarea ->
                !tarea.completada && esFechaAnterior(tarea.fecha, hoyTexto)
            }
            .forEach { tarea ->
                if (!estadoStore.fueVencidaNotificadaHoy(
                        usuarioId = usuarioId,
                        tareaId = tarea.id,
                        dia = hoyTexto
                    )
                ) {
                    NotificationHelper.enviarTareaVencida(
                        context = applicationContext,
                        tarea = TareaRecordatorio.desdeDto(tarea),
                        usuarioId = usuarioId,
                        sonido = sonido,
                        vibracion = vibracion
                    )

                    estadoStore.marcarVencidaNotificada(
                        usuarioId = usuarioId,
                        tareaId = tarea.id,
                        dia = hoyTexto
                    )
                }
            }
    }
}
