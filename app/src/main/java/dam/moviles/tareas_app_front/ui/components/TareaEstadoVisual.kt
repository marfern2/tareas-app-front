package dam.moviles.tareas_app_front.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun esTemaOscuroActual(): Boolean {
    return MaterialTheme.colorScheme.surface.luminance() < 0.5f
}

@Composable
private fun verdeCompletada(): Color {
    return if (esTemaOscuroActual()) {
        Color(0xFF66BB6A)
    } else {
        Color(0xFF2E7D32)
    }
}

@Composable
fun colorCompletadaSuave(): Color {
    val verde = verdeCompletada()

    return if (esTemaOscuroActual()) {
        verde.copy(alpha = 0.16f).compositeOver(
            MaterialTheme.colorScheme.surface
        )
    } else {
        verde.copy(alpha = 0.12f).compositeOver(Color.White)
    }
}

@Composable
fun colorBadgeCompletadaFondo(): Color {
    val verde = verdeCompletada()

    return if (esTemaOscuroActual()) {
        verde.copy(alpha = 0.24f).compositeOver(
            MaterialTheme.colorScheme.surface
        )
    } else {
        verde.copy(alpha = 0.16f).compositeOver(Color.White)
    }
}

@Composable
fun colorBadgeCompletadaTexto(): Color {
    return verdeCompletada()
}

@Composable
fun BadgeCompletada(
    modifier: Modifier = Modifier,
    fondo: Color? = null,
    texto: Color? = null
) {
    Box(
        modifier = modifier
            .background(
                color = fondo ?: colorBadgeCompletadaFondo(),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Completada",
            color = texto ?: colorBadgeCompletadaTexto(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
