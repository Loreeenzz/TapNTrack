package com.nenquit.tapntrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.Track

/**
 * RecentActivityAdapter: Adapter for displaying recent attendance logs in dashboard.
 * Shows student name, time-in, and status badge.
 */
class RecentActivityAdapter(
    private var logs: List<Track>,
    private val onLogClick: (Track) -> Unit
) : RecyclerView.Adapter<RecentActivityAdapter.RecentActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return RecentActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentActivityViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size

    /**
     * Update adapter data
     */
    fun updateData(newLogs: List<Track>) {
        logs = newLogs
        notifyItemRangeChanged(0, logs.size)
    }

    inner class RecentActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvTimeIn: TextView = itemView.findViewById(R.id.tvTimeIn)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(log: Track) {
            // Student name
            tvStudentName.text = log.studentName.ifEmpty {
                itemView.context.getString(R.string.dashboard_unknown_student)
            }

            // Time in
            tvTimeIn.text = Track.formatTime(log.timeIn)

            // Status with badge color
            tvStatus.text = log.status
            when (log.status) {
                "PRESENT" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_present)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                "LATE" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_late)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                "ABSENT" -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_absent)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.status_badge_default)
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                }
            }

            // Click listener
            itemView.setOnClickListener {
                onLogClick(log)
            }
        }
    }
}
