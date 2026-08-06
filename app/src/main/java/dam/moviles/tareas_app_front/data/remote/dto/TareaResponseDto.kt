package dam.moviles.tareas_app_front.data.remote.dto

data class TareaResponseDto(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val fecha: String,
    val completada: Boolean,
    val urgencia: Int,
    val usuarioId: Long,
    val tipoTareaId: Long,
    val tipoTareaNombre: String,
    val tipoTareaColor: String
)