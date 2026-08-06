package dam.moviles.tareas_app_front.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.ui.components.BadgeCompletada
import dam.moviles.tareas_app_front.ui.components.colorCompletadaSuave

@Composable
fun CalendarDayTaskItem(
    tarea: TareaResponseDto,
    onAbrirDetalle: () -> Unit,
    onEditar: () -> Unit,
    onCambiarCompletada: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val colorTipo = try {
        Color(android.graphics.Color.parseColor(tarea.tipoTareaColor))
    } catch (e: Exception) {
        colorScheme.primary
    }

    val textoUrgencia = when (tarea.urgencia) {
        0 -> "Urgencia baja"
        1 -> "Urgencia media"
        2 -> "Urgencia alta"
        else -> "Urgencia normal"
    }

    val colorUrgencia = when (tarea.urgencia) {
        0 -> Color(0xFF66BB6A)
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFFFF5252)
        else -> colorScheme.onSurfaceVariant
    }

    val fondoTarjeta = if (tarea.completada) {
        colorCompletadaSuave()
    } else {
        colorScheme.surface
    }

    val colorTitulo = if (tarea.completada) {
        colorScheme.onSurface.copy(alpha = 0.65f)
    } else {
        colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondoTarjeta
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colorTipo, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = tarea.titulo,
                    modifier = Modifier.weight(1f),
                    color = colorTitulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = onCambiarCompletada
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = textoUrgencia,
                        color = colorUrgencia,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${tarea.tipoTareaNombre} · ${
                            if (tarea.completada) "Completada" else "Pendiente"
                        }",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (tarea.completada) {
                        Spacer(modifier = Modifier.height(4.dp))

                        BadgeCompletada()
                    }
                }

                TextButton(
                    onClick = onAbrirDetalle,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Detalle",
                        color = colorScheme.primary
                    )
                }

                TextButton(
                    onClick = onEditar,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Editar",
                        color = colorScheme.primary
                    )
                }
            }
        }
    }
}
