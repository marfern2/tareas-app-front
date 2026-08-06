package dam.moviles.tareas_app_front.data.settings

val AjustesPredeterminados = AppSettings()

data class AppSettings(
    val temaColor: TemaColor = TemaColor.AZUL,
    val modoApariencia: ModoApariencia = ModoApariencia.SISTEMA,
    val densidadTareas: DensidadTareas = DensidadTareas.NORMAL,
    val mostrarCompletadas: Boolean = true,
    val confirmarEliminacion: Boolean = true,
    val ordenTareas: OrdenTareas = OrdenTareas.FECHA,
    val animacionesActivadas: Boolean = true,
    val notificacionesActivadas: Boolean = false,
    val resumenDiarioActivado: Boolean = true,
    val avisarTareasVencidas: Boolean = true,
    val avisarUrgenciaAlta: Boolean = true,
    val horaNotificacion: Int = 9,
    val minutoNotificacion: Int = 0,
    val antelacionDias: Int = 1,
    val sonidoNotificaciones: Boolean = true,
    val vibracionNotificaciones: Boolean = true
)

enum class TemaColor {
    AZUL,
    VIOLETA,
    VERDE,
    CORAL,
    AMBAR;

    companion object {
        fun desdeNombre(valor: String?): TemaColor? {
            return values().find { tema ->
                tema.name == valor
            }
        }
    }
}

enum class ModoApariencia {
    SISTEMA,
    OSCURO,
    CLARO;

    companion object {
        fun desdeNombre(valor: String?): ModoApariencia? {
            return values().find { modo ->
                modo.name == valor
            }
        }
    }
}

enum class DensidadTareas {
    COMPACTA,
    NORMAL,
    COMODA;

    companion object {
        fun desdeNombre(valor: String?): DensidadTareas? {
            return values().find { densidad ->
                densidad.name == valor
            }
        }
    }
}

enum class OrdenTareas {
    FECHA,
    URGENCIA,
    NOMBRE,
    ESTADO;

    companion object {
        fun desdeNombre(valor: String?): OrdenTareas? {
            return values().find { orden ->
                orden.name == valor
            }
        }
    }
}
