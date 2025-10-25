package com.nenquit.tapntrack.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager: Manages user session persistence using SharedPreferences.
 * Handles saving and retrieving user login state across app sessions.
 */
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "TapNTrackSession"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_UID = "user_uid"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_TEACHER_ID = "teacher_id"
    }

    /**
     * Save user session after successful login
     * @param email User's email
     * @param uid User's Firebase UID
     * @param role User's role (ADMIN, TEACHER, STUDENT)
     * @param teacherId Teacher's UID if user is a student, null otherwise
     */
    fun saveUserSession(email: String, uid: String, role: String = "STUDENT", teacherId: String? = null) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_UID, uid)
            putString(KEY_USER_ROLE, role)
            if (teacherId != null) {
                putString(KEY_TEACHER_ID, teacherId)
            }
            apply()
        }
    }

    /**
     * Check if user is logged in
     * @return True if user has a saved session, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Get saved user UID
     * @return User's UID or null if not saved
     */
    fun getUserUID(): String? {
        return sharedPreferences.getString(KEY_USER_UID, null)
    }

    /**
     * Get saved user role
     * @return User's role (ADMIN, TEACHER, STUDENT) or STUDENT if not saved
     */
    fun getUserRole(): String {
        return sharedPreferences.getString(KEY_USER_ROLE, "STUDENT") ?: "STUDENT"
    }

    /**
     * Check if user is admin
     * @return True if user has ADMIN role
     */
    fun isAdmin(): Boolean {
        return getUserRole() == "ADMIN"
    }

    /**
     * Clear user session (logout)
     */
    fun clearUserSession() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_UID)
            remove(KEY_USER_ROLE)
            remove(KEY_TEACHER_ID)
            apply()
        }
    }
}
