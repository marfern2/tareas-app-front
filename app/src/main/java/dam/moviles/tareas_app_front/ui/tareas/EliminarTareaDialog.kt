package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto

@Composable
fun EliminarTareaDialog(
    tarea: TareaResponseDto,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Color(0xFF121212),
        title = {
            Text(
                text = "Eliminar tarea",
                color = Color.White
            )
        },
        text = {
            Text(
                text = "¿Seguro que quieres eliminar \"${tarea.titulo}\"?",
                color = Color.LightGray
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar
            ) {
                Text(
                    text = "Cancelar",
                    color = Color(0xFF2196F3)
                )
            }
        }
    )
}