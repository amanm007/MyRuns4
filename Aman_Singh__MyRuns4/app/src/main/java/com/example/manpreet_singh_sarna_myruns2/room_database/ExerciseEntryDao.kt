package com.example.manpreet_singh_sarna_myruns2.room_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDao {

    @Insert
    suspend fun insertEntry(entry: ExerciseEntry)

    @Query("SELECT * FROM exercise_entry_table")
    fun getAllEntries(): Flow<List<ExerciseEntry>>

    @Query("DELETE FROM exercise_entry_table WHERE id = :entryId")
    suspend fun deleteEntry(entryId: Long): Int

}
