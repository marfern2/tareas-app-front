package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.settings.DensidadTareas
import dam.moviles.tareas_app_front.ui.components.BadgeCompletada
import dam.moviles.tareas_app_front.ui.components.colorCompletadaSuave

@Composable
fun TareaItem(
    tarea: TareaResponseDto,
    onClick: () -> Unit,
    onCompletarChange: (Boolean) -> Unit,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    densidad: DensidadTareas = DensidadTareas.NORMAL
) {
    var menuAbierto by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val blanco = colorScheme.onSurface
    val gris = colorScheme.onSurfaceVariant

    val paddingTarjeta = when (densidad) {
        DensidadTareas.COMPACTA -> 12.dp
        DensidadTareas.NORMAL -> 18.dp
        DensidadTareas.COMODA -> 22.dp
    }

    val colorTipo = try {
        Color(android.graphics.Color.parseColor(tarea.tipoTareaColor))
    } catch (e: Exception) {
        colorScheme.primary
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

    val colorDescripcion = if (tarea.completada) {
        colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    } else {
        colorScheme.onSurfaceVariant
    }

    val alphaBarra = if (tarea.completada) 0.55f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = fondoTarjeta
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(paddingTarjeta),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
                    .background(
                        color = colorTipo.copy(alpha = alphaBarra),
                        shape = RoundedCornerShape(20.dp)
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tarea.titulo,
                    color = colorTitulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!tarea.descripcion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tarea.descripcion,
                        color = colorDescripcion,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colorTipo, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = tarea.tipoTareaNombre,
                        color = colorTipo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = tarea.fecha,
                        color = gris,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (tarea.urgencia) {
                            0 -> "Urgencia baja"
                            1 -> "Urgencia media"
                            2 -> "Urgencia alta"
                            else -> "Urgencia normal"
                        },
                        color = when (tarea.urgencia) {
                            0 -> Color(0xFF66BB6A)
                            1 -> Color(0xFFFFC107)
                            2 -> Color(0xFFFF5252)
                            else -> gris
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (tarea.completada) {
                        Spacer(modifier = Modifier.width(8.dp))

                        BadgeCompletada()
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Checkbox(
                    checked = tarea.completada,
                    onCheckedChange = { marcado ->
                        onCompletarChange(marcado)
                    }
                )

                Box {
                    IconButton(
                        onClick = {
                            menuAbierto = true
                        }
                    ) {
                        Text(
                            text = "⋮",
                            color = blanco,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = {
                            menuAbierto = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Editar")
                            },
                            onClick = {
                                menuAbierto = false
                                onEditarClick()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("Eliminar")
                            },
                            onClick = {
                                menuAbierto = false
                                onEliminarClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
