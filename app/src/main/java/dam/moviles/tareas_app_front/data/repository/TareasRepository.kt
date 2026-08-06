package dam.moviles.tareas_app_front.data.repository

import dam.moviles.tareas_app_front.data.remote.RetrofitClient
import dam.moviles.tareas_app_front.data.remote.dto.CrearTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.CrearTipoTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.ActualizarTareaRequestDto

class TareasRepository {

    suspend fun obtenerTareas(token: String): List<TareaResponseDto> {
        return RetrofitClient.tareasApi.obtenerTareas(
            token = "Bearer $token"
        )
    }

    suspend fun obtenerTiposTarea(token: String): List<TipoTareaResponseDto> {
        return RetrofitClient.tiposTareaApi.obtenerTiposTarea(
            token = "Bearer $token"
        )
    }

    suspend fun crearTarea(
        token: String,
        tarea: CrearTareaRequestDto
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.crearTarea(
            token = "Bearer $token",
            tarea = tarea
        )
    }

    suspend fun crearTipoTarea(
        token: String,
        tipo: CrearTipoTareaRequestDto
    ): TipoTareaResponseDto {
        return RetrofitClient.tiposTareaApi.crearTipoTarea(
            token = "Bearer $token",
            tipo = tipo
        )
    }

    suspend fun editarTarea(
        token: String,
        id: Long,
        tarea: ActualizarTareaRequestDto
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.editarTarea(
            token = "Bearer $token",
            id = id,
            tarea = tarea
        )
    }

    suspend fun completarTarea(
        token: String,
        id: Long
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.completarTarea(
            token = "Bearer $token",
            id = id
        )
    }

    suspend fun reabrirTarea(
        token: String,
        id: Long
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.reabrirTarea(
            token = "Bearer $token",
            id = id
        )
    }

    suspend fun eliminarTarea(
        token: String,
        id: Long
    ) {
        RetrofitClient.tareasApi.eliminarTarea(
            token = "Bearer $token",
            id = id
        )
    }
}