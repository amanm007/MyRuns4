package com.example.manpreet_singh_sarna_myruns2


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment

class StartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        val spinner = view.findViewById<Spinner>(R.id.spinner2)
        val spinner1 = view.findViewById<Spinner>(R.id.spinner3)

        val activityTypes = arrayOf("Manual Entry", "GPS", "Automatic")
        val activityTypes1 = arrayOf("Running","Walking", "Standing","Cycling", "Hiking","DownHill Sking", "Cross-Country Sking"," Snowboarding","Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityTypes)
        val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityTypes1)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner1.adapter = adapter1
        val startMapButton = view.findViewById<Button>(R.id.button)

        startMapButton.setOnClickListener {
            val selectedItem = spinner.selectedItem.toString()
            if (selectedItem == "GPS" || selectedItem == "Automatic") {
                val intent = Intent(requireContext(), MapActivity::class.java)
                intent.putExtra("input_type", selectedItem)
                intent.putExtra("activity_type", spinner1.selectedItem.toString())
                startActivity(intent)
            } else if (selectedItem == "Manual Entry") {
                val intent = Intent(requireContext(), ManualEntryActivity::class.java)
                intent.putExtra("input_type", selectedItem)
                intent.putExtra("activity_type", spinner1.selectedItem.toString())
                startActivity(intent)
            }
        }




        return view
    }
}

