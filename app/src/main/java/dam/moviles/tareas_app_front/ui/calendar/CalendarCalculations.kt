package dam.moviles.tareas_app_front.ui.calendar

import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val FORMATO_FECHA = "yyyy-MM-dd"

fun parsearFecha(fecha: String): Date? {
    return try {
        val formato = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault())
        formato.isLenient = false
        formato.parse(fecha)
    } catch (e: Exception) {
        null
    }
}

fun formatearFecha(fecha: Date): String {
    val formato = SimpleDateFormat(FORMATO_FECHA, Locale.getDefault())
    return formato.format(fecha)
}

fun normalizarFecha(fecha: String): String {
    val fechaParseada = parsearFecha(fecha) ?: return fecha
    return formatearFecha(fechaParseada)
}

fun hoy(): String {
    return formatearFecha(Calendar.getInstance().time)
}

fun fechaDesdePartes(anio: Int, mes: Int, dia: Int): String {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(anio, mes, dia)
    return formatearFecha(calendario.time)
}

fun obtenerAnio(fecha: String): Int {
    return obtenerCampo(fecha, Calendar.YEAR)
}

fun obtenerMes(fecha: String): Int {
    return obtenerCampo(fecha, Calendar.MONTH)
}

fun obtenerDia(fecha: String): Int {
    return obtenerCampo(fecha, Calendar.DAY_OF_MONTH)
}

private fun obtenerCampo(fecha: String, campo: Int): Int {
    val fechaParseada = parsearFecha(fecha) ?: return 0
    val calendario = Calendar.getInstance()
    calendario.time = fechaParseada
    return calendario.get(campo)
}

fun celdasVaciasInicioMes(mes: Int, anio: Int): Int {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(anio, mes, 1)
    val diaSemana = calendario.get(Calendar.DAY_OF_WEEK)
    return (diaSemana + 5) % 7
}

fun numeroDiasMes(mes: Int, anio: Int): Int {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(anio, mes + 1, 0)
    return calendario.get(Calendar.DAY_OF_MONTH)
}

fun numeroFilasMes(mes: Int, anio: Int): Int {
    val celdasVacias = celdasVaciasInicioMes(mes, anio)
    val totalCeldas = celdasVacias + numeroDiasMes(mes, anio)
    return (totalCeldas + 6) / 7
}

fun mesAnterior(anio: Int, mes: Int): Pair<Int, Int> {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(anio, mes, 1)
    calendario.add(Calendar.MONTH, -1)
    return calendario.get(Calendar.YEAR) to calendario.get(Calendar.MONTH)
}

fun mesSiguiente(anio: Int, mes: Int): Pair<Int, Int> {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(anio, mes, 1)
    calendario.add(Calendar.MONTH, 1)
    return calendario.get(Calendar.YEAR) to calendario.get(Calendar.MONTH)
}

fun construirDiasMes(mes: Int, anio: Int): List<DiaCalendario> {
    val celdasVacias = celdasVaciasInicioMes(mes, anio)
    val numeroDias = numeroDiasMes(mes, anio)
    val totalCeldas = numeroFilasMes(mes, anio) * 7

    val (anioAnterior, mesAnterior) = mesAnterior(anio, mes)
    val diasMesAnterior = numeroDiasMes(mesAnterior, anioAnterior)

    val (anioSiguiente, mesSiguiente) = mesSiguiente(anio, mes)

    val resultado = mutableListOf<DiaCalendario>()

    for (i in 0 until celdasVacias) {
        val dia = diasMesAnterior - celdasVacias + 1 + i
        resultado.add(
            DiaCalendario(
                anio = anioAnterior,
                mes = mesAnterior,
                dia = dia,
                perteneceAlMes = false
            )
        )
    }

    for (dia in 1..numeroDias) {
        resultado.add(
            DiaCalendario(
                anio = anio,
                mes = mes,
                dia = dia,
                perteneceAlMes = true
            )
        )
    }

    val restantes = totalCeldas - resultado.size

    for (i in 1..restantes) {
        resultado.add(
            DiaCalendario(
                anio = anioSiguiente,
                mes = mesSiguiente,
                dia = i,
                perteneceAlMes = false
            )
        )
    }

    return resultado
}

fun agruparTareasPorFecha(
    tareas: List<TareaResponseDto>
): Map<String, List<TareaResponseDto>> {
    return tareas.groupBy { tarea ->
        normalizarFecha(tarea.fecha)
    }
}

private fun truncarFecha(fecha: String): Date? {
    val fechaParseada = parsearFecha(fecha) ?: return null
    val calendario = Calendar.getInstance()
    calendario.time = fechaParseada
    calendario.set(Calendar.HOUR_OF_DAY, 0)
    calendario.set(Calendar.MINUTE, 0)
    calendario.set(Calendar.SECOND, 0)
    calendario.set(Calendar.MILLISECOND, 0)
    return calendario.time
}

fun compararFechasSinHora(fechaA: String, fechaB: String): Int {
    val fechaATruncada = truncarFecha(fechaA) ?: return 0
    val fechaBTruncada = truncarFecha(fechaB) ?: return 0
    return fechaATruncada.compareTo(fechaBTruncada)
}

fun esFechaAnterior(fechaA: String, fechaB: String): Boolean {
    return compararFechasSinHora(fechaA, fechaB) < 0
}

fun esFechaVencida(fecha: String, hoy: String): Boolean {
    return esFechaAnterior(fecha, hoy)
}

fun calcularEstadoDia(
    tareasDelDia: List<TareaResponseDto>,
    hoy: String
): EstadoDiaCalendario {
    if (tareasDelDia.isEmpty()) {
        return EstadoDiaCalendario.SIN_TAREAS
    }

    val hayVencidas = tareasDelDia.any { tarea ->
        !tarea.completada && esFechaVencida(tarea.fecha, hoy)
    }

    val completadas = tareasDelDia.count { tarea ->
        tarea.completada
    }

    val pendientes = tareasDelDia.count { tarea ->
        !tarea.completada
    }

    return when {
        hayVencidas -> EstadoDiaCalendario.VENCIDAS
        completadas > 0 && pendientes > 0 -> EstadoDiaCalendario.MIXTAS
        pendientes > 0 -> EstadoDiaCalendario.PENDIENTES
        completadas > 0 -> EstadoDiaCalendario.COMPLETADAS
        else -> EstadoDiaCalendario.SIN_TAREAS
    }
}

fun nombreMesAnio(anio: Int, mes: Int): String {
    val calendario = Calendar.getInstance()
    calendario.clear()
    calendario.set(anio, mes, 1)

    val formato = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return capitalizar(formato.format(calendario.time))
}

fun formatearFechaLarga(fecha: String): String {
    val fechaParseada = parsearFecha(fecha) ?: return fecha

    val formato = SimpleDateFormat(
        "EEEE d 'de' MMMM 'de' yyyy",
        Locale.getDefault()
    )

    return capitalizar(formato.format(fechaParseada))
}

private fun capitalizar(texto: String): String {
    return texto.replaceFirstChar { caracter ->
        if (caracter.isLowerCase()) {
            caracter.titlecase()
        } else {
            caracter.toString()
        }
    }
}
