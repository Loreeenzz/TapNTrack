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
    }

    /**
     * Save user session after successful login
     * @param email User's email
     * @param uid User's Firebase UID
     */
    fun saveUserSession(email: String, uid: String) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_UID, uid)
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
     * Get saved user email
     * @return User's email or null if not saved
     */
    fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Get saved user UID
     * @return User's UID or null if not saved
     */
    fun getUserUID(): String? {
        return sharedPreferences.getString(KEY_USER_UID, null)
    }

    /**
     * Clear user session (logout)
     */
    fun clearUserSession() {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_UID)
            apply()
        }
    }
}
