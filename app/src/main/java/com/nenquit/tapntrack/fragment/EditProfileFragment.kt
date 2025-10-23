package com.nenquit.tapntrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.mvp.settings.EditProfileContract
import com.nenquit.tapntrack.mvp.settings.EditProfilePresenter

/**
 * EditProfileFragment: Allows users to edit their profile information.
 * Implements MVP pattern with EditProfilePresenter as the business logic handler.
 */
class EditProfileFragment : Fragment(), EditProfileContract.View {
    private lateinit var presenter: EditProfileContract.Presenter
    private var currentToast: Toast? = null
    private var userUid: String = ""

    // UI components
    private lateinit var userEmailTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get current user UID from FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        userUid = currentUser?.uid ?: ""

        // Initialize presenter
        presenter = EditProfilePresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents(view)

        // Set up click listeners
        setupClickListeners()

        // Load user profile
        if (userUid.isNotEmpty()) {
            presenter.loadUserProfile(userUid)
        } else {
            showError("User ID not found")
        }
    }

    /**
     * Initialize UI components by finding views
     */
    private fun initializeUIComponents(view: View) {
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        nameEditText = view.findViewById(R.id.nameEditText)
        updateButton = view.findViewById(R.id.updateButton)
        cancelButton = view.findViewById(R.id.cancelButton)
    }

    /**
     * Set up click listeners for UI components
     */
    private fun setupClickListeners() {
        updateButton.setOnClickListener {
            presenter.onUpdateProfileClicked(userUid, nameEditText.text.toString())
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun showLoading() {
        updateButton.isEnabled = false
        cancelButton.isEnabled = false
        nameEditText.isEnabled = false
    }

    override fun hideLoading() {
        updateButton.isEnabled = true
        cancelButton.isEnabled = true
        nameEditText.isEnabled = true
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

    override fun populateUserData(user: User) {
        userEmailTextView.text = user.email
        nameEditText.setText(user.name)
    }

    override fun getNameInput(): String {
        return nameEditText.text.toString().trim()
    }

    override fun setNameInput(name: String) {
        nameEditText.setText(name)
    }

    override fun setUpdateButtonEnabled(enabled: Boolean) {
        updateButton.isEnabled = enabled
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
        fun newInstance(): EditProfileFragment {
            return EditProfileFragment()
        }
    }
}
