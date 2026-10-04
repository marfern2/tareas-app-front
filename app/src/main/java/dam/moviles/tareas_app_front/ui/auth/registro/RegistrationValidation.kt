package dam.moviles.tareas_app_front.ui.auth.registro

import dam.moviles.tareas_app_front.data.remote.dto.RegistroRequestDto

internal fun usernameValidationError(username: String): String? = when {
    username.isBlank() -> "El nombre de usuario es obligatorio"
    username.length > 20 -> "El nombre de usuario no puede superar los 20 caracteres"
    username.trim().length < 3 -> "El nombre de usuario debe tener al menos 3 caracteres"
    else -> null
}

internal fun submitIfValidUsername(
    username: String,
    email: String,
    password: String,
    submit: (RegistroRequestDto) -> Unit
): String? {
    val error = usernameValidationError(username)
    if (error != null) return error

    submit(RegistroRequestDto(username.trim(), email.trim(), password))
    return null
}
