package com.nenquit.tapntrack.mvp.users

import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.models.User

/**
 * UserDetailsContract: MVP contract for User Details feature.
 * Defines the interface between View and Presenter for viewing individual user details.
 */
interface UserDetailsContract {

    /**
     * View interface for User Details screen
     */
    interface View {
        /**
         * Show loading state
         */
        fun showLoading()

        /**
         * Hide loading state
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
         * Display user profile information
         * @param user User object with profile data
         */
        fun displayUserProfile(user: User)

        /**
         * Display user attendance records
         * @param tracks List of attendance records (check-in/out)
         */
        fun displayAttendanceRecords(tracks: List<Track>)

        /**
         * Display user statistics
         * @param totalAttendance Total attendance records count
         * @param attendanceRate Attendance rate percentage
         * @param lastSeen Last seen timestamp
         */
        fun displayUserStatistics(totalAttendance: Int, attendanceRate: Double, lastSeen: Long)

        /**
         * Navigate back to users list
         */
        fun navigateBack()

        /**
         * Show confirmation dialog for deactivating user
         */
        fun showDeactivateConfirmation()

        /**
         * Show confirmation dialog for deleting user
         */
        fun showDeleteConfirmation()

        /**
         * Enable/disable action buttons
         * @param enabled True to enable, false to disable
         */
        fun setActionButtonsEnabled(enabled: Boolean)
    }

    /**
     * Presenter interface for User Details screen
     */
    interface Presenter {
        /**
         * Attach view to presenter
         * @param view The view implementation
         */
        fun attach(view: View)

        /**
         * Detach view from presenter
         */
        fun detach()

        /**
         * Load user details by UID
         * @param userId The Firebase UID of the user
         */
        fun loadUserDetails(userId: String)

        /**
         * Load attendance records for a user
         * @param userId The Firebase UID of the user
         */
        fun loadAttendanceRecords(userId: String)

        /**
         * Calculate and display user statistics
         * @param userId The Firebase UID of the user
         */
        fun calculateUserStatistics(userId: String)

        /**
         * Deactivate user account
         * @param userId The Firebase UID of the user
         */
        fun deactivateUser(userId: String)

        /**
         * Activate user account
         * @param userId The Firebase UID of the user
         */
        fun activateUser(userId: String)

        /**
         * Delete user account
         * @param userId The Firebase UID of the user
         */
        fun deleteUser(userId: String)

        /**
         * Handle back navigation
         */
        fun onBackPressed()
    }
}
