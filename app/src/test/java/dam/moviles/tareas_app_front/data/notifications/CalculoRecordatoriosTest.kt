package dam.moviles.tareas_app_front.data.notifications

import dam.moviles.tareas_app_front.data.settings.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CalculoRecordatoriosTest {

    private fun calendario(
        anio: Int,
        mes: Int,
        dia: Int,
        hora: Int = 0,
        minuto: Int = 0
    ): Calendar {
        val c = Calendar.getInstance()
        c.clear()
        c.set(anio, mes, dia, hora, minuto, 0)
        return c
    }

    private val ahoraJueves8h: Calendar = calendario(2026, 7, 6, 8, 0)

    private fun tarea(
        id: Long = 1L,
        fecha: String = "2026-08-07",
        urgencia: Int = 0,
        completada: Boolean = false
    ): TareaRecordatorio {
        return TareaRecordatorio(
            id = id,
            titulo = "Tarea de prueba",
            descripcion = null,
            fecha = fecha,
            urgencia = urgencia,
            tipoNombre = "Trabajo",
            completada = completada
        )
    }

    private val ajustesPorDefecto = AppSettings(
        notificacionesActivadas = true,
        avisarUrgenciaAlta = true,
        horaNotificacion = 9,
        minutoNotificacion = 0,
        antelacionDias = 1
    )

    // 1. Tarea para mañana, antelación 1 día, hora 09:00
    @Test
    fun tareaParaManana_conAntelacionUnDia_yHora9_seProgramaHoy9() {
        val instante = calcularInstanteRecordatorio(
            fecha = "2026-08-07",
            hora = 9,
            minuto = 0,
            antelacionDias = 1,
            ahora = ahoraJueves8h
        )

        val esperado = calendario(2026, 7, 6, 9, 0)

        assertNotNull(instante)
        assertEquals(esperado.timeInMillis, instante)
        assertTrue(instante!! > ahoraJueves8h.timeInMillis)
    }

    // 2. Tarea para dentro de una semana
    @Test
    fun tareaDentroDeUnaSemana_seProgramaEnSuFechaConAntelacion() {
        val instante = calcularInstanteRecordatorio(
            fecha = "2026-08-13",
            hora = 9,
            minuto = 0,
            antelacionDias = 2,
            ahora = ahoraJueves8h
        )

        val esperado = calendario(2026, 7, 11, 9, 0)

        assertNotNull(instante)
        assertEquals(esperado.timeInMillis, instante)
    }

    // 3. Instante calculado ya pasado
    @Test
    fun instanteYaPasado_devuelveNull() {
        val ahoraMediodia = calendario(2026, 7, 6, 12, 0)

        val instante = calcularInstanteRecordatorio(
            fecha = "2026-08-06",
            hora = 9,
            minuto = 0,
            antelacionDias = 0,
            ahora = ahoraMediodia
        )

        assertNull(instante)
    }

    // 4. Fecha inválida
    @Test
    fun fechaInvalida_devuelveNull() {
        val instante = calcularInstanteRecordatorio(
            fecha = "2026-13-45",
            hora = 9,
            minuto = 0,
            antelacionDias = 1,
            ahora = ahoraJueves8h
        )

        assertNull(instante)
    }

    // 5. Tarea completada: no programar
    @Test
    fun tareaCompletada_noSePrograma() {
        val resultado = deberiaProgramarRecordatorio(
            tarea = tarea(completada = true),
            ajustes = ajustesPorDefecto
        )

        assertFalse(resultado)
    }

    // 6. Notificaciones desactivadas: no programar
    @Test
    fun notificacionesDesactivadas_noSePrograma() {
        val ajustes = ajustesPorDefecto.copy(notificacionesActivadas = false)

        val resultado = deberiaProgramarRecordatorio(
            tarea = tarea(),
            ajustes = ajustes
        )

        assertFalse(resultado)
    }

    // 7. Urgencia alta con avisos urgentes desactivados
    @Test
    fun urgenciaAltaSinAvisosUrgentes_noSePrograma() {
        val ajustes = ajustesPorDefecto.copy(avisarUrgenciaAlta = false)

        val resultado = deberiaProgramarRecordatorio(
            tarea = tarea(urgencia = 2),
            ajustes = ajustes
        )

        assertFalse(resultado)
    }

    @Test
    fun urgenciaAltaConAvisosUrgentes_siSePrograma() {
        val resultado = deberiaProgramarRecordatorio(
            tarea = tarea(urgencia = 2),
            ajustes = ajustesPorDefecto
        )

        assertTrue(resultado)
    }

    // 8. Cambio de hora
    @Test
    fun cambioDeHora_cambiaElInstante() {
        val aLas9 = calcularInstanteRecordatorio(
            fecha = "2026-08-13",
            hora = 9,
            minuto = 0,
            antelacionDias = 0,
            ahora = ahoraJueves8h
        )

        val aLas18 = calcularInstanteRecordatorio(
            fecha = "2026-08-13",
            hora = 18,
            minuto = 0,
            antelacionDias = 0,
            ahora = ahoraJueves8h
        )

        assertNotNull(aLas9)
        assertNotNull(aLas18)
        assertNotEquals(aLas9, aLas18)
    }

    // 9. Cambio de antelación
    @Test
    fun cambioDeAntelacion_cambiaElInstante() {
        val sinAntelacion = calcularInstanteRecordatorio(
            fecha = "2026-08-13",
            hora = 9,
            minuto = 0,
            antelacionDias = 0,
            ahora = ahoraJueves8h
        )

        val dosDiasAntes = calcularInstanteRecordatorio(
            fecha = "2026-08-13",
            hora = 9,
            minuto = 0,
            antelacionDias = 2,
            ahora = ahoraJueves8h
        )

        assertNotNull(sinAntelacion)
        assertNotNull(dosDiasAntes)
        assertNotEquals(sinAntelacion, dosDiasAntes)
    }

    // 10. IDs de usuario diferentes no colisionan
    @Test
    fun idsDeUsuariosDiferentes_noColisionan() {
        val usuario1 = NotificationIds.idRecordatorio(1L, 5L)
        val usuario2 = NotificationIds.idRecordatorio(2L, 5L)

        assertNotEquals(usuario1, usuario2)

        val otraTareaMismoUsuario =
            NotificationIds.idRecordatorio(1L, 6L)

        assertNotEquals(usuario1, otraTareaMismoUsuario)

        val resumenUsuario1 = NotificationIds.idResumen(1L)
        assertNotEquals(usuario1, resumenUsuario1)
    }

    @Test
    fun idsDeUsuariosIguales_sonEstables() {
        assertEquals(
            NotificationIds.idRecordatorio(7L, 42L),
            NotificationIds.idRecordatorio(7L, 42L)
        )
    }

    // Retraso hasta la próxima hora configurada
    @Test
    fun retrasoHastaProximaHora_cuandoAunNoHaPasado() {
        val ahora = calendario(2026, 7, 6, 8, 0)

        val retraso = retrasoHastaProximaHora(9, 0, ahora)

        assertEquals(60L * 60 * 1000, retraso)
    }

    @Test
    fun retrasoHastaProximaHora_cuandoYaHaPasado_vaAlDiaSiguiente() {
        val ahora = calendario(2026, 7, 6, 10, 0)

        val retraso = retrasoHastaProximaHora(9, 0, ahora)

        assertEquals(23L * 60 * 60 * 1000, retraso)
    }
}
