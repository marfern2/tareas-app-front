package dam.moviles.tareas_app_front.data.remote.dto

data class CrearTareaRequestDto(
    val titulo: String,
    val descripcion: String?,
    val fecha: String,
    val completada: Boolean = false,
    val urgencia: Int = 0,
    val tipoTareaId: Long
)