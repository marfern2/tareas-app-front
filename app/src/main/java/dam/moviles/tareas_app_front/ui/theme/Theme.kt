package dam.moviles.tareas_app_front.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import dam.moviles.tareas_app_front.data.settings.AppSettings
import dam.moviles.tareas_app_front.data.settings.ModoApariencia

@Composable
fun TareasappfrontTheme(
    ajustes: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val usarTemaOscuro = when (ajustes.modoApariencia) {
        ModoApariencia.SISTEMA -> isSystemInDarkTheme()
        ModoApariencia.OSCURO -> true
        ModoApariencia.CLARO -> false
    }

    val paleta = if (usarTemaOscuro) {
        paletaOscura(ajustes.temaColor)
    } else {
        paletaClara(ajustes.temaColor)
    }

    val colorScheme = if (usarTemaOscuro) {
        darkColorScheme(
            primary = paleta.primary,
            onPrimary = paleta.onPrimary,
            primaryContainer = paleta.primaryContainer,
            secondary = paleta.secondary,
            background = paleta.background,
            onBackground = paleta.onBackground,
            surface = paleta.surface,
            onSurface = paleta.onSurface,
            surfaceVariant = paleta.surfaceVariant,
            onSurfaceVariant = paleta.onSurfaceVariant,
            error = paleta.error
        )
    } else {
        lightColorScheme(
            primary = paleta.primary,
            onPrimary = paleta.onPrimary,
            primaryContainer = paleta.primaryContainer,
            secondary = paleta.secondary,
            background = paleta.background,
            onBackground = paleta.onBackground,
            surface = paleta.surface,
            onSurface = paleta.onSurface,
            surfaceVariant = paleta.surfaceVariant,
            onSurfaceVariant = paleta.onSurfaceVariant,
            error = paleta.error
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
