package dam.moviles.tareas_app_front.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun AvatarUsuario(
    nombreUsuario: String,
    fotoUri: String?,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var avatarModifier = modifier
        .size(tamano)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        )

    if (onClick != null) {
        avatarModifier = avatarModifier.clickable {
            onClick()
        }
    }

    Box(
        modifier = avatarModifier,
        contentAlignment = Alignment.Center
    ) {
        if (!fotoUri.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(fotoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(tamano)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    AvatarFallback(nombreUsuario, tamano)
                },
                error = {
                    AvatarFallback(nombreUsuario, tamano)
                }
            )
        } else {
            AvatarFallback(nombreUsuario, tamano)
        }
    }
}

@Composable
private fun AvatarFallback(
    nombreUsuario: String,
    tamano: Dp
) {
    Text(
        text = obtenerIniciales(nombreUsuario),
        color = Color.White,
        fontSize = when {
            tamano >= 100.dp -> 30.sp
            tamano >= 70.dp -> 22.sp
            else -> 16.sp
        },
        fontWeight = FontWeight.Bold
    )
}

private fun obtenerIniciales(nombreUsuario: String): String {
    val partes = nombreUsuario
        .trim()
        .split(Regex("\\s+"))
        .filter { parte ->
            parte.isNotBlank()
        }

    if (partes.isEmpty()) {
        return "U"
    }

    return partes
        .take(2)
        .joinToString(separator = "") { parte ->
            parte.first().uppercaseChar().toString()
        }
}
