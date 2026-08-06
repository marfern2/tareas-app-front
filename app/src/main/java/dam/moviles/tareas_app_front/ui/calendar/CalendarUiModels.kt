package dam.moviles.tareas_app_front.ui.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

enum class EstadoDiaCalendario {
    SIN_TAREAS,
    COMPLETADAS,
    PENDIENTES,
    MIXTAS,
    VENCIDAS
}

data class DiaCalendario(
    val anio: Int,
    val mes: Int,
    val dia: Int,
    val perteneceAlMes: Boolean
) {
    fun aCadena(): String {
        return fechaDesdePartes(anio, mes, dia)
    }
}

@Composable
fun esTemaOscuro(): Boolean {
    return MaterialTheme.colorScheme.surface.luminance() < 0.5f
}

@Composable
fun colorCompletadas(): Color {
    return if (esTemaOscuro()) {
        Color(0xFF66BB6A)
    } else {
        Color(0xFF2E7D32)
    }
}
