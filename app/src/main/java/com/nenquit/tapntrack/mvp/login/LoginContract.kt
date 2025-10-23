package com.nenquit.tapntrack.mvp.login

/**
 * LoginContract: MVP contract for Login feature.
 * Defines the interface between View and Presenter.
 */
interface LoginContract {

    /**
     * View interface for Login screen
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
         * Navigate to dashboard after successful login
         */
        fun navigateToDashboard()

        /**
         * Navigate to sign up screen
         */
        fun navigateToSignUp()

        /**
         * Navigate to forgot password screen
         */
        fun navigateToForgotPassword()

        /**
         * Enable or disable login button
         * @param enabled True to enable, false to disable
         */
        fun setLoginButtonEnabled(enabled: Boolean)

        /**
         * Validate email input
         * @return True if email is valid
         */
        fun getEmail(): String

        /**
         * Validate password input
         * @return True if password is valid
         */
        fun getPassword(): String

        /**
         * Clear input fields
         */
        fun clearInputs()
    }

    /**
     * Presenter interface for Login screen
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
         * Handle login button click
         */
        fun onLoginClicked()

        /**
         * Handle sign up text click
         */
        fun onSignUpClicked()

        /**
         * Handle forgot password click
         */
        fun onForgotPasswordClicked()

        /**
         * Validate email format
         * @param email Email to validate
         * @return True if valid
         */
        fun isValidEmail(email: String): Boolean

        /**
         * Validate password
         * @param password Password to validate
         * @return True if valid
         */
        fun isValidPassword(password: String): Boolean
    }
}
