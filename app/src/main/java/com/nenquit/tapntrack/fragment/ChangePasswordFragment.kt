package com.nenquit.tapntrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.mvp.settings.ChangePasswordContract
import com.nenquit.tapntrack.mvp.settings.ChangePasswordPresenter

/**
 * ChangePasswordFragment: Allows users to securely change their password.
 * Implements MVP pattern with ChangePasswordPresenter as the business logic handler.
 */
class ChangePasswordFragment : Fragment(), ChangePasswordContract.View {
    private lateinit var presenter: ChangePasswordContract.Presenter
    private var currentToast: Toast? = null

    // UI components
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize presenter
        presenter = ChangePasswordPresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents(view)

        // Set up click listeners
        setupClickListeners()
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents(view: View) {
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        cancelButton = view.findViewById(R.id.cancelButton)
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        changePasswordButton.setOnClickListener {
            presenter.onChangePasswordClicked()
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun showLoading() {
        changePasswordButton.isEnabled = false
        cancelButton.isEnabled = false
        currentPasswordEditText.isEnabled = false
        newPasswordEditText.isEnabled = false
        confirmPasswordEditText.isEnabled = false
    }

    override fun hideLoading() {
        changePasswordButton.isEnabled = true
        cancelButton.isEnabled = true
        currentPasswordEditText.isEnabled = true
        newPasswordEditText.isEnabled = true
        confirmPasswordEditText.isEnabled = true
    }

    override fun showError(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun showSuccess(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    override fun getCurrentPassword(): String {
        return currentPasswordEditText.text.toString()
    }

    override fun getNewPassword(): String {
        return newPasswordEditText.text.toString()
    }

    override fun getConfirmPassword(): String {
        return confirmPasswordEditText.text.toString()
    }

    override fun clearInputs() {
        currentPasswordEditText.text.clear()
        newPasswordEditText.text.clear()
        confirmPasswordEditText.text.clear()
    }

    override fun setChangePasswordButtonEnabled(enabled: Boolean) {
        changePasswordButton.isEnabled = enabled
    }

    override fun navigateBackToSettings() {
        parentFragmentManager.popBackStack()
    }

    /**
     * Cancel current toast if exists
     */
    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detach()
        cancelCurrentToast()
    }

    companion object {
        fun newInstance(): ChangePasswordFragment {
            return ChangePasswordFragment()
        }
    }
}
