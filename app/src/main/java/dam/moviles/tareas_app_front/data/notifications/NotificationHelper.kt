package dam.moviles.tareas_app_front.data.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.content.ContextCompat
import dam.moviles.tareas_app_front.MainActivity
import dam.moviles.tareas_app_front.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper reutilizable para construir y publicar notificaciones.
 *
 * No contiene lógica de Compose ni de Retrofit. Comprueba el permiso antes de
 * notificar y deja que Android gestione los canales (Android 8+).
 */
object NotificationHelper {

    /** Color principal usado en el icono, independiente de Compose. */
    private const val COLOR_NOTIFICACION = 0xFF2F80ED.toInt()

    private const val MAX_LONGITUD_DESCRIPCION = 180

    fun puedeNotificar(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!concedido) {
                return false
            }
        }

        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // ---------------------------------------------------------------------
    // Publicación de notificaciones
    // ---------------------------------------------------------------------

    fun enviarRecordatorio(
        context: Context,
        tarea: TareaRecordatorio,
        usuarioId: Long,
        sonido: Boolean,
        vibracion: Boolean
    ) {
        if (!puedeNotificar(context)) {
            return
        }

        val urgente = esUrgenciaAlta(tarea.urgencia)
        val canal = if (urgente) {
            NotificationChannels.CANAL_TAREAS_URGENTES
        } else {
            NotificationChannels.CANAL_RECORDATORIOS
        }

        val titulo = if (urgente) {
            "Tarea urgente: ${tarea.titulo}"
        } else {
            "Próxima tarea: ${tarea.titulo}"
        }

        val texto = "Para ${formatearFechaCorta(tarea.fecha)} · ${tarea.tipoNombre}"

        val id = NotificationIds.idRecordatorio(usuarioId, tarea.id)

        val builder = baseBuilder(
            context = context,
            canal = canal,
            sonido = sonido,
            vibracion = vibracion
        )
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(estiloBigText(tarea, titulo, texto))
            .addAction(
                0,
                "Ver tarea",
                pendingIntentAbrirApp(
                    context = context,
                    destino = ExtrasNotificacion.DESTINO_CALENDARIO,
                    fecha = tarea.fecha,
                    tareaId = tarea.id,
                    requestCode = NotificationIds.codigoAccion(id)
                )
            )
            .setContentIntent(
                pendingIntentAbrirApp(
                    context = context,
                    destino = ExtrasNotificacion.DESTINO_CALENDARIO,
                    fecha = tarea.fecha,
                    tareaId = tarea.id,
                    requestCode = id
                )
            )

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun enviarTareaVencida(
        context: Context,
        tarea: TareaRecordatorio,
        usuarioId: Long,
        sonido: Boolean,
        vibracion: Boolean
    ) {
        if (!puedeNotificar(context)) {
            return
        }

        val canal = if (esUrgenciaAlta(tarea.urgencia)) {
            NotificationChannels.CANAL_TAREAS_URGENTES
        } else {
            NotificationChannels.CANAL_RECORDATORIOS
        }

        val id = NotificationIds.idRecordatorio(usuarioId, tarea.id)

        val builder = baseBuilder(
            context = context,
            canal = canal,
            sonido = sonido,
            vibracion = vibracion
        )
            .setContentTitle("Tarea vencida: ${tarea.titulo}")
            .setContentText("Vencío el ${formatearFechaCorta(tarea.fecha)}")
            .setContentIntent(
                pendingIntentAbrirApp(
                    context = context,
                    destino = ExtrasNotificacion.DESTINO_CALENDARIO,
                    fecha = tarea.fecha,
                    tareaId = tarea.id,
                    requestCode = id
                )
            )

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun enviarResumen(
        context: Context,
        pendientes: Int,
        urgentes: Int,
        vencidas: Int,
        usuarioId: Long,
        sonido: Boolean,
        vibracion: Boolean
    ) {
        if (!puedeNotificar(context)) {
            return
        }

        val partes = mutableListOf<String>()

        if (pendientes > 0) {
            partes += "$pendientes pendientes"
        }

        if (urgentes > 0) {
            partes += "$urgentes urgentes"
        }

        if (vencidas > 0) {
            partes += "$vencidas vencidas"
        }

        if (partes.isEmpty()) {
            return
        }

        val id = NotificationIds.idResumen(usuarioId)

        val builder = baseBuilder(
            context = context,
            canal = NotificationChannels.CANAL_RESUMEN_DIARIO,
            sonido = sonido,
            vibracion = vibracion
        )
            .setContentTitle("Tu resumen de hoy")
            .setContentText(partes.joinToString(" · "))
            .setContentIntent(
                pendingIntentAbrirApp(
                    context = context,
                    destino = ExtrasNotificacion.DESTINO_CALENDARIO,
                    fecha = null,
                    tareaId = null,
                    requestCode = id
                )
            )

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun enviarPrueba(context: Context): ResultadoNotificacionPrueba {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!concedido) {
                return ResultadoNotificacionPrueba.PERMISO_FALTANTE
            }
        }

        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return ResultadoNotificacionPrueba.BLOQUEADA_POR_ANDROID
        }

        val id = NotificationIds.idPrueba()

        val builder = baseBuilder(
            context = context,
            canal = NotificationChannels.CANAL_RECORDATORIOS,
            sonido = true,
            vibracion = true
        )
            .setContentTitle("Notificación de prueba")
            .setContentText("¡Tus notificaciones funcionan!")
            .setContentIntent(
                pendingIntentAbrirApp(
                    context = context,
                    destino = ExtrasNotificacion.DESTINO_AJUSTES,
                    fecha = null,
                    tareaId = null,
                    requestCode = id
                )
            )

        NotificationManagerCompat.from(context).notify(id, builder.build())

        return ResultadoNotificacionPrueba.EXITO
    }

    // ---------------------------------------------------------------------
    // Apertura de ajustes
    // ---------------------------------------------------------------------

    fun abrirAjustesSistema(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    fun abrirAjustesCanal(context: Context, canalId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, canalId)

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return
            }
        }

        abrirAjustesSistema(context)
    }

    // ---------------------------------------------------------------------
    // Construcción interna
    // ---------------------------------------------------------------------

    private fun baseBuilder(
        context: Context,
        canal: String,
        sonido: Boolean,
        vibracion: Boolean
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, canal)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(COLOR_NOTIFICACION)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        /*
         * En Android 7.1 y anteriores no existen canales, así que el sonido y
         * la vibración se aplican aquí directamente. En Android 8+ el canal
         * tiene el control final.
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)

            if (sonido) {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            } else {
                builder.setSound(null)
            }

            if (vibracion) {
                builder.setVibrate(longArrayOf(0, 250, 150, 250))
            } else {
                builder.setVibrate(null)
            }
        }

        return builder
    }

    private fun estiloBigText(
        tarea: TareaRecordatorio,
        titulo: String,
        texto: String
    ): BigTextStyle? {
        val descripcion = tarea.descripcion
            ?.trim()
            ?.take(MAX_LONGITUD_DESCRIPCION)
            .orEmpty()

        if (descripcion.isBlank()) {
            return null
        }

        return BigTextStyle()
            .setBigContentTitle(titulo)
            .bigText("$texto\n$descripcion")
    }

    private fun pendingIntentAbrirApp(
        context: Context,
        destino: String,
        fecha: String?,
        tareaId: Long?,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ExtrasNotificacion.DESTINO, destino)

            if (fecha != null) {
                putExtra(ExtrasNotificacion.FECHA, fecha)
            }

            if (tareaId != null) {
                putExtra(ExtrasNotificacion.TAREA_ID, tareaId)
            }
        }

        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun formatearFechaCorta(fecha: String): String {
        return try {
            val formato = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val fechaParseada = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).parse(fecha)

            if (fechaParseada == null) {
                fecha
            } else {
                formato.format(fechaParseada)
            }
        } catch (_: Exception) {
            fecha
        }
    }
}

enum class ResultadoNotificacionPrueba {
    EXITO,
    PERMISO_FALTANTE,
    BLOQUEADA_POR_ANDROID
}
