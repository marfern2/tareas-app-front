package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import dam.moviles.tareas_app_front.data.remote.ErrorMapper
import dam.moviles.tareas_app_front.data.remote.ResultadoGuardarTarea
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import dam.moviles.tareas_app_front.data.repository.TareasRepository
import dam.moviles.tareas_app_front.data.settings.AppSettings
import dam.moviles.tareas_app_front.data.settings.DensidadTareas
import dam.moviles.tareas_app_front.data.settings.OrdenTareas
import dam.moviles.tareas_app_front.ui.components.AnimatedBlueButton
import dam.moviles.tareas_app_front.ui.components.IconoCalendario
import kotlinx.coroutines.launch

@Composable
fun TareasScreen(
    usuarioId: Long?,
    nombreUsuario: String,
    emailUsuario: String,
    fotoPerfilUri: String?,
    ajustes: AppSettings,
    onVerPerfil: () -> Unit,
    onLogout: () -> Unit,
    onAjustes: () -> Unit,
    onAbrirCalendario: () -> Unit
) {
    val repository = remember {
        TareasRepository()
    }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    var tareas by remember {
        mutableStateOf<List<TareaResponseDto>>(emptyList())
    }

    var tiposTarea by remember {
        mutableStateOf<List<TipoTareaResponseDto>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var mostrarDialogCrearTarea by remember {
        mutableStateOf(false)
    }

    var mostrarDialogCrearTipo by remember {
        mutableStateOf(false)
    }

    var tareaSeleccionada by remember {
        mutableStateOf<TareaResponseDto?>(null)
    }

    var tareaEditando by remember {
        mutableStateOf<TareaResponseDto?>(null)
    }

    var tareaEliminando by remember {
        mutableStateOf<TareaResponseDto?>(null)
    }

    var tipoFiltroSeleccionadoId by remember {
        mutableStateOf<Long?>(null)
    }

    val colorScheme = MaterialTheme.colorScheme
    val fondo = colorScheme.background
    val azul = colorScheme.primary
    val blanco = colorScheme.onBackground
    val rojo = colorScheme.error
    val gris = colorScheme.onSurfaceVariant

    val tareasFiltradas = remember(
        tareas,
        tipoFiltroSeleccionadoId,
        ajustes.mostrarCompletadas,
        ajustes.ordenTareas
    ) {
        var resultado = tareas

        if (tipoFiltroSeleccionadoId != null) {
            resultado = resultado.filter { tarea ->
                tarea.tipoTareaId == tipoFiltroSeleccionadoId
            }
        }

        if (!ajustes.mostrarCompletadas) {
            resultado = resultado.filter { tarea ->
                !tarea.completada
            }
        }

        when (ajustes.ordenTareas) {
            OrdenTareas.FECHA -> resultado.sortedBy { tarea -> tarea.fecha }
            OrdenTareas.URGENCIA -> resultado.sortedByDescending { tarea -> tarea.urgencia }
            OrdenTareas.NOMBRE -> resultado.sortedBy { tarea -> tarea.titulo.lowercase() }
            OrdenTareas.ESTADO -> resultado.sortedWith(
                compareBy({ tarea -> tarea.completada }, { tarea -> tarea.fecha })
            )
        }
    }

    val contadorTodas = tareas.size

    val contadorPorTipo = tareas
        .groupBy { tarea ->
            tarea.tipoTareaId
        }
        .mapValues { entrada ->
            entrada.value.size
        }

    val separacionLista = when (ajustes.densidadTareas) {
        DensidadTareas.COMPACTA -> 6.dp
        DensidadTareas.NORMAL -> 12.dp
        DensidadTareas.COMODA -> 18.dp
    }

    fun cargarDatos() {
        scope.launch {
            try {
                isLoading = true
                error = null

                tareas = repository.obtenerTareas()
                tiposTarea = repository.obtenerTiposTarea()
            } catch (e: Exception) {
                e.printStackTrace()
                error = ErrorMapper.mensajePara(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun eliminarTarea(tarea: TareaResponseDto) {
        scope.launch {
            try {
                error = null

                repository.eliminarTarea(
                    id = tarea.id
                )

                usuarioId?.let { userId ->
                    TaskReminderScheduler(context)
                        .cancelarRecordatorio(tarea.id, userId)
                }

                tareaEliminando = null
                cargarDatos()
            } catch (e: Exception) {
                e.printStackTrace()
                error = ErrorMapper.mensajePara(e)
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarDatos()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            TareasDrawer(
                nombreUsuario = nombreUsuario,
                emailUsuario = emailUsuario,
                fotoPerfilUri = fotoPerfilUri,
                tiposTarea = tiposTarea,
                contadorTodas = contadorTodas,
                contadorPorTipo = contadorPorTipo,
                tipoSeleccionadoId = tipoFiltroSeleccionadoId,

                onVerPerfil = {
                    scope.launch {
                        drawerState.close()
                        onVerPerfil()
                    }
                },

                onSeleccionarTodas = {
                    tipoFiltroSeleccionadoId = null

                    scope.launch {
                        drawerState.close()
                    }
                },

                onSeleccionarTipo = { tipoId ->
                    tipoFiltroSeleccionadoId = tipoId

                    scope.launch {
                        drawerState.close()
                    }
                },

                onCerrarDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                },

                onLogout = {
                    scope.launch {
                        drawerState.close()
                    }

                    onLogout()
                },

                onAjustes = {
                    scope.launch {
                        drawerState.close()
                    }

                    onAjustes()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = fondo,
            floatingActionButton = {
                AnimatedBlueButton(
                    text = "+",
                    onClick = {
                        mostrarDialogCrearTarea = true
                    },
                    size = 58.dp,
                    fontSize = 34.sp,
                    animacionesActivadas = ajustes.animacionesActivadas,
                    contentDescription = "Crear tarea"
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fondo)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedBlueButton(
                        text = "☰",
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        size = 48.dp,
                        fontSize = 26.sp,
                        animacionesActivadas = ajustes.animacionesActivadas,
                        contentDescription = "Abrir menú"
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Mis tareas",
                        modifier = Modifier.weight(1f),
                        color = blanco,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    AnimatedBlueButton(
                        text = "",
                        onClick = onAbrirCalendario,
                        size = 48.dp,
                        fontSize = 26.sp,
                        animacionesActivadas = ajustes.animacionesActivadas,
                        contentDescription = "Abrir calendario",
                        content = {
                            IconoCalendario(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                val nombreFiltro = if (tipoFiltroSeleccionadoId == null) {
                    "Todas"
                } else {
                    tiposTarea
                        .find { tipo ->
                            tipo.id == tipoFiltroSeleccionadoId
                        }
                        ?.nombre
                        ?: "Filtro"
                }

                Text(
                    text = "Etiqueta: $nombreFiltro",
                    color = gris,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = azul
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = "Cargando tareas...",
                                color = blanco
                            )
                        }
                    }

                    error != null -> {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error.orEmpty(),
                                color = rojo
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Button(
                                onClick = {
                                    cargarDatos()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = azul,
                                    contentColor = colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = "Reintentar"
                                )
                            }
                        }
                    }

                    tareasFiltradas.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Todavía no tienes tareas",
                                color = blanco,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "Pulsa el botón + para crear una tarea",
                                color = gris
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(separacionLista)
                        ) {
                            items(
                                items = tareasFiltradas,
                                key = { tarea ->
                                    tarea.id
                                }
                            ) { tarea ->
                                TareaItem(
                                    tarea = tarea,
                                    densidad = ajustes.densidadTareas,

                                    onClick = {
                                        tareaSeleccionada = tarea
                                    },

                    onCompletarChange = { completada ->
                        scope.launch {
                            try {
                                error = null

                                val tareaActualizada = if (completada) {
                                    repository.completarTarea(
                                        id = tarea.id
                                    )
                                } else {
                                    repository.reabrirTarea(
                                        id = tarea.id
                                    )
                                }

                                tareas = tareas.map { tareaActual ->
                                    if (tareaActual.id == tareaActualizada.id) {
                                        tareaActualizada
                                    } else {
                                        tareaActual
                                    }
                                }

                                usuarioId?.let { userId ->
                                    val scheduler =
                                        TaskReminderScheduler(context)

                                    if (completada) {
                                        scheduler.cancelarRecordatorio(
                                            tarea.id,
                                            userId
                                        )
                                    } else {
                                        scheduler.programarRecordatorio(
                                            tareaActualizada,
                                            ajustes,
                                            userId
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                error = ErrorMapper.mensajePara(e)
                            }
                        }
                    },

                                    onEditarClick = {
                                        tareaEditando = tarea
                                    },

                                    onEliminarClick = {
                                        if (ajustes.confirmarEliminacion) {
                                            tareaEliminando = tarea
                                        } else {
                                            eliminarTarea(tarea)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogCrearTarea) {
        CrearTareaDialog(
            tiposTarea = tiposTarea,

            onCerrar = {
                mostrarDialogCrearTarea = false
            },

            onCrearTipo = {
                mostrarDialogCrearTipo = true
            },

            onGuardar = { nuevaTarea ->
                try {
                    val tareaCreada = repository.crearTarea(
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

    if (mostrarDialogCrearTipo) {
        CrearTipoTareaDialog(
            onCerrar = {
                mostrarDialogCrearTipo = false
            },

            onGuardar = { nuevoTipo ->
                scope.launch {
                    try {
                        error = null

                        repository.crearTipoTarea(
                            tipo = nuevoTipo
                        )

                        mostrarDialogCrearTipo = false

                        tiposTarea =
                            repository.obtenerTiposTarea()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        error = ErrorMapper.mensajePara(e)
                    }
                }
            }
        )
    }

    tareaSeleccionada?.let { tarea ->
        DetalleTareaDialog(
            tarea = tarea,
            onCerrar = {
                tareaSeleccionada = null
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
                        id = tarea.id,
                        tarea = tareaActualizada
                    )

                    tareas = tareas.map { tareaExistente ->
                        if (tareaExistente.id == tarea.id) {
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

    tareaEliminando?.let { tarea ->
        EliminarTareaDialog(
            tarea = tarea,

            onCancelar = {
                tareaEliminando = null
            },

            onConfirmar = {
                eliminarTarea(tarea)
            }
        )
    }
}
