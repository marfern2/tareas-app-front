package dam.moviles.tareas_app_front.data.remote.dto

data class LoginResponseDto(
    val token: String,
    val type: String,
    val id: Long,
    val username: String,
    val email: String
)