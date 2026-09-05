package dam.moviles.tareas_app_front.data.remote.api

import dam.moviles.tareas_app_front.data.remote.dto.LoginRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.LoginResponseDto
import dam.moviles.tareas_app_front.data.remote.dto.RefreshRequestDto
import dam.moviles.tareas_app_front.data.remote.dto.RefreshResponseDto
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

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequestDto
    ): RefreshResponseDto

    @POST("auth/logout")
    suspend fun logout(
        @Body request: RefreshRequestDto
    )
}