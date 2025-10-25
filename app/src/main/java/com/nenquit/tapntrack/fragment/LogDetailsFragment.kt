package com.nenquit.tapntrack.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.utils.FirebaseHelper

/**
 * LogDetailsFragment: Displays detailed information about a specific attendance log.
 * Allows deleting the log record.
 */
class LogDetailsFragment : Fragment() {

    private lateinit var tvStudentName: TextView
    private lateinit var tvRfidTag: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvTimeIn: TextView
    private lateinit var tvTimeOut: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvLocation: TextView
    private lateinit var btnDelete: Button
    private lateinit var btnBack: Button

    private val firebaseHelper = FirebaseHelper()
    private var logId: String = ""
    private var currentLog: Track? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get log ID from arguments
        logId = arguments?.getString(ARG_LOG_ID) ?: ""
        if (logId.isEmpty()) {
            showError(getString(R.string.log_details_log_id_not_found))
            navigateBack()
            return
        }

        initializeViews(view)
        setupClickListeners()
        loadLogDetails()
    }

    private fun initializeViews(view: View) {
        tvStudentName = view.findViewById(R.id.tvStudentName)
        tvRfidTag = view.findViewById(R.id.tvRfidTag)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvDate = view.findViewById(R.id.tvDate)
        tvTimeIn = view.findViewById(R.id.tvTimeIn)
        tvTimeOut = view.findViewById(R.id.tvTimeOut)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvLocation = view.findViewById(R.id.tvLocation)
        btnDelete = view.findViewById(R.id.btnDelete)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            navigateBack()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadLogDetails() {
        firebaseHelper.getTrackReference(logId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val trackData = snapshot.value as? Map<String, Any>
                        if (trackData != null) {
                            currentLog = Track.fromMap(trackData)
                            displayLogDetails(currentLog!!)
                        } else {
                            showError(getString(R.string.log_details_invalid_data))
                            navigateBack()
                        }
                    } else {
                        showError(getString(R.string.log_details_not_found))
                        navigateBack()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showError(getString(R.string.log_details_load_failed, error.message))
                    navigateBack()
                }
            })
    }

    private fun displayLogDetails(log: Track) {
        // Student name
        tvStudentName.text = log.studentName.ifEmpty { getString(R.string.log_details_unknown_student) }

        // RFID tag
        tvRfidTag.text = log.rfidTag

        // Status with color
        tvStatus.text = log.status
        when (log.status) {
            "PRESENT" -> {
                tvStatus.setBackgroundResource(R.drawable.status_badge_present)
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            "LATE" -> {
                tvStatus.setBackgroundResource(R.drawable.status_badge_late)
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            "ABSENT" -> {
                tvStatus.setBackgroundResource(R.drawable.status_badge_absent)
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            else -> {
                tvStatus.setBackgroundResource(R.drawable.status_badge_default)
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
        }

        // Date
        tvDate.text = Track.formatDate(log.date)

        // Time in
        tvTimeIn.text = Track.formatTime(log.timeIn)

        // Time out
        if (log.timeOut != null && log.timeOut > 0) {
            tvTimeOut.text = Track.formatTime(log.timeOut)
        } else {
            tvTimeOut.text = getString(R.string.log_details_still_in)
            tvTimeOut.setTextColor("#FF9800".toColorInt())
        }

        // Duration
        val duration = log.getFormattedDuration()
        tvDuration.text = duration.ifEmpty { getString(R.string.log_details_na) }

        // Location
        tvLocation.text = log.location.ifEmpty { getString(R.string.log_details_na) }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.log_details_delete_title))
            .setMessage(getString(R.string.log_details_delete_message))
            .setPositiveButton(getString(R.string.log_details_delete_confirm)) { _, _ ->
                deleteLog()
            }
            .setNegativeButton(getString(R.string.log_details_delete_cancel), null)
            .show()
    }

    private fun deleteLog() {
        firebaseHelper.getTrackReference(logId).removeValue()
            .addOnSuccessListener {
                showSuccess(getString(R.string.log_details_delete_success))
                navigateBack()
            }
            .addOnFailureListener { error ->
                showError(getString(R.string.log_details_delete_failed, error.message))
            }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateBack() {
        parentFragmentManager.popBackStack()
    }

    companion object {
        private const val ARG_LOG_ID = "log_id"

        fun newInstance(logId: String): LogDetailsFragment {
            return LogDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOG_ID, logId)
                }
            }
        }
    }
}
