package dam.moviles.tareas_app_front.ui.auth.registro

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam.moviles.tareas_app_front.R
import dam.moviles.tareas_app_front.data.remote.RetrofitClient
import dam.moviles.tareas_app_front.data.remote.dto.RegistroRequestDto
import dam.moviles.tareas_app_front.ui.auth.login.LoginScreen
import dam.moviles.tareas_app_front.ui.components.AnimatedBlueButton
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun RegisterScreen(
    onGoToLogin: (String?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val azul = Color(0xFF2196F3)
    val azulOscuro = Color(0xFF1565C0)
    val fondo = Color(0xFF050505)
    val tarjeta = Color(0xFF121212)
    val blanco = Color.White
    val gris = Color(0xFFBDBDBD)
    val rojo = Color(0xFFFF5252)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = tarjeta
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo de la app",
                    modifier = Modifier
                        .size(115.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "Crear cuenta",
                    color = blanco,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Regístrate para tu espacio personal Donit",
                    color = gris,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = null
                        generalError = null
                    },
                    label = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = usernameError != null,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = blanco,
                        unfocusedTextColor = blanco,
                        focusedContainerColor = Color(0xFF1B1B1B),
                        unfocusedContainerColor = Color(0xFF1B1B1B),
                        focusedBorderColor = azul,
                        unfocusedBorderColor = Color.DarkGray,
                        errorBorderColor = rojo,
                        focusedLabelColor = azul,
                        unfocusedLabelColor = gris,
                        errorLabelColor = rojo,
                        cursorColor = azul
                    )
                )

                if (usernameError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = usernameError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                        generalError = null
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = emailError != null,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = blanco,
                        unfocusedTextColor = blanco,
                        focusedContainerColor = Color(0xFF1B1B1B),
                        unfocusedContainerColor = Color(0xFF1B1B1B),
                        focusedBorderColor = azul,
                        unfocusedBorderColor = Color.DarkGray,
                        errorBorderColor = rojo,
                        focusedLabelColor = azul,
                        unfocusedLabelColor = gris,
                        errorLabelColor = rojo,
                        cursorColor = azul
                    )
                )

                if (emailError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = emailError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                        generalError = null
                    },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = passwordError != null,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = blanco,
                        unfocusedTextColor = blanco,
                        focusedContainerColor = Color(0xFF1B1B1B),
                        unfocusedContainerColor = Color(0xFF1B1B1B),
                        focusedBorderColor = azul,
                        unfocusedBorderColor = Color.DarkGray,
                        errorBorderColor = rojo,
                        focusedLabelColor = azul,
                        unfocusedLabelColor = gris,
                        errorLabelColor = rojo,
                        cursorColor = azul
                    )
                )

                if (passwordError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = passwordError!!,
                        color = rojo,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (generalError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = generalError!!,
                        color = rojo,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedBlueButton(
                    text = "Crear cuenta",
                    onClick = {
                        usernameError = null
                        emailError = null
                        passwordError = null
                        generalError = null

                        var hayError = false

                        if (username.isBlank()) {
                            usernameError = "El nombre de usuario es obligatorio"
                            hayError = true
                        }

                        if (email.isBlank()) {
                            emailError = "El email es obligatorio"
                            hayError = true
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            emailError = "Introduce un email válido"
                            hayError = true
                        }

                        if (password.isBlank()) {
                            passwordError = "La contraseña es obligatoria"
                            hayError = true
                        } else if (password.length < 6) {
                            passwordError = "La contraseña debe tener mínimo 6 caracteres"
                            hayError = true
                        }

                        if (hayError) {
                            return@AnimatedBlueButton
                        }

                        scope.launch {
                            try {
                                isLoading = true

                                RetrofitClient.authApi.registro(
                                    RegistroRequestDto(
                                        username = username.trim(),
                                        email = email.trim(),
                                        password = password
                                    )
                                )

                                Toast.makeText(
                                    context,
                                    "Cuenta creada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onGoToLogin("Cuenta creada correctamente. Ya puedes iniciar sesión.")

                            } catch (e: HttpException) {
                                generalError = when (e.code()) {
                                    400 -> "Revisa los datos introducidos"
                                    409 -> "El usuario o email ya existe"
                                    else -> "Error del servidor: ${e.code()}"
                                }
                            } catch (e: Exception) {
                                generalError = "No se pudo conectar con la API"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    expanded = true,
                    height = 54.dp,
                    fontSize = 16.sp,
                    isLoading = isLoading,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        if (!isLoading) {
                            onGoToLogin(null)
                        }
                    }
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = azul,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Componente Register"
)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onGoToLogin = {}
    )
}