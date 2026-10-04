package dam.moviles.tareas_app_front.data.session

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dam.moviles.tareas_app_front.data.remote.AuthRefresher
import dam.moviles.tareas_app_front.data.remote.TokenAuthenticator
import dam.moviles.tareas_app_front.data.remote.dto.RefreshResponseDto
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenManagerInstrumentedTest {
    // Prefijos aislados en la app DEV; no toca la sesión real del emulador.
    private val context: Context = object : ContextWrapper(
        InstrumentationRegistry.getInstrumentation().targetContext
    ) {
        override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences =
            super.getSharedPreferences("test_$name", mode)
    }
    private val legacy by lazy { context.getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE) }
    private val encrypted by lazy { context.getSharedPreferences("sesion_segura", Context.MODE_PRIVATE) }

    @Before fun cleanBefore() = clean()
    @After fun cleanAfter() = clean()

    private fun clean() {
        TokenManager(context).borrarTokens()
        legacy.edit().clear().commit()
        encrypted.edit().clear().commit()
    }

    @Test fun guardaYLeeAmbosTokensCifrados() {
        val store = TokenManager(context)
        store.guardarTokens("ACCESS_TEST", "REFRESH_TEST", true)

        assertEquals("ACCESS_TEST", TokenManager(context).obtenerAccessToken())
        assertEquals("REFRESH_TEST", TokenManager(context).obtenerRefreshToken())
        assertFalse(legacy.contains(TokenManager.CLAVE_ACCESS_TOKEN))
        assertFalse(legacy.contains(TokenManager.CLAVE_REFRESH_TOKEN))
        val raw = encrypted.getString("tokens_v1", null).orEmpty()
        assertTrue(raw.isNotEmpty())
        assertFalse(raw.contains("ACCESS_TEST"))
        assertFalse(raw.contains("REFRESH_TEST"))
    }

    @Test fun migraLegacyYRepetirLecturaNoReescribe() {
        legacy.edit()
            .putString(TokenManager.CLAVE_ACCESS_TOKEN, "ACCESS_VIEJO")
            .putString(TokenManager.CLAVE_REFRESH_TOKEN, "REFRESH_VIEJO")
            .putBoolean(TokenManager.CLAVE_PERSISTIR_SESION, true)
            .commit()

        val store = TokenManager(context)
        assertEquals("ACCESS_VIEJO", store.obtenerAccessToken())
        assertEquals("REFRESH_VIEJO", store.obtenerRefreshToken())
        assertFalse(legacy.contains(TokenManager.CLAVE_ACCESS_TOKEN))
        assertFalse(legacy.contains(TokenManager.CLAVE_REFRESH_TOKEN))
        val firstCiphertext = encrypted.getString("tokens_v1", null)
        assertEquals("ACCESS_VIEJO", TokenManager(context).obtenerAccessToken())
        assertEquals(firstCiphertext, encrypted.getString("tokens_v1", null))
        assertTrue(store.obtenerPersistirSesion())
    }

    @Test fun falloEscrituraSeguraConservaLegacy() {
        legacy.edit()
            .putString(TokenManager.CLAVE_ACCESS_TOKEN, "ACCESS_VIEJO")
            .putString(TokenManager.CLAVE_REFRESH_TOKEN, "REFRESH_VIEJO")
            .commit()
        val fallido = object : SecureTokenStorage {
            override fun read(): SessionTokens? = null
            override fun write(tokens: SessionTokens): Unit = error("fallo simulado")
            override fun clear() = Unit
        }

        try {
            TokenManager(context, fallido).obtenerAccessToken()
            throw AssertionError("La migración debió fallar")
        } catch (_: IllegalStateException) {
            assertEquals("ACCESS_VIEJO", legacy.getString(TokenManager.CLAVE_ACCESS_TOKEN, null))
            assertEquals("REFRESH_VIEJO", legacy.getString(TokenManager.CLAVE_REFRESH_TOKEN, null))
        }
    }

    @Test fun datoCifradoDanadoPermiteMigrarLegacyTrasVerificarEscritura() {
        encrypted.edit().putString("tokens_v1", "valor_invalido").commit()
        assertNull(TokenManager(context).obtenerAccessToken())
        legacy.edit()
            .putString(TokenManager.CLAVE_ACCESS_TOKEN, "ACCESS_VIEJO")
            .putString(TokenManager.CLAVE_REFRESH_TOKEN, "REFRESH_VIEJO")
            .commit()

        assertEquals("ACCESS_VIEJO", TokenManager(context).obtenerAccessToken())
        assertEquals("REFRESH_VIEJO", TokenManager(context).obtenerRefreshToken())
        assertFalse(legacy.contains(TokenManager.CLAVE_ACCESS_TOKEN))
        assertEquals("ACCESS_VIEJO", KeystoreTokenStorage(context).read()?.access)
    }

    @Test fun logoutBorraSecureLegacyYRememberMe() {
        val store = TokenManager(context)
        store.guardarTokens("ACCESS_TEST", "REFRESH_TEST", true)
        legacy.edit()
            .putString(TokenManager.CLAVE_ACCESS_TOKEN, "ACCESS_LEGACY")
            .putString(TokenManager.CLAVE_REFRESH_TOKEN, "REFRESH_LEGACY")
            .putString("jwt_token", "JWT_LEGACY")
            .commit()

        store.borrarTokens()

        assertNull(TokenManager(context).obtenerAccessToken())
        assertNull(TokenManager(context).obtenerRefreshToken())
        assertFalse(encrypted.contains("tokens_v1"))
        assertFalse(legacy.contains(TokenManager.CLAVE_ACCESS_TOKEN))
        assertFalse(legacy.contains(TokenManager.CLAVE_REFRESH_TOKEN))
        assertFalse(legacy.contains("jwt_token"))
        assertFalse(legacy.contains(TokenManager.CLAVE_PERSISTIR_SESION))
    }

    @Test fun rememberMeDesactivadoSoloMemoria() {
        val store = TokenManager(context)
        store.guardarTokens("ACCESS_TEMP", "REFRESH_TEMP", false)
        assertEquals("ACCESS_TEMP", TokenManager(context).obtenerAccessToken())
        assertEquals("REFRESH_TEMP", TokenManager(context).obtenerRefreshToken())
        assertFalse(store.obtenerPersistirSesion())
        assertFalse(encrypted.contains("tokens_v1"))
    }

    @Test fun refreshRotaAmbosTokensEnAlmacenSeguro() {
        val store = TokenManager(context)
        store.guardarTokens("ACCESS_TEMP", "REFRESH_TEMP", true)
        val request = Request.Builder()
            .url("https://example.test/tareas")
            .header("Authorization", "Bearer ACCESS_TEMP")
            .build()
        val unauthorized = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()
        val retried = TokenAuthenticator(
            store,
            AuthRefresher { refresh ->
                assertEquals("REFRESH_TEMP", refresh)
                RefreshResponseDto(token = "ACCESS_ROTADO", refreshToken = "REFRESH_ROTADO")
            }
        ).authenticate(null, unauthorized)

        assertEquals("Bearer ACCESS_ROTADO", retried?.header("Authorization"))
        assertEquals("ACCESS_ROTADO", KeystoreTokenStorage(context).read()?.access)
        assertEquals("REFRESH_ROTADO", KeystoreTokenStorage(context).read()?.refresh)
        assertTrue(store.obtenerPersistirSesion())
    }
}
