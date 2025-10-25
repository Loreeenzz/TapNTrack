package com.nenquit.tapntrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.adapter.AttendanceAdapter
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.mvp.users.UserDetailsContract
import com.nenquit.tapntrack.mvp.users.UserDetailsPresenter
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserDetailsFragment: Displays detailed information about a specific user.
 * Shows profile info, activity statistics, and attendance records.
 * Implements MVP pattern with UserDetailsPresenter as the business logic handler.
 */
class UserDetailsFragment : Fragment(), UserDetailsContract.View {
    private lateinit var presenter: UserDetailsContract.Presenter
    private var currentToast: Toast? = null
    private var currentUserId: String = ""
    private var currentUser: User? = null

    // UI components
    private lateinit var backButton: Button
    private lateinit var detailUserName: TextView
    private lateinit var detailUserEmail: TextView
    private lateinit var detailUserRole: TextView
    private lateinit var detailUserStatus: TextView
    private lateinit var detailUserCreatedDate: TextView
    private lateinit var totalAttendanceTextView: TextView
    private lateinit var attendanceRateTextView: TextView
    private lateinit var lastSeenTextView: TextView
    private lateinit var attendanceRecyclerView: RecyclerView
    private lateinit var noAttendanceTextView: TextView
    private lateinit var statusToggleButton: Button
    private lateinit var deleteButton: Button

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user ID from arguments
        currentUserId = arguments?.getString(KEY_USER_ID) ?: ""
        if (currentUserId.isEmpty()) {
            showError("User ID not found")
            return
        }

        // Initialize presenter
        presenter = UserDetailsPresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents(view)

        // Set up click listeners
        setupClickListeners()

        // Load user details
        presenter.loadUserDetails(currentUserId)
        presenter.loadAttendanceRecords(currentUserId)
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents(view: View) {
        backButton = view.findViewById(R.id.backButton)
        detailUserName = view.findViewById(R.id.detailUserName)
        detailUserEmail = view.findViewById(R.id.detailUserEmail)
        detailUserRole = view.findViewById(R.id.detailUserRole)
        detailUserStatus = view.findViewById(R.id.detailUserStatus)
        detailUserCreatedDate = view.findViewById(R.id.detailUserCreatedDate)
        totalAttendanceTextView = view.findViewById(R.id.totalAttendanceTextView)
        attendanceRateTextView = view.findViewById(R.id.attendanceRateTextView)
        lastSeenTextView = view.findViewById(R.id.lastSeenTextView)
        attendanceRecyclerView = view.findViewById(R.id.attendanceRecyclerView)
        noAttendanceTextView = view.findViewById(R.id.noAttendanceTextView)
        statusToggleButton = view.findViewById(R.id.statusToggleButton)
        deleteButton = view.findViewById(R.id.deleteButton)

        // Set up attendance RecyclerView
        attendanceRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AttendanceAdapter(emptyList())
        }
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            presenter.onBackPressed()
        }

        statusToggleButton.setOnClickListener {
            currentUser?.let { user ->
                if (user.isActive) {
                    displayDeactivateConfirmation()
                } else {
                    presenter.activateUser(currentUserId)
                }
            }
        }

        deleteButton.setOnClickListener {
            displayDeleteConfirmation()
        }
    }

    /**
     * Show deactivate confirmation dialog
     */
    private fun displayDeactivateConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Deactivate this user?")
        builder.setPositiveButton("Yes") { _, _ -> presenter.deactivateUser(currentUserId) }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    /**
     * Show delete confirmation dialog
     */
    private fun displayDeleteConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Delete this user? This cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ -> presenter.deleteUser(currentUserId) }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun showLoading() {
        setActionButtonsEnabled(false)
    }

    override fun hideLoading() {
        setActionButtonsEnabled(true)
    }

    override fun showError(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun showSuccess(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun displayUserProfile(user: User) {
        currentUser = user
        detailUserName.text = getString(R.string.user_details_name_format, user.name)
        detailUserEmail.text = getString(R.string.user_details_email_format, user.email)
        detailUserRole.text = getString(R.string.user_details_role_format, user.role)

        val statusText = if (user.isActive) {
            getString(R.string.user_status_active)
        } else {
            getString(R.string.user_status_inactive)
        }
        val statusColor = if (user.isActive) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        detailUserStatus.apply {
            text = getString(R.string.user_details_status_format, statusText)
            setTextColor(requireContext().getColor(statusColor))
        }

        val createdDate = dateFormat.format(Date(user.createdAt))
        detailUserCreatedDate.text = getString(R.string.user_details_created_format, createdDate)

        // Update status toggle button text
        statusToggleButton.text = if (user.isActive) {
            getString(R.string.user_details_deactivate_button)
        } else {
            getString(R.string.user_details_activate_button)
        }

        // Statistics will be calculated after attendance records are loaded
    }

    override fun displayAttendanceRecords(tracks: List<Track>) {
        if (tracks.isEmpty()) {
            attendanceRecyclerView.visibility = View.GONE
            noAttendanceTextView.visibility = View.VISIBLE
        } else {
            attendanceRecyclerView.visibility = View.VISIBLE
            noAttendanceTextView.visibility = View.GONE
            attendanceRecyclerView.adapter = AttendanceAdapter(tracks)
        }
    }

    override fun displayUserStatistics(
        totalAttendance: Int,
        attendanceRate: Double,
        lastSeen: Long
    ) {
        totalAttendanceTextView.text = totalAttendance.toString()
        attendanceRateTextView.text = getString(
            R.string.user_details_attendance_rate_format,
            attendanceRate
        )

        if (lastSeen > 0) {
            val lastSeenDate = dateTimeFormat.format(Date(lastSeen))
            lastSeenTextView.text = getString(R.string.user_details_last_seen_format, lastSeenDate)
        } else {
            lastSeenTextView.text = getString(R.string.user_details_last_seen_never)
        }
    }

    override fun navigateBack() {
        parentFragmentManager.popBackStack()
    }

    override fun showDeactivateConfirmation() {
        displayDeactivateConfirmation()
    }

    override fun showDeleteConfirmation() {
        displayDeleteConfirmation()
    }

    override fun setActionButtonsEnabled(enabled: Boolean) {
        statusToggleButton.isEnabled = enabled
        deleteButton.isEnabled = enabled
    }

    /**
     * Cancel current toast if exists
     */
    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
        cancelCurrentToast()
    }

    companion object {
        private const val KEY_USER_ID = "user_id"

        fun newInstance(userId: String): UserDetailsFragment {
            return UserDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_USER_ID, userId)
                }
            }
        }
    }
}
