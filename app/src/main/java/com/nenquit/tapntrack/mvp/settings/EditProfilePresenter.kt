package com.nenquit.tapntrack.mvp.settings

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.utils.FirebaseHelper

/**
 * EditProfilePresenter: Handles edit profile business logic.
 * Manages user profile loading and updating in Firebase.
 */
class EditProfilePresenter : EditProfileContract.Presenter {
    private var view: EditProfileContract.View? = null
    private val firebaseHelper: FirebaseHelper = FirebaseHelper()

    override fun attach(view: EditProfileContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun loadUserProfile(userId: String) {
        view?.showLoading()

        firebaseHelper.getUserReference(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.hideLoading()
                    if (snapshot.exists()) {
                        try {
                            val user = User(
                                uid = snapshot.child("uid").value as? String ?: "",
                                email = snapshot.child("email").value as? String ?: "",
                                name = snapshot.child("name").value as? String ?: "",
                                createdAt = (snapshot.child("createdAt").value as? Number)?.toLong() ?: 0L
                            )
                            view?.populateUserData(user)
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

    override fun onUpdateProfileClicked(userId: String, newName: String) {
        // Validate name
        if (!isValidName(newName)) {
            view?.showError("Name must be between 2 and 50 characters")
            return
        }

        view?.showLoading()
        view?.setUpdateButtonEnabled(false)

        // Update user name in Firebase Realtime Database
        firebaseHelper.getUserReference(userId)
            .child("name")
            .setValue(newName)
            .addOnSuccessListener {
                view?.hideLoading()
                view?.setUpdateButtonEnabled(true)
                view?.showSuccess("Profile updated successfully")
                view?.navigateBackToSettings()
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.setUpdateButtonEnabled(true)
                view?.showError("Failed to update profile: ${exception.message}")
            }
    }

    override fun isValidName(name: String): Boolean {
        val trimmedName = name.trim()
        return trimmedName.isNotEmpty() && trimmedName.length >= 2 && trimmedName.length <= 50
    }
}
