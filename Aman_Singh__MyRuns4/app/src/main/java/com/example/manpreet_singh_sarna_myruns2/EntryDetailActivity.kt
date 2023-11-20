package com.example.manpreet_singh_sarna_myruns2

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.manpreet_singh_sarna_myruns2.room_database.ExerciseEntry
import com.example.manpreet_singh_sarna_myruns2.room_database.ExerciseEntryDatabase
import com.example.manpreet_singh_sarna_myruns2.room_database.ExerciseEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class EntryDetailActivity : AppCompatActivity() {

    private lateinit var edInputType: EditText
    private lateinit var edActivityType: EditText
    private lateinit var edDateAndTime: EditText
    private lateinit var edDuration: EditText
    private lateinit var edDistance: EditText
    private lateinit var edCalories: EditText
    private lateinit var edHeartRate: EditText
    private lateinit var btnDelete: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_detail)

        edInputType = findViewById(R.id.tvInputType)
        edActivityType = findViewById(R.id.tvActivityType)
        edDateAndTime = findViewById(R.id.tvDateAndTime)
        edDuration = findViewById(R.id.tvDuration)
        edDistance = findViewById(R.id.tvDistance)
        edCalories = findViewById(R.id.tvCalories)
        edHeartRate = findViewById(R.id.tvHeartRate)
        btnDelete = findViewById(R.id.btnDelete)

        val entry = (intent.getSerializableExtra("selected_entry") as? ExerciseEntry)

        if (entry != null) {
            edInputType.setText("Input Type: " + entry.inputType)
            edActivityType.setText("Activity Type: " + entry.activityType)
            edDateAndTime.setText("Date and time: " + entry.date + entry.time)
            edDuration.setText("Duration: " + entry.duration)
            edDistance.setText("Distance: " + entry.distance)
            edCalories.setText("Calories: " + entry.calories)
            edHeartRate.setText("Heart Rate: " + entry.heartRate)
        }

        btnDelete.setOnClickListener {
            val database = ExerciseEntryDatabase.getInstance(applicationContext)
            val entryDao = database.exerciseEntryDao
            val repository = ExerciseEntryRepository(entryDao)

            // Delete the entry
            CoroutineScope(IO).launch {
                repository.deleteEntry(entry!!.id)
            }

            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1000)
        }
    }
}