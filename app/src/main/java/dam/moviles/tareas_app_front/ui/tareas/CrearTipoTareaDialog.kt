package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dam.moviles.tareas_app_front.data.remote.dto.CrearTipoTareaRequestDto

@Composable
fun CrearTipoTareaDialog(
    onCerrar: () -> Unit,
    onGuardar: (CrearTipoTareaRequestDto) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var colorSeleccionado by remember { mutableStateOf("#2196F3") }
    var error by remember { mutableStateOf<String?>(null) }

    val colores = listOf(
        "#2196F3",
        "#4F46E5",
        "#22C55E",
        "#F59E0B",
        "#EF4444",
        "#EC4899",
        "#A855F7"
    )

    val fondoDialog = Color(0xFF121212)
    val blanco = Color.White
    val gris = Color(0xFFBDBDBD)
    val azul = Color(0xFF2196F3)

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = fondoDialog,
        title = {
            Text(
                text = "Nuevo tipo de tarea",
                color = blanco,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        error = null
                    },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = coloresTextField()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                    },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = coloresTextField()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Color",
                    color = blanco,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colores.forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (colorSeleccionado == colorHex) 3.dp else 1.dp,
                                    color = if (colorSeleccionado == colorHex) blanco else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    colorSeleccionado = colorHex
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 18.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(colorSeleccionado)),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )

                    Spacer(modifier = Modifier.size(10.dp))

                    Text(
                        text = colorSeleccionado,
                        color = gris
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = error!!,
                        color = Color(0xFFFF5252)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        error = "El nombre es obligatorio"
                        return@Button
                    }

                    if (nombre.length < 2) {
                        error = "Debe tener mínimo 2 caracteres"
                        return@Button
                    }

                    onGuardar(
                        CrearTipoTareaRequestDto(
                            nombre = nombre.trim(),
                            descripcion = descripcion.ifBlank { null },
                            color = colorSeleccionado
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = azul,
                    contentColor = blanco
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCerrar
            ) {
                Text(
                    text = "Cancelar",
                    color = gris
                )
            }
        }
    )
}