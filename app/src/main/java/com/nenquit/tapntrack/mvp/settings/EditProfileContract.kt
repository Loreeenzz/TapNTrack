package com.nenquit.tapntrack.mvp.settings

import com.nenquit.tapntrack.models.User

/**
 * EditProfileContract: MVP contract for Edit Profile feature.
 * Defines the interface between View and Presenter.
 */
interface EditProfileContract {

    /**
     * View interface for Edit Profile screen
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
         * Populate UI with user data
         * @param user User object with profile information
         */
        fun populateUserData(user: User)

        /**
         * Get name input from EditText
         * @return Name string
         */
        fun getNameInput(): String

        /**
         * Set name input to EditText
         * @param name Name to set
         */
        fun setNameInput(name: String)

        /**
         * Enable or disable update button
         * @param enabled True to enable, false to disable
         */
        fun setUpdateButtonEnabled(enabled: Boolean)

        /**
         * Navigate back to settings screen
         */
        fun navigateBackToSettings()
    }

    /**
     * Presenter interface for Edit Profile screen
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
         * Load user profile data from Firebase
         * @param userId The user's unique identifier
         */
        fun loadUserProfile(userId: String)

        /**
         * Handle update profile button click
         * @param userId The user's unique identifier
         * @param newName The new name to update
         */
        fun onUpdateProfileClicked(userId: String, newName: String)

        /**
         * Validate name input
         * @param name Name to validate
         * @return True if valid
         */
        fun isValidName(name: String): Boolean
    }
}
