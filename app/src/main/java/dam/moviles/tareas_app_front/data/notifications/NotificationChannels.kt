package dam.moviles.tareas_app_front.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import dam.moviles.tareas_app_front.data.settings.AppSettings

/*
 * Centraliza la creación de los canales de notificación.
 *
 * Android solo permite configurar sonido, vibración e importancia del canal
 * la primera vez que se crea. A partir de ahí el usuario tiene el control
 * desde los ajustes del sistema y la app no debe sobrescribirlo.
 */
object NotificationChannels {

    const val CANAL_RECORDATORIOS = "recordatorios_tareas"
    const val CANAL_TAREAS_URGENTES = "tareas_urgentes"
    const val CANAL_RESUMEN_DIARIO = "resumen_diario"

    /**
     * Crea los canales de forma segura y repetible.
     * En Android 7.1 y anteriores no existen canales: es una operación sin efecto.
     */
    fun crearCanales(
        context: Context,
        ajustes: AppSettings
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val gestor = context.getSystemService(
            NotificationManager::class.java
        ) ?: return

        val canales = listOf(
            crearCanal(
                id = CANAL_RECORDATORIOS,
                nombre = "Recordatorios de tareas",
                descripcion = "Avisos de tareas próximas",
                importancia = NotificationManager.IMPORTANCE_DEFAULT,
                ajustes = ajustes
            ),
            crearCanal(
                id = CANAL_TAREAS_URGENTES,
                nombre = "Tareas urgentes",
                descripcion = "Avisos importantes para tareas de urgencia alta",
                importancia = NotificationManager.IMPORTANCE_HIGH,
                ajustes = ajustes
            ),
            crearCanal(
                id = CANAL_RESUMEN_DIARIO,
                nombre = "Resumen diario",
                descripcion = "Resumen de tareas pendientes y vencidas",
                importancia = NotificationManager.IMPORTANCE_DEFAULT,
                ajustes = ajustes
            )
        )

        gestor.createNotificationChannels(canales)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun crearCanal(
        id: String,
        nombre: String,
        descripcion: String,
        importancia: Int,
        ajustes: AppSettings
    ): NotificationChannel {
        val canal = NotificationChannel(
            id,
            nombre,
            importancia
        ).apply {
            this.description = descripcion
            enableLights(true)
        }

        /*
         * Configuración inicial razonable según las preferencias del usuario.
         * Solo se aplica la primera vez que se crea el canal; Android manda
         * después de eso.
         */
        if (ajustes.vibracionNotificaciones) {
            canal.enableVibration(true)
            canal.vibrationPattern = longArrayOf(0, 250, 150, 250)
        } else {
            canal.enableVibration(false)
        }

        if (ajustes.sonidoNotificaciones) {
            canal.setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        } else {
            canal.setSound(
                null,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }

        return canal
    }
}
