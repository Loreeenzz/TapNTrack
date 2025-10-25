package com.nenquit.tapntrack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.activity.login.LoginActivity
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.mvp.settings.SettingsContract
import com.nenquit.tapntrack.mvp.settings.SettingsPresenter

/**
 * SettingsFragment: Displays app settings and user preferences.
 * Implements MVP pattern with SettingsPresenter as the business logic handler.
 */
class SettingsFragment : Fragment(), SettingsContract.View {
    private lateinit var presenter: SettingsContract.Presenter
    private var currentToast: Toast? = null

    // UI components
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize presenter
        presenter = SettingsPresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents(view)

        // Set up click listeners
        setupClickListeners()

        // Load user profile
        presenter.loadUserProfile()
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents(view: View) {
        userNameTextView = view.findViewById(R.id.userNameTextView)
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        logoutButton = view.findViewById(R.id.logoutButton)
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        editProfileButton.setOnClickListener {
            presenter.onEditProfileClicked()
        }

        changePasswordButton.setOnClickListener {
            presenter.onChangePasswordClicked()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    override fun showLoading() {
        editProfileButton.isEnabled = false
        changePasswordButton.isEnabled = false
        logoutButton.isEnabled = false
    }

    override fun hideLoading() {
        editProfileButton.isEnabled = true
        changePasswordButton.isEnabled = true
        logoutButton.isEnabled = true
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

    override fun displayUserProfile(user: User) {
        userNameTextView.text = user.name
        userEmailTextView.text = user.email
    }

    override fun navigateToEditProfile(user: User) {
        // Replace current fragment with EditProfileFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, EditProfileFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    override fun navigateToChangePassword() {
        // Replace current fragment with ChangePasswordFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ChangePasswordFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    override fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun setEditProfileButtonEnabled(enabled: Boolean) {
        editProfileButton.isEnabled = enabled
    }

    override fun setChangePasswordButtonEnabled(enabled: Boolean) {
        changePasswordButton.isEnabled = enabled
    }

    override fun setLogoutButtonEnabled(enabled: Boolean) {
        logoutButton.isEnabled = enabled
    }

    /**
     * Show logout confirmation and perform logout
     */
    private fun showLogoutConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            presenter.onLogoutClicked()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
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
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
