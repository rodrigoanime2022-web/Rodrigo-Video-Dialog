package com.example.data.model

import java.util.UUID

data class CharacterConfig(
    val name: String = "Rodrigo",
    val keyword: String = "Rodrigo",
    val entrancePhrase: String = "Rodrigo acabou de entrar na cena.",
    val tone: String = "Carismático e bem-humorado",
    val entranceMoment: String = "No momento oportuno da discussão"
)

data class DialogueLine(
    val id: String = UUID.randomUUID().toString(),
    val speaker: String,
    val text: String,
    val action: String = "",
    val timestamp: String = "",
    val isRodrigo: Boolean = false
)

data class ScriptResult(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sceneHeader: String = "INT. AMBIENTE DO VÍDEO - CENA",
    val synopsis: String = "",
    val characters: List<String> = emptyList(),
    val lines: List<DialogueLine> = emptyList(),
    val videoSource: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toFormattedScreenplay(): String {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("RODRIGO VIDEO DIALOG - ROTEIRO GERADO")
        sb.appendLine("==========================================")
        sb.appendLine()
        sb.appendLine("TÍTULO: $title")
        sb.appendLine("FONTE DO VÍDEO: $videoSource")
        sb.appendLine("AMBIENTAÇÃO: $sceneHeader")
        sb.appendLine("PERSONAGENS IDENTIFICADOS: ${characters.joinToString(", ")}")
        sb.appendLine()
        if (synopsis.isNotBlank()) {
            sb.appendLine("SINOPSES / CONTEXTO DA CENA:")
            sb.appendLine(synopsis)
            sb.appendLine()
        }
        sb.appendLine("----------------- DIÁLOGO -----------------")
        sb.appendLine()
        lines.forEach { line ->
            val timeTag = if (line.timestamp.isNotBlank()) "[${line.timestamp}] " else ""
            val star = if (line.isRodrigo) "⭐ " else ""
            sb.appendLine("$timeTag$star${line.speaker.uppercase()}:")
            if (line.action.isNotBlank()) {
                sb.appendLine("   (${line.action})")
            }
            sb.appendLine("   \"${line.text}\"")
            sb.appendLine()
        }
        sb.appendLine("==========================================")
        sb.appendLine("Gerado por Rodrigo Video Dialog - IA Script Engine")
        return sb.toString()
    }
}

sealed class VideoInputType {
    data class Link(val url: String) : VideoInputType()
    data class Upload(
        val uriString: String,
        val fileName: String,
        val durationMs: Long,
        val sizeBytes: Long,
        val thumbnailBase64: String? = null
    ) : VideoInputType()
    data class Sample(
        val id: String,
        val title: String,
        val category: String,
        val description: String,
        val duration: String
    ) : VideoInputType()
}

data class SampleScenario(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val duration: String,
    val defaultLink: String
)
