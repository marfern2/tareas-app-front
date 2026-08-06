package dam.moviles.tareas_app_front.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ajustesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ajustes_usuario"
)

class SettingsPreferences(context: Context) {

    private val dataStore = context.applicationContext.ajustesDataStore

    val ajustes: Flow<AppSettings> = dataStore.data.map { preferencias ->
        AppSettings(
            temaColor = TemaColor.desdeNombre(
                preferencias[CLAVE_TEMA_COLOR]
            ) ?: AjustesPredeterminados.temaColor,
            modoApariencia = ModoApariencia.desdeNombre(
                preferencias[CLAVE_MODO_APARIENCIA]
            ) ?: AjustesPredeterminados.modoApariencia,
            densidadTareas = DensidadTareas.desdeNombre(
                preferencias[CLAVE_DENSIDAD]
            ) ?: AjustesPredeterminados.densidadTareas,
            mostrarCompletadas = preferencias[CLAVE_MOSTRAR_COMPLETADAS]
                ?: AjustesPredeterminados.mostrarCompletadas,
            confirmarEliminacion = preferencias[CLAVE_CONFIRMAR_ELIMINACION]
                ?: AjustesPredeterminados.confirmarEliminacion,
            ordenTareas = OrdenTareas.desdeNombre(
                preferencias[CLAVE_ORDEN]
            ) ?: AjustesPredeterminados.ordenTareas,
            animacionesActivadas = preferencias[CLAVE_ANIMACIONES]
                ?: AjustesPredeterminados.animacionesActivadas,
            notificacionesActivadas = preferencias[CLAVE_NOTIF_ACTIVADAS]
                ?: AjustesPredeterminados.notificacionesActivadas,
            resumenDiarioActivado = preferencias[CLAVE_RESUMEN_DIARIO]
                ?: AjustesPredeterminados.resumenDiarioActivado,
            avisarTareasVencidas = preferencias[CLAVE_AVISAR_VENCIDAS]
                ?: AjustesPredeterminados.avisarTareasVencidas,
            avisarUrgenciaAlta = preferencias[CLAVE_AVISAR_URGENCIA]
                ?: AjustesPredeterminados.avisarUrgenciaAlta,
            horaNotificacion = preferencias[CLAVE_HORA]
                ?: AjustesPredeterminados.horaNotificacion,
            minutoNotificacion = preferencias[CLAVE_MINUTO]
                ?: AjustesPredeterminados.minutoNotificacion,
            antelacionDias = preferencias[CLAVE_ANTELACION]
                ?: AjustesPredeterminados.antelacionDias,
            sonidoNotificaciones = preferencias[CLAVE_SONIDO]
                ?: AjustesPredeterminados.sonidoNotificaciones,
            vibracionNotificaciones = preferencias[CLAVE_VIBRACION]
                ?: AjustesPredeterminados.vibracionNotificaciones
        )
    }

    suspend fun guardarAjustes(nuevosAjustes: AppSettings) {
        dataStore.edit { preferencias ->
            preferencias[CLAVE_TEMA_COLOR] = nuevosAjustes.temaColor.name
            preferencias[CLAVE_MODO_APARIENCIA] = nuevosAjustes.modoApariencia.name
            preferencias[CLAVE_DENSIDAD] = nuevosAjustes.densidadTareas.name
            preferencias[CLAVE_MOSTRAR_COMPLETADAS] = nuevosAjustes.mostrarCompletadas
            preferencias[CLAVE_CONFIRMAR_ELIMINACION] = nuevosAjustes.confirmarEliminacion
            preferencias[CLAVE_ORDEN] = nuevosAjustes.ordenTareas.name
            preferencias[CLAVE_ANIMACIONES] = nuevosAjustes.animacionesActivadas
            preferencias[CLAVE_NOTIF_ACTIVADAS] = nuevosAjustes.notificacionesActivadas
            preferencias[CLAVE_RESUMEN_DIARIO] = nuevosAjustes.resumenDiarioActivado
            preferencias[CLAVE_AVISAR_VENCIDAS] = nuevosAjustes.avisarTareasVencidas
            preferencias[CLAVE_AVISAR_URGENCIA] = nuevosAjustes.avisarUrgenciaAlta
            preferencias[CLAVE_HORA] = nuevosAjustes.horaNotificacion
            preferencias[CLAVE_MINUTO] = nuevosAjustes.minutoNotificacion
            preferencias[CLAVE_ANTELACION] = nuevosAjustes.antelacionDias
            preferencias[CLAVE_SONIDO] = nuevosAjustes.sonidoNotificaciones
            preferencias[CLAVE_VIBRACION] = nuevosAjustes.vibracionNotificaciones
        }
    }

    suspend fun restaurarPredeterminados() {
        dataStore.edit { preferencias ->
            preferencias.clear()
        }
    }

    private companion object {
        val CLAVE_TEMA_COLOR = stringPreferencesKey("tema_color")
        val CLAVE_MODO_APARIENCIA = stringPreferencesKey("modo_apariencia")
        val CLAVE_DENSIDAD = stringPreferencesKey("densidad_tareas")
        val CLAVE_MOSTRAR_COMPLETADAS = booleanPreferencesKey("mostrar_completadas")
        val CLAVE_CONFIRMAR_ELIMINACION = booleanPreferencesKey("confirmar_eliminacion")
        val CLAVE_ORDEN = stringPreferencesKey("orden_tareas")
        val CLAVE_ANIMACIONES = booleanPreferencesKey("animaciones_activadas")
        val CLAVE_NOTIF_ACTIVADAS = booleanPreferencesKey("notificaciones_activadas")
        val CLAVE_RESUMEN_DIARIO = booleanPreferencesKey("resumen_diario_activado")
        val CLAVE_AVISAR_VENCIDAS = booleanPreferencesKey("avisar_tareas_vencidas")
        val CLAVE_AVISAR_URGENCIA = booleanPreferencesKey("avisar_urgencia_alta")
        val CLAVE_HORA = intPreferencesKey("hora_notificacion")
        val CLAVE_MINUTO = intPreferencesKey("minuto_notificacion")
        val CLAVE_ANTELACION = intPreferencesKey("antelacion_dias")
        val CLAVE_SONIDO = booleanPreferencesKey("sonido_notificaciones")
        val CLAVE_VIBRACION = booleanPreferencesKey("vibracion_notificaciones")
    }
}
