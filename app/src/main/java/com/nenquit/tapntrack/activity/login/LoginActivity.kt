package com.nenquit.tapntrack.activity.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.activity.dashboard.DashboardActivity
import com.nenquit.tapntrack.activity.signup.SignUpActivity
import com.nenquit.tapntrack.activity.forgotpassword.ForgotPassword
import com.nenquit.tapntrack.mvp.login.LoginContract
import com.nenquit.tapntrack.mvp.login.LoginPresenter
import com.nenquit.tapntrack.utils.SessionManager

/**
 * LoginActivity: Handles user login.
 * Implements MVP pattern with LoginPresenter as the business logic handler.
 */
class LoginActivity : Activity(), LoginContract.View {
    private var presenter: LoginContract.Presenter? = null
    private var currentToast: Toast? = null
    private val handler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var dotCount = 0
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "LoginActivity"
    }

    // UI components (to be bound from layout)
    private lateinit var emailEditText: android.widget.EditText
    private lateinit var passwordEditText: android.widget.EditText
    private lateinit var loginButton: android.widget.Button
    private lateinit var signUpTextView: android.widget.TextView
    private lateinit var forgotPasswordTextView: android.widget.TextView

    // Store original button text for restoring after loading
    private var loginButtonText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Check if user is already logged in (session AND Firebase)
        if (sessionManager.isUserLoggedIn() && FirebaseAuth.getInstance().currentUser != null) {
            navigateToDashboard()
            return
        }

        // Initialize presenter
        presenter = LoginPresenter()
        presenter?.attach(this)

        // Initialize UI components
        initializeUIComponents()

        // Set up click listeners
        setupClickListeners()
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpTextView = findViewById(R.id.signUpTextView)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)

        // Store original button text
        loginButtonText = loginButton.text.toString()
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            presenter?.onLoginClicked()
        }

        signUpTextView.setOnClickListener {
            presenter?.onSignUpClicked()
        }

        forgotPasswordTextView.setOnClickListener {
            presenter?.onForgotPasswordClicked()
        }
    }

    override fun showLoading() {
        // Disable button and start animated dots
        loginButton.isEnabled = false
        dotCount = 0
        startDotAnimation()
    }

    override fun hideLoading() {
        // Stop animation and restore button text
        stopDotAnimation()
        loginButton.text = loginButtonText
        loginButton.isEnabled = true
    }

    /**
     * Start animated dots animation
     */
    private fun startDotAnimation() {
        animationRunnable = object : Runnable {
            override fun run() {
                dotCount = (dotCount + 1) % 4
                val dots = when (dotCount) {
                    0 -> "."
                    1 -> ".."
                    2 -> "..."
                    else -> ""
                }
                loginButton.text = dots
                handler.postDelayed(this, 400) // Update every 400ms
            }
        }
        animationRunnable?.let { handler.post(it) }
    }

    /**
     * Stop animated dots animation
     */
    private fun stopDotAnimation() {
        animationRunnable?.let { handler.removeCallbacks(it) }
        animationRunnable = null
        dotCount = 0
    }

    override fun showError(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun showSuccess(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun navigateToDashboard() {
        Log.d(TAG, "navigateToDashboard: Creating intent to DashboardActivity")
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        Log.d(TAG, "navigateToDashboard: Starting DashboardActivity")
        startActivity(intent)
        Log.d(TAG, "navigateToDashboard: Finishing LoginActivity")
        finish()
        Log.d(TAG, "navigateToDashboard: Complete")
    }

    override fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    override fun navigateToForgotPassword() {
        startActivity(Intent(this, ForgotPassword::class.java))
    }

    override fun setLoginButtonEnabled(enabled: Boolean) {
        loginButton.isEnabled = enabled
    }

    override fun getEmail(): String {
        return emailEditText.text.toString().trim()
    }

    override fun getPassword(): String {
        return passwordEditText.text.toString()
    }

    override fun clearInputs() {
        emailEditText.text.clear()
        passwordEditText.text.clear()
    }

    /**
     * Cancel current toast if exists
     */
    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.detach()
        cancelCurrentToast()
        stopDotAnimation()
    }
}