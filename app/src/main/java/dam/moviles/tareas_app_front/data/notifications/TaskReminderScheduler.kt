package dam.moviles.tareas_app_front.data.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.repository.TareasRepository
import dam.moviles.tareas_app_front.data.session.TokenManager
import dam.moviles.tareas_app_front.data.settings.AppSettings
import java.util.concurrent.TimeUnit

/*
 * Punto único de programación de recordatorios y resumen diario.
 *
 * Todas las pantallas deben pasar por aquí en lugar de llamar a WorkManager
 * directamente.
 */
class TaskReminderScheduler(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    // -----------------------------------------------------------------
    // Recordatorios individuales
    // -----------------------------------------------------------------

    fun programarRecordatorio(
        tarea: TareaResponseDto,
        ajustes: AppSettings,
        usuarioId: Long
    ) {
        val tareaMinima = TareaRecordatorio.desdeDto(tarea)

        if (!deberiaProgramarRecordatorio(tareaMinima, ajustes)) {
            cancelarRecordatorio(tarea.id, usuarioId)
            return
        }

        val instante = calcularInstanteRecordatorio(
            fecha = tarea.fecha,
            hora = ajustes.horaNotificacion,
            minuto = ajustes.minutoNotificacion,
            antelacionDias = ajustes.antelacionDias
        )

        if (instante == null) {
            // El momento ya pasó: no se programa un recordatorio antiguo.
            // Las tareas vencidas las gestiona el worker diario.
            cancelarRecordatorio(tarea.id, usuarioId)
            return
        }

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(
                instante - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .setInputData(crearDataRecordatorio(tareaMinima, ajustes, usuarioId))
            .addTag(TAG_RECORDATORIOS + usuarioId)
            .build()

        workManager.enqueueUniqueWork(
            nombreUnicoRecordatorio(usuarioId, tarea.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelarRecordatorio(
        tareaId: Long,
        usuarioId: Long
    ) {
        workManager.cancelUniqueWork(
            nombreUnicoRecordatorio(usuarioId, tareaId)
        )
    }

    fun reprogramarTodas(
        tareas: List<TareaResponseDto>,
        ajustes: AppSettings,
        usuarioId: Long
    ) {
        workManager.cancelAllWorkByTag(TAG_RECORDATORIOS + usuarioId)

        tareas.forEach { tarea ->
            programarRecordatorio(tarea, ajustes, usuarioId)
        }
    }

    // -----------------------------------------------------------------
    // Resumen diario y tareas vencidas
    // -----------------------------------------------------------------

    fun programarResumenDiario(
        ajustes: AppSettings,
        usuarioId: Long
    ) {
        val hayAvisosDiarios = ajustes.notificacionesActivadas &&
            (ajustes.resumenDiarioActivado || ajustes.avisarTareasVencidas)

        if (!hayAvisosDiarios) {
            workManager.cancelUniqueWork(nombreUnicoResumen(usuarioId))
            return
        }

        val retrasoInicial = retrasoHastaProximaHora(
            hora = ajustes.horaNotificacion,
            minuto = ajustes.minutoNotificacion
        )

        val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24,
            TimeUnit.HOURS
        )
            .setInitialDelay(retrasoInicial, TimeUnit.MILLISECONDS)
            .setInputData(crearDataResumen(ajustes, usuarioId))
            .addTag(TAG_RESUMEN + usuarioId)
            .build()

        workManager.enqueueUniquePeriodicWork(
            nombreUnicoResumen(usuarioId),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    // -----------------------------------------------------------------
    // Operaciones globales por usuario
    // -----------------------------------------------------------------

    fun cancelarTodoDelUsuario(usuarioId: Long) {
        workManager.cancelAllWorkByTag(TAG_RECORDATORIOS + usuarioId)
        workManager.cancelUniqueWork(nombreUnicoResumen(usuarioId))
    }

    /**
     * Reprograma todos los recordatorios consultando el servidor.
     *
     * Usa la autenticación automática (el repositorio renueva el access token
     * ante un 401). Si no hay sesión renovable no hace nada: no envía
     * información antigua y no lanza errores. Si la petición falla, deja los
     * trabajos ya programados tal cual.
     */
    suspend fun sincronizarDesdeServidor(
        ajustes: AppSettings,
        usuarioId: Long
    ) {
        if (!ajustes.notificacionesActivadas) {
            cancelarTodoDelUsuario(usuarioId)
            return
        }

        // Sin refresh token no se puede renovar la sesión: no tiene sentido
        // consultar tareas en segundo plano.
        if (TokenManager(context).obtenerRefreshToken() == null) {
            return
        }

        val tareas = try {
            TareasRepository().obtenerTareas()
        } catch (_: Exception) {
            return
        }

        reprogramarTodas(tareas, ajustes, usuarioId)
        programarResumenDiario(ajustes, usuarioId)
    }

    // -----------------------------------------------------------------
    // Nombres únicos y tags
    // -----------------------------------------------------------------

    private fun nombreUnicoRecordatorio(usuarioId: Long, tareaId: Long): String {
        return "recordatorio_tarea_${usuarioId}_$tareaId"
    }

    private fun nombreUnicoResumen(usuarioId: Long): String {
        return "resumen_diario_$usuarioId"
    }

    companion object {
        private const val TAG_RECORDATORIOS = "recordatorios_usuario_"
        private const val TAG_RESUMEN = "resumen_usuario_"

        fun tagRecordatorios(usuarioId: Long): String {
            return TAG_RECORDATORIOS + usuarioId
        }

        fun tagResumen(usuarioId: Long): String {
            return TAG_RESUMEN + usuarioId
        }
    }

    // -----------------------------------------------------------------
    // Construcción del Data
    // -----------------------------------------------------------------

    private fun crearDataRecordatorio(
        tarea: TareaRecordatorio,
        ajustes: AppSettings,
        usuarioId: Long
    ): Data {
        return Data.Builder()
            .putLong(ClavesWorkData.USUARIO_ID, usuarioId)
            .putLong(ClavesWorkData.TAREA_ID, tarea.id)
            .putString(ClavesWorkData.TITULO, tarea.titulo)
            .putString(ClavesWorkData.DESCRIPCION, tarea.descripcion.orEmpty())
            .putString(ClavesWorkData.FECHA, tarea.fecha)
            .putInt(ClavesWorkData.URGENCIA, tarea.urgencia)
            .putString(ClavesWorkData.TIPO_NOMBRE, tarea.tipoNombre)
            .putBoolean(ClavesWorkData.COMPLETADA, tarea.completada)
            .putBoolean(
                ClavesWorkData.NOTIFICACIONES_ACTIVADAS,
                ajustes.notificacionesActivadas
            )
            .putBoolean(
                ClavesWorkData.AVISAR_URGENCIA,
                ajustes.avisarUrgenciaAlta
            )
            .putBoolean(ClavesWorkData.SONIDO, ajustes.sonidoNotificaciones)
            .putBoolean(ClavesWorkData.VIBRACION, ajustes.vibracionNotificaciones)
            .build()
    }

    private fun crearDataResumen(
        ajustes: AppSettings,
        usuarioId: Long
    ): Data {
        return Data.Builder()
            .putLong(ClavesWorkData.USUARIO_ID, usuarioId)
            .putBoolean(
                ClavesWorkData.NOTIFICACIONES_ACTIVADAS,
                ajustes.notificacionesActivadas
            )
            .putBoolean(
                ClavesWorkData.RESUMEN_DIARIO_ACTIVADO,
                ajustes.resumenDiarioActivado
            )
            .putBoolean(
                ClavesWorkData.AVISAR_VENCIDAS,
                ajustes.avisarTareasVencidas
            )
            .putBoolean(
                ClavesWorkData.AVISAR_URGENCIA,
                ajustes.avisarUrgenciaAlta
            )
            .putBoolean(ClavesWorkData.SONIDO, ajustes.sonidoNotificaciones)
            .putBoolean(ClavesWorkData.VIBRACION, ajustes.vibracionNotificaciones)
            .build()
    }
}
