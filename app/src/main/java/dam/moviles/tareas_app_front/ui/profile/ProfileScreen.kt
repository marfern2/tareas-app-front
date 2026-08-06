package dam.moviles.tareas_app_front.ui.profile

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.repository.TareasRepository
import dam.moviles.tareas_app_front.ui.components.AnimatedBlueButton
import dam.moviles.tareas_app_front.ui.components.AvatarUsuario
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    token: String,
    nombreUsuario: String,
    emailUsuario: String,
    fotoPerfilUri: String?,
    onFotoCambiada: (String) -> Unit,
    onVolver: () -> Unit
) {
    BackHandler {
        onVolver()
    }

    val context = LocalContext.current
    val repository = remember { TareasRepository() }
    val scope = rememberCoroutineScope()

    var tareas by remember {
        mutableStateOf<List<TareaResponseDto>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var fotoActual by remember(fotoPerfilUri) {
        mutableStateOf(fotoPerfilUri)
    }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            val nuevaUri = uri.toString()

            fotoActual = nuevaUri
            onFotoCambiada(nuevaUri)
        }
    }

    fun cargarTareas() {
        scope.launch {
            try {
                isLoading = true
                error = null
                tareas = repository.obtenerTareas(token)
            } catch (e: Exception) {
                e.printStackTrace()
                error = "No se pudieron cargar las estadísticas"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarTareas()
    }

    val resumen = remember(tareas) {
        calcularResumenPerfil(tareas)
    }

    val colorScheme = MaterialTheme.colorScheme
    val fondo = colorScheme.background
    val tarjeta = colorScheme.surface
    val tarjetaSecundaria = colorScheme.surfaceVariant
    val azul = colorScheme.primary
    val blanco = colorScheme.onBackground
    val gris = colorScheme.onSurfaceVariant
    val rojo = colorScheme.error
    val verde = Color(0xFF35C984)

    Scaffold(
        containerColor = fondo
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
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
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.size(14.dp))

                Column {
                    Text(
                        text = "Mi perfil",
                        color = blanco,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tu actividad y productividad",
                        color = gris,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = tarjeta
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarUsuario(
                        nombreUsuario = nombreUsuario,
                        fotoUri = fotoActual,
                        tamano = 110.dp,
                        onClick = {
                            selectorFoto.launch(
                                arrayOf("image/*")
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = nombreUsuario.ifBlank { "Usuario" },
                        color = blanco,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (emailUsuario.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = emailUsuario,
                            color = gris,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Pulsa la foto para cambiarla",
                        color = azul,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Resumen",
                color = blanco,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = azul
                    )
                }
            } else if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tarjeta
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = rojo,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedBlueButton(
                            text = "Reintentar",
                            onClick = {
                                cargarTareas()
                            },
                            expanded = true
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaEstadistica(
                        titulo = "Total",
                        valor = resumen.total.toString(),
                        colorValor = azul,
                        modifier = Modifier.weight(1f)
                    )

                    TarjetaEstadistica(
                        titulo = "Completadas",
                        valor = resumen.completadas.toString(),
                        colorValor = verde,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaEstadistica(
                        titulo = "Pendientes",
                        valor = resumen.pendientes.toString(),
                        colorValor = Color(0xFFFFB547),
                        modifier = Modifier.weight(1f)
                    )

                    TarjetaEstadistica(
                        titulo = "Vencidas",
                        valor = resumen.vencidas.toString(),
                        colorValor = rojo,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tarjetaSecundaria
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Progreso general",
                                    color = blanco,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${resumen.porcentajeCompletado}% completado",
                                    color = gris,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = "${resumen.porcentajeCompletado}%",
                                color = azul,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(
                                    color = Color(0xFF283548),
                                    shape = RoundedCornerShape(50.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        fraction = resumen.porcentajeCompletado / 100f
                                    )
                                    .height(10.dp)
                                    .background(
                                        color = azul,
                                        shape = RoundedCornerShape(50.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEstadistica(
    titulo: String,
    valor: String,
    colorValor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = valor,
                color = colorValor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

private data class ResumenPerfil(
    val total: Int,
    val completadas: Int,
    val pendientes: Int,
    val vencidas: Int,
    val porcentajeCompletado: Int
)

private fun calcularResumenPerfil(
    tareas: List<TareaResponseDto>
): ResumenPerfil {
    val total = tareas.size

    val completadas = tareas.count { tarea ->
        tarea.completada
    }

    val pendientes = tareas.count { tarea ->
        !tarea.completada
    }

    val vencidas = tareas.count { tarea ->
        !tarea.completada && esFechaVencida(tarea.fecha)
    }

    val porcentaje = if (total == 0) {
        0
    } else {
        ((completadas.toFloat() / total.toFloat()) * 100)
            .toInt()
    }

    return ResumenPerfil(
        total = total,
        completadas = completadas,
        pendientes = pendientes,
        vencidas = vencidas,
        porcentajeCompletado = porcentaje
    )
}

private fun esFechaVencida(fechaTexto: String): Boolean {
    val fechaTarea = convertirFecha(fechaTexto) ?: return false

    val calendarioHoy = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return fechaTarea.before(calendarioHoy.time)
}

private fun convertirFecha(fechaTexto: String): Date? {
    return try {
        val formato = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        formato.isLenient = false
        formato.parse(fechaTexto)
    } catch (e: Exception) {
        null
    }
}