package com.nenquit.tapntrack.mvp.signup

/**
 * SignUpContract: MVP contract for Sign Up feature.
 * Defines the interface between View and Presenter.
 */
interface SignUpContract {

    /**
     * View interface for Sign Up screen
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
         * Navigate to login screen after successful signup
         */
        fun navigateToLogin()

        /**
         * Enable or disable sign up button
         * @param enabled True to enable, false to disable
         */
        fun setSignUpButtonEnabled(enabled: Boolean)

        /**
         * Get email from input
         * @return Email string
         */
        fun getEmail(): String

        /**
         * Get password from input
         * @return Password string
         */
        fun getPassword(): String

        /**
         * Get confirm password from input
         * @return Confirm password string
         */
        fun getConfirmPassword(): String

        /**
         * Get name from input
         * @return Name string
         */
        fun getName(): String

        /**
         * Clear input fields
         */
        fun clearInputs()
    }

    /**
     * Presenter interface for Sign Up screen
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
         * Handle sign up button click
         */
        fun onSignUpClicked()

        /**
         * Handle login text click
         */
        fun onLoginClicked()

        /**
         * Validate email format
         * @param email Email to validate
         * @return True if valid
         */
        fun isValidEmail(email: String): Boolean

        /**
         * Validate password strength
         * @param password Password to validate
         * @return True if valid (min 6 characters)
         */
        fun isValidPassword(password: String): Boolean

        /**
         * Check if passwords match
         * @param password Password
         * @param confirmPassword Confirm password
         * @return True if match
         */
        fun passwordsMatch(password: String, confirmPassword: String): Boolean

        /**
         * Validate name
         * @param name Name to validate
         * @return True if valid
         */
        fun isValidName(name: String): Boolean
    }
}
