package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Query("SELECT * FROM saved_scripts ORDER BY createdAt DESC")
    fun getAllScripts(): Flow<List<SavedScriptEntity>>

    @Query("SELECT * FROM saved_scripts WHERE id = :id LIMIT 1")
    suspend fun getScriptById(id: Long): SavedScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScriptEntity): Long

    @Query("DELETE FROM saved_scripts WHERE id = :id")
    suspend fun deleteScriptById(id: Long)

    @Query("DELETE FROM saved_scripts")
    suspend fun clearAll()
}
