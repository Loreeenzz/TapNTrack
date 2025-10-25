package com.nenquit.tapntrack.activity.adduser

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.mvp.adduser.AddUserContract
import com.nenquit.tapntrack.mvp.adduser.AddUserPresenter
import com.nenquit.tapntrack.utils.SessionManager

/**
 * AddUserActivity: Admin interface for creating new TEACHER and STUDENT accounts.
 * Only accessible by users with ADMIN role.
 *
 * CRITICAL: This app is ADMIN-ONLY. Teachers and students don't log in.
 * Passwords are auto-generated for created users.
 */
class AddUserActivity : Activity(), AddUserContract.View {
    private lateinit var presenter: AddUserContract.Presenter
    private lateinit var sessionManager: SessionManager
    private var currentToast: Toast? = null

    // UI Components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var roleRadioGroup: RadioGroup
    private lateinit var teacherRadioButton: RadioButton
    private lateinit var studentRadioButton: RadioButton
    private lateinit var teacherSelectionLayout: LinearLayout
    private lateinit var teacherSpinner: Spinner
    private lateinit var addUserButton: Button
    private lateinit var cancelButton: Button
    private lateinit var progressBar: ProgressBar

    private val database = FirebaseDatabase.getInstance("https://tapntrack-00011-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val teachers = mutableListOf<User>()
    private val teacherNames = mutableListOf<String>()
    private val teacherIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is admin
        sessionManager = SessionManager(this)
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_add_user)

        // Initialize presenter
        presenter = AddUserPresenter()
        presenter.attach(this)

        // Initialize UI components
        initializeUIComponents()

        // Set up listeners
        setupListeners()

        // Load teachers for dropdown
        loadTeachers()

        // CRITICAL FIX: Prompt admin to re-enter password for re-authentication
        // This prevents admin session from being lost when creating users
        promptForAdminPassword()
    }

    /**
     * Prompt admin to enter their password once for re-authentication after user creation.
     * This is required because Firebase's createUserWithEmailAndPassword() logs in the new user,
     * replacing the current admin session.
     */
    private fun promptForAdminPassword() {
        val adminEmail = FirebaseAuth.getInstance().currentUser?.email

        if (adminEmail == null) {
            Toast.makeText(this, "Error: No admin session found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Create password input field
        val passwordInput = EditText(this)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordInput.hint = "Enter your admin password"

        AlertDialog.Builder(this)
            .setTitle("Verify Admin Identity")
            .setMessage("To create users securely, please re-enter your admin password.\n\nAdmin: $adminEmail")
            .setView(passwordInput)
            .setCancelable(false)
            .setPositiveButton("Continue") { dialog, _ ->
                val password = passwordInput.text.toString()
                if (password.isNotEmpty()) {
                    (presenter as AddUserPresenter).setAdminCredentials(adminEmail, password)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                finish()
            }
            .show()
    }

    private fun initializeUIComponents() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        roleRadioGroup = findViewById(R.id.roleRadioGroup)
        teacherRadioButton = findViewById(R.id.teacherRadioButton)
        studentRadioButton = findViewById(R.id.studentRadioButton)
        teacherSelectionLayout = findViewById(R.id.teacherSelectionLayout)
        teacherSpinner = findViewById(R.id.teacherSpinner)
        addUserButton = findViewById(R.id.addUserButton)
        cancelButton = findViewById(R.id.cancelButton)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        // Role selection listener
        roleRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.teacherRadioButton -> {
                    teacherSelectionLayout.visibility = View.GONE
                }
                R.id.studentRadioButton -> {
                    teacherSelectionLayout.visibility = View.VISIBLE
                }
            }
        }

        // Add user button
        addUserButton.setOnClickListener {
            presenter.onAddUserClicked()
        }

        // Cancel button
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadTeachers() {
        database.getReference("users").orderByChild("role").equalTo("TEACHER")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    teachers.clear()
                    teacherNames.clear()
                    teacherIds.clear()

                    // Add default option
                    teacherNames.add("Select a teacher")
                    teacherIds.add("")

                    for (userSnapshot in snapshot.children) {
                        @Suppress("UNCHECKED_CAST")
                        val userData = userSnapshot.value as? Map<String, Any> ?: continue
                        val teacher = User.fromMap(userData)
                        teachers.add(teacher)
                        teacherNames.add(teacher.name)
                        teacherIds.add(teacher.uid)
                    }

                    // Set up spinner
                    val adapter = ArrayAdapter(
                        this@AddUserActivity,
                        android.R.layout.simple_spinner_item,
                        teacherNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    teacherSpinner.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Failed to load teachers: ${error.message}")
                }
            })
    }

    // AddUserContract.View implementation
    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showError(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        currentToast?.show()
    }

    override fun showSuccess(message: String) {
        cancelCurrentToast()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        currentToast?.show()
    }

    override fun getEmail(): String {
        return emailEditText.text.toString().trim()
    }

    override fun getPassword(): String {
        return passwordEditText.text.toString()
    }

    override fun getName(): String {
        return nameEditText.text.toString().trim()
    }

    override fun getRole(): String {
        return when (roleRadioGroup.checkedRadioButtonId) {
            R.id.teacherRadioButton -> "TEACHER"
            R.id.studentRadioButton -> "STUDENT"
            else -> ""
        }
    }

    override fun getSelectedTeacherId(): String {
        val position = teacherSpinner.selectedItemPosition
        return if (position > 0 && position < teacherIds.size) {
            teacherIds[position]
        } else {
            ""
        }
    }

    override fun clearInputs() {
        emailEditText.text.clear()
        passwordEditText.text.clear()
        nameEditText.text.clear()
        roleRadioGroup.clearCheck()
        teacherSpinner.setSelection(0)
        teacherSelectionLayout.visibility = View.GONE
    }

    override fun setAddButtonEnabled(enabled: Boolean) {
        addUserButton.isEnabled = enabled
    }

    override fun navigateBack() {
        finish()
    }

    private fun cancelCurrentToast() {
        currentToast?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detach()
        cancelCurrentToast()
    }
}
