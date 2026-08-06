package dam.moviles.tareas_app_front.ui.theme

import androidx.compose.ui.graphics.Color
import dam.moviles.tareas_app_front.data.settings.TemaColor

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/*
 * Paletas predefinidas de la aplicación.
 *
 * Cada paleta define los roles de color que necesita MaterialTheme,
 * tanto para el tema oscuro como para el tema claro.
 */
data class PaletaColores(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val error: Color
)

// ---------------- Azul ----------------

val PaletaAzulOscura = PaletaColores(
    primary = Color(0xFF2F80ED),
    primaryContainer = Color(0xFF14406E),
    secondary = Color(0xFF72B1FF),
    background = Color(0xFF05070A),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF172033),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFFF5C5C)
)

val PaletaAzulClara = PaletaColores(
    primary = Color(0xFF2F80ED),
    primaryContainer = Color(0xFFD6E6FF),
    secondary = Color(0xFF1B4B8A),
    background = Color(0xFFF7F9FC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7EDF6),
    onPrimary = Color.White,
    onBackground = Color(0xFF0A0F1A),
    onSurface = Color(0xFF0A0F1A),
    onSurfaceVariant = Color(0xFF55606E),
    error = Color(0xFFB3261E)
)

// ---------------- Violeta ----------------

val PaletaVioletaOscura = PaletaColores(
    primary = Color(0xFF7C4DFF),
    primaryContainer = Color(0xFF2A1E5C),
    secondary = Color(0xFFA98FFF),
    background = Color(0xFF08060F),
    surface = Color(0xFF151128),
    surfaceVariant = Color(0xFF211A3D),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFA69FC0),
    error = Color(0xFFFF5C5C)
)

val PaletaVioletaClara = PaletaColores(
    primary = Color(0xFF7C4DFF),
    primaryContainer = Color(0xFFE4DDFF),
    secondary = Color(0xFF5A36C8),
    background = Color(0xFFF8F6FE),
    surface = Color.White,
    surfaceVariant = Color(0xFFECE7FB),
    onPrimary = Color.White,
    onBackground = Color(0xFF0B0914),
    onSurface = Color(0xFF0B0914),
    onSurfaceVariant = Color(0xFF5F5A70),
    error = Color(0xFFB3261E)
)

// ---------------- Verde ----------------

val PaletaVerdeOscura = PaletaColores(
    primary = Color(0xFF20B486),
    primaryContainer = Color(0xFF0F4A35),
    secondary = Color(0xFF63D9B0),
    background = Color(0xFF04110C),
    surface = Color(0xFF0E2319),
    surfaceVariant = Color(0xFF15382A),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFF8FB8A9),
    error = Color(0xFFFF5C5C)
)

val PaletaVerdeClara = PaletaColores(
    primary = Color(0xFF20B486),
    primaryContainer = Color(0xFFC9F2E3),
    secondary = Color(0xFF147556),
    background = Color(0xFFF2FBF7),
    surface = Color.White,
    surfaceVariant = Color(0xFFDFF5EC),
    onPrimary = Color.White,
    onBackground = Color(0xFF06251C),
    onSurface = Color(0xFF06251C),
    onSurfaceVariant = Color(0xFF4F6B60),
    error = Color(0xFFB3261E)
)

// ---------------- Coral ----------------

val PaletaCoralOscura = PaletaColores(
    primary = Color(0xFFFF5C5C),
    primaryContainer = Color(0xFF5C1A1B),
    secondary = Color(0xFFFF9A9A),
    background = Color(0xFF120607),
    surface = Color(0xFF241112),
    surfaceVariant = Color(0xFF381A1B),
    onPrimary = Color.White,
    onBackground = Color(0xFFFFF8F6),
    onSurface = Color(0xFFFFF8F6),
    onSurfaceVariant = Color(0xFFC0A8A6),
    error = Color(0xFFFFB4AB)
)

val PaletaCoralClara = PaletaColores(
    primary = Color(0xFFFF5C5C),
    primaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFA64444),
    background = Color(0xFFFEF6F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFFBE5E5),
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1010),
    onSurface = Color(0xFF1C1010),
    onSurfaceVariant = Color(0xFF6E5555),
    error = Color(0xFFB3261E)
)

// ---------------- Ámbar ----------------

val PaletaAmbarOscura = PaletaColores(
    primary = Color(0xFFF5A623),
    primaryContainer = Color(0xFF4A3A0F),
    secondary = Color(0xFFFFC86A),
    background = Color(0xFF100B02),
    surface = Color(0xFF20170B),
    surfaceVariant = Color(0xFF33270F),
    onPrimary = Color(0xFF241A00),
    onBackground = Color(0xFFFFF9EF),
    onSurface = Color(0xFFFFF9EF),
    onSurfaceVariant = Color(0xFFBDAF8F),
    error = Color(0xFFFF5C5C)
)

val PaletaAmbarClara = PaletaColores(
    primary = Color(0xFFF5A623),
    primaryContainer = Color(0xFFFFE3AD),
    secondary = Color(0xFF8A5D00),
    background = Color(0xFFFEF9F0),
    surface = Color.White,
    surfaceVariant = Color(0xFFF8EBCF),
    onPrimary = Color(0xFF241A00),
    onBackground = Color(0xFF1A1300),
    onSurface = Color(0xFF1A1300),
    onSurfaceVariant = Color(0xFF6F6248),
    error = Color(0xFFB3261E)
)

fun paletaOscura(tema: TemaColor): PaletaColores {
    return when (tema) {
        TemaColor.AZUL -> PaletaAzulOscura
        TemaColor.VIOLETA -> PaletaVioletaOscura
        TemaColor.VERDE -> PaletaVerdeOscura
        TemaColor.CORAL -> PaletaCoralOscura
        TemaColor.AMBAR -> PaletaAmbarOscura
    }
}

fun paletaClara(tema: TemaColor): PaletaColores {
    return when (tema) {
        TemaColor.AZUL -> PaletaAzulClara
        TemaColor.VIOLETA -> PaletaVioletaClara
        TemaColor.VERDE -> PaletaVerdeClara
        TemaColor.CORAL -> PaletaCoralClara
        TemaColor.AMBAR -> PaletaAmbarClara
    }
}
