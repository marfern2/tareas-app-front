package dam.moviles.tareas_app_front.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException

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

    /**
     * Extrae los errores técnicos y los traduce a un resultado de UI.
     *
     * Los errores de validación por campo que envía el backend se conservan
     * tal cual (p. ej. para crear/editar tareas). Cuando no hay errores por
     * campo, el mensaje general es siempre un mensaje natural e inteligible,
     * nunca un código HTTP ni el nombre de una excepción.
     */
    fun parsear(
        e: Exception,
        contexto: ErrorContext = ErrorContext.GENERAL
    ): ApiErrorResult {
        val erroresCampos = extraerErroresDeCampo(e)

        return ApiErrorResult(
            fieldErrors = erroresCampos,
            generalMessage = if (erroresCampos.isNotEmpty()) {
                null
            } else {
                ErrorMapper.mensajePara(e, contexto)
            }
        )
    }

    private fun extraerErroresDeCampo(e: Exception): Map<String, String> {
        if (e is HttpException) {
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

                    return erroresCampos
                } catch (_: Exception) {
                    // El cuerpo no era JSON válido; se usa el mensaje natural
                }
            }
        }

        return emptyMap()
    }
}
