package dam.moviles.tareas_app_front.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedBlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    expanded: Boolean = false,
    size: Dp = 48.dp,
    height: Dp = 54.dp,
    fontSize: TextUnit = 16.sp,
    animacionesActivadas: Boolean = true,
    contentDescription: String? = null,
    content: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val escala by animateFloatAsState(
        targetValue = if (animacionesActivadas && isPressed && enabled && !isLoading) {
            0.94f
        } else {
            1f
        },
        label = "animated_blue_button_scale"
    )

    val colorFondo by animateColorAsState(
        targetValue = when {
            !enabled || isLoading -> colorScheme.surfaceVariant
            isPressed -> colorScheme.primary.copy(alpha = 0.85f)
            else -> colorScheme.primary
        },
        label = "animated_blue_button_color"
    )

    val colorContenido = when {
        !enabled || isLoading -> colorScheme.onSurfaceVariant
        else -> colorScheme.onPrimary
    }

    val finalModifier = if (expanded) {
        modifier
            .fillMaxWidth()
            .height(height)
            .scale(escala)
    } else {
        modifier
            .size(size)
            .scale(escala)
    }

    val modifierAccesible = if (contentDescription != null) {
        finalModifier.semantics {
            this.contentDescription = contentDescription
        }
    } else {
        finalModifier
    }

    Button(
        onClick = onClick,
        modifier = modifierAccesible,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorFondo,
            contentColor = colorContenido,
            disabledContainerColor = colorScheme.surfaceVariant,
            disabledContentColor = colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
            disabledElevation = 2.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = colorContenido,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else if (content != null) {
            content()
        } else {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = colorContenido
            )
        }
    }
}
