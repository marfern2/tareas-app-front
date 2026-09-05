package dam.moviles.tareas_app_front.data.remote

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Traduce errores técnicos (excepciones y códigos HTTP) a mensajes
 * naturales y reutilizables para toda la app.
 */
object ErrorMapper {

    private val mensajes = mapOf(
        AppErrorType.SIN_CONEXION to
            "No tienes conexión a Internet. Comprueba tu conexión e inténtalo de nuevo.",
        AppErrorType.TIMEOUT to
            "La conexión está tardando más de lo esperado. Inténtalo de nuevo.",
        AppErrorType.CREDENCIALES_INCORRECTAS to
            "El correo o la contraseña no son correctos.",
        AppErrorType.SESION_CADUCADA to
            "Tu sesión ha caducado. Inicia sesión de nuevo.",
        AppErrorType.DATOS_INCORRECTOS to
            "Revisa los datos introducidos.",
        AppErrorType.CONFLICTO to
            "Ya existe una cuenta con esos datos.",
        AppErrorType.PERMISO_DENEGADO to
            "No tienes permiso para realizar esta acción.",
        AppErrorType.NO_ENCONTRADO to
            "No se ha encontrado lo que buscabas.",
        AppErrorType.SERVICIO_NO_DISPONIBLE to
            "No podemos completar la operación ahora mismo. Inténtalo de nuevo en unos minutos.",
        AppErrorType.ERROR_INESPERADO to
            "Ha ocurrido un problema inesperado. Inténtalo de nuevo."
    )

    fun mensaje(tipo: AppErrorType): String = mensajes.getValue(tipo)

    /**
     * Determina el tipo de error visible a partir de una excepción.
     * Los errores de red se tratan de forma separada de los HTTP.
     */
    fun tipoPara(e: Exception, contexto: ErrorContext = ErrorContext.GENERAL): AppErrorType {
        if (e is HttpException) {
            return tipoPara(e.code(), contexto)
        }

        return when (e) {
            is UnknownHostException -> AppErrorType.SIN_CONEXION
            is ConnectException -> AppErrorType.SIN_CONEXION
            is SocketTimeoutException -> AppErrorType.TIMEOUT
            is IOException -> AppErrorType.SIN_CONEXION
            else -> AppErrorType.ERROR_INESPERADO
        }
    }

    /**
     * Determina el tipo de error visible a partir de un código HTTP.
     */
    fun tipoPara(codigo: Int, contexto: ErrorContext = ErrorContext.GENERAL): AppErrorType {
        return when (codigo) {
            400 -> AppErrorType.DATOS_INCORRECTOS
            401 -> if (contexto == ErrorContext.LOGIN) {
                AppErrorType.CREDENCIALES_INCORRECTAS
            } else {
                AppErrorType.SESION_CADUCADA
            }
            403 -> AppErrorType.PERMISO_DENEGADO
            404 -> if (contexto == ErrorContext.LOGIN) {
                AppErrorType.CREDENCIALES_INCORRECTAS
            } else {
                AppErrorType.NO_ENCONTRADO
            }
            408, 504 -> AppErrorType.TIMEOUT
            409 -> AppErrorType.CONFLICTO
            in 500..599 -> AppErrorType.SERVICIO_NO_DISPONIBLE
            else -> AppErrorType.ERROR_INESPERADO
        }
    }

    fun mensajePara(e: Exception, contexto: ErrorContext = ErrorContext.GENERAL): String =
        mensaje(tipoPara(e, contexto))

    fun mensajePara(codigo: Int, contexto: ErrorContext = ErrorContext.GENERAL): String =
        mensaje(tipoPara(codigo, contexto))
}