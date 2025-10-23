package com.nenquit.tapntrack.mvp.signup

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.nenquit.tapntrack.models.User

/**
 * SignUpPresenter: Handles sign up business logic.
 * Manages user registration and data storage in Firebase.
 */
class SignUpPresenter : SignUpContract.Presenter {
    private var view: SignUpContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://tapntrack-00001-default-rtdb.asia-southeast1.firebasedatabase.app")

    override fun attach(view: SignUpContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun onSignUpClicked() {
        val email = view?.getEmail() ?: return
        val password = view?.getPassword() ?: return
        val confirmPassword = view?.getConfirmPassword() ?: return
        val name = view?.getName() ?: return

        // Validate all inputs
        if (email.isEmpty()) {
            view?.showError("Email address is required.")
            return
        }

        if (password.isEmpty()) {
            view?.showError("Password is required.")
            return
        }

        if (confirmPassword.isEmpty()) {
            view?.showError("Password confirmation is required.")
            return
        }

        if (name.isEmpty()) {
            view?.showError("Full name is required.")
            return
        }

        if (!isValidEmail(email)) {
            view?.showError("Please enter a valid email address.")
            return
        }

        if (!isValidPassword(password)) {
            view?.showError("Password must be at least 6 characters long.")
            return
        }

        if (!passwordsMatch(password, confirmPassword)) {
            view?.showError("Passwords do not match.")
            return
        }

        if (!isValidName(name)) {
            view?.showError("Full name must be between 2 and 50 characters.")
            return
        }

        // Create user
        createUser(email, password, name)
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

    /**
     * Create user account and store user data
     * @param email User email
     * @param password User password
     * @param name User full name
     */
    private fun createUser(email: String, password: String, name: String) {
        view?.showLoading()
        view?.setSignUpButtonEnabled(false)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        // Create user object
                        val userData = User(
                            uid = user.uid,
                            email = email,
                            name = name,
                            createdAt = System.currentTimeMillis()
                        )

                        // Store user data in database
                        database.getReference("users").child(user.uid).setValue(userData.toMap())
                            .addOnSuccessListener {
                                view?.hideLoading()
                                view?.setSignUpButtonEnabled(true)
                                view?.clearInputs()
                                view?.showSuccess("Account created successfully. Please log in to continue.")

                                // Delay navigation to allow user to see the success message
                                Handler(Looper.getMainLooper()).postDelayed({
                                    view?.navigateToLogin()
                                }, 2000) // 2 second delay
                            }
                            .addOnFailureListener { exception ->
                                view?.hideLoading()
                                view?.setSignUpButtonEnabled(true)
                                view?.showError("Failed to save user data: ${exception.message}")
                            }
                    } ?: run {
                        view?.hideLoading()
                        view?.setSignUpButtonEnabled(true)
                        view?.showError("Authentication was successful, but user information could not be retrieved.")
                    }
                } else {
                    view?.hideLoading()
                    view?.setSignUpButtonEnabled(true)
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> "This email address is already in use."
                        else -> exception?.message ?: "Registration failed. Please try again."
                    }
                    view?.showError(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.setSignUpButtonEnabled(true)
                view?.showError(exception.message ?: "An error occurred during registration.")
            }
    }
}
