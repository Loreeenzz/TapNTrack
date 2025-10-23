package com.nenquit.tapntrack.mvp.settings

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.utils.FirebaseHelper
import com.nenquit.tapntrack.utils.SessionManager

/**
 * SettingsPresenter: Handles settings business logic.
 * Manages user profile loading, logout, and navigation to edit screens.
 */
class SettingsPresenter : SettingsContract.Presenter {
    private var view: SettingsContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseHelper: FirebaseHelper = FirebaseHelper()
    private var sessionManager: SessionManager? = null

    override fun attach(view: SettingsContract.View) {
        this.view = view
        // Initialize SessionManager from the view's context
        if (view is Context) {
            this.sessionManager = SessionManager(view)
        }
    }

    override fun detach() {
        this.view = null
    }

    override fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            view?.showError("User not logged in")
            return
        }

        view?.showLoading()

        // Fetch user data from Firebase Realtime Database
        firebaseHelper.getUserReference(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.hideLoading()
                    if (snapshot.exists()) {
                        try {
                            val user = User(
                                uid = snapshot.child("uid").value as? String ?: currentUser.uid,
                                email = snapshot.child("email").value as? String ?: currentUser.email ?: "",
                                name = snapshot.child("name").value as? String ?: "",
                                createdAt = (snapshot.child("createdAt").value as? Number)?.toLong() ?: 0L
                            )
                            view?.displayUserProfile(user)
                        } catch (e: Exception) {
                            view?.showError("Failed to load profile: ${e.message}")
                        }
                    } else {
                        view?.showError("User profile not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.hideLoading()
                    view?.showError("Failed to load profile: ${error.message}")
                }
            })
    }

    override fun onEditProfileClicked() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            view?.showError("User not logged in")
            return
        }

        view?.showLoading()

        // Fetch current user data
        firebaseHelper.getUserReference(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.hideLoading()
                    if (snapshot.exists()) {
                        try {
                            val user = User(
                                uid = snapshot.child("uid").value as? String ?: currentUser.uid,
                                email = snapshot.child("email").value as? String ?: currentUser.email ?: "",
                                name = snapshot.child("name").value as? String ?: "",
                                createdAt = (snapshot.child("createdAt").value as? Number)?.toLong() ?: 0L
                            )
                            view?.navigateToEditProfile(user)
                        } catch (e: Exception) {
                            view?.showError("Failed to load profile: ${e.message}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.hideLoading()
                    view?.showError("Failed to load profile: ${error.message}")
                }
            })
    }

    override fun onChangePasswordClicked() {
        view?.navigateToChangePassword()
    }

    override fun onLogoutClicked() {
        try {
            // Clear saved session
            sessionManager?.clearUserSession()

            // Sign out from Firebase
            auth.signOut()

            view?.showSuccess("Logged out successfully")
            view?.navigateToLogin()
        } catch (e: Exception) {
            view?.showError("Logout failed: ${e.message}")
        }
    }
}
