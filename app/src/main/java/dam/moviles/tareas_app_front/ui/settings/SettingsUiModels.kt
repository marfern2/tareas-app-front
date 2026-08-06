package dam.moviles.tareas_app_front.ui.settings

import androidx.compose.ui.graphics.Color
import dam.moviles.tareas_app_front.data.settings.DensidadTareas
import dam.moviles.tareas_app_front.data.settings.ModoApariencia
import dam.moviles.tareas_app_front.data.settings.OrdenTareas
import dam.moviles.tareas_app_front.data.settings.TemaColor

internal const val VERSION_APLICACION = "1.0"

internal data class OpcionModoApariencia(
    val modo: ModoApariencia,
    val titulo: String,
    val descripcion: String
)

internal val opcionesModoApariencia = listOf(
    OpcionModoApariencia(
        modo = ModoApariencia.SISTEMA,
        titulo = "Sistema",
        descripcion = "Sigue el tema del dispositivo"
    ),
    OpcionModoApariencia(
        modo = ModoApariencia.OSCURO,
        titulo = "Oscuro",
        descripcion = "Tema oscuro siempre"
    ),
    OpcionModoApariencia(
        modo = ModoApariencia.CLARO,
        titulo = "Claro",
        descripcion = "Tema claro siempre"
    )
)

internal data class OpcionDensidad(
    val densidad: DensidadTareas,
    val titulo: String,
    val descripcion: String
)

internal val opcionesDensidad = listOf(
    OpcionDensidad(
        densidad = DensidadTareas.COMPACTA,
        titulo = "Compacta",
        descripcion = "Más tareas en pantalla"
    ),
    OpcionDensidad(
        densidad = DensidadTareas.NORMAL,
        titulo = "Normal",
        descripcion = "Tamaño equilibrado"
    ),
    OpcionDensidad(
        densidad = DensidadTareas.COMODA,
        titulo = "Cómoda",
        descripcion = "Tarjetas más amplias"
    )
)

internal data class OpcionOrden(
    val orden: OrdenTareas,
    val titulo: String,
    val descripcion: String
)

internal val opcionesOrden = listOf(
    OpcionOrden(
        orden = OrdenTareas.FECHA,
        titulo = "Fecha",
        descripcion = "Más próximas primero"
    ),
    OpcionOrden(
        orden = OrdenTareas.URGENCIA,
        titulo = "Urgencia",
        descripcion = "Más urgentes primero"
    ),
    OpcionOrden(
        orden = OrdenTareas.NOMBRE,
        titulo = "Nombre",
        descripcion = "Orden alfabético"
    ),
    OpcionOrden(
        orden = OrdenTareas.ESTADO,
        titulo = "Estado",
        descripcion = "Pendientes antes que completadas"
    )
)

internal data class OpcionTemaColor(
    val tema: TemaColor,
    val nombre: String,
    val colorPrimario: Color
)

internal val opcionesTemaColor = listOf(
    OpcionTemaColor(
        tema = TemaColor.AZUL,
        nombre = "Azul",
        colorPrimario = Color(0xFF2F80ED)
    ),
    OpcionTemaColor(
        tema = TemaColor.VIOLETA,
        nombre = "Violeta",
        colorPrimario = Color(0xFF7C4DFF)
    ),
    OpcionTemaColor(
        tema = TemaColor.VERDE,
        nombre = "Verde",
        colorPrimario = Color(0xFF20B486)
    ),
    OpcionTemaColor(
        tema = TemaColor.CORAL,
        nombre = "Coral",
        colorPrimario = Color(0xFFFF5C5C)
    ),
    OpcionTemaColor(
        tema = TemaColor.AMBAR,
        nombre = "Ámbar",
        colorPrimario = Color(0xFFF5A623)
    )
)
