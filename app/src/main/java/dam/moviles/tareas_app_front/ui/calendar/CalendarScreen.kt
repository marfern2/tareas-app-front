package dam.moviles.tareas_app_front.ui.calendar

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.notifications.TaskReminderScheduler
import dam.moviles.tareas_app_front.data.remote.ApiErrorParser
import dam.moviles.tareas_app_front.data.remote.ResultadoGuardarTarea
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import dam.moviles.tareas_app_front.data.repository.TareasRepository
import dam.moviles.tareas_app_front.data.settings.AppSettings
import dam.moviles.tareas_app_front.ui.components.AnimatedBlueButton
import dam.moviles.tareas_app_front.ui.tareas.CrearTareaDialog
import dam.moviles.tareas_app_front.ui.tareas.CrearTipoTareaDialog
import dam.moviles.tareas_app_front.ui.tareas.DetalleTareaDialog
import dam.moviles.tareas_app_front.ui.tareas.EditarTareaDialog
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun CalendarScreen(
    token: String,
    usuarioId: Long?,
    ajustes: AppSettings,
    fechaInicial: String? = null,
    onVolver: () -> Unit
) {
    BackHandler {
        onVolver()
    }

    val repository = remember { TareasRepository() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    var tareas by remember {
        mutableStateOf<List<TareaResponseDto>>(emptyList())
    }

    var tiposTarea by remember {
        mutableStateOf<List<TipoTareaResponseDto>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    val hoyTexto = remember { hoy() }

    val calendarioInicial = remember(fechaInicial) {
        val calendarioTemp = Calendar.getInstance()

        val fechaElegida = fechaInicial

        if (!fechaElegida.isNullOrBlank()) {
            parsearFecha(fechaElegida)?.let { fecha ->
                calendarioTemp.time = fecha
            }
        }

        calendarioTemp
    }

    var anioVisible by remember(fechaInicial) {
        mutableIntStateOf(calendarioInicial.get(Calendar.YEAR))
    }

    var mesVisible by remember(fechaInicial) {
        mutableIntStateOf(calendarioInicial.get(Calendar.MONTH))
    }

    var diaSeleccionado by remember(fechaInicial) {
        mutableStateOf(
            fechaInicial
                ?.takeIf { fecha -> fecha.isNotBlank() }
                ?: hoyTexto
        )
    }

    var mostrarCrearTarea by remember {
        mutableStateOf(false)
    }

    var mostrarCrearTipo by remember {
        mutableStateOf(false)
    }

    var tareaDetalle by remember {
        mutableStateOf<TareaResponseDto?>(null)
    }

    var tareaEditando by remember {
        mutableStateOf<TareaResponseDto?>(null)
    }

    fun cargarDatos() {
        scope.launch {
            try {
                isLoading = true
                error = null

                tareas = repository.obtenerTareas(token)
                tiposTarea = repository.obtenerTiposTarea(token)
            } catch (e: Exception) {
                e.printStackTrace()
                error = "No se pudieron cargar las tareas"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(token) {
        cargarDatos()
    }

    val tareasVisibles = remember(tareas, ajustes.mostrarCompletadas) {
        if (ajustes.mostrarCompletadas) {
            tareas
        } else {
            tareas.filter { tarea ->
                !tarea.completada
            }
        }
    }

    val tareasPorFecha = remember(tareasVisibles) {
        agruparTareasPorFecha(tareasVisibles)
    }

    val tareasDelDia = remember(tareasPorFecha, diaSeleccionado) {
        tareasPorFecha[diaSeleccionado]
            .orEmpty()
            .sortedWith(
                compareBy({ tarea -> tarea.completada }, { tarea -> tarea.titulo.lowercase() })
            )
    }

    fun cambiarMes(delta: Int) {
        val calendarioTemp = Calendar.getInstance()
        calendarioTemp.clear()
        calendarioTemp.set(anioVisible, mesVisible, 1)
        calendarioTemp.add(Calendar.MONTH, delta)

        val nuevoAnio = calendarioTemp.get(Calendar.YEAR)
        val nuevoMes = calendarioTemp.get(Calendar.MONTH)

        anioVisible = nuevoAnio
        mesVisible = nuevoMes
        diaSeleccionado = fechaDesdePartes(nuevoAnio, nuevoMes, 1)
    }

    fun irHoy() {
        val hoyCalendario = Calendar.getInstance()
        anioVisible = hoyCalendario.get(Calendar.YEAR)
        mesVisible = hoyCalendario.get(Calendar.MONTH)
        diaSeleccionado = hoyTexto
    }

    fun seleccionarDia(dia: DiaCalendario) {
        diaSeleccionado = dia.aCadena()

        if (!dia.perteneceAlMes) {
            anioVisible = dia.anio
            mesVisible = dia.mes
        }
    }

    fun cambiarCompletada(tarea: TareaResponseDto, completada: Boolean) {
        scope.launch {
            try {
                error = null

                val actualizada = if (completada) {
                    repository.completarTarea(
                        token = token,
                        id = tarea.id
                    )
                } else {
                    repository.reabrirTarea(
                        token = token,
                        id = tarea.id
                    )
                }

                tareas = tareas.map { tareaActual ->
                    if (tareaActual.id == actualizada.id) {
                        actualizada
                    } else {
                        tareaActual
                    }
                }

                usuarioId?.let { userId ->
                    val scheduler = TaskReminderScheduler(context)

                    if (completada) {
                        scheduler.cancelarRecordatorio(
                            tarea.id,
                            userId
                        )
                    } else {
                        scheduler.programarRecordatorio(
                            actualizada,
                            ajustes,
                            userId
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error = "No se pudo actualizar la tarea"
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val fondo = colorScheme.background
    val azul = colorScheme.primary
    val blanco = colorScheme.onBackground
    val gris = colorScheme.onSurfaceVariant
    val rojo = colorScheme.error

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
                    fontSize = 22.sp,
                    animacionesActivadas = ajustes.animacionesActivadas,
                    contentDescription = "Volver"
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Calendario",
                        color = blanco,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Organiza tus tareas por fecha",
                        color = gris,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = azul
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Cargando tareas...",
                            color = blanco
                        )
                    }
                }

                error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
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
                                    cargarDatos()
                                },
                                expanded = true,
                                animacionesActivadas = ajustes.animacionesActivadas
                            )
                        }
                    }
                }

                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            CabeceraMes(
                                anio = anioVisible,
                                mes = mesVisible,
                                animacionesActivadas = ajustes.animacionesActivadas,
                                onMesAnterior = {
                                    cambiarMes(-1)
                                },
                                onMesSiguiente = {
                                    cambiarMes(1)
                                },
                                onIrHoy = {
                                    irHoy()
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            CabeceraSemana()

                            Spacer(modifier = Modifier.height(6.dp))

                            GridMes(
                                mes = mesVisible,
                                anio = anioVisible,
                                tareasPorFecha = tareasPorFecha,
                                diaSeleccionado = diaSeleccionado,
                                hoyTexto = hoyTexto,
                                animacionesActivadas = ajustes.animacionesActivadas,
                                onDiaClick = { dia ->
                                    seleccionarDia(dia)
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            LeyendaCalendario()
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Día seleccionado",
                        color = blanco,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatearFechaLarga(diaSeleccionado),
                        color = gris,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedBlueButton(
                        text = "+ Crear tarea para este día",
                        onClick = {
                            mostrarCrearTarea = true
                        },
                        expanded = true,
                        animacionesActivadas = ajustes.animacionesActivadas,
                        contentDescription = "Crear tarea para este día"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (tareasDelDia.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay tareas para este día",
                                    color = gris,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            tareasDelDia.forEach { tarea ->
                                CalendarDayTaskItem(
                                    tarea = tarea,
                                    onAbrirDetalle = {
                                        tareaDetalle = tarea
                                    },
                                    onEditar = {
                                        tareaEditando = tarea
                                    },
                                    onCambiarCompletada = { completada ->
                                        cambiarCompletada(tarea, completada)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarCrearTarea) {
        CrearTareaDialog(
            tiposTarea = tiposTarea,
            fechaInicial = diaSeleccionado,

            onCerrar = {
                mostrarCrearTarea = false
            },

            onCrearTipo = {
                mostrarCrearTipo = true
            },

            onGuardar = { nuevaTarea ->
                try {
                    val tareaCreada = repository.crearTarea(
                        token = token,
                        tarea = nuevaTarea
                    )

                    tareas = tareas + tareaCreada

                    usuarioId?.let { userId ->
                        TaskReminderScheduler(context)
                            .programarRecordatorio(
                                tareaCreada,
                                ajustes,
                                userId
                            )
                    }

                    ResultadoGuardarTarea.Exito
                } catch (e: Exception) {
                    e.printStackTrace()

                    val apiError = ApiErrorParser.parsear(e)

                    if (apiError.fieldErrors.isNotEmpty()) {
                        ResultadoGuardarTarea.ErrorCampos(
                            apiError.fieldErrors
                        )
                    } else {
                        ResultadoGuardarTarea.ErrorGeneral(
                            apiError.generalMessage
                                ?: "No se pudo crear la tarea"
                        )
                    }
                }
            }
        )
    }

    if (mostrarCrearTipo) {
        CrearTipoTareaDialog(
            onCerrar = {
                mostrarCrearTipo = false
            },

            onGuardar = { nuevoTipo ->
                scope.launch {
                    try {
                        error = null

                        repository.crearTipoTarea(
                            token = token,
                            tipo = nuevoTipo
                        )

                        mostrarCrearTipo = false

                        tiposTarea = repository.obtenerTiposTarea(token)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        error = "No se pudo crear el tipo de tarea"
                    }
                }
            }
        )
    }

    tareaDetalle?.let { tarea ->
        DetalleTareaDialog(
            tarea = tarea,
            onCerrar = {
                tareaDetalle = null
            }
        )
    }

    tareaEditando?.let { tarea ->
        EditarTareaDialog(
            tarea = tarea,
            tiposTarea = tiposTarea,

            onCerrar = {
                tareaEditando = null
            },

            onGuardar = { tareaActualizada ->
                try {
                    val tareaEditada = repository.editarTarea(
                        token = token,
                        id = tarea.id,
                        tarea = tareaActualizada
                    )

                    tareas = tareas.map { tareaExistente ->
                        if (tareaExistente.id == tareaEditada.id) {
                            tareaEditada
                        } else {
                            tareaExistente
                        }
                    }

                    usuarioId?.let { userId ->
                        TaskReminderScheduler(context)
                            .programarRecordatorio(
                                tareaEditada,
                                ajustes,
                                userId
                            )
                    }

                    ResultadoGuardarTarea.Exito
                } catch (e: Exception) {
                    e.printStackTrace()

                    val apiError = ApiErrorParser.parsear(e)

                    if (apiError.fieldErrors.isNotEmpty()) {
                        ResultadoGuardarTarea.ErrorCampos(
                            apiError.fieldErrors
                        )
                    } else {
                        ResultadoGuardarTarea.ErrorGeneral(
                            apiError.generalMessage
                                ?: "No se pudo editar la tarea"
                        )
                    }
                }
            }
        )
    }
}
