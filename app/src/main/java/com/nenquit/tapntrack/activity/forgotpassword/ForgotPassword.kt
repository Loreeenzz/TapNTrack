package com.nenquit.tapntrack.activity.forgotpassword

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.activity.login.LoginActivity
import com.nenquit.tapntrack.mvp.forgotpassword.ForgotPasswordContract
import com.nenquit.tapntrack.mvp.forgotpassword.ForgotPasswordPresenter

/**
 * ForgotPassword: Handles password reset requests.
 * Implements MVP pattern with ForgotPasswordPresenter as the business logic handler.
 */
class ForgotPassword : Activity(), ForgotPasswordContract.View {
    private lateinit var presenter: ForgotPasswordContract.Presenter
    private var currentToast: Toast? = null
    private val handler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var dotCount = 0

    // UI components (to be bound from layout)
    private lateinit var emailEditText: android.widget.EditText
    private lateinit var resetPasswordButton: android.widget.Button
    private lateinit var backToLoginTextView: android.widget.TextView
    private lateinit var instructionsTextView: android.widget.TextView

    // Store original button text for restoring after loading
    private var resetButtonText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        try {
            // Initialize UI components FIRST
            initializeUIComponents()

            // Initialize presenter AFTER UI components are ready
            presenter = ForgotPasswordPresenter()
            presenter.attach(this)

            // Set up click listeners
            setupClickListeners()
        } catch (e: Exception) {
            showError("Failed to initialize activity: ${e.message}")
        }
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents() {
        try {
            emailEditText = findViewById(R.id.emailEditText)
            resetPasswordButton = findViewById(R.id.resetPasswordButton)
            backToLoginTextView = findViewById(R.id.backToLoginTextView)
            instructionsTextView = findViewById(R.id.instructionsTextView)

            // Store original button text
            resetButtonText = resetPasswordButton.text.toString()
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize UI components: ${e.message}", e)
        }
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        try {
            resetPasswordButton.setOnClickListener {
                // Disable user interaction during processing
                emailEditText.isEnabled = false
                presenter.onResetPasswordClicked()
            }

            backToLoginTextView.setOnClickListener {
                presenter.onBackToLoginClicked()
            }

            // Allow user to re-enable email input on text change
            emailEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && !resetPasswordButton.isEnabled) {
                    emailEditText.isEnabled = true
                }
            }
        } catch (e: Exception) {
            showError("Failed to set up listeners: ${e.message}")
        }
    }

    override fun showLoading() {
        // Disable button and email input, start animated dots
        resetPasswordButton.isEnabled = false
        emailEditText.isEnabled = false
        dotCount = 0
        startDotAnimation()
    }

    override fun hideLoading() {
        // Stop animation and restore button text
        stopDotAnimation()
        resetPasswordButton.text = resetButtonText
        resetPasswordButton.isEnabled = true
        emailEditText.isEnabled = true
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
                resetPasswordButton.text = dots
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
        // Re-enable input on error
        emailEditText.isEnabled = true
        resetPasswordButton.isEnabled = true
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

    override fun setResetButtonEnabled(enabled: Boolean) {
        resetPasswordButton.isEnabled = enabled
    }

    override fun getEmail(): String {
        return emailEditText.text.toString().trim()
    }

    override fun clearInputs() {
        emailEditText.text.clear()
    }

    override fun showInstructions(message: String) {
        instructionsTextView.text = message
    }

    /**
     * Cancel current toast if exists
     */
    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            presenter.detach()
            cancelCurrentToast()
            stopDotAnimation()
        } catch (_: Exception) {
            // Silently ignore cleanup errors
        }
    }
}