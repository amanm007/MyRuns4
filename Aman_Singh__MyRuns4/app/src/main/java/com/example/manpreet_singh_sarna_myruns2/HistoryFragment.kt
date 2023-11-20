package com.example.manpreet_singh_sarna_myruns2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.manpreet_singh_sarna_myruns2.room_database.*

class HistoryFragment : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var historyAdapter: HistoryAdapter


    private lateinit var database: ExerciseEntryDatabase
    private lateinit var databaseDao: ExerciseEntryDao
    private lateinit var repository: ExerciseEntryRepository
    private lateinit var viewModelFactory: ExerciseEntryViewModelFactory
    private lateinit var historyViewModel: ExerciseEntryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        initData(root)
        setObserver()

        return root
    }

    private fun initData(root: View) {
        recyclerView = root.findViewById(R.id.recyclerView)


        // Init Repository & ViewModel
        database = ExerciseEntryDatabase.getInstance(requireContext())
        databaseDao = database.exerciseEntryDao
        repository = ExerciseEntryRepository(databaseDao)
        viewModelFactory = ExerciseEntryViewModelFactory(repository)
        historyViewModel = ViewModelProvider(this, viewModelFactory)[ExerciseEntryViewModel::class.java]

        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        historyAdapter = HistoryAdapter()
        recyclerView.adapter = historyAdapter

    }

    private fun setObserver() {
        historyViewModel.allEntriesLiveData.observe(viewLifecycleOwner, Observer { entries ->
            historyAdapter.setEntries(entries)
        })
    }

}