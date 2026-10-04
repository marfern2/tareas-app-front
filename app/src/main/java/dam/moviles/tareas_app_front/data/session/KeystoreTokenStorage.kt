package dam.moviles.tareas_app_front.data.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONException
import org.json.JSONObject
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal data class SessionTokens(val access: String, val refresh: String)

internal interface SecureTokenStorage {
    fun read(): SessionTokens?
    fun write(tokens: SessionTokens)
    fun clear()
}

/** Un único valor cifrado mantiene atómica la rotación de los dos tokens. */
internal class KeystoreTokenStorage(context: Context) : SecureTokenStorage {
    private val prefs = context.getSharedPreferences("sesion_segura", Context.MODE_PRIVATE)

    companion object {
        private const val ALIAS = "donit_session_tokens_v1"
        private const val KEY = "tokens_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
        private val keyLock = Any()
    }

    override fun read(): SessionTokens? {
        val encoded = prefs.getString(KEY, null) ?: return null
        return try {
            val payload = Base64.decode(encoded, Base64.NO_WRAP)
            require(payload.size > IV_BYTES) { "Sesión cifrada inválida" }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, payload, 0, IV_BYTES))
            val json = JSONObject(
                String(cipher.doFinal(payload, IV_BYTES, payload.size - IV_BYTES), Charsets.UTF_8)
            )
            SessionTokens(json.getString("access"), json.getString("refresh"))
        } catch (_: GeneralSecurityException) {
            null // Clave perdida o dato alterado: la sesión no puede restaurarse.
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: JSONException) {
            null
        }
    }

    override fun write(tokens: SessionTokens) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val plaintext = JSONObject()
            .put("access", tokens.access)
            .put("refresh", tokens.refresh)
            .toString().toByteArray(Charsets.UTF_8)
        val encrypted = cipher.iv + cipher.doFinal(plaintext)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        check(prefs.edit().putString(KEY, encoded).commit()) { "No se pudo guardar la sesión cifrada" }
        check(read() == tokens) { "No se pudo verificar la sesión cifrada" }
    }

    override fun clear() {
        check(prefs.edit().remove(KEY).commit()) { "No se pudo borrar la sesión cifrada" }
    }

    private fun key(): SecretKey = synchronized(keyLock) {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey) ?: KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }
}
