package com.nenquit.tapntrack.mvp.users

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.utils.FirebaseHelper

/**
 * UsersPresenter: Handles user management business logic.
 * Manages user loading, filtering, searching, and sorting from Firebase.
 */
class UsersPresenter : UsersContract.Presenter {
    private var view: UsersContract.View? = null
    private val firebaseHelper: FirebaseHelper = FirebaseHelper()
    private var allUsers: List<User> = emptyList()
    private var currentFilteredUsers: List<User> = emptyList()

    // Filters and search state
    private var searchQuery: String = ""
    private var statusFilter: Boolean? = null // null = no filter, true = active, false = inactive
    private var currentSortBy: String = "name" // "name", "createdAt", "lastLogin"

    override fun attach(view: UsersContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun loadUsers() {
        view?.showLoading()

        firebaseHelper.getUsersReference()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.hideLoading()
                    if (snapshot.exists()) {
                        try {
                            val users = mutableListOf<User>()
                            for (userSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val userData = userSnapshot.value as? Map<String, Any> ?: continue
                                users.add(User.fromMap(userData))
                            }
                            allUsers = users
                            currentFilteredUsers = users
                            applyCurrentFilters()
                            view?.displayUsers(currentFilteredUsers)
                        } catch (e: Exception) {
                            view?.showError("Failed to load users: ${e.message}")
                        }
                    } else {
                        view?.showError("No users found")
                        allUsers = emptyList()
                        currentFilteredUsers = emptyList()
                        view?.displayUsers(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.hideLoading()
                    view?.showError("Failed to load users: ${error.message}")
                }
            })
    }

    override fun searchUsers(query: String) {
        searchQuery = query.trim().lowercase()
        applyCurrentFilters()
    }

    override fun filterByStatus(isActive: Boolean) {
        statusFilter = isActive
        applyCurrentFilters()
    }

    override fun sortUsers(sortBy: String) {
        currentSortBy = sortBy
        applyCurrentFilters()
    }

    override fun resetFilters() {
        searchQuery = ""
        statusFilter = null
        currentSortBy = "name"
        currentFilteredUsers = allUsers
        view?.displayUsers(currentFilteredUsers)
    }

    override fun onUserSelected(user: User) {
        view?.navigateToUserDetails(user)
    }

    override fun deactivateUser(userId: String) {
        view?.showLoading()
        firebaseHelper.getUserReference(userId)
            .updateChildren(mapOf("isActive" to false))
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("User deactivated successfully")
                loadUsers() // Refresh list
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to deactivate user: ${exception.message}")
            }
    }

    override fun activateUser(userId: String) {
        view?.showLoading()
        firebaseHelper.getUserReference(userId)
            .updateChildren(mapOf("isActive" to true))
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("User activated successfully")
                loadUsers() // Refresh list
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to activate user: ${exception.message}")
            }
    }

    override fun deleteUser(userId: String) {
        view?.showLoading()
        firebaseHelper.getUserReference(userId)
            .removeValue()
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("User deleted successfully")
                loadUsers() // Refresh list
            }
            .addOnFailureListener { e ->
                view?.hideLoading()
                view?.showError("Failed to delete user: ${e.message}")
            }
    }

    override fun bulkDeactivateUsers(userIds: List<String>) {
        view?.showLoading()
        var completed = 0

        userIds.forEach { userId ->
            firebaseHelper.getUserReference(userId)
                .updateChildren(mapOf("isActive" to false))
                .addOnSuccessListener {
                    completed++
                    if (completed == userIds.size) {
                        view?.hideLoading()
                        view?.showSuccess("${userIds.size} users deactivated")
                        view?.clearSelection()
                        loadUsers()
                    }
                }
                .addOnFailureListener { exception ->
                    view?.hideLoading()
                    view?.showError("Bulk deactivate failed: ${exception.message}")
                }
        }
    }

    override fun bulkActivateUsers(userIds: List<String>) {
        view?.showLoading()
        var completed = 0

        userIds.forEach { userId ->
            firebaseHelper.getUserReference(userId)
                .updateChildren(mapOf("isActive" to true))
                .addOnSuccessListener {
                    completed++
                    if (completed == userIds.size) {
                        view?.hideLoading()
                        view?.showSuccess("${userIds.size} users activated")
                        view?.clearSelection()
                        loadUsers()
                    }
                }
                .addOnFailureListener { exception ->
                    view?.hideLoading()
                    view?.showError("Bulk activate failed: ${exception.message}")
                }
        }
    }

    override fun bulkDeleteUsers(userIds: List<String>) {
        view?.showLoading()
        var completed = 0

        userIds.forEach { userId ->
            firebaseHelper.getUserReference(userId)
                .removeValue()
                .addOnSuccessListener {
                    completed++
                    if (completed == userIds.size) {
                        view?.hideLoading()
                        view?.showSuccess("${userIds.size} users deleted")
                        view?.clearSelection()
                        loadUsers()
                    }
                }
                .addOnFailureListener { exception ->
                    view?.hideLoading()
                    view?.showError("Bulk delete failed: ${exception.message}")
                }
        }
    }

    override fun getCurrentUsers(): List<User> {
        return currentFilteredUsers
    }

    /**
     * Apply current filters and search to the user list
     */
    private fun applyCurrentFilters() {
        var filtered = allUsers

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { user ->
                user.name.lowercase().contains(searchQuery) ||
                        user.email.lowercase().contains(searchQuery)
            }
        }

        // Apply status filter
        if (statusFilter != null) {
            filtered = filtered.filter { it.isActive == statusFilter }
        }

        // Apply sorting
        filtered = when (currentSortBy) {
            "createdAt" -> filtered.sortedByDescending { it.createdAt }
            "lastLogin" -> filtered.sortedByDescending { it.lastLoginTime }
            else -> filtered.sortedBy { it.name }
        }

        currentFilteredUsers = filtered
        view?.updateUserList(currentFilteredUsers)
    }
}
