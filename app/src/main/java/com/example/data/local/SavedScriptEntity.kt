package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DialogueLine
import com.example.data.model.ScriptResult
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "saved_scripts")
data class SavedScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val sceneHeader: String,
    val synopsis: String,
    val charactersJson: String,
    val linesJson: String,
    val videoSource: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toScriptResult(): ScriptResult {
        val charList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(charactersJson)
            for (i in 0 until jsonArray.length()) {
                charList.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {}

        val lineList = mutableListOf<DialogueLine>()
        try {
            val jsonArray = JSONArray(linesJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                lineList.add(
                    DialogueLine(
                        id = obj.optString("id"),
                        speaker = obj.optString("speaker"),
                        text = obj.optString("text"),
                        action = obj.optString("action"),
                        timestamp = obj.optString("timestamp"),
                        isRodrigo = obj.optBoolean("isRodrigo")
                    )
                )
            }
        } catch (_: Exception) {}

        return ScriptResult(
            id = id.toString(),
            title = title,
            sceneHeader = sceneHeader,
            synopsis = synopsis,
            characters = charList,
            lines = lineList,
            videoSource = videoSource,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromScriptResult(script: ScriptResult): SavedScriptEntity {
            val charJson = JSONArray(script.characters).toString()
            val linesArr = JSONArray()
            script.lines.forEach { line ->
                val obj = JSONObject()
                obj.put("id", line.id)
                obj.put("speaker", line.speaker)
                obj.put("text", line.text)
                obj.put("action", line.action)
                obj.put("timestamp", line.timestamp)
                obj.put("isRodrigo", line.isRodrigo)
                linesArr.put(obj)
            }
            return SavedScriptEntity(
                title = script.title,
                sceneHeader = script.sceneHeader,
                synopsis = script.synopsis,
                charactersJson = charJson,
                linesJson = linesArr.toString(),
                videoSource = script.videoSource,
                createdAt = script.createdAt
            )
        }
    }
}
