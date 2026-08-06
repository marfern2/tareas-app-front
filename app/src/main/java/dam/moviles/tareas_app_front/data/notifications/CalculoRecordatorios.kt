package dam.moviles.tareas_app_front.data.notifications

import dam.moviles.tareas_app_front.data.settings.AppSettings
import dam.moviles.tareas_app_front.ui.calendar.parsearFecha
import java.util.Calendar

/**
 * Funciones puras de cálculo de recordatorios.
 *
 * No dependen de Android, por lo que se pueden probar con JUnit en la JVM.
 */

/** Nivel de urgencia que se considera "urgencia alta" en la API. */
const val URGENCIA_ALTA = 2

fun esUrgenciaAlta(urgencia: Int): Boolean {
    return urgencia >= URGENCIA_ALTA
}

/**
 * Decide si una tarea debe tener recordatorio programado.
 *
 * Reglas:
 *  - una tarea completada nunca se programa;
 *  - si las notificaciones están desactivadas no se programa nada;
 *  - una tarea de urgencia alta necesita además el aviso específico activado.
 */
fun deberiaProgramarRecordatorio(
    tarea: TareaRecordatorio,
    ajustes: AppSettings
): Boolean {
    if (tarea.completada) {
        return false
    }

    if (!ajustes.notificacionesActivadas) {
        return false
    }

    if (esUrgenciaAlta(tarea.urgencia) && !ajustes.avisarUrgenciaAlta) {
        return false
    }

    return true
}

/**
 * Calcula el instante (epoch millis) en el que debe dispararse el recordatorio:
 *
 * fecha de la tarea - antelacionDias  a las  hora:minuto configurados.
 *
 * Devuelve null si la fecha es inválida o si el instante ya ha pasado
 * (en ese caso no se programa un recordatorio antiguo).
 */
fun calcularInstanteRecordatorio(
    fecha: String,
    hora: Int,
    minuto: Int,
    antelacionDias: Int,
    ahora: Calendar = Calendar.getInstance()
): Long? {
    val fechaTarea = parsearFecha(fecha) ?: return null

    val calendario = Calendar.getInstance()
    calendario.time = fechaTarea
    calendario.set(Calendar.HOUR_OF_DAY, 0)
    calendario.set(Calendar.MINUTE, 0)
    calendario.set(Calendar.SECOND, 0)
    calendario.set(Calendar.MILLISECOND, 0)
    calendario.add(Calendar.DAY_OF_MONTH, -antelacionDias)
    calendario.set(Calendar.HOUR_OF_DAY, hora)
    calendario.set(Calendar.MINUTE, minuto)

    val instante = calendario.timeInMillis
    return if (instante <= ahora.timeInMillis) {
        null
    } else {
        instante
    }
}

/**
 * Retraso en millis hasta la próxima ocurrencia de hora:minuto.
 * Se usa como initialDelay del resumen diario. Si la hora ya pasó hoy,
 * devuelve el retraso hasta mañana a esa hora.
 */
fun retrasoHastaProximaHora(
    hora: Int,
    minuto: Int,
    ahora: Calendar = Calendar.getInstance()
): Long {
    val objetivo = Calendar.getInstance()
    objetivo.time = ahora.time
    objetivo.set(Calendar.HOUR_OF_DAY, hora)
    objetivo.set(Calendar.MINUTE, minuto)
    objetivo.set(Calendar.SECOND, 0)
    objetivo.set(Calendar.MILLISECOND, 0)

    if (!objetivo.after(ahora)) {
        objetivo.add(Calendar.DAY_OF_MONTH, 1)
    }

    return objetivo.timeInMillis - ahora.timeInMillis
}
