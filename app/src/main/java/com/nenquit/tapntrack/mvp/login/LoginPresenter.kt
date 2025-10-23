package com.nenquit.tapntrack.mvp.login

import android.content.Context
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.nenquit.tapntrack.utils.SessionManager

/**
 * LoginPresenter: Handles login business logic.
 * Manages user authentication via Firebase Authentication.
 */
class LoginPresenter : LoginContract.Presenter {
    private var view: LoginContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var sessionManager: SessionManager? = null

    override fun attach(view: LoginContract.View) {
        this.view = view
        // Initialize SessionManager from the view's context
        if (view is android.content.Context) {
            this.sessionManager = SessionManager(view)
        }
    }

    override fun detach() {
        this.view = null
    }

    override fun onLoginClicked() {
        val email = view?.getEmail() ?: return
        val password = view?.getPassword() ?: return

        // Validate inputs
        if (email.isEmpty()) {
            view?.showError("Please enter your email")
            return
        }

        if (password.isEmpty()) {
            view?.showError("Please enter your password")
            return
        }

        if (!isValidEmail(email)) {
            view?.showError("Please enter a valid email")
            return
        }

        if (!isValidPassword(password)) {
            view?.showError("Password must be at least 6 characters")
            return
        }

        // Perform login
        loginUser(email, password)
    }

    override fun onSignUpClicked() {
        view?.navigateToSignUp()
    }

    override fun onForgotPasswordClicked() {
        view?.navigateToForgotPassword()
    }

    override fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Authenticate user with Firebase
     * @param email User email
     * @param password User password
     */
    private fun loginUser(email: String, password: String) {
        view?.showLoading()
        view?.setLoginButtonEnabled(false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                view?.hideLoading()
                view?.setLoginButtonEnabled(true)

                if (task.isSuccessful) {
                    // Save user session
                    val user = auth.currentUser
                    if (user != null) {
                        sessionManager?.saveUserSession(user.email ?: email, user.uid)
                    }

                    view?.showSuccess("Login successful")
                    view?.clearInputs()
                    view?.navigateToDashboard()
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidUserException -> "User account not found"
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                        else -> exception?.message ?: "Login failed. Please try again."
                    }
                    view?.showError(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.setLoginButtonEnabled(true)
                view?.showError(exception.message ?: "An error occurred")
            }
    }
}
