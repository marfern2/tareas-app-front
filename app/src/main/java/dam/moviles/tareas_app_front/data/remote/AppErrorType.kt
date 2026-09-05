package dam.moviles.tareas_app_front.data.remote

/**
 * Categorías de error entendibles por el usuario final.
 * Cada tipo se traduce siempre a un mensaje natural, sin códigos HTTP
 * ni nombres técnicos de excepciones.
 */
enum class AppErrorType {
    SIN_CONEXION,
    TIMEOUT,
    CREDENCIALES_INCORRECTAS,
    SESION_CADUCADA,
    DATOS_INCORRECTOS,
    CONFLICTO,
    PERMISO_DENEGADO,
    NO_ENCONTRADO,
    SERVICIO_NO_DISPONIBLE,
    ERROR_INESPERADO
}

/**
 * Contexto en el que ocurre un error para decidir cómo interpretarlo.
 * Por ejemplo, un 401 en LOGIN significa credenciales incorrectas,
 * mientras que fuera del login significa sesión caducada.
 */
enum class ErrorContext {
    LOGIN,
    GENERAL
}