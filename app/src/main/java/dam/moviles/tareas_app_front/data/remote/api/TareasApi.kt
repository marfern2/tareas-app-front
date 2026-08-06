package dam.moviles.tareas_app_front.data.remote.api

import dam.moviles.tareas_app_front.data.remote.dto.ActualizarTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.CrearTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TareasApi {

    @GET("tareas")
    suspend fun obtenerTareas(
        @Header("Authorization") token: String
    ): List<TareaResponseDto>

    @POST("tareas")
    suspend fun crearTarea(
        @Header("Authorization") token: String,
        @Body tarea: CrearTareaRequestDto
    ): TareaResponseDto

    @PATCH("tareas/{id}")
    suspend fun editarTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body tarea: ActualizarTareaRequestDto
    ): TareaResponseDto

    @PATCH("tareas/{id}/completar")
    suspend fun completarTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): TareaResponseDto

    @PATCH("tareas/{id}/reabrir")
    suspend fun reabrirTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): TareaResponseDto

    @DELETE("tareas/{id}")
    suspend fun eliminarTarea(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Unit>
}