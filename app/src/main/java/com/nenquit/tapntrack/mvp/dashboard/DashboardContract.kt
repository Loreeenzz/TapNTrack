package com.nenquit.tapntrack.mvp.dashboard

import com.nenquit.tapntrack.models.Track

/**
 * DashboardContract: MVP contract for Dashboard feature.
 * Defines the interaction between View and Presenter.
 */
interface DashboardContract {

    /**
     * View: UI layer that displays dashboard data
     */
    interface View {
        /**
         * Display today's attendance statistics
         * @param presentCount Number of students present
         * @param lateCount Number of students late
         * @param absentCount Number of students absent
         * @param totalStudents Total number of students tracked today
         */
        fun displayTodayAttendance(presentCount: Int, lateCount: Int, absentCount: Int, totalStudents: Int)

        /**
         * Display quick statistics
         * @param totalStudents Total active students
         * @param totalTeachers Total active teachers
         * @param logsThisWeek Total logs this week
         */
        fun displayQuickStats(totalStudents: Int, totalTeachers: Int, logsThisWeek: Int)

        /**
         * Display recent activity logs
         * @param recentLogs List of recent track logs (limited to 5)
         */
        fun displayRecentActivity(recentLogs: List<Track>)

        /**
         * Show loading indicator
         */
        fun showLoading()

        /**
         * Hide loading indicator
         */
        fun hideLoading()

        /**
         * Show error message
         * @param message Error message to display
         */
        fun showError(message: String)

        /**
         * Navigate to Logs tab
         */
        fun navigateToLogs()

        /**
         * Navigate to Users tab
         */
        fun navigateToUsers()

        /**
         * Navigate to log details
         * @param log The log to display details for
         */
        fun navigateToLogDetails(log: Track)

        /**
         * Update current date time display
         * @param dateTime Formatted date time string
         */
        fun updateDateTime(dateTime: String)
    }

    /**
     * Presenter: Business logic layer that handles data operations
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
         * Load all dashboard data
         */
        fun loadDashboardData()

        /**
         * Handle view all logs button click
         */
        fun onViewAllLogsClick()

        /**
         * Handle view all users button click
         */
        fun onViewAllUsersClick()

        /**
         * Handle recent activity item click
         * @param log The log that was clicked
         */
        fun onRecentActivityClick(log: Track)

        /**
         * Refresh dashboard data
         */
        fun refreshData()
    }
}
