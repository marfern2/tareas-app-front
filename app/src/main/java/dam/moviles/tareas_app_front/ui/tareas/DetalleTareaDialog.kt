package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.ui.components.BadgeCompletada

@Composable
fun DetalleTareaDialog(
    tarea: TareaResponseDto,
    onCerrar: () -> Unit
) {
    val fondoDialog = Color(0xFF121212)
    val blanco = Color.White
    val gris = Color(0xFFBDBDBD)
    val azul = Color(0xFF2196F3)

    val colorTipo = try {
        Color(android.graphics.Color.parseColor(tarea.tipoTareaColor))
    } catch (e: Exception) {
        azul
    }

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = fondoDialog,
        title = {
            Text(
                text = tarea.titulo,
                color = blanco,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(colorTipo, CircleShape)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = tarea.tipoTareaNombre,
                        color = colorTipo,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Descripción",
                    color = blanco,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tarea.descripcion ?: "Sin descripción",
                    color = gris,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Fecha: ${tarea.fecha}",
                    color = gris
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado:",
                        color = gris
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    if (tarea.completada) {
                        BadgeCompletada(
                            fondo = Color(0xFF1E3A2B),
                            texto = Color(0xFF66BB6A)
                        )
                    } else {
                        Text(
                            text = "Pendiente",
                            color = gris
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Urgencia: ${
                        when (tarea.urgencia) {
                            0 -> "Baja"
                            1 -> "Media"
                            2 -> "Alta"
                            else -> "Normal"
                        }
                    }",
                    color = when (tarea.urgencia) {
                        0 -> Color(0xFF66BB6A)
                        1 -> Color(0xFFFFC107)
                        2 -> Color(0xFFFF5252)
                        else -> gris
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onCerrar
            ) {
                Text(
                    text = "Cerrar",
                    color = azul
                )
            }
        }
    )
}