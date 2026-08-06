package dam.moviles.tareas_app_front.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.ui.components.AnimatedBlueButton

@Composable
fun CabeceraMes(
    anio: Int,
    mes: Int,
    animacionesActivadas: Boolean,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onIrHoy: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedBlueButton(
            text = "‹",
            onClick = onMesAnterior,
            size = 40.dp,
            fontSize = 26.sp,
            animacionesActivadas = animacionesActivadas,
            contentDescription = "Mes anterior"
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = nombreMesAnio(anio, mes),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = colorScheme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        AnimatedBlueButton(
            text = "Hoy",
            onClick = onIrHoy,
            size = 56.dp,
            fontSize = 13.sp,
            animacionesActivadas = animacionesActivadas,
            contentDescription = "Ir al mes actual"
        )

        Spacer(modifier = Modifier.width(8.dp))

        AnimatedBlueButton(
            text = "›",
            onClick = onMesSiguiente,
            size = 40.dp,
            fontSize = 26.sp,
            animacionesActivadas = animacionesActivadas,
            contentDescription = "Mes siguiente"
        )
    }
}

@Composable
fun CabeceraSemana() {
    val colorScheme = MaterialTheme.colorScheme
    val letras = listOf("L", "M", "X", "J", "V", "S", "D")

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        letras.forEach { letra ->
            Text(
                text = letra,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GridMes(
    mes: Int,
    anio: Int,
    tareasPorFecha: Map<String, List<TareaResponseDto>>,
    diaSeleccionado: String,
    hoyTexto: String,
    animacionesActivadas: Boolean,
    onDiaClick: (DiaCalendario) -> Unit
) {
    val contenido: @Composable (Int, Int) -> Unit = { mesActual, anioActual ->
        val dias = construirDiasMes(mesActual, anioActual)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            dias.chunked(7).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { dia ->
                        CalendarDayCell(
                            dia = dia,
                            estado = calcularEstadoDia(
                                tareasPorFecha[dia.aCadena()].orEmpty(),
                                hoyTexto
                            ),
                            esHoy = dia.aCadena() == hoyTexto,
                            seleccionado = dia.aCadena() == diaSeleccionado,
                            onClick = {
                                onDiaClick(dia)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (animacionesActivadas) {
        AnimatedContent(
            targetState = mes to anio,
            transitionSpec = {
                val haciaAdelante =
                    targetState.second > initialState.second ||
                        (targetState.second == initialState.second &&
                            targetState.first > initialState.first)

                val direccion = if (haciaAdelante) 1 else -1

                (slideInHorizontally { ancho -> direccion * ancho } + fadeIn()) togetherWith
                    (slideOutHorizontally { ancho -> -direccion * ancho } + fadeOut())
            }
        ) { (mesActual, anioActual) ->
            contenido(mesActual, anioActual)
        }
    } else {
        contenido(mes, anio)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeyendaCalendario() {
    val colorScheme = MaterialTheme.colorScheme

    val elementos = listOf(
        "Completadas" to colorCompletadas(),
        "Pendientes" to colorScheme.primary,
        "Mixtas" to colorScheme.secondary,
        "Vencidas" to colorScheme.error,
        "Sin tareas" to colorScheme.onSurfaceVariant
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        elementos.forEach { (texto, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )

                Spacer(modifier = Modifier.size(6.dp))

                Text(
                    text = texto,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
