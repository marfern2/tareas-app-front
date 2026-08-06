package dam.moviles.tareas_app_front.data.remote.dto

data class TipoTareaResponseDto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val color: String
)