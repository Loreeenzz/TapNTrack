package com.nenquit.tapntrack.mvp.settings

import com.nenquit.tapntrack.models.User

/**
 * SettingsContract: MVP contract for Settings feature.
 * Defines the interface between View and Presenter.
 */
interface SettingsContract {

    /**
     * View interface for Settings screen
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
         * Display user profile data
         * @param user User object with profile information
         */
        fun displayUserProfile(user: User)

        /**
         * Navigate to edit profile screen
         */
        fun navigateToEditProfile(user: User)

        /**
         * Navigate to change password screen
         */
        fun navigateToChangePassword()

        /**
         * Navigate to login screen after logout
         */
        fun navigateToLogin()

        /**
         * Enable or disable edit profile button
         * @param enabled True to enable, false to disable
         */
        fun setEditProfileButtonEnabled(enabled: Boolean)

        /**
         * Enable or disable change password button
         * @param enabled True to enable, false to disable
         */
        fun setChangePasswordButtonEnabled(enabled: Boolean)

        /**
         * Enable or disable logout button
         * @param enabled True to enable, false to disable
         */
        fun setLogoutButtonEnabled(enabled: Boolean)
    }

    /**
     * Presenter interface for Settings screen
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
         */
        fun loadUserProfile()

        /**
         * Handle edit profile button click
         */
        fun onEditProfileClicked()

        /**
         * Handle change password button click
         */
        fun onChangePasswordClicked()

        /**
         * Handle logout button click
         */
        fun onLogoutClicked()
    }
}
