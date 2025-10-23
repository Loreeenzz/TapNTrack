package com.nenquit.tapntrack.mvp.users

import com.nenquit.tapntrack.models.User

/**
 * UsersContract: MVP contract for Users management feature.
 * Defines the interface between View and Presenter for user list and filtering.
 */
interface UsersContract {

    /**
     * View interface for Users management screen
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
         * Display list of users
         * @param users List of User objects to display
         */
        fun displayUsers(users: List<User>)

        /**
         * Update user list with filtered results
         * @param users Filtered list of users
         */
        fun updateUserList(users: List<User>)

        /**
         * Navigate to user details screen
         * @param user The user to display details for
         */
        fun navigateToUserDetails(user: User)

        /**
         * Show confirmation dialog for deactivating user
         * @param userId ID of user to deactivate
         * @param userName Name of user to deactivate
         */
        fun showDeactivateConfirmation(userId: String, userName: String)

        /**
         * Show confirmation dialog for deleting user
         * @param userId ID of user to delete
         * @param userName Name of user to delete
         */
        fun showDeleteConfirmation(userId: String, userName: String)

        /**
         * Enable/disable bulk action buttons
         * @param enabled True if any items are selected
         */
        fun setBulkActionsEnabled(enabled: Boolean)

        /**
         * Show bulk action options dialog
         * @param selectedCount Number of selected users
         */
        fun showBulkActionsDialog(selectedCount: Int)

        /**
         * Clear selection and refresh UI
         */
        fun clearSelection()
    }

    /**
     * Presenter interface for Users management screen
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
         * Load all users from Firebase
         */
        fun loadUsers()

        /**
         * Search users by name or email
         * @param query Search query
         */
        fun searchUsers(query: String)

        /**
         * Filter users by status
         * @param isActive True to show active users, false for inactive
         */
        fun filterByStatus(isActive: Boolean)

        /**
         * Sort users by criteria
         * @param sortBy One of: "name", "createdAt", "lastLogin"
         */
        fun sortUsers(sortBy: String)

        /**
         * Reset all filters and search
         */
        fun resetFilters()

        /**
         * Handle user item click
         * @param user The selected user
         */
        fun onUserSelected(user: User)

        /**
         * Deactivate a user account
         * @param userId ID of user to deactivate
         */
        fun deactivateUser(userId: String)

        /**
         * Activate a user account
         * @param userId ID of user to activate
         */
        fun activateUser(userId: String)

        /**
         * Delete a user account
         * @param userId ID of user to delete
         */
        fun deleteUser(userId: String)

        /**
         * Perform bulk deactivate on multiple users
         * @param userIds List of user IDs to deactivate
         */
        fun bulkDeactivateUsers(userIds: List<String>)

        /**
         * Perform bulk activate on multiple users
         * @param userIds List of user IDs to activate
         */
        fun bulkActivateUsers(userIds: List<String>)

        /**
         * Perform bulk delete on multiple users
         * @param userIds List of user IDs to delete
         */
        fun bulkDeleteUsers(userIds: List<String>)

        /**
         * Get current user list (for adapter)
         * @return List of currently displayed users
         */
        fun getCurrentUsers(): List<User>
    }
}
