package dam.moviles.tareas_app_front.data.repository

import dam.moviles.tareas_app_front.data.remote.RetrofitClient
import dam.moviles.tareas_app_front.data.remote.dto.CrearTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.CrearTipoTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.ActualizarTareaRequestDto

class TareasRepository {

    suspend fun obtenerTareas(): List<TareaResponseDto> {
        return RetrofitClient.tareasApi.obtenerTareas()
    }

    suspend fun obtenerTiposTarea(): List<TipoTareaResponseDto> {
        return RetrofitClient.tiposTareaApi.obtenerTiposTarea()
    }

    suspend fun crearTarea(
        tarea: CrearTareaRequestDto
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.crearTarea(
            tarea = tarea
        )
    }

    suspend fun crearTipoTarea(
        tipo: CrearTipoTareaRequestDto
    ): TipoTareaResponseDto {
        return RetrofitClient.tiposTareaApi.crearTipoTarea(
            tipo = tipo
        )
    }

    suspend fun editarTarea(
        id: Long,
        tarea: ActualizarTareaRequestDto
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.editarTarea(
            id = id,
            tarea = tarea
        )
    }

    suspend fun completarTarea(
        id: Long
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.completarTarea(
            id = id
        )
    }

    suspend fun reabrirTarea(
        id: Long
    ): TareaResponseDto {
        return RetrofitClient.tareasApi.reabrirTarea(
            id = id
        )
    }

    suspend fun eliminarTarea(
        id: Long
    ) {
        RetrofitClient.tareasApi.eliminarTarea(
            id = id
        )
    }
}