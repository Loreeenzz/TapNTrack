package com.nenquit.tapntrack.mvp.settings

/**
 * ChangePasswordContract: MVP contract for Change Password feature.
 * Defines the interface between View and Presenter.
 */
interface ChangePasswordContract {

    /**
     * View interface for Change Password screen
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
         * Get current password input
         * @return Current password string
         */
        fun getCurrentPassword(): String

        /**
         * Get new password input
         * @return New password string
         */
        fun getNewPassword(): String

        /**
         * Get confirm password input
         * @return Confirm password string
         */
        fun getConfirmPassword(): String

        /**
         * Clear all input fields
         */
        fun clearInputs()

        /**
         * Enable or disable change password button
         * @param enabled True to enable, false to disable
         */
        fun setChangePasswordButtonEnabled(enabled: Boolean)

        /**
         * Navigate back to settings screen
         */
        fun navigateBackToSettings()
    }

    /**
     * Presenter interface for Change Password screen
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
         * Handle change password button click
         */
        fun onChangePasswordClicked()

        /**
         * Validate password
         * @param password Password to validate
         * @return True if valid (min 6 characters)
         */
        fun isValidPassword(password: String): Boolean
    }
}
