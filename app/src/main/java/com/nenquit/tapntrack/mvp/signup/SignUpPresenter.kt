package com.nenquit.tapntrack.mvp.signup

import android.os.Handler
import android.os.Looper
import android.util.Patterns

/**
 * SignUpPresenter: Handles sign up business logic.
 * Manages user registration and data storage in Firebase.
 */
class SignUpPresenter : SignUpContract.Presenter {
    private var view: SignUpContract.View? = null

    override fun attach(view: SignUpContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun onSignUpClicked() {
        // Signup is disabled - only admins can add new users
        view?.showError("New user registration is disabled. Contact an administrator to create an account.")
        // Optionally navigate back to login
        Handler(Looper.getMainLooper()).postDelayed({
            view?.navigateToLogin()
        }, 2000)
    }

    override fun onLoginClicked() {
        view?.navigateToLogin()
    }

    override fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    override fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    override fun isValidName(name: String): Boolean {
        return name.length >= 2 && name.length <= 50
    }
}
