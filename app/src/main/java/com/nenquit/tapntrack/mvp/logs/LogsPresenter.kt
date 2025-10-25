package com.nenquit.tapntrack.mvp.logs

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.utils.FirebaseHelper
import com.nenquit.tapntrack.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * LogsPresenter: Handles logs/attendance business logic.
 * Manages log loading, filtering, searching, and sorting from Firebase.
 *
 * Features:
 * - Real-time Firebase data synchronization
 * - Advanced filtering (date, status, teacher, student)
 * - Search functionality
 * - Role-based access control
 */
@Suppress("unused")
class LogsPresenter(private val context: Context? = null) : LogsContract.Presenter {
    private var view: LogsContract.View? = null
    private val firebaseHelper: FirebaseHelper = FirebaseHelper()
    private var sessionManager: SessionManager? = null
    private var allLogs: List<Track> = emptyList()
    private var currentFilteredLogs: List<Track> = emptyList()

    // Filter and search state
    private var searchQuery: String = ""
    private var statusFilter: String? = null // PRESENT, LATE, ABSENT, or null for all
    private var teacherFilter: String? = null
    private var dateFilter: String? = null
    private var startDateFilter: String? = null
    private var endDateFilter: String? = null
    private var currentSortBy: String = "timeIn" // timeIn, studentName, status
    private var currentUserRole: String = "ADMIN"
    private var currentUserId: String = ""

    override fun attach(view: LogsContract.View) {
        this.view = view
        // Initialize SessionManager from view context if available
        if (context != null) {
            sessionManager = SessionManager(context)
            currentUserRole = sessionManager?.getUserRole() ?: "ADMIN"
            currentUserId = sessionManager?.getUserUID() ?: ""
        }
    }

    override fun detach() {
        this.view = null
    }

