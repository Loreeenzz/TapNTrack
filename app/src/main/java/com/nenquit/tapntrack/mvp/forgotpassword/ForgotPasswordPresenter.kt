package com.nenquit.tapntrack.mvp.forgotpassword

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

/**
 * ForgotPasswordPresenter: Handles forgot password business logic.
 * Manages password reset requests via Firebase Authentication.
 */
class ForgotPasswordPresenter : ForgotPasswordContract.Presenter {
    private var view: ForgotPasswordContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var isResettingPassword = false

    override fun attach(view: ForgotPasswordContract.View) {
        this.view = view
        // Show initial instructions
        view.showInstructions("Enter your email address and we'll send you a link to reset your password.")
    }

    override fun detach() {
        this.view = null
    }

    override fun onResetPasswordClicked() {
        // Prevent multiple simultaneous requests
        if (isResettingPassword) {
            view?.showError("Password reset in progress. Please wait...")
            return
        }

        val email = view?.getEmail() ?: return

        // Validate input
        if (email.isEmpty()) {
            view?.showError("Please enter your email")
            return
        }

        if (!isValidEmail(email)) {
            view?.showError("Please enter a valid email address")
            return
        }

        // Send password reset email
        sendPasswordResetEmail(email)
    }

    override fun onBackToLoginClicked() {
        view?.navigateToLogin()
    }

    override fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Send password reset email
     * @param email User email address
     */
    private fun sendPasswordResetEmail(email: String) {
        isResettingPassword = true
        view?.showLoading()
        view?.setResetButtonEnabled(false)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isResettingPassword = false
                view?.hideLoading()
                view?.setResetButtonEnabled(true)

                if (task.isSuccessful) {
                    view?.showSuccess("Password reset email sent successfully. Check your inbox.")
                    view?.clearInputs()
                    // Navigate back to login after a delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        view?.navigateToLogin()
                    }, 2000)
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidUserException -> "Email not found. Please check and try again."
                        else -> exception?.message ?: "Failed to send reset email. Please try again later."
                    }
                    view?.showError(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                isResettingPassword = false
                view?.hideLoading()
                view?.setResetButtonEnabled(true)
                view?.showError(exception.message ?: "An error occurred. Please try again.")
            }
    }
}
