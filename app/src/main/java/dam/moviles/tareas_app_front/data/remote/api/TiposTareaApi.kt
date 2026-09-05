package dam.moviles.tareas_app_front.data.remote.api

import dam.moviles.tareas_app_front.data.remote.dto.CrearTipoTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TiposTareaApi {

    @GET("tipos-tarea")
    suspend fun obtenerTiposTarea(): List<TipoTareaResponseDto>

    @POST("tipos-tarea")
    suspend fun crearTipoTarea(
        @Body tipo: CrearTipoTareaRequestDto
    ): TipoTareaResponseDto
}