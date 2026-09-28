package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CharacterConfig
import com.example.data.model.DialogueLine
import com.example.data.model.ScriptResult
import com.example.util.VideoUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Rodrigo Video Dialog", appName)
    }

    @Test
    fun `validate video urls`() {
        assertTrue(VideoUtils.isValidUrl("https://www.youtube.com/watch?v=12345"))
        assertTrue(VideoUtils.isValidUrl("http://example.com/video.mp4"))
        assertFalse(VideoUtils.isValidUrl("invalid-url"))
        assertFalse(VideoUtils.isValidUrl(""))
    }

    @Test
    fun `validate screenplay formatting with Rodrigo`() {
        val config = CharacterConfig()
        val script = ScriptResult(
            title = "Teste de Cena",
            sceneHeader = "INT. SALA - DIA",
            characters = listOf("Marcos", "Rodrigo"),
            lines = listOf(
                DialogueLine(speaker = "Marcos", text = "Quem é você?"),
                DialogueLine(
                    speaker = "Rodrigo",
                    text = "Eu sou o Rodrigo. Acabei de chegar.",
                    action = "entrando na sala",
                    isRodrigo = true
                )
            )
        )

        val formatted = script.toFormattedScreenplay()
        assertTrue(formatted.contains("RODRIGO VIDEO DIALOG"))
        assertTrue(formatted.contains("Quem é você?"))
        assertTrue(formatted.contains("Eu sou o Rodrigo. Acabei de chegar."))
        assertTrue(formatted.contains("⭐ RODRIGO:"))
    }
}