    override fun loadLogs(dateFilter: String?) {
        view?.showLoading()
        this.dateFilter = dateFilter

        firebaseHelper.getTracksReference()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.hideLoading()
                    if (snapshot.exists()) {
                        try {
                            val logs = mutableListOf<Track>()
                            for (logSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val logData = logSnapshot.value as? Map<String, Any> ?: continue
                                logs.add(Track.fromMap(logData))
                            }
                            allLogs = logs
                            applyCurrentFilters()

                            if (currentFilteredLogs.isEmpty()) {
                                view?.showEmptyState()
                            } else {
                                view?.hideEmptyState()
                                view?.displayLogs(currentFilteredLogs)
                            }

                            // Update summary statistics
                            updateSummary()
                        } catch (e: Exception) {
                            view?.showError("Failed to load logs: ${e.message}")
                        }
                    } else {
                        view?.showEmptyState()
                        allLogs = emptyList()
                        currentFilteredLogs = emptyList()
                        view?.displayLogs(emptyList())
                        view?.updateSummary(0, 0, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.hideLoading()
                    view?.showError("Failed to load logs: ${error.message}")
                }
            })
    }

    override fun searchLogs(query: String) {
        searchQuery = query.trim().lowercase()
        applyCurrentFilters()
    }

    override fun filterByStatus(status: String?) {
        statusFilter = status?.ifEmpty { null }?.takeIf { it != "ALL" }
        applyCurrentFilters()
    }

    override fun filterByTeacher(teacherId: String) {
        teacherFilter = teacherId.ifEmpty { null }
        applyCurrentFilters()
    }

    override fun filterByDateRange(startDate: String, endDate: String) {
        startDateFilter = startDate
        endDateFilter = endDate
        applyCurrentFilters()
    }

    override fun sortLogs(sortBy: String) {
        currentSortBy = sortBy
        applyCurrentFilters()
    }

    override fun resetFilters() {
        searchQuery = ""
        statusFilter = null
        teacherFilter = null
        dateFilter = null
        startDateFilter = null
        endDateFilter = null
        currentSortBy = "timeIn"
        applyCurrentFilters()
    }

    override fun onLogSelected(log: Track) {
        view?.navigateToLogDetails(log)
    }

    override fun deleteLog(logId: String) {
        view?.showLoading()
        firebaseHelper.getTrackReference(logId)
            .removeValue()
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("Log deleted successfully")
                loadLogs() // Refresh list
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to delete log: ${exception.message}")
            }
    }

    override fun updateLog(logId: String, updates: Map<String, Any>) {
        view?.showLoading()
        val updatesWithTimestamp = updates.toMutableMap()
        updatesWithTimestamp["updatedAt"] = System.currentTimeMillis()

        firebaseHelper.getTrackReference(logId)
            .updateChildren(updatesWithTimestamp)
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("Log updated successfully")
                loadLogs() // Refresh list
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to update log: ${exception.message}")
            }
    }

    override fun bulkDeleteLogs(logIds: List<String>) {
        view?.showLoading()
        var completed = 0

        logIds.forEach { logId ->
            firebaseHelper.getTrackReference(logId)
                .removeValue()
                .addOnSuccessListener {
                    completed++
                    if (completed == logIds.size) {
                        view?.hideLoading()
                        view?.showSuccess("${logIds.size} logs deleted")
                        view?.clearSelection()
                        loadLogs()
                    }
                }
                .addOnFailureListener { exception ->
                    view?.hideLoading()
                    view?.showError("Bulk delete failed: ${exception.message}")
                }
        }
    }

    override fun getCurrentLogs(): List<Track> {
        return currentFilteredLogs
    }

    override fun loadTodayLogs() {
        loadLogs(Track.getTodayDate())
    }

    override fun loadWeekLogs() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        filterByDateRange(startDate, endDate)
        loadLogs()
    }

    override fun loadMonthLogs() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        filterByDateRange(startDate, endDate)
        loadLogs()
    }

    /**
     * Apply all current filters and search to the logs list
     */
    private fun applyCurrentFilters() {
        var filtered = allLogs

        // Apply date filter (single date)
        if (dateFilter != null) {
            filtered = filtered.filter { it.date == dateFilter }
        }

        // Apply date range filter
        if (startDateFilter != null && endDateFilter != null) {
            filtered = filtered.filter { log ->
                log.date >= startDateFilter!! && log.date <= endDateFilter!!
            }
        }

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { log ->
                log.studentName.lowercase().contains(searchQuery) ||
                        log.rfidTag.lowercase().contains(searchQuery) ||
                        log.location.lowercase().contains(searchQuery)
            }
        }

        // Apply status filter
        if (statusFilter != null) {
            filtered = filtered.filter { it.status == statusFilter }
        }

        // Apply teacher filter
        if (teacherFilter != null) {
            filtered = filtered.filter { it.teacherId == teacherFilter }
        }

        // Apply role-based filter (if TEACHER, show only their students' logs)
        if (currentUserRole == "TEACHER") {
            filtered = filtered.filter { it.teacherId == currentUserId }
        }

        // Apply sorting
        filtered = when (currentSortBy) {
            "studentName" -> filtered.sortedBy { it.studentName }
            "status" -> filtered.sortedBy { it.status }
            "timeOut" -> filtered.sortedByDescending { it.timeOut ?: 0L }
            else -> filtered.sortedByDescending { it.timeIn } // Default: latest first
        }

        currentFilteredLogs = filtered
        view?.updateLogsList(currentFilteredLogs)
        updateSummary()
    }

    /**
     * Calculate and update summary statistics
     */
    private fun updateSummary() {
        val totalPresent = currentFilteredLogs.count { it.status == "PRESENT" }
        val totalLate = currentFilteredLogs.count { it.status == "LATE" }
        val totalAbsent = currentFilteredLogs.count { it.status == "ABSENT" }

        view?.updateSummary(totalPresent, totalLate, totalAbsent)
    }
}
