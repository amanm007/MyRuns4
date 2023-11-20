package com.example.manpreet_singh_sarna_myruns2



import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("SettingsData", Context.MODE_PRIVATE)

        val textView4 = view.findViewById<TextView>(R.id.textView4)

val textView7=view.findViewById<TextView>(R.id.textView7)


        textView4.setOnClickListener {
            val intent = Intent(activity, UserProfile::class.java)

            startActivity(intent)
        }
        val textView10 = view.findViewById<TextView>(R.id.textView10)

        textView10.setOnClickListener {
            val webLink = "https://www.sfu.ca/computing.html"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webLink))
            startActivity(intent)
        }
        val unitPreference= view.findViewById<TextView>(R.id.textView7)
        val comment = view.findViewById<TextView>(R.id.textView8)
        unitPreference.setOnClickListener {
            showUnitPreferenceDialog()
        }

        comment.setOnClickListener {
            showCommentsDialog()
        }
    }
    private fun showUnitPreferenceDialog() {
        val unitOptions = arrayOf("KM/s", "M/s")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Unit Preference")

        val checkedItem = sharedPreferences.getInt("UnitPreference", 0) // Load saved choice
        builder.setSingleChoiceItems(unitOptions, checkedItem) { dialog, which ->
            sharedPreferences.edit().putInt("UnitPreference", which).apply() // Save choice
            Toast.makeText(requireContext(), "Selected: ${unitOptions[which]}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun showCommentsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Comments")

        val input = EditText(requireContext())
        input.setText(sharedPreferences.getString("Comments", ""))
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val comment = input.text.toString()
            sharedPreferences.edit().putString("Comments", comment).apply()
            Toast.makeText(requireContext(), "Comment: $comment", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
//            dialog.cancel()
        input.text.clear()
        dialog.dismiss()}

        val dialog = builder.create()
        dialog.show()
    }




}


