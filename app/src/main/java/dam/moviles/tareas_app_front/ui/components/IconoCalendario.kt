package dam.moviles.tareas_app_front.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun IconoCalendario(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val ancho = size.width
        val alto = size.height

        if (ancho <= 0f || alto <= 0f) {
            return@Canvas
        }

        val grosor = ancho * 0.08f
        val cuerpoInicioX = ancho * 0.16f
        val cuerpoFinX = ancho * 0.84f
        val cuerpoInicioY = alto * 0.28f
        val cuerpoFinY = alto * 0.88f
        val radio = ancho * 0.10f

        drawLine(
            color = color,
            start = Offset(ancho * 0.30f, alto * 0.16f),
            end = Offset(ancho * 0.30f, cuerpoInicioY + ancho * 0.02f),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(ancho * 0.70f, alto * 0.16f),
            end = Offset(ancho * 0.70f, cuerpoInicioY + ancho * 0.02f),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(cuerpoInicioX, cuerpoInicioY),
            size = Size(
                width = cuerpoFinX - cuerpoInicioX,
                height = cuerpoFinY - cuerpoInicioY
            ),
            cornerRadius = CornerRadius(radio, radio),
            style = Stroke(width = grosor, cap = StrokeCap.Round)
        )

        drawLine(
            color = color,
            start = Offset(cuerpoInicioX + ancho * 0.10f, alto * 0.46f),
            end = Offset(cuerpoFinX - ancho * 0.10f, alto * 0.46f),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        val radioPunto = ancho * 0.035f

        listOf(ancho * 0.34f, ancho * 0.52f, ancho * 0.70f).forEach { cx ->
            drawCircle(
                color = color,
                radius = radioPunto,
                center = Offset(cx, alto * 0.64f)
            )
        }
    }
}
