package com.nenquit.tapntrack.mvp.adduser

/**
 * AddUserContract: MVP contract for Add User feature (Admin only).
 * Defines the interface between View and Presenter for creating teacher and student accounts.
 */
interface AddUserContract {

    /**
     * View interface for Add User screen
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
         * Get email input from user
         * @return Email string
         */
        fun getEmail(): String

        /**
         * Get password input from user
         * @return Password string
         */
        fun getPassword(): String

        /**
         * Get name input from user
         * @return Name string
         */
        fun getName(): String

        /**
         * Get selected role (TEACHER or STUDENT)
         * @return Role string
         */
        fun getRole(): String

        /**
         * Get selected teacher ID (for student role)
         * @return Teacher UID or empty string
         */
        fun getSelectedTeacherId(): String

        /**
         * Clear all input fields
         */
        fun clearInputs()

        /**
         * Enable/disable add button
         * @param enabled Boolean to enable or disable
         */
        fun setAddButtonEnabled(enabled: Boolean)

        /**
         * Navigate back after successful user creation
         */
        fun navigateBack()
    }

    /**
     * Presenter interface for Add User
     */
    interface Presenter {
        /**
         * Attach view to presenter
         * @param view The view to attach
         */
        fun attach(view: View)

        /**
         * Detach view from presenter
         */
        fun detach()

        /**
         * Handle add user button click
         */
        fun onAddUserClicked()

        /**
         * Validate email format
         * @param email Email to validate
         * @return Boolean indicating if email is valid
         */
        fun isValidEmail(email: String): Boolean

        /**
         * Validate password length
         * @param password Password to validate
         * @return Boolean indicating if password is valid
         */
        fun isValidPassword(password: String): Boolean

        /**
         * Validate name
         * @param name Name to validate
         * @return Boolean indicating if name is valid
         */
        fun isValidName(name: String): Boolean

        /**
         * Check if passwords match
         * @param password First password
         * @param confirmPassword Second password
         * @return Boolean indicating if passwords match
         */
        fun passwordsMatch(password: String, confirmPassword: String): Boolean

        /**
         * Get list of all teachers for student assignment
         */
        fun loadTeachers()
    }
}
