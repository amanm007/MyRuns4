package com.example.manpreet_singh_sarna_myruns2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.manpreet_singh_sarna_myruns2.room_database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

//This is for the Manual Entry Part
class ManualEntryActivity : AppCompatActivity() {

    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)
    private lateinit var durationEditText: EditText
    private lateinit var distanceEditText: EditText

    private lateinit var caloriesEditText: EditText
    private lateinit var heartRateEditText: EditText
    private lateinit var commentEditText: EditText

    private lateinit var inputType: String
    private lateinit var activityType: String

    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var historyViewModel: ExerciseEntryViewModel

    /*private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var exerciseEntryViewModel: ExerciseEntryViewModel*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manualentry_activity)

        inputType = intent.getStringExtra("input_type")!!
        activityType = intent.getStringExtra("activity_type")!!

        dateEditText = findViewById(R.id.dateEditText)
        timeEditText = findViewById(R.id.timeEditText)
        durationEditText = findViewById(R.id.durationEditText)
        distanceEditText = findViewById(R.id.distanceEditText)

        caloriesEditText = findViewById(R.id.caloriesEditText)
        heartRateEditText = findViewById(R.id.heartRateEditText)
        commentEditText = findViewById(R.id.commentEditText)

        val saveButton: Button = findViewById(R.id.saveButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)

        // Init Repository & ViewModel
        database = ExerciseEntryDatabase.getInstance(this@ManualEntryActivity)
        databaseDao = database.exerciseEntryDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this@ManualEntryActivity, viewModelFactory)[ExerciseEntryViewModel::class.java]


        /*repository = ExerciseEntryRepository(ExerciseEntryDatabase.getInstance(applicationContext).exerciseEntryDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        exerciseEntryViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseEntryViewModel::class.java]*/

        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }
        timeEditText.setOnClickListener {
            showTimePickerDialog()
        }
        durationEditText.setOnClickListener {
            showDurationInputDialog()
        }
        distanceEditText.setOnClickListener {
            showDistanceInputDialog()
        }
        caloriesEditText.setOnClickListener {
            showInputDialog("Enter Calories", caloriesEditText)
        }
        heartRateEditText.setOnClickListener {
            showInputDialog("Enter Heart Rate", heartRateEditText)
        }

        commentEditText.setOnClickListener {
            showInputDialog("Enter Comment", commentEditText)
        }

        saveButton.setOnClickListener {
            saveEntryToDatabase()
        }
        cancelButton.setOnClickListener {
            // Go back to the previous activity
            finish()
        }

    }

    private fun saveEntryToDatabase() {
        val date = dateEditText.text.toString()
        val time = timeEditText.text.toString()
        val duration = durationEditText.text.toString()
        val distance = distanceEditText.text.toString()
        val calories = caloriesEditText.text.toString()
        val heartRate = heartRateEditText.text.toString()
        val comment = commentEditText.text.toString()

        val newEntry = ExerciseEntry(
            inputType = inputType,
            activityType = activityType,
            date = date,
            time = time,
            duration = duration,
            distance = distance,
            calories = calories,
            heartRate = heartRate,
            comment = comment
        )

        // Insert the new entry into the database via the ViewModel and repository
        historyViewModel.insertEntry(newEntry)
        Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                dateEditText.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                timeEditText.setText(selectedTime)
            },
            currentHour,
            currentMinute,
            true
        )


        timePickerDialog.show()
    }

    private fun showDurationInputDialog() {
        val durationInput = EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle("Enter Duration")
            .setView(durationInput)
            .setPositiveButton("OK") { dialog, _ ->
                durationEditText.setText(durationInput.text.toString())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDistanceInputDialog() {
        val distanceInput = EditText(this)
        distanceInput.inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        distanceInput.hint = "Enter Distance"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Distance")
            .setView(distanceInput)
            .setPositiveButton("OK") { _, _ ->
                // Set the text of the distance EditText to the value entered in the dialog
                distanceEditText.setText(distanceInput.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()

        dialog.show()
    }


    private fun showInputDialog(title: String, editText: EditText) {
        val inputType = if (editText == commentEditText) {
            InputType.TYPE_CLASS_TEXT
        } else {
            InputType.TYPE_CLASS_NUMBER
        }

        val inputField = EditText(this).apply {
            this.inputType = inputType
            this.hint = title
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(inputField)
            .setPositiveButton("OK") { _, _ ->
                // Handle input validation here if necessary
                editText.setText(inputField.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

}
