package dam.moviles.tareas_app_front.ui.auth.registro

import dam.moviles.tareas_app_front.data.remote.dto.RegistroRequestDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationValidationTest {
    @Test
    fun emptyUsernameShowsRequiredErrorAndDoesNotSubmit() {
        var submitCount = 0

        val error = submitIfValidUsername("", "test@example.com", "password1") {
            submitCount++
        }

        assertEquals("El nombre de usuario es obligatorio", error)
        assertEquals(0, submitCount)
    }

    @Test
    fun oneCharacterUsernameShowsMinimumErrorAndDoesNotSubmit() {
        var submitCount = 0

        val error = submitIfValidUsername("a", "test@example.com", "password1") {
            submitCount++
        }

        assertEquals("El nombre de usuario debe tener al menos 3 caracteres", error)
        assertEquals(0, submitCount)
    }

    @Test
    fun twoCharacterUsernameShowsMinimumErrorAndDoesNotSubmit() {
        var submitCount = 0

        val error = submitIfValidUsername("ab", "test@example.com", "password1") {
            submitCount++
        }

        assertEquals("El nombre de usuario debe tener al menos 3 caracteres", error)
        assertEquals(0, submitCount)
    }

    @Test
    fun threeCharacterUsernameAllowsRegistration() {
        var submittedRequest: RegistroRequestDto? = null

        val error = submitIfValidUsername("abc", "test@example.com", "password1") {
            submittedRequest = it
        }

        assertNull(error)
        assertEquals("abc", submittedRequest?.username)
    }

    @Test
    fun twentyCharacterUsernameAllowsRegistration() {
        var submittedRequest: RegistroRequestDto? = null
        val username = "a".repeat(20)

        val error = submitIfValidUsername(username, "test@example.com", "password1") {
            submittedRequest = it
        }

        assertNull(error)
        assertEquals(username, submittedRequest?.username)
    }

    @Test
    fun twentyOneCharacterUsernameShowsErrorAndDoesNotSubmit() {
        var submitCount = 0
        val username = "a".repeat(21)

        val error = submitIfValidUsername(username, "test@example.com", "password1") {
            submitCount++
        }

        assertEquals("El nombre de usuario no puede superar los 20 caracteres", error)
        assertEquals(0, submitCount)
    }

    @Test
    fun validUsernamePreservesExistingRequestValues() {
        var submittedRequest: RegistroRequestDto? = null

        val error = submitIfValidUsername(" maria ", " test@example.com ", "password1") {
            submittedRequest = it
        }

        assertNull(error)
        assertEquals("maria", submittedRequest?.username)
        assertEquals("test@example.com", submittedRequest?.email)
        assertEquals("password1", submittedRequest?.password)
    }

    @Test
    fun paddingDoesNotMakeTwoCharacterUsernameValid() {
        var submitCount = 0

        val error = submitIfValidUsername(" ab ", "test@example.com", "password1") {
            submitCount++
        }

        assertEquals("El nombre de usuario debe tener al menos 3 caracteres", error)
        assertEquals(0, submitCount)
    }
}
