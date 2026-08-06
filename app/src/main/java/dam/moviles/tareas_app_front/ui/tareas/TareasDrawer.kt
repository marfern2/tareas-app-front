package dam.moviles.tareas_app_front.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.data.remote.dto.TipoTareaResponseDto
import dam.moviles.tareas_app_front.ui.components.AvatarUsuario

@Composable
fun TareasDrawer(
    nombreUsuario: String,
    emailUsuario: String,
    fotoPerfilUri: String?,
    tiposTarea: List<TipoTareaResponseDto>,
    contadorTodas: Int,
    contadorPorTipo: Map<Long, Int>,
    tipoSeleccionadoId: Long?,
    onVerPerfil: () -> Unit,
    onSeleccionarTodas: () -> Unit,
    onSeleccionarTipo: (Long) -> Unit,
    onCerrarDrawer: () -> Unit,
    onLogout: () -> Unit,
    onAjustes: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val fondoDrawer = colorScheme.surfaceVariant
    val tarjetaUsuario = colorScheme.surface
    val azul = colorScheme.primary
    val itemNormal = colorScheme.surfaceVariant
    val blanco = colorScheme.onSurface
    val gris = colorScheme.onSurfaceVariant
    val rojo = colorScheme.error

    val nombreMostrado = nombreUsuario.ifBlank {
        "Usuario"
    }

    ModalDrawerSheet(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        drawerContainerColor = fondoDrawer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Donit",
                    color = blanco,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "×",
                    color = gris,
                    fontSize = 26.sp,
                    modifier = Modifier
                        .clickable {
                            onCerrarDrawer()
                        }
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onVerPerfil()
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = tarjetaUsuario
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarUsuario(
                            nombreUsuario = nombreMostrado,
                            fotoUri = fotoPerfilUri,
                            tamano = 58.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = nombreMostrado,
                                color = blanco,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )

                            if (emailUsuario.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = emailUsuario,
                                    color = gris,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Text(
                            text = "›",
                            color = azul,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Light
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = colorScheme.onSurface.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ver perfil",
                            color = azul,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "→",
                            color = azul,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "TAREAS",
                color = gris,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "todas") {
                    EtiquetaDrawerItem(
                        nombre = "Todas",
                        contador = contadorTodas,
                        seleccionado = tipoSeleccionadoId == null,
                        colorSeleccionado = azul,
                        colorNormal = itemNormal,
                        onClick = onSeleccionarTodas
                    )
                }

                items(
                    items = tiposTarea,
                    key = { tipo ->
                        tipo.id
                    }
                ) { tipo ->
                    EtiquetaDrawerItem(
                        nombre = tipo.nombre,
                        contador = contadorPorTipo[tipo.id] ?: 0,
                        seleccionado = tipoSeleccionadoId == tipo.id,
                        colorSeleccionado = azul,
                        colorNormal = itemNormal,
                        onClick = {
                            onSeleccionarTipo(tipo.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colorScheme.onSurface.copy(alpha = 0.12f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAjustes()
                    }
                    .padding(
                        horizontal = 10.dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙",
                    color = azul,
                    fontSize = 19.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Ajustes",
                    color = blanco,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLogout()
                    }
                    .padding(
                        horizontal = 10.dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "↪",
                    color = rojo,
                    fontSize = 19.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Cerrar sesión",
                    color = rojo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
