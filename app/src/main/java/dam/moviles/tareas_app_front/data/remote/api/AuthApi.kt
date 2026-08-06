package dam.moviles.tareas_app_front.data.remote.api

import dam.moviles.tareas_app_front.data.remote.dto.LoginRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.LoginResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.RegistroRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @POST("auth/registro")
    suspend fun registro(
        @Body request: RegistroRequestDto
    )
}