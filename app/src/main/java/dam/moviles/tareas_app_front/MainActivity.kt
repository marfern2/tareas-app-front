package dam.moviles.tareas_app_front

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dam.moviles.tareas_app_front.data.notifications.ExtrasNotificacion
import dam.moviles.tareas_app_front.data.notifications.NotificationChannels
import dam.moviles.tareas_app_front.data.notifications.NotificacionEstadoStore
import dam.moviles.tareas_app_front.data.notifications.TaskReminderScheduler
import dam.moviles.tareas_app_front.data.session.ProfilePreferences
import dam.moviles.tareas_app_front.data.session.TokenManager
import dam.moviles.tareas_app_front.data.settings.AppSettings
import dam.moviles.tareas_app_front.data.settings.SettingsPreferences
import dam.moviles.tareas_app_front.ui.auth.login.LoginScreen
import dam.moviles.tareas_app_front.ui.auth.registro.RegisterScreen
import dam.moviles.tareas_app_front.ui.calendar.CalendarScreen
import dam.moviles.tareas_app_front.ui.profile.ProfileScreen
import dam.moviles.tareas_app_front.ui.settings.SettingsScreen
import dam.moviles.tareas_app_front.ui.tareas.TareasScreen
import dam.moviles.tareas_app_front.ui.theme.TareasappfrontTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val intentNotificacion = mutableStateOf<DatosNotificacion?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        intentNotificacion.value = DatosNotificacion.desdeIntent(intent)

        val appContext = applicationContext
        val tokenManager = TokenManager(this)
        val profilePreferences = ProfilePreferences(this)
        val settingsPreferences = SettingsPreferences(this)

        setContent {
            val scope = rememberCoroutineScope()

            val ajustes by settingsPreferences.ajustes.collectAsStateWithLifecycle(
                initialValue = AppSettings()
            )

            TareasappfrontTheme(
                ajustes = ajustes
            ) {
                var tokenJwt by remember {
                    mutableStateOf(tokenManager.obtenerToken())
                }

                var usuarioId by remember {
                    mutableStateOf(profilePreferences.obtenerUsuarioId())
                }

                var nombreUsuario by remember {
                    mutableStateOf(profilePreferences.obtenerUsername())
                }

                var emailUsuario by remember {
                    mutableStateOf(profilePreferences.obtenerEmail())
                }

                var fotoPerfilUri by remember {
                    mutableStateOf(
                        profilePreferences.obtenerFotoUri(usuarioId)
                    )
                }

                var pantallaActual by remember {
                    mutableStateOf(
                        if (tokenJwt != null) {
                            "tareas"
                        } else {
                            "login"
                        }
                    )
                }

                var fechaCalendarioInicial by remember {
                    mutableStateOf<String?>(null)
                }

                var mensajeRegistroCorrecto by remember {
                    mutableStateOf<String?>(null)
                }

                // Crea los canales una sola vez con los ajustes cargados.
                var canalesCreados by remember { mutableStateOf(false) }

                LaunchedEffect(ajustes) {
                    if (!canalesCreados) {
                        NotificationChannels.crearCanales(
                            appContext,
                            ajustes
                        )
                        canalesCreados = true
                    }
                }

                // Reprograma recordatorios y resumen diario cuando cambian los
                // ajustes de notificación o la sesión.
                LaunchedEffect(
                    ajustes.notificacionesActivadas,
                    ajustes.resumenDiarioActivado,
                    ajustes.avisarTareasVencidas,
                    ajustes.avisarUrgenciaAlta,
                    ajustes.horaNotificacion,
                    ajustes.minutoNotificacion,
                    ajustes.antelacionDias,
                    usuarioId,
                    tokenJwt
                ) {
                    val userId = usuarioId
                    val token = tokenJwt

                    if (userId != null && token != null) {
                        TaskReminderScheduler(appContext)
                            .sincronizarDesdeServidor(
                                token = token,
                                ajustes = ajustes,
                                usuarioId = userId
                            )
                    }
                }

                // Apertura desde una notificación.
                LaunchedEffect(intentNotificacion.value) {
                    val dato = intentNotificacion.value ?: return@LaunchedEffect

                    if (tokenJwt == null) {
                        return@LaunchedEffect
                    }

                    when (dato.destino) {
                        ExtrasNotificacion.DESTINO_CALENDARIO -> {
                            fechaCalendarioInicial = dato.fecha
                            pantallaActual = "calendario"
                        }

                        ExtrasNotificacion.DESTINO_AJUSTES -> {
                            pantallaActual = "ajustes"
                        }
                    }

                    // Se consume el intent para que pulsar de nuevo la misma
                    // notificación vuelva a navegar.
                    intentNotificacion.value = null
                }

                when (pantallaActual) {
                    "login" -> {
                        LoginScreen(
                            mensajeRegistroCorrecto = mensajeRegistroCorrecto,
                            onGoToRegister = {
                                mensajeRegistroCorrecto = null
                                pantallaActual = "registro"
                            },
                            onLoginSuccess = {
                                    token,
                                    mantenerSesion,
                                    nuevoUsuarioId,
                                    nuevoUsername,
                                    nuevoEmail ->

                                tokenJwt = token
                                usuarioId = nuevoUsuarioId
                                nombreUsuario = nuevoUsername
                                emailUsuario = nuevoEmail

                                profilePreferences.guardarUsuario(
                                    id = nuevoUsuarioId,
                                    username = nuevoUsername,
                                    email = nuevoEmail
                                )

                                fotoPerfilUri =
                                    profilePreferences.obtenerFotoUri(
                                        nuevoUsuarioId
                                    )

                                if (mantenerSesion) {
                                    tokenManager.guardarToken(token)
                                } else {
                                    tokenManager.borrarToken()
                                }

                                pantallaActual = "tareas"
                            }
                        )
                    }

                    "registro" -> {
                        RegisterScreen(
                            onGoToLogin = { mensaje ->
                                mensajeRegistroCorrecto = mensaje
                                pantallaActual = "login"
                            }
                        )
                    }

                    "tareas" -> {
                        TareasScreen(
                            token = tokenJwt.orEmpty(),
                            usuarioId = usuarioId,
                            nombreUsuario = nombreUsuario,
                            emailUsuario = emailUsuario,
                            fotoPerfilUri = fotoPerfilUri,
                            ajustes = ajustes,
                            onVerPerfil = {
                                pantallaActual = "perfil"
                            },
                            onLogout = {
                                val userIdActual = usuarioId

                                tokenJwt = null
                                usuarioId = null
                                nombreUsuario = ""
                                emailUsuario = ""
                                fotoPerfilUri = null

                                tokenManager.borrarToken()
                                profilePreferences.borrarUsuarioActual()

                                if (userIdActual != null) {
                                    TaskReminderScheduler(appContext)
                                        .cancelarTodoDelUsuario(userIdActual)

                                    NotificacionEstadoStore(appContext)
                                        .limpiarUsuario(userIdActual)
                                }

                                mensajeRegistroCorrecto = null
                                pantallaActual = "login"
                            },
                            onAjustes = {
                                pantallaActual = "ajustes"
                            },
                            onAbrirCalendario = {
                                fechaCalendarioInicial = null
                                pantallaActual = "calendario"
                            }
                        )
                    }

                    "calendario" -> {
                        CalendarScreen(
                            token = tokenJwt.orEmpty(),
                            usuarioId = usuarioId,
                            ajustes = ajustes,
                            fechaInicial = fechaCalendarioInicial,
                            onVolver = {
                                pantallaActual = "tareas"
                            }
                        )
                    }

                    "perfil" -> {
                        ProfileScreen(
                            token = tokenJwt.orEmpty(),
                            nombreUsuario = nombreUsuario,
                            emailUsuario = emailUsuario,
                            fotoPerfilUri = fotoPerfilUri,
                            onFotoCambiada = { nuevaFotoUri ->
                                fotoPerfilUri = nuevaFotoUri

                                val idActual = usuarioId

                                if (idActual != null) {
                                    profilePreferences.guardarFotoUri(
                                        usuarioId = idActual,
                                        fotoUri = nuevaFotoUri
                                    )
                                }
                            },
                            onVolver = {
                                pantallaActual = "tareas"
                            }
                        )
                    }

                    "ajustes" -> {
                        SettingsScreen(
                            ajustes = ajustes,
                            onGuardarAjustes = { nuevosAjustes ->
                                scope.launch {
                                    settingsPreferences.guardarAjustes(
                                        nuevosAjustes
                                    )
                                }
                            },
                            onRestaurarAjustes = {
                                scope.launch {
                                    settingsPreferences.restaurarPredeterminados()
                                }
                            },
                            onReprogramarRecordatorios = {
                                val userId = usuarioId
                                val token = tokenJwt

                                if (userId != null && token != null) {
                                    scope.launch {
                                        TaskReminderScheduler(appContext)
                                            .sincronizarDesdeServidor(
                                                token = token,
                                                ajustes = ajustes,
                                                usuarioId = userId
                                            )
                                    }
                                }
                            },
                            onVolver = {
                                pantallaActual = "tareas"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentNotificacion.value = DatosNotificacion.desdeIntent(intent)
    }
}

private data class DatosNotificacion(
    val destino: String,
    val fecha: String?,
    val tareaId: Long?
) {
    companion object {
        fun desdeIntent(intent: Intent?): DatosNotificacion? {
            val destino = intent
                ?.getStringExtra(ExtrasNotificacion.DESTINO)
                ?: return null

            val tareaId = intent.getLongExtra(
                ExtrasNotificacion.TAREA_ID,
                -1L
            )

            return DatosNotificacion(
                destino = destino,
                fecha = intent.getStringExtra(ExtrasNotificacion.FECHA),
                tareaId = tareaId.takeIf { it >= 0 }
            )
        }
    }
}
