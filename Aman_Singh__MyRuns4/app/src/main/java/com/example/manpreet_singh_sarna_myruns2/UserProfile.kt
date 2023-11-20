package com.example.manpreet_singh_sarna_myruns2

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

class UserProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userprofile)


        //Name component
        val nameTextView = findViewById<TextView>(R.id.tvNameLabel)
        val enterNameEditText = findViewById<EditText>(R.id.tvEnterName)
        //Profile Photo
        val profilePhotoTextView = findViewById<TextView>(R.id.tvProfilePhoto)

        //Email
        val emailLabelTextView = findViewById<TextView>(R.id.tvEmailLabel)
        val enterEmailEditText = findViewById<EditText>(R.id.tvEnterEmail)
        //Phone
        val phoneLabelTextView = findViewById<TextView>(R.id.tvPhoneLabel)
        val enterPhoneEditText = findViewById<EditText>(R.id.tvEnterPhoneLabel)
        //Gender Selection
        val genderLabelTextView = findViewById<TextView>(R.id.tvGenderLabel)
        val maleRadioButton = findViewById<RadioButton>(R.id.tvMaleRadio)
        val femaleRadioButton = findViewById<RadioButton>(R.id.tvFemaleRadio)
        //Classes
        val classLabelTextView = findViewById<TextView>(R.id.tvClassLabel)
        val enterClassEditText = findViewById<EditText>(R.id.editTextText8)
        //Major
        val majorLabelTextView = findViewById<TextView>(R.id.tvMajorLabel)
        val enterMajorEditText = findViewById<EditText>(R.id.tvEnterMajorLabel)


        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
//Radio Button to select just one Male or Female
        if (selectedRadioButtonId != -1) {
            val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
            val selectedGender = selectedRadioButton.text.toString()

        } else {
        }

//This is for saving the details
        val saveButton = findViewById<Button>(R.id.tvSaveButton)
        val cancelButton = findViewById<Button>(R.id.tvCancelButton)

//Using Shared Preferences
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val enteredName = sharedPreferences.getString("name", "")
        val enteredEmail = sharedPreferences.getString("email", "")
        val enteredPhone = sharedPreferences.getString("phone", "")
        val enteredClass = sharedPreferences.getString("class", "")
        val enteredMajor = sharedPreferences.getString("major", "")
        val selectedGender = sharedPreferences.getString("gender", "")

        // Set the loaded data to the EditText fields and RadioGroup
        enterNameEditText.setText(enteredName)
        enterEmailEditText.setText(enteredEmail)
        enterPhoneEditText.setText(enteredPhone)
        enterClassEditText.setText(enteredClass)
        enterMajorEditText.setText(enteredMajor)
//Calling the saveData Function
        saveButton.setOnClickListener {
            saveData()
            finish()
        }

//Calling the Clear Function
        cancelButton.setOnClickListener {
            clearFields()
            finish();
        }



    }
    private fun saveData() {
        val enteredName = findViewById<EditText>(R.id.tvEnterName).text.toString()
        val enteredEmail = findViewById<EditText>(R.id.tvEnterEmail).text.toString()
        val enteredPhone = findViewById<EditText>(R.id.tvEnterPhoneLabel).text.toString()
        val enteredClass = findViewById<EditText>(R.id.editTextText8).text.toString()
        val enteredMajor = findViewById<EditText>(R.id.tvEnterMajorLabel).text.toString()

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        val selectedGender = when (radioGroup.checkedRadioButtonId) {
            R.id.tvMaleRadio -> "Male"
            R.id.tvFemaleRadio -> "Female"
            else -> "Not specified"
        }
        //This is saving for the rest of the strings by converting anything to string.
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("name", enteredName)
        editor.putString("email", enteredEmail)
        editor.putString("phone", enteredPhone)
        editor.putString("class", enteredClass)
        editor.putString("major", enteredMajor)
        editor.putString("gender", selectedGender)
        editor.apply()

        val message = "Saved!!"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    private fun clearFields() {
        findViewById<EditText>(R.id.tvEnterName).text.clear()
        findViewById<EditText>(R.id.tvEnterEmail).text.clear()
        findViewById<EditText>(R.id.tvEnterPhoneLabel).text.clear()
        findViewById<EditText>(R.id.editTextText8).text.clear()
        findViewById<EditText>(R.id.tvEnterMajorLabel).text.clear()

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.clearCheck()
    }

}