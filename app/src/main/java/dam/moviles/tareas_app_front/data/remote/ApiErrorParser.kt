package dam.moviles.tareas_app_front.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException
import java.io.IOException

data class ApiErrorResult(
    val fieldErrors: Map<String, String> = emptyMap(),
    val generalMessage: String? = null
)

sealed class ResultadoGuardarTarea {
    data object Exito : ResultadoGuardarTarea()
    data class ErrorCampos(
        val errores: Map<String, String>
    ) : ResultadoGuardarTarea()
    data class ErrorGeneral(
        val mensaje: String
    ) : ResultadoGuardarTarea()
}

object ApiErrorParser {

    fun parsear(e: Exception): ApiErrorResult {
        if (e is HttpException) {
            val codigo = e.code()
            val body = e.response()?.errorBody()?.string()

            if (!body.isNullOrBlank()) {
                try {
                    val json = Gson().fromJson(body, JsonObject::class.java)

                    val erroresCampos = mutableMapOf<String, String>()

                    if (json.has("errors") && json.get("errors").isJsonObject) {
                        json.getAsJsonObject("errors").entrySet().forEach { (campo, valor) ->
                            val mensaje = if (valor.isJsonPrimitive) {
                                valor.asString
                            } else {
                                valor.toString()
                            }
                            erroresCampos[campo] = mensaje
                        }
                    }

                    val mensajeGeneral = when {
                        json.has("message") && json.get("message").isJsonPrimitive ->
                            json.get("message").asString
                        json.has("error") && json.get("error").isJsonPrimitive ->
                            json.get("error").asString
                        else -> null
                    }

                    return ApiErrorResult(
                        fieldErrors = erroresCampos,
                        generalMessage = if (erroresCampos.isNotEmpty()) {
                            null
                        } else {
                            mensajeGeneral
                        }
                    )
                } catch (_: Exception) {
                    // El cuerpo no era JSON válido; se usa el mensaje genérico
                }
            }

            return ApiErrorResult(
                generalMessage = "Error del servidor (código $codigo)"
            )
        }

        if (e is IOException) {
            return ApiErrorResult(
                generalMessage = "No se pudo conectar con el servidor"
            )
        }

        return ApiErrorResult(
            generalMessage = "Ocurrió un error inesperado"
        )
    }
}
