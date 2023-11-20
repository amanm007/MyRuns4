package com.example.manpreet_singh_sarna_myruns2.room_database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class ExerciseEntryRepository(private val entryDao: ExerciseEntryDao) {

    val allEntries: Flow<List<ExerciseEntry>> = entryDao.getAllEntries()

    fun insertEntry(entry: ExerciseEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            entryDao.insertEntry(entry)
        }
    }

    fun deleteEntry(id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            entryDao.deleteEntry(id)
        }
    }
}