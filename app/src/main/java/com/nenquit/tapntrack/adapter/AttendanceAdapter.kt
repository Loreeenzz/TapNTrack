package com.nenquit.tapntrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.Track
import java.text.SimpleDateFormat
import java.util.*

/**
 * AttendanceAdapter: RecyclerView adapter for displaying attendance records.
 */
class AttendanceAdapter(private val attendanceRecords: List<Track>) :
    RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {

    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTypeTextView: TextView = itemView.findViewById(R.id.eventTypeTextView)
        private val eventTimeTextView: TextView = itemView.findViewById(R.id.eventTimeTextView)
        private val eventLocationTextView: TextView = itemView.findViewById(R.id.eventLocationTextView)

        fun bind(track: Track) {
            eventTypeTextView.text = track.status
            eventTypeTextView.setTextColor(
                itemView.context.getColor(
                    if (track.status == "PRESENT") android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            val dateTime = dateTimeFormat.format(Date(track.timeIn))
            eventTimeTextView.text = dateTime

            eventLocationTextView.text = itemView.context.getString(
                R.string.attendance_location_format,
                track.location
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(attendanceRecords[position])
    }

    override fun getItemCount(): Int = attendanceRecords.size
}
