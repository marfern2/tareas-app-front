package dam.moviles.tareas_app_front.data.remote.dto

data class LoginResponseDto(
    val token: String? = null,
    val refreshToken: String? = null,
    val type: String? = null,
    val id: Long,
    val username: String,
    val email: String
)