package dam.moviles.tareas_app_front.data.remote.api

import dam.moviles.tareas_app_front.data.remote.dto.ActualizarTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.CrearTareaRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TareasApi {

    @GET("tareas")
    suspend fun obtenerTareas(): List<TareaResponseDto>

    @POST("tareas")
    suspend fun crearTarea(
        @Body tarea: CrearTareaRequestDto
    ): TareaResponseDto

    @PATCH("tareas/{id}")
    suspend fun editarTarea(
        @Path("id") id: Long,
        @Body tarea: ActualizarTareaRequestDto
    ): TareaResponseDto

    @PATCH("tareas/{id}/completar")
    suspend fun completarTarea(
        @Path("id") id: Long
    ): TareaResponseDto

    @PATCH("tareas/{id}/reabrir")
    suspend fun reabrirTarea(
        @Path("id") id: Long
    ): TareaResponseDto

    @DELETE("tareas/{id}")
    suspend fun eliminarTarea(
        @Path("id") id: Long
    ): Response<Unit>
}