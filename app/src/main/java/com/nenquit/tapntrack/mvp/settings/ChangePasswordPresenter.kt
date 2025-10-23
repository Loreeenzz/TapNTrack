package com.nenquit.tapntrack.mvp.settings

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * ChangePasswordPresenter: Handles change password business logic.
 * Manages secure password updates via Firebase Authentication.
 */
class ChangePasswordPresenter : ChangePasswordContract.Presenter {
    private var view: ChangePasswordContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun attach(view: ChangePasswordContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun onChangePasswordClicked() {
        val currentPassword = view?.getCurrentPassword() ?: return
        val newPassword = view?.getNewPassword() ?: return
        val confirmPassword = view?.getConfirmPassword() ?: return

        // Validate all inputs
        if (currentPassword.isEmpty()) {
            view?.showError("Please enter your current password")
            return
        }

        if (newPassword.isEmpty()) {
            view?.showError("Please enter your new password")
            return
        }

        if (confirmPassword.isEmpty()) {
            view?.showError("Please confirm your new password")
            return
        }

        if (!isValidPassword(newPassword)) {
            view?.showError("New password must be at least 6 characters")
            return
        }

        if (newPassword != confirmPassword) {
            view?.showError("Passwords do not match")
            return
        }

        if (currentPassword == newPassword) {
            view?.showError("New password must be different from current password")
            return
        }

        // Change password
        changePassword(currentPassword, newPassword)
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Update user password in Firebase Authentication
     * @param currentPassword User's current password
     * @param newPassword User's new password
     */
    private fun changePassword(currentPassword: String, newPassword: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            view?.showError("User not logged in")
            return
        }

        view?.showLoading()
        view?.setChangePasswordButtonEnabled(false)

        val email = currentUser.email ?: return

        // Reauthenticate user with current password
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Reauthentication successful, now update password
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            view?.hideLoading()
                            view?.setChangePasswordButtonEnabled(true)

                            if (updateTask.isSuccessful) {
                                view?.showSuccess("Password changed successfully")
                                view?.clearInputs()
                                view?.navigateBackToSettings()
                            } else {
                                val exception = updateTask.exception
                                val errorMessage = when (exception) {
                                    is FirebaseAuthWeakPasswordException -> "New password is too weak. Please use at least 6 characters."
                                    else -> exception?.message ?: "Failed to change password. Please try again."
                                }
                                view?.showError(errorMessage)
                            }
                        }
                        .addOnFailureListener { exception ->
                            view?.hideLoading()
                            view?.setChangePasswordButtonEnabled(true)
                            view?.showError(exception.message ?: "Failed to change password")
                        }
                } else {
                    view?.hideLoading()
                    view?.setChangePasswordButtonEnabled(true)
                    val exception = reauthTask.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Current password is incorrect"
                        else -> exception?.message ?: "Authentication failed. Please try again."
                    }
                    view?.showError(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.setChangePasswordButtonEnabled(true)
                view?.showError(exception.message ?: "An error occurred")
            }
    }
}
