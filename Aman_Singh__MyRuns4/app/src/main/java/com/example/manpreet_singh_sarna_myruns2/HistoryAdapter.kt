package com.example.manpreet_singh_sarna_myruns2

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.manpreet_singh_sarna_myruns2.room_database.ExerciseEntry

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private var modelList: List<ExerciseEntry> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = modelList[position]
        holder.tvInputType.text = "Input type: ${entry.inputType}"
        holder.tvActivityType.text = "Activity type: ${entry.activityType}"
        holder.tvDate.text = "Date: ${entry.date}"
        holder.tvTime.text = "Time: ${entry.time}"
        holder.tvDuration.text = "Duration: ${entry.duration} minutes"
        holder.tvDistance.text = "Distance: ${entry.distance} miles"
        holder.tvCalories.text = "Calories: ${entry.calories} cals"
        holder.tvHeartRate.text = "Avg Speed: ${entry.heartRate} bpm"
        // holder.tvComment.text = "Comment: ${entry.comment}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EntryDetailActivity::class.java)
            intent.putExtra("selected_entry", entry)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInputType: TextView = itemView.findViewById(R.id.tvInputType)
        val tvActivityType: TextView = itemView.findViewById(R.id.tvActivityType)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDistance)
        val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)
        val tvHeartRate: TextView = itemView.findViewById(R.id.tvHeartRate)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setEntries(entries: List<ExerciseEntry>) {
        this.modelList = entries
        notifyDataSetChanged()
    }
}