package com.example.data.repository

import com.example.data.local.SavedScriptEntity
import com.example.data.local.ScriptDao
import com.example.data.model.ScriptResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScriptRepository(private val dao: ScriptDao) {

    val allScripts: Flow<List<ScriptResult>> = dao.getAllScripts().map { entities ->
        entities.map { it.toScriptResult() }
    }

    suspend fun saveScript(script: ScriptResult): Long {
        val entity = SavedScriptEntity.fromScriptResult(script)
        return dao.insertScript(entity)
    }

    suspend fun deleteScript(id: String) {
        val longId = id.toLongOrNull() ?: return
        dao.deleteScriptById(longId)
    }
}
