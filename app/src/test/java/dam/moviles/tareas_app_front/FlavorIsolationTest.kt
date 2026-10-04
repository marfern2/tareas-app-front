package dam.moviles.tareas_app_front

import org.junit.Assert.assertEquals
import org.junit.Test

class FlavorIsolationTest {
    @Test fun applicationIdSeparaDevYProd() {
        val expected = if (BuildConfig.FLAVOR == "dev") {
            "dam.moviles.tareas_app_front.dev"
        } else {
            "dam.moviles.tareas_app_front"
        }
        assertEquals(expected, BuildConfig.APPLICATION_ID)
    }
}
