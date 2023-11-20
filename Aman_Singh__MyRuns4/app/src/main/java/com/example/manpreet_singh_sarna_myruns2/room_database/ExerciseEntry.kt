package com.example.manpreet_singh_sarna_myruns2.room_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "exercise_entry_table")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "input_type")
    var inputType: String = "",

    @ColumnInfo(name = "activity_type")
    var activityType: String = "",

    @ColumnInfo(name = "date_column")
    var date: String = "",

    @ColumnInfo(name = "time_column")
    var time: String = "",

    @ColumnInfo(name = "duration_column")
    var duration: String = "",

    @ColumnInfo(name = "distance_column")
    var distance: String = "",

    @ColumnInfo(name = "calories_column")
    var calories: String = "",

    @ColumnInfo(name = "heart_rate_column")
    var heartRate: String = "",

    @ColumnInfo(name = "comment_column")
    var comment: String = ""
) : Serializable

