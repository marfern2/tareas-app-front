package dam.moviles.tareas_app_front.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.settings.AppSettings
import dam.moviles.tareas_app_front.ui.components.AnimatedBlueButton

@Composable
fun SettingsScreen(
    ajustes: AppSettings,
    onGuardarAjustes: (AppSettings) -> Unit,
    onRestaurarAjustes: () -> Unit,
    onVolver: () -> Unit,
    onReprogramarRecordatorios: () -> Unit
) {
    BackHandler {
        onVolver()
    }

    var mostrarDialogoRestaurar by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 30.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedBlueButton(
                    text = "←",
                    onClick = onVolver,
                    size = 46.dp,
                    fontSize = 22.sp,
                    contentDescription = "Volver a mis tareas"
                )

                Spacer(modifier = Modifier.size(14.dp))

                Column {
                    Text(
                        text = "Ajustes",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Personaliza tu experiencia",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            TarjetaVistaPrevia()

            Spacer(modifier = Modifier.height(28.dp))

            SettingsSection(
                titulo = "Apariencia"
            ) {
                SelectorModoApariencia(
                    seleccionado = ajustes.modoApariencia,
                    onSeleccionar = { modo ->
                        onGuardarAjustes(
                            ajustes.copy(modoApariencia = modo)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSection(
                titulo = "Color principal"
            ) {
                SelectorColorTema(
                    seleccionado = ajustes.temaColor,
                    onSeleccionar = { tema ->
                        onGuardarAjustes(
                            ajustes.copy(temaColor = tema)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSection(
                titulo = "Lista de tareas"
            ) {
                TituloControl(texto = "Densidad")

                SelectorDensidad(
                    seleccionado = ajustes.densidadTareas,
                    onSeleccionar = { densidad ->
                        onGuardarAjustes(
                            ajustes.copy(densidadTareas = densidad)
                        )
                    }
                )

                SeparadorFila()

                FilaConmutador(
                    titulo = "Mostrar completadas",
                    descripcion = "Muestra también las tareas ya completadas",
                    activado = ajustes.mostrarCompletadas,
                    onCambiar = { mostrar ->
                        onGuardarAjustes(
                            ajustes.copy(mostrarCompletadas = mostrar)
                        )
                    }
                )

                SeparadorFila()

                TituloControl(texto = "Orden predeterminado")

                SelectorOrden(
                    seleccionado = ajustes.ordenTareas,
                    onSeleccionar = { orden ->
                        onGuardarAjustes(
                            ajustes.copy(ordenTareas = orden)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSection(
                titulo = "Comportamiento"
            ) {
                FilaConmutador(
                    titulo = "Confirmar antes de eliminar",
                    descripcion = "Pide confirmación al borrar una tarea",
                    activado = ajustes.confirmarEliminacion,
                    onCambiar = { confirmar ->
                        onGuardarAjustes(
                            ajustes.copy(confirmarEliminacion = confirmar)
                        )
                    }
                )

                SeparadorFila()

                FilaConmutador(
                    titulo = "Activar animaciones",
                    descripcion = "Efectos visuales al pulsar los botones",
                    activado = ajustes.animacionesActivadas,
                    onCambiar = { animaciones ->
                        onGuardarAjustes(
                            ajustes.copy(animacionesActivadas = animaciones)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSection(
                titulo = "Notificaciones y recordatorios"
            ) {
                SeccionNotificaciones(
                    ajustes = ajustes,
                    onGuardarAjustes = onGuardarAjustes,
                    onReprogramarRecordatorios = onReprogramarRecordatorios
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSection(
                titulo = "Aplicación"
            ) {
                FilaVersion()

                SeparadorFila()

                FilaRestaurar {
                    mostrarDialogoRestaurar = true
                }
            }
        }
    }

    if (mostrarDialogoRestaurar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoRestaurar = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Restaurar ajustes",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que quieres volver a los ajustes predeterminados?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestaurarAjustes()
                        mostrarDialogoRestaurar = false
                    }
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoRestaurar = false
                    }
                ) {
                    Text(
                        text = "Cancelar",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

@Composable
private fun TarjetaVistaPrevia() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Vista previa",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedBlueButton(
                text = "Botón de ejemplo",
                onClick = {},
                expanded = true,
                height = 50.dp,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            TarjetaTareaEjemplo()

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { 0.65f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun TarjetaTareaEjemplo() {
    val colorPrimario = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(colorPrimario, CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Tarea de ejemplo",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Etiqueta · 2026-01-01",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Checkbox(
                checked = false,
                onCheckedChange = null,
                modifier = Modifier.semantics {
                    contentDescription = "Tarea de ejemplo"
                }
            )
        }
    }
}

@Composable
private fun FilaVersion() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Versión",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Donit $VERSION_APLICACION",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun FilaRestaurar(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .semantics {
                contentDescription = "Restaurar ajustes"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Restaurar ajustes",
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
