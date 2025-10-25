package com.nenquit.tapntrack.mvp.logs

import com.nenquit.tapntrack.models.Track

/**
 * LogsContract: MVP contract for Logs/Attendance tracking feature.
 * Defines the interface between View (LogsFragment) and Presenter (LogsPresenter).
 *
 * Features:
 * - Display attendance logs (RFID scans)
 * - Filter by date, status, student, teacher
 * - Search functionality
 * - Sort options
 */
interface LogsContract {

    /**
     * View interface for Logs screen
     */
    interface View {
        /**
         * Show loading indicator
         */
        fun showLoading()

        /**
         * Hide loading indicator
         */
        fun hideLoading()

        /**
         * Display error message to user
         * @param message Error message to display
         */
        fun showError(message: String)

        /**
         * Show success message
         * @param message Success message
         */
        fun showSuccess(message: String)

        /**
         * Display list of attendance logs
         * @param logs List of Track objects to display
         */
        fun displayLogs(logs: List<Track>)

        /**
         * Update the logs list (for filtering/sorting)
         * @param logs Updated list of Track objects
         */
        fun updateLogsList(logs: List<Track>)

        /**
         * Navigate to log details screen
         * @param log The Track object to view details
         */
        fun navigateToLogDetails(log: Track)

        /**
         * Update summary statistics
         * @param totalPresent Total students present
         * @param totalLate Total students late
         * @param totalAbsent Total students absent
         */
        fun updateSummary(totalPresent: Int, totalLate: Int, totalAbsent: Int)

        /**
         * Clear selection checkboxes
         */
        fun clearSelection()

        /**
         * Show empty state when no logs found
         */
        fun showEmptyState()

        /**
         * Hide empty state
         */
        fun hideEmptyState()
    }

    /**
     * Presenter interface for Logs business logic
     */
    interface Presenter {
        /**
         * Attach view to presenter
         * @param view View instance
         */
        fun attach(view: View)

        /**
         * Detach view from presenter
         */
        fun detach()

        /**
         * Load all attendance logs from Firebase
         * @param dateFilter Optional date filter (yyyy-MM-dd format)
         */
        fun loadLogs(dateFilter: String? = null)

        /**
         * Search logs by student name or RFID tag
         * @param query Search query string
         */
        fun searchLogs(query: String)

        /**
         * Filter logs by status
         * @param status Status to filter by (PRESENT, LATE, ABSENT, or empty/null for all)
         */
        fun filterByStatus(status: String?)

        /**
         * Filter logs by teacher
         * @param teacherId Teacher's UID
         */
        fun filterByTeacher(teacherId: String)

        /**
         * Filter logs by date range
         * @param startDate Start date (yyyy-MM-dd)
         * @param endDate End date (yyyy-MM-dd)
         */
        fun filterByDateRange(startDate: String, endDate: String)

        /**
         * Sort logs by specified field
         * @param sortBy Field to sort by (timeIn, studentName, status)
         */
        fun sortLogs(sortBy: String)

        /**
         * Reset all filters and show all logs
         */
        fun resetFilters()

        /**
         * Handle log item click
         * @param log Selected Track object
         */
        fun onLogSelected(log: Track)

        /**
         * Delete a log entry
         * @param logId Log ID to delete
         */
        fun deleteLog(logId: String)

        /**
         * Update log entry (e.g., time-out, status)
         * @param logId Log ID to update
         * @param updates Map of fields to update
         */
        fun updateLog(logId: String, updates: Map<String, Any>)

        /**
         * Bulk delete logs
         * @param logIds List of log IDs to delete
         */
        fun bulkDeleteLogs(logIds: List<String>)

        /**
         * Get current filtered logs
         * @return List of currently displayed logs
         */
        fun getCurrentLogs(): List<Track>

        /**
         * Load logs for today only
         */
        fun loadTodayLogs()

        /**
         * Load logs for current week
         */
        fun loadWeekLogs()

        /**
         * Load logs for current month
         */
        fun loadMonthLogs()
    }
}
