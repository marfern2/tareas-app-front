package dam.moviles.tareas_app_front.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.settings.DensidadTareas
import dam.moviles.tareas_app_front.data.settings.ModoApariencia
import dam.moviles.tareas_app_front.data.settings.OrdenTareas
import dam.moviles.tareas_app_front.data.settings.TemaColor

@Composable
fun SelectorModoApariencia(
    seleccionado: ModoApariencia,
    onSeleccionar: (ModoApariencia) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opcionesModoApariencia.forEach { opcion ->
            TarjetaSeleccionable(
                texto = opcion.titulo,
                descripcion = opcion.descripcion,
                seleccionada = seleccionado == opcion.modo,
                onClick = {
                    onSeleccionar(opcion.modo)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SelectorDensidad(
    seleccionado: DensidadTareas,
    onSeleccionar: (DensidadTareas) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opcionesDensidad.forEach { opcion ->
            TarjetaSeleccionable(
                texto = opcion.titulo,
                descripcion = opcion.descripcion,
                seleccionada = seleccionado == opcion.densidad,
                onClick = {
                    onSeleccionar(opcion.densidad)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SelectorOrden(
    seleccionado: OrdenTareas,
    onSeleccionar: (OrdenTareas) -> Unit
) {
    val filas = opcionesOrden.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { opcion ->
                    TarjetaSeleccionable(
                        texto = opcion.titulo,
                        descripcion = opcion.descripcion,
                        seleccionada = seleccionado == opcion.orden,
                        onClick = {
                            onSeleccionar(opcion.orden)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectorColorTema(
    seleccionado: TemaColor,
    onSeleccionar: (TemaColor) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        opcionesTemaColor.forEach { opcion ->
            OpcionColorTema(
                nombre = opcion.nombre,
                colorPrimario = opcion.colorPrimario,
                seleccionada = seleccionado == opcion.tema,
                onClick = {
                    onSeleccionar(opcion.tema)
                }
            )
        }
    }
}

@Composable
private fun TarjetaSeleccionable(
    texto: String,
    descripcion: String? = null,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrimario = MaterialTheme.colorScheme.primary
    val forma = RoundedCornerShape(14.dp)
    val colorBorde = if (seleccionada) {
        colorPrimario
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    Column(
        modifier = modifier
            .clip(forma)
            .border(
                width = 1.5.dp,
                color = colorBorde,
                shape = forma
            )
            .background(
                color = if (seleccionada) {
                    colorPrimario.copy(alpha = 0.10f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .semantics {
                contentDescription = texto
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = texto,
            color = if (seleccionada) colorPrimario else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        if (descripcion != null) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun OpcionColorTema(
    nombre: String,
    colorPrimario: Color,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onClick()
            }
            .semantics {
                contentDescription = "Color $nombre"
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colorPrimario)
                .then(
                    if (seleccionada) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (seleccionada) {
                Text(
                    text = "✓",
                    color = colorContraste(colorPrimario),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = nombre,
            color = if (seleccionada) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 12.sp,
            fontWeight = if (seleccionada) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
        )
    }
}

private fun colorContraste(color: Color): Color {
    val luminancia = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue

    return if (luminancia > 0.5f) {
        Color(0xFF1A1A1A)
    } else {
        Color.White
    }
}
