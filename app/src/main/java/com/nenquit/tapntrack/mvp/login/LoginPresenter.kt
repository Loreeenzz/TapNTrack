package com.nenquit.tapntrack.mvp.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.FirebaseDatabase
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.utils.SessionManager

/**
 * LoginPresenter: Handles login business logic.
 * Manages user authentication via Firebase Authentication.
 */
class LoginPresenter : LoginContract.Presenter {
    private var view: LoginContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://tapntrack-00011-default-rtdb.asia-southeast1.firebasedatabase.app")
    private var sessionManager: SessionManager? = null

    companion object {
        private const val TAG = "LoginPresenter"
    }

    override fun attach(view: LoginContract.View) {
        this.view = view
        // Initialize SessionManager from the view's context
        if (view is Context) {
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
     * Authenticate user with Firebase and fetch user role from database
     * @param email User email
     * @param password User password
     */
    private fun loginUser(email: String, password: String) {
        view?.showLoading()
        view?.setLoginButtonEnabled(false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User authenticated successfully, now fetch their role from database
                    val user = auth.currentUser
                    if (user != null) {
                        fetchUserRole(user.uid, user.email ?: email)
                    } else {
                        view?.hideLoading()
                        view?.setLoginButtonEnabled(true)
                        view?.showError("Authentication successful but user information unavailable")
                    }
                } else {
                    view?.hideLoading()
                    view?.setLoginButtonEnabled(true)
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

    /**
     * Fetch user role and other details from database
     * @param uid User's Firebase UID
     * @param email User's email
     */
    @Suppress("UNCHECKED_CAST")
    private fun fetchUserRole(uid: String, email: String) {
        Log.d(TAG, "fetchUserRole: Starting to fetch user data for UID: $uid")
        database.getReference("users/$uid").get()
            .addOnSuccessListener { snapshot ->
                Log.d(TAG, "fetchUserRole: Database query successful")
                view?.hideLoading()
                view?.setLoginButtonEnabled(true)

                if (snapshot.exists()) {
                    Log.d(TAG, "fetchUserRole: User data exists in database")
                    val userData = snapshot.value as? Map<String, Any>
                    if (userData != null) {
                        Log.d(TAG, "fetchUserRole: User data parsed successfully: $userData")
                        val user = User.fromMap(userData)
                        Log.d(TAG, "fetchUserRole: User object created - Role: ${user.role}, Name: ${user.name}")
                        // Save user session with role and teacherId
                        sessionManager?.saveUserSession(email, uid, user.role, user.teacherId)
                        Log.d(TAG, "fetchUserRole: Session saved successfully")
                        view?.showSuccess("Login successful")
                        view?.clearInputs()
                        Log.d(TAG, "fetchUserRole: About to navigate to dashboard")
                        view?.navigateToDashboard()
                        Log.d(TAG, "fetchUserRole: Navigate to dashboard called")
                    } else {
                        Log.e(TAG, "fetchUserRole: User data is null")
                        view?.showError("User data format error")
                    }
                } else {
                    Log.e(TAG, "fetchUserRole: User profile does not exist in database")
                    view?.showError("User profile not found in database")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "fetchUserRole: Database query failed", exception)
                view?.hideLoading()
                view?.setLoginButtonEnabled(true)
                view?.showError("Failed to load user data: ${exception.message}")
            }
    }
}
