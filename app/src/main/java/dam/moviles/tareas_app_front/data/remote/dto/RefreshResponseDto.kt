package dam.moviles.tareas_app_front.data.remote.dto

data class RefreshResponseDto(
    val token: String? = null,
    val refreshToken: String? = null,
    val type: String? = null
)