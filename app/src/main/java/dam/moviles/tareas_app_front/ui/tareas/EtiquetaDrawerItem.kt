package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EtiquetaDrawerItem(
    nombre: String,
    contador: Int,
    seleccionado: Boolean,
    colorSeleccionado: Color,
    colorNormal: Color,
    onClick: () -> Unit
) {
    val blanco = Color.White
    val grisClaro = Color(0xFFE5E7EB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(
                color = if (seleccionado) colorSeleccionado else colorNormal,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nombre,
            color = blanco,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .background(
                    color = if (seleccionado) Color.White else Color(0xFF64748B),
                    shape = RoundedCornerShape(7.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contador.toString(),
                color = if (seleccionado) colorSeleccionado else grisClaro,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}