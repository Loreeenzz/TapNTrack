package com.nenquit.tapntrack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.Track

/**
 * Adapter for displaying attendance logs in RecyclerView
 * Handles click events and visual state for selected items
 */
class LogsAdapter(
    private val onLogClick: (Track) -> Unit,
    private val onLogLongClick: (Track) -> Boolean
) : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {

    private var logs: List<Track> = emptyList()
    private val selectedLogs = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.bind(log, isSelected(log.id))
    }

    override fun getItemCount(): Int = logs.size

    /**
     * Update the logs list with DiffUtil for efficient updates
     */
    fun updateLogs(newLogs: List<Track>) {
        val diffCallback = LogsDiffCallback(logs, newLogs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        logs = newLogs
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Check if a log is selected
     */
    fun isSelected(logId: String): Boolean {
        return selectedLogs.contains(logId)
    }

    /**
     * Toggle selection state of a log
     */
    fun toggleSelection(logId: String) {
        val position = logs.indexOfFirst { it.id == logId }
        if (position != -1) {
            if (selectedLogs.contains(logId)) {
                selectedLogs.remove(logId)
            } else {
                selectedLogs.add(logId)
            }
            notifyItemChanged(position)
        }
    }

    /**
     * Clear all selections
     */
    fun clearSelections() {
        val previouslySelected = selectedLogs.toList()
        selectedLogs.clear()
        previouslySelected.forEach { logId ->
            val position = logs.indexOfFirst { it.id == logId }
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }

    /**
     * Get list of selected log IDs
     */
    @Suppress("unused")
    fun getSelectedLogIds(): List<String> {
        return selectedLogs.toList()
    }

    /**
     * Get count of selected logs
     */
    @Suppress("unused")
    fun getSelectedCount(): Int {
        return selectedLogs.size
    }

    /**
     * Check if any logs are selected
     */
    fun hasSelections(): Boolean {
        return selectedLogs.isNotEmpty()
    }

    /**
     * ViewHolder for log items
     */
    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootLayout: LinearLayout = itemView as LinearLayout
        private val checkBox: CheckBox = itemView.findViewById(R.id.logSelectCheckBox)
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvRfidTag: TextView = itemView.findViewById(R.id.tvRfidTag)
        private val tvTimeIn: TextView = itemView.findViewById(R.id.tvTimeIn)
        private val tvTimeOut: TextView = itemView.findViewById(R.id.tvTimeOut)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(log: Track, isSelected: Boolean) {
            // Set student name (or "Unknown" if not available)
            tvStudentName.text = log.studentName.ifEmpty {
                itemView.context.getString(R.string.log_unknown_student)
            }

            // Set RFID tag
            tvRfidTag.text = itemView.context.getString(R.string.log_rfid_format, log.rfidTag)

            // Set time in
            tvTimeIn.text = Track.formatTime(log.timeIn)

            // Set time out (or "Still In" if null)
            if (log.timeOut != null && log.timeOut > 0) {
                tvTimeOut.text = Track.formatTime(log.timeOut)
                tvTimeOut.visibility = View.VISIBLE
            } else {
                tvTimeOut.text = itemView.context.getString(R.string.log_still_in)
                tvTimeOut.visibility = View.VISIBLE
            }

            // Set duration
            val duration = log.getFormattedDuration()
            if (duration.isNotEmpty()) {
                tvDuration.text = duration
                tvDuration.visibility = View.VISIBLE
            } else {
                tvDuration.visibility = View.GONE
            }

            // Set date
            tvDate.text = Track.formatDate(log.date)

            // Set status with color
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

            // Show/hide checkbox and update selection state
            if (hasSelections()) {
                checkBox.visibility = View.VISIBLE
                checkBox.isChecked = isSelected
            } else {
                checkBox.visibility = View.GONE
            }

            // Handle selection state background color
            if (isSelected) {
                rootLayout.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.selected_item_background)
                )
            } else {
                rootLayout.background = null
                rootLayout.isClickable = true
                rootLayout.isFocusable = true
            }

            // Click listeners
            rootLayout.setOnClickListener {
                if (hasSelections()) {
                    // In selection mode, toggle selection
                    toggleSelection(log.id)
                } else {
                    // Normal mode, trigger click event
                    onLogClick(log)
                }
            }

            rootLayout.setOnLongClickListener {
                // Start selection mode
                toggleSelection(log.id)
                onLogLongClick(log)
            }

            // Checkbox click
            checkBox.setOnClickListener {
                toggleSelection(log.id)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class LogsDiffCallback(
        private val oldList: List<Track>,
        private val newList: List<Track>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldLog = oldList[oldItemPosition]
            val newLog = newList[newItemPosition]

            return oldLog.rfidTag == newLog.rfidTag &&
                    oldLog.timeIn == newLog.timeIn &&
                    oldLog.timeOut == newLog.timeOut &&
                    oldLog.status == newLog.status &&
                    oldLog.studentName == newLog.studentName
        }
    }
}
