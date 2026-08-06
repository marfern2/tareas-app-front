package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TareaResponseDto
import dam.moviles.tareas_app_front.ui.theme.TareasappfrontTheme

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Tareas Screen Preview"
)
@Composable
fun TareasScreenPreview() {
    val tareasFake = listOf(
        TareaResponseDto(
            id = 1,
            titulo = "Estudiar Jetpack Compose",
            descripcion = "Repasar Column, Row, estados, previews y navegación simple.",
            fecha = "2026-07-05",
            completada = false,
            urgencia = 2,
            usuarioId = 1,
            tipoTareaId = 1,
            tipoTareaNombre = "Clases",
            tipoTareaColor = "#2196F3"
        ),
        TareaResponseDto(
            id = 2,
            titulo = "Hacer login",
            descripcion = "Guardar token JWT si el usuario marca mantener sesión iniciada.",
            fecha = "2026-07-06",
            completada = true,
            urgencia = 1,
            usuarioId = 1,
            tipoTareaId = 2,
            tipoTareaNombre = "Trabajo",
            tipoTareaColor = "#22C55E"
        ),
        TareaResponseDto(
            id = 3,
            titulo = "Ir al gimnasio",
            descripcion = "Pierna y cardio suave.",
            fecha = "2026-07-07",
            completada = false,
            urgencia = 0,
            usuarioId = 1,
            tipoTareaId = 3,
            tipoTareaNombre = "gym",
            tipoTareaColor = "#A855F7"
        )
    )

    TareasappfrontTheme {
        TareasScreenPreviewContent(
            tareas = tareasFake
        )
    }
}

@Composable
fun TareasScreenPreviewContent(
    tareas: List<TareaResponseDto>
) {
    val morado = Color(0xFF5B35F5)
    val fondo = Color(0xFF050505)
    val blanco = Color.White

    Scaffold(
        containerColor = fondo,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = morado,
                contentColor = blanco,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Text(
                    text = "+",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                // Usamos el padding superior e inferior del sistema, y 20.dp en los lados
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 25.dp,
                    bottom = paddingValues.calculateBottomPadding() + 20.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {}
                ) {
                    Text(
                        text = "☰",
                        color = blanco,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Mis tareas",
                    color = blanco,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Etiqueta: Todas",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tareas) { tarea ->
                    TareaItem(
                        tarea = tarea,
                        onClick = {},
                        onCompletarChange = {},
                        onEditarClick = {},
                        onEliminarClick = {}
                    )
                }
            }
        }
    }
}