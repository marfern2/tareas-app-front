package dam.moviles.tareas_app_front.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Puente minimalista entre la capa de autenticación y la UI.
 *
 * Cuando el refresh token deja de servir (expirado, revocado o inexistente),
 * el TokenAuthenticator avisa aquí y MainActivity reacciona limpiando la
 * sesión y volviendo al login. No introduce ningún framework: solo un
 * StateFlow observable desde Compose.
 */
object SessionManager {

    private val _sesionInvalida = MutableStateFlow(false)

    val sesionInvalida: StateFlow<Boolean> = _sesionInvalida.asStateFlow()

    fun marcarSesionInvalida() {
        _sesionInvalida.value = true
    }

    fun reiniciar() {
        _sesionInvalida.value = false
    }
}