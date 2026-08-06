package dam.moviles.tareas_app_front.data.remote.dto

data class ActualizarTareaRequestDto(
    val titulo: String?,
    val descripcion: String?,
    val fecha: String?,
    val completada: Boolean?,
    val urgencia: Int?,
    val tipoTareaId: Long?
)