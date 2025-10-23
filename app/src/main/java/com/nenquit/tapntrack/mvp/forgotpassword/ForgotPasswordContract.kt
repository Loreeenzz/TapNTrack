package com.nenquit.tapntrack.mvp.forgotpassword

/**
 * ForgotPasswordContract: MVP contract for Forgot Password feature.
 * Defines the interface between View and Presenter.
 */
interface ForgotPasswordContract {

    /**
     * View interface for Forgot Password screen
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
         * Navigate back to login screen
         */
        fun navigateToLogin()

        /**
         * Enable or disable reset button
         * @param enabled True to enable, false to disable
         */
        fun setResetButtonEnabled(enabled: Boolean)

        /**
         * Get email from input
         * @return Email string
         */
        fun getEmail(): String

        /**
         * Clear input fields
         */
        fun clearInputs()

        /**
         * Display instructions message
         * @param message Instructions for password reset
         */
        fun showInstructions(message: String)
    }

    /**
     * Presenter interface for Forgot Password screen
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
         * Handle reset password button click
         */
        fun onResetPasswordClicked()

        /**
         * Handle back to login click
         */
        fun onBackToLoginClicked()

        /**
         * Validate email format
         * @param email Email to validate
         * @return True if valid
         */
        fun isValidEmail(email: String): Boolean
    }
}
