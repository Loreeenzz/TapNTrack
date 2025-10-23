package com.nenquit.tapntrack.activity.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.activity.login.LoginActivity
import com.nenquit.tapntrack.mvp.signup.SignUpContract
import com.nenquit.tapntrack.mvp.signup.SignUpPresenter

/**
 * SignUpActivity: Handles user registration.
 * Implements MVP pattern with SignUpPresenter as the business logic handler.
 */
class SignUpActivity : Activity(), SignUpContract.View {
    private lateinit var presenter: SignUpContract.Presenter
    private var currentToast: Toast? = null
    private val handler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var dotCount = 0

    // UI components (to be bound from layout)
    private lateinit var firstNameEditText: android.widget.EditText
    private lateinit var lastNameEditText: android.widget.EditText
    private lateinit var middleNameEditText: android.widget.EditText
    private lateinit var emailEditText: android.widget.EditText
    private lateinit var passwordEditText: android.widget.EditText
    private lateinit var confirmPasswordEditText: android.widget.EditText
    private lateinit var signUpButton: android.widget.Button
    private lateinit var loginTextView: android.widget.TextView

    // Store original button text for restoring after loading
    private var signUpButtonText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize presenter
        presenter = SignUpPresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents()

        // Set up click listeners
        setupClickListeners()
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents() {
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        middleNameEditText = findViewById(R.id.middleNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginTextView = findViewById(R.id.loginTextView)

        // Store original button text
        signUpButtonText = signUpButton.text.toString()
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        signUpButton.setOnClickListener {
            presenter.onSignUpClicked()
        }

        loginTextView.setOnClickListener {
            presenter.onLoginClicked()
        }
    }

    override fun showLoading() {
        // Disable button and start animated dots
        signUpButton.isEnabled = false
        dotCount = 0
        startDotAnimation()
    }

    override fun hideLoading() {
        // Stop animation and restore button text
        stopDotAnimation()
        signUpButton.text = signUpButtonText
        signUpButton.isEnabled = true
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
                signUpButton.text = dots
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
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        currentToast?.show()
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun setSignUpButtonEnabled(enabled: Boolean) {
        signUpButton.isEnabled = enabled
    }

    override fun getEmail(): String {
        return emailEditText.text.toString().trim()
    }

    override fun getPassword(): String {
        return passwordEditText.text.toString()
    }

    override fun getConfirmPassword(): String {
        return confirmPasswordEditText.text.toString()
    }

    override fun getName(): String {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val middleName = middleNameEditText.text.toString().trim()
        return if (middleName.isNotEmpty()) {
            "$firstName $middleName $lastName"
        } else {
            "$firstName $lastName"
        }
    }

    override fun clearInputs() {
        firstNameEditText.text.clear()
        lastNameEditText.text.clear()
        middleNameEditText.text.clear()
        emailEditText.text.clear()
        passwordEditText.text.clear()
        confirmPasswordEditText.text.clear()
    }

    /**
     * Cancel current toast if exists
     */
    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
        cancelCurrentToast()
        stopDotAnimation()
    }
}