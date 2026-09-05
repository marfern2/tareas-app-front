package dam.moviles.tareas_app_front

import android.app.Application
import dam.moviles.tareas_app_front.data.remote.RetrofitClient

/**
 * Punto de entrada de cualquier proceso de la app (Activity o Worker).
 *
 * Inicializa la red de autenticación automática antes de que se use la API,
 * incluso cuando el proceso lo lanza WorkManager sin una Activity.
 */
class DonitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.iniciar(this)
    }
}