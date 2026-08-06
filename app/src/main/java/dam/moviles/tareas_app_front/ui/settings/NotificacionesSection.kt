package dam.moviles.tareas_app_front.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dam.moviles.tareas_app_front.data.notifications.NotificationChannels
import dam.moviles.tareas_app_front.data.notifications.NotificationHelper
import dam.moviles.tareas_app_front.data.notifications.ResultadoNotificacionPrueba
import dam.moviles.tareas_app_front.data.settings.AppSettings
import kotlinx.coroutines.launch

internal enum class EstadoPermisoNotificaciones {
    PERMITIDAS,
    DESACTIVADAS_EN_APP,
    BLOQUEADAS_POR_ANDROID,
    PERMISO_PENDIENTE
}

internal data class OpcionAntelacion(
    val dias: Int,
    val titulo: String
)

internal val opcionesAntelacion = listOf(
    OpcionAntelacion(dias = 0, titulo = "El mismo día"),
    OpcionAntelacion(dias = 1, titulo = "1 día antes"),
    OpcionAntelacion(dias = 2, titulo = "2 días antes"),
    OpcionAntelacion(dias = 3, titulo = "3 días antes"),
    OpcionAntelacion(dias = 7, titulo = "1 semana antes")
)

internal fun formatearHora(hora: Int, minuto: Int): String {
    return "%02d:%02d".format(hora, minuto)
}

@Composable
fun SeccionNotificaciones(
    ajustes: AppSettings,
    onGuardarAjustes: (AppSettings) -> Unit,
    onReprogramarRecordatorios: () -> Unit
) {
    val context = LocalContext.current
    val sdk = Build.VERSION.SDK_INT

    val requierePermisoRuntime =
        sdk >= Build.VERSION_CODES.TIRAMISU

    val permisoRuntimeConcedido =
        !requierePermisoRuntime ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    val notificacionesSistemaHabilitadas =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    var mostrarExplicacionPermiso by remember { mutableStateOf(false) }
    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var mostrarDialogoBloqueoSistema by remember { mutableStateOf(false) }
    var mostrarMensajePermisoRechazado by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val currentAjustes by rememberUpdatedState(ajustes)
    val currentOnGuardar by rememberUpdatedState(onGuardarAjustes)
    val currentOnReprogramar by rememberUpdatedState(onReprogramarRecordatorios)

    val launcherPermisoNotificaciones =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { concedido ->
            scope.launch {
                if (concedido) {
                    currentOnGuardar(
                        currentAjustes.copy(notificacionesActivadas = true)
                    )
                    NotificationChannels.crearCanales(
                        context,
                        currentAjustes
                    )
                    currentOnReprogramar()
                } else {
                    currentOnGuardar(
                        currentAjustes.copy(notificacionesActivadas = false)
                    )
                    mostrarMensajePermisoRechazado = true
                }
            }
        }

    if (mostrarMensajePermisoRechazado) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context,
                "No has permitido las notificaciones. Puedes activarlas más tarde desde los ajustes del sistema.",
                Toast.LENGTH_LONG
            ).show()
            mostrarMensajePermisoRechazado = false
        }
    }

    val estadoPermiso = when {
        !ajustes.notificacionesActivadas ->
            EstadoPermisoNotificaciones.DESACTIVADAS_EN_APP
        !permisoRuntimeConcedido ->
            EstadoPermisoNotificaciones.PERMISO_PENDIENTE
        !notificacionesSistemaHabilitadas ->
            EstadoPermisoNotificaciones.BLOQUEADAS_POR_ANDROID
        else -> EstadoPermisoNotificaciones.PERMITIDAS
    }

    val habilitado = ajustes.notificacionesActivadas

    FilaConmutador(
        titulo = "Permitir notificaciones",
        descripcion = "Recibe avisos sobre tareas próximas y vencidas",
        activado = ajustes.notificacionesActivadas &&
            permisoRuntimeConcedido &&
            notificacionesSistemaHabilitadas,
        enabled = true,
        onCambiar = { activar ->
            if (!activar) {
                currentOnGuardar(
                    currentAjustes.copy(notificacionesActivadas = false)
                )
            } else {
                when {
                    !permisoRuntimeConcedido -> {
                        mostrarExplicacionPermiso = true
                    }

                    !notificacionesSistemaHabilitadas -> {
                        mostrarDialogoBloqueoSistema = true
                    }

                    else -> {
                        currentOnGuardar(
                            currentAjustes.copy(notificacionesActivadas = true)
                        )
                        currentOnReprogramar()
                    }
                }
            }
        }
    )

    TarjetaEstadoPermiso(
        estado = estadoPermiso,
        onSolicitarPermiso = {
            launcherPermisoNotificaciones.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        },
        onAbrirAjustesSistema = {
            NotificationHelper.abrirAjustesSistema(context)
        }
    )

    SeparadorFila()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (habilitado) 1f else 0.45f)
    ) {
        TituloControl(texto = "Hora de los recordatorios")

        FilaHora(
            texto = formatearHora(
                ajustes.horaNotificacion,
                ajustes.minutoNotificacion
            ),
            habilitado = habilitado,
            onClick = {
                mostrarSelectorHora = true
            }
        )

        TituloControl(texto = "Antelación predeterminada")

        SelectorAntelacion(
            seleccionado = ajustes.antelacionDias,
            habilitado = habilitado,
            onSeleccionar = { dias ->
                currentOnGuardar(
                    currentAjustes.copy(antelacionDias = dias)
                )
            }
        )

        SeparadorFila()

        FilaConmutador(
            titulo = "Resumen diario",
            descripcion = "Muestra las tareas pendientes, urgentes y vencidas del día",
            activado = ajustes.resumenDiarioActivado,
            enabled = habilitado,
            onCambiar = { valor ->
                currentOnGuardar(
                    currentAjustes.copy(resumenDiarioActivado = valor)
                )
            }
        )

        SeparadorFila()

        FilaConmutador(
            titulo = "Tareas vencidas",
            descripcion = "Avisa cuando una tarea ha quedado vencida",
            activado = ajustes.avisarTareasVencidas,
            enabled = habilitado,
            onCambiar = { valor ->
                currentOnGuardar(
                    currentAjustes.copy(avisarTareasVencidas = valor)
                )
            }
        )

        SeparadorFila()

        FilaConmutador(
            titulo = "Urgencia alta",
            descripcion = "Avisa con más prioridad las tareas de urgencia alta",
            activado = ajustes.avisarUrgenciaAlta,
            enabled = habilitado,
            onCambiar = { valor ->
                currentOnGuardar(
                    currentAjustes.copy(avisarUrgenciaAlta = valor)
                )
            }
        )

        SeparadorFila()

        FilaConmutador(
            titulo = "Sonido",
            descripcion = "Android puede cambiar esta opción desde los ajustes del canal",
            activado = ajustes.sonidoNotificaciones,
            enabled = habilitado,
            onCambiar = { valor ->
                currentOnGuardar(
                    currentAjustes.copy(sonidoNotificaciones = valor)
                )
            }
        )

        SeparadorFila()

        FilaConmutador(
            titulo = "Vibración",
            descripcion = "Android puede cambiar esta opción desde los ajustes del canal",
            activado = ajustes.vibracionNotificaciones,
            enabled = habilitado,
            onCambiar = { valor ->
                currentOnGuardar(
                    currentAjustes.copy(vibracionNotificaciones = valor)
                )
            }
        )

        FilaAbrirAjustesCanal(
            habilitado = habilitado,
            onClick = {
                NotificationHelper.abrirAjustesCanal(
                    context,
                    NotificationChannels.CANAL_RECORDATORIOS
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                when {
                    !permisoRuntimeConcedido -> {
                        mostrarExplicacionPermiso = true
                    }

                    !notificacionesSistemaHabilitadas -> {
                        mostrarDialogoBloqueoSistema = true
                    }

                    else -> {
                        val resultado = NotificationHelper.enviarPrueba(context)

                        if (resultado == ResultadoNotificacionPrueba.EXITO) {
                            Toast.makeText(
                                context,
                                "Notificación de prueba enviada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Enviar notificación de prueba")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onReprogramarRecordatorios,
            enabled = habilitado,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Reprogramar recordatorios")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Los recordatorios pueden ejecutarse con unos minutos de diferencia debido a las restricciones de batería de Android.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }

    if (mostrarExplicacionPermiso) {
        AlertDialog(
            onDismissRequest = {
                mostrarExplicacionPermiso = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Permitir notificaciones",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Para recibir recordatorios de tus tareas, la aplicación necesita tu permiso para mostrar notificaciones. Podrás cambiarlo en cualquier momento desde los ajustes del sistema.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarExplicacionPermiso = false
                        launcherPermisoNotificaciones.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                ) {
                    Text("Continuar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarExplicacionPermiso = false
                    }
                ) {
                    Text(
                        text = "Ahora no",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    if (mostrarDialogoBloqueoSistema) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoBloqueoSistema = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Notificaciones desactivadas",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Las notificaciones están desactivadas en los ajustes del sistema. Ábrelos para permitirlas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoBloqueoSistema = false
                        NotificationHelper.abrirAjustesSistema(context)
                    }
                ) {
                    Text("Abrir ajustes del sistema")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoBloqueoSistema = false
                    }
                ) {
                    Text(
                        text = "Ahora no",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    if (mostrarSelectorHora) {
        SelectorHora(
            ajustes = ajustes,
            onGuardarAjustes = currentOnGuardar,
            onCerrar = {
                mostrarSelectorHora = false
            }
        )
    }
}

@Composable
private fun TarjetaEstadoPermiso(
    estado: EstadoPermisoNotificaciones,
    onSolicitarPermiso: () -> Unit,
    onAbrirAjustesSistema: () -> Unit
) {
    val colorTexto = when (estado) {
        EstadoPermisoNotificaciones.PERMITIDAS -> MaterialTheme.colorScheme.primary
        EstadoPermisoNotificaciones.PERMISO_PENDIENTE -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = when (estado) {
                    EstadoPermisoNotificaciones.PERMITIDAS -> "Permitidas"
                    EstadoPermisoNotificaciones.DESACTIVADAS_EN_APP ->
                        "Desactivadas en la app"
                    EstadoPermisoNotificaciones.BLOQUEADAS_POR_ANDROID ->
                        "Bloqueadas por Android"
                    EstadoPermisoNotificaciones.PERMISO_PENDIENTE ->
                        "Permiso pendiente"
                },
                color = colorTexto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            when (estado) {
                EstadoPermisoNotificaciones.BLOQUEADAS_POR_ANDROID -> {
                    Text(
                        text = "Android está bloqueando las notificaciones de esta aplicación.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    TextButton(
                        onClick = onAbrirAjustesSistema,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Abrir ajustes del sistema")
                    }
                }

                EstadoPermisoNotificaciones.PERMISO_PENDIENTE -> {
                    Text(
                        text = "Concede el permiso para recibir recordatorios.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    TextButton(
                        onClick = onSolicitarPermiso,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Solicitar permiso")
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun FilaHora(
    texto: String,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hora preferida",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = texto,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SelectorAntelacion(
    seleccionado: Int,
    habilitado: Boolean,
    onSeleccionar: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        opcionesAntelacion.forEach { opcion ->
            val esSeleccionada = opcion.dias == seleccionado

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 3.dp)
                    .clickable(enabled = habilitado) {
                        onSeleccionar(opcion.dias)
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (esSeleccionada) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = esSeleccionada,
                        onClick = null,
                        enabled = habilitado,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = opcion.titulo,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaAbrirAjustesCanal(
    habilitado: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Text(
            text = "Abrir ajustes del canal",
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorHora(
    ajustes: AppSettings,
    onGuardarAjustes: (AppSettings) -> Unit,
    onCerrar: () -> Unit
) {
    val estadoHora = rememberTimePickerState(
        initialHour = ajustes.horaNotificacion,
        initialMinute = ajustes.minutoNotificacion,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Hora de los recordatorios",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            TimePicker(state = estadoHora)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGuardarAjustes(
                        ajustes.copy(
                            horaNotificacion = estadoHora.hour,
                            minutoNotificacion = estadoHora.minute
                        )
                    )
                    onCerrar()
                }
            ) {
                Text(
                    text = "Aceptar",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text(
                    text = "Cancelar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
