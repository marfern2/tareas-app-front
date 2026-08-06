package dam.moviles.tareas_app_front.data.remote

import dam.moviles.tareas_app_front.data.remote.api.AuthApi
import dam.moviles.tareas_app_front.data.remote.api.TareasApi
import dam.moviles.tareas_app_front.data.remote.api.TiposTareaApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.100:8080/"

    // Interceptor para logs
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // En producción cambiar a NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    val tareasApi: TareasApi = retrofit.create(TareasApi::class.java)

    val tiposTareaApi: TiposTareaApi = retrofit.create(TiposTareaApi::class.java)
}