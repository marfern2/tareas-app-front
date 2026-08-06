package dam.moviles.tareas_app_front.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalendarDayCell(
    dia: DiaCalendario,
    estado: EstadoDiaCalendario,
    esHoy: Boolean,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val estadoColor = when (estado) {
        EstadoDiaCalendario.VENCIDAS -> colorScheme.error
        EstadoDiaCalendario.PENDIENTES -> colorScheme.primary
        EstadoDiaCalendario.COMPLETADAS -> colorCompletadas()
        EstadoDiaCalendario.MIXTAS -> colorScheme.secondary
        EstadoDiaCalendario.SIN_TAREAS -> colorScheme.onSurfaceVariant
    }

    val mostrarIndicador = estado != EstadoDiaCalendario.SIN_TAREAS && dia.perteneceAlMes

    val fondoCelda = when {
        seleccionado -> colorScheme.primary
        mostrarIndicador -> estadoColor.copy(alpha = 0.13f)
        else -> Color.Transparent
    }

    val colorNumero = when {
        seleccionado -> colorScheme.onPrimary
        esHoy -> colorScheme.primary
        dia.perteneceAlMes -> colorScheme.onSurface
        else -> colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }

    val formaCelda = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(formaCelda)
            .background(fondoCelda)
            .then(
                if (esHoy && !seleccionado) {
                    Modifier.border(
                        width = 2.dp,
                        color = colorScheme.primary,
                        shape = formaCelda
                    )
                } else {
                    Modifier
                }
            )
            .clickable {
                onClick()
            }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dia.dia.toString(),
                color = colorNumero,
                fontSize = 14.sp,
                fontWeight = if (esHoy || seleccionado) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )

            if (mostrarIndicador) {
                Spacer(modifier = Modifier.height(3.dp))

                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 3.dp)
                        .background(
                            color = if (seleccionado) {
                                colorScheme.onPrimary
                            } else {
                                estadoColor
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
