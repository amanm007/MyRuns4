package com.example.manpreet_singh_sarna_myruns2.room_database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

class ExerciseEntryViewModel(private val repository: ExerciseEntryRepository) : ViewModel() {
    val allEntriesLiveData: LiveData<List<ExerciseEntry>> = repository.allEntries.asLiveData()

    fun insertEntry(entry: ExerciseEntry) {
        repository.insertEntry(entry)
    }

    fun deleteFirstEntry() {
        val entryList = allEntriesLiveData.value
        if (entryList != null && entryList.isNotEmpty()) {
            val id = entryList[0].id
            repository.deleteEntry(id)
        }
    }
}

class ExerciseEntryViewModelFactory(private val repository: ExerciseEntryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}