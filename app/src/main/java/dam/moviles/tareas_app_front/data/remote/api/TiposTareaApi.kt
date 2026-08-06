package dam.moviles.tareas_app_front.data.remote.api

import dam.moviles.tareas_app_front.data.remote.dto.CrearTipoTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface TiposTareaApi {

    @GET("tipos-tarea")
    suspend fun obtenerTiposTarea(
        @Header("Authorization") token: String
    ): List<TipoTareaResponseDto>

    @POST("tipos-tarea")
    suspend fun crearTipoTarea(
        @Header("Authorization") token: String,
        @Body tipo: CrearTipoTareaRequestDto
    ): TipoTareaResponseDto
}