package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.ErrorMapper
import dam.moviles.tareas_app_front.data.remote.ResultadoGuardarTarea
import dam.moviles.tareas_app_front.data.remote.dto.CrearTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import dam.moviles.tareas_app_front.ui.components.FechaPickerField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTareaDialog(
    tiposTarea: List<TipoTareaResponseDto>,
    fechaInicial: String = "",
    onCerrar: () -> Unit,
    onCrearTipo: () -> Unit,
    onGuardar: suspend (CrearTareaRequestDto) -> ResultadoGuardarTarea
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember(fechaInicial) {
        mutableStateOf(fechaInicial)
    }
    var urgencia by remember { mutableIntStateOf(0) }

    var tipoSeleccionado by remember { mutableStateOf<TipoTareaResponseDto?>(null) }
    var menuTiposAbierto by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    var tituloError by remember { mutableStateOf<String?>(null) }
    var descripcionError by remember { mutableStateOf<String?>(null) }
    var fechaError by remember { mutableStateOf<String?>(null) }
    var tipoError by remember { mutableStateOf<String?>(null) }
    var urgenciaError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val azul = Color(0xFF2196F3)
    val fondoDialog = Color(0xFF121212)
    val blanco = Color.White
    val gris = Color(0xFFBDBDBD)
    val rojo = Color(0xFFFF5252)

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                onCerrar()
            }
        },
        containerColor = fondoDialog,
        title = {
            Text(
                text = "Nueva tarea",
                color = blanco,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        tituloError = null
                        error = null
                    },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = tituloError != null,
                    colors = coloresTextField()
                )

                if (tituloError != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = tituloError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        descripcion = it
                        descripcionError = null
                    },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 120.dp,
                            max = 160.dp
                        ),
                    minLines = 3,
                    maxLines = 5,
                    colors = coloresTextField()
                )

                if (descripcionError != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = descripcionError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FechaPickerField(
                    fecha = fecha,
                    onFechaSeleccionada = { fechaElegida ->
                        fecha = fechaElegida
                        fechaError = null
                        error = null
                    },
                    label = "Fecha",
                    isError = fechaError != null,
                    errorText = fechaError
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Urgencia: ${
                        when (urgencia) {
                            0 -> "Baja"
                            1 -> "Media"
                            2 -> "Alta"
                            else -> "Baja"
                        }
                    }",
                    color = blanco
                )

                Slider(
                    value = urgencia.toFloat(),
                    onValueChange = { nuevoValor ->
                        urgencia = nuevoValor.toInt()
                    },
                    valueRange = 0f..2f,
                    steps = 1
                )

                if (urgenciaError != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = urgenciaError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = menuTiposAbierto,
                    onExpandedChange = {
                        menuTiposAbierto = !menuTiposAbierto
                    }
                ) {
                    OutlinedTextField(
                        value = tipoSeleccionado?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        isError = tipoError != null,
                        label = { Text("Tipo de tarea") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = menuTiposAbierto
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                        colors = coloresTextField()
                    )

                    ExposedDropdownMenu(
                        expanded = menuTiposAbierto,
                        onDismissRequest = {
                            menuTiposAbierto = false
                        }
                    ) {
                        tiposTarea.forEach { tipo ->

                            val colorTipo = try {
                                Color(android.graphics.Color.parseColor(tipo.color))
                            } catch (e: Exception) {
                                azul
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(
                                                    color = colorTipo,
                                                    shape = CircleShape
                                                )
                                        )

                                        Spacer(modifier = Modifier.size(8.dp))

                                        Text(
                                            text = tipo.nombre
                                        )
                                    }
                                },
                                onClick = {
                                    tipoSeleccionado = tipo
                                    menuTiposAbierto = false
                                    error = null
                                }
                            )
                        }
                    }
                }

                if (tipoError != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = tipoError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onCrearTipo()
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B1B1B),
                        contentColor = azul
                    )
                ) {
                    Text("+ Añadir tipo de tarea")
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = error!!,
                        color = rojo
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isLoading) {
                        return@Button
                    }

                    tituloError = null
                    descripcionError = null
                    fechaError = null
                    tipoError = null
                    urgenciaError = null
                    error = null

                    var hayError = false

                    if (titulo.isBlank()) {
                        tituloError = "El título es obligatorio"
                        hayError = true
                    } else if (titulo.length < 3) {
                        tituloError = "El título debe tener mínimo 3 caracteres"
                        hayError = true
                    } else if (titulo.length > 100) {
                        tituloError = "El título debe tener máximo 100 caracteres"
                        hayError = true
                    }

                    if (descripcion.length > 500) {
                        descripcionError = "La descripción debe tener máximo 500 caracteres"
                        hayError = true
                    }

                    if (fecha.isBlank()) {
                        fechaError = "La fecha es obligatoria"
                        hayError = true
                    }

                    if (tipoSeleccionado == null) {
                        tipoError = "Selecciona un tipo de tarea"
                        hayError = true
                    }

                    if (hayError) {
                        return@Button
                    }

                    val tareaNueva = CrearTareaRequestDto(
                        titulo = titulo.trim(),
                        descripcion = descripcion.ifBlank { null },
                        fecha = fecha.trim(),
                        completada = false,
                        urgencia = urgencia,
                        tipoTareaId = tipoSeleccionado!!.id
                    )

                    scope.launch {
                        isLoading = true

                        val resultado = try {
                            onGuardar(tareaNueva)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ResultadoGuardarTarea.ErrorGeneral(
                                ErrorMapper.mensajePara(e)
                            )
                        } finally {
                            isLoading = false
                        }

                        when (resultado) {
                            is ResultadoGuardarTarea.Exito -> {
                                onCerrar()
                            }
                            is ResultadoGuardarTarea.ErrorCampos -> {
                                resultado.errores.forEach { (campo, mensaje) ->
                                    when (campo) {
                                        "titulo" -> tituloError = mensaje
                                        "descripcion" -> descripcionError = mensaje
                                        "fecha" -> fechaError = mensaje
                                        "tipoTareaId" -> tipoError = mensaje
                                        "urgencia" -> urgenciaError = mensaje
                                    }
                                }
                            }
                            is ResultadoGuardarTarea.ErrorGeneral -> {
                                error = resultado.mensaje
                            }
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCerrar()
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "Cancelar",
                    color = gris
                )
            }
        }
    )
}