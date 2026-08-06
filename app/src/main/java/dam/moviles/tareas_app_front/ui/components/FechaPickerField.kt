package dam.moviles.tareas_app_front.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.ui.tareas.coloresTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaPickerField(
    fecha: String,
    onFechaSeleccionada: (String) -> Unit,
    label: String = "Fecha",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null
) {
    var mostrarDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = convertirFechaAMillis(fecha)
    )

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = fecha,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = true,
            singleLine = true,
            isError = isError,
            colors = coloresTextField(),
            trailingIcon = {
                Text(
                    text = "📅",
                    color = Color.White
                )
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) {
                    mostrarDatePicker = true
                }
        )
    }

    if (errorText != null) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = errorText,
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                mostrarDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fechaMillis = datePickerState.selectedDateMillis

                        if (fechaMillis != null) {
                            val fechaFormateada = convertirMillisAFecha(fechaMillis)
                            onFechaSeleccionada(fechaFormateada)
                        }

                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}

private fun convertirMillisAFecha(millis: Long): String {
    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(millis))
}

private fun convertirFechaAMillis(fecha: String): Long? {
    if (fecha.isBlank()) {
        return null
    }

    return try {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("UTC")
        formato.parse(fecha)?.time
    } catch (e: Exception) {
        null
    }
}