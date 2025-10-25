package com.nenquit.tapntrack.mvp.adduser

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.nenquit.tapntrack.models.User
import java.util.UUID

/**
 * AddUserPresenter: Handles add user business logic for admin.
 * Manages creation of teacher and student accounts via admin panel.
 *
 * IMPORTANT: This app is ADMIN-ONLY. Teachers and students are tracked but cannot log in.
 * Passwords are auto-generated since users don't need to authenticate.
 */
class AddUserPresenter : AddUserContract.Presenter {
    private var view: AddUserContract.View? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://tap-n-track-d0310-default-rtdb.asia-southeast1.firebasedatabase.app")

    // Store admin credentials for re-authentication after creating users
    private var adminEmail: String? = null
    private var adminPassword: String? = null

    override fun attach(view: AddUserContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
        // Clear sensitive data
        adminEmail = null
        adminPassword = null
    }

    /**
     * Set admin credentials for re-authentication.
     * CRITICAL: Firebase's createUserWithEmailAndPassword() logs in the new user,
     * replacing the admin's session. We must re-authenticate the admin after.
     */
    fun setAdminCredentials(email: String, password: String) {
        this.adminEmail = email
        this.adminPassword = password
    }

    override fun onAddUserClicked() {
        val email = view?.getEmail() ?: return
        val password = view?.getPassword() ?: return
        val name = view?.getName() ?: return
        val role = view?.getRole() ?: return
        val teacherId = view?.getSelectedTeacherId() ?: ""

        // Validate all inputs
        if (email.isEmpty()) {
            view?.showError("Email address is required.")
            return
        }

        // Auto-generate password if not provided (since users won't log in)
        val actualPassword = password.ifEmpty {
            generateSecurePassword()
        }

        if (name.isEmpty()) {
            view?.showError("Full name is required.")
            return
        }

        if (role.isEmpty()) {
            view?.showError("Please select a role.")
            return
        }

        if (role == "STUDENT" && teacherId.isEmpty()) {
            view?.showError("Please select a teacher for this student.")
            return
        }

        if (!isValidEmail(email)) {
            view?.showError("Please enter a valid email address.")
            return
        }

        if (!isValidPassword(actualPassword)) {
            view?.showError("Password must be at least 6 characters long.")
            return
        }

        if (!isValidName(name)) {
            view?.showError("Full name must be between 2 and 50 characters.")
            return
        }

        // Create user with admin privileges
        createUserAsAdmin(email, actualPassword, name, role, if (role == "STUDENT") teacherId else null)
    }

    /**
     * Generate a secure random password for users who won't be logging in
     */
    private fun generateSecurePassword(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }

    override fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    override fun isValidName(name: String): Boolean {
        return name.length >= 2 && name.length <= 50
    }

    override fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    override fun loadTeachers() {
        // Load list of all teachers for dropdown selection
        @Suppress("UNCHECKED_CAST")
        database.getReference("users").orderByChild("role").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val usersData = snapshot.value as? Map<String, Any>
                    usersData?.filter { (_, userData) ->
                        val data = userData as? Map<String, Any>
                        data?.get("role") == "TEACHER"
                    }
                    // Notify view with teacher list (implementation depends on view)
                }
            }
    }

    /**
     * Create user account with admin privileges
     * CRITICAL BUG FIX: createUserWithEmailAndPassword() automatically logs in the new user,
     * which replaces the admin's session. We must re-authenticate as admin after creation.
     *
     * @param email User email
     * @param password User password
     * @param name User full name
     * @param role User role (TEACHER or STUDENT)
     * @param teacherId Teacher ID if role is STUDENT
     */
    private fun createUserAsAdmin(
        email: String,
        password: String,
        name: String,
        role: String,
        teacherId: String?
    ) {
        view?.showLoading()
        view?.setAddButtonEnabled(false)

        // Create the new user account
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newUserId = auth.currentUser?.uid
                    if (newUserId == null) {
                        view?.hideLoading()
                        view?.setAddButtonEnabled(true)
                        view?.showError("User created but ID could not be retrieved.")
                        return@addOnCompleteListener
                    }

                    // Create user object with role
                    val userData = User(
                        uid = newUserId,
                        email = email,
                        name = name,
                        role = role,
                        teacherId = teacherId,
                        createdAt = System.currentTimeMillis()
                    )

                    // Store user data in database
                    database.getReference("users").child(newUserId).setValue(userData.toMap())
                        .addOnSuccessListener {
                            // CRITICAL: Re-authenticate as admin to restore admin session
                            reAuthenticateAdmin(name, role)
                        }
                        .addOnFailureListener { exception ->
                            view?.hideLoading()
                            view?.setAddButtonEnabled(true)
                            view?.showError("Failed to save user data: ${exception.message}")
                            // Try to re-authenticate admin anyway
                            reAuthenticateAdminSilently()
                        }
                } else {
                    view?.hideLoading()
                    view?.setAddButtonEnabled(true)
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> "This email address is already in use."
                        else -> exception?.message ?: "Failed to create user. Please try again."
                    }
                    view?.showError(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.setAddButtonEnabled(true)
                view?.showError(exception.message ?: "An error occurred during user creation.")
            }
    }

    /**
     * Re-authenticate as admin after creating a new user.
     * This restores the admin's session which was replaced by the new user.
     */
    private fun reAuthenticateAdmin(createdUserName: String, createdUserRole: String) {
        if (adminEmail == null || adminPassword == null) {
            view?.hideLoading()
            view?.setAddButtonEnabled(true)
            view?.showError("User created but admin session lost. Please log in again.")
            Handler(Looper.getMainLooper()).postDelayed({
                view?.navigateBack()
            }, 3000)
            return
        }

        auth.signInWithEmailAndPassword(adminEmail!!, adminPassword!!)
            .addOnSuccessListener {
                view?.hideLoading()
                view?.setAddButtonEnabled(true)
                view?.clearInputs()
                view?.showSuccess("User '$createdUserName' created successfully as $createdUserRole.")

                // Delay navigation to allow user to see the success message
                Handler(Looper.getMainLooper()).postDelayed({
                    view?.navigateBack()
                }, 2000)
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.setAddButtonEnabled(true)
                view?.showError("User created but admin re-login failed: ${exception.message}. Please log in again.")
                Handler(Looper.getMainLooper()).postDelayed({
                    view?.navigateBack()
                }, 3000)
            }
    }

    /**
     * Re-authenticate admin silently without showing success message
     */
    private fun reAuthenticateAdminSilently() {
        if (adminEmail != null && adminPassword != null) {
            auth.signInWithEmailAndPassword(adminEmail!!, adminPassword!!)
                .addOnFailureListener {
                    // Silent failure - admin will need to log in again
                }
        }
    }
}
