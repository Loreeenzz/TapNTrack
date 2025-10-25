package com.nenquit.tapntrack.activity.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.nenquit.tapntrack.R
import com.nenquit.tapntrack.activity.login.LoginActivity
import com.nenquit.tapntrack.fragment.HomeFragment
import com.nenquit.tapntrack.fragment.LogsFragment
import com.nenquit.tapntrack.fragment.SettingsFragment
import com.nenquit.tapntrack.fragment.UsersFragment
import com.nenquit.tapntrack.utils.SessionManager

/**
 * DashboardActivity: Main activity after successful login.
 * Hosts fragments for Home, Logs, Users, and Settings screens.
 */
class DashboardActivity : FragmentActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Auth and Session Manager
        firebaseAuth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

        // Check if user is logged in
        if (firebaseAuth.currentUser == null) {
            navigateToLogin()
            return
        }

        // Initialize UI components
        initializeUIComponents()

        // Set up bottom navigation
        setupBottomNavigation()

        // Set up back press handling
        setupBackPressedHandler()

        // Load default fragment (Home) if no saved state
        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance())
            bottomNavigation.selectedItemId = R.id.menu_home
        }
    }

    /**
     * Initialize UI components
     */
    private fun initializeUIComponents() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    /**
     * Set up bottom navigation item selection listener
     */
    private fun setupBottomNavigation() {
        // Hide users menu for students
        val userRole = sessionManager.getUserRole()
        if (userRole == "STUDENT") {
            val usersMenu = bottomNavigation.menu.findItem(R.id.menu_users)
            usersMenu?.isVisible = false
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    loadFragment(HomeFragment.newInstance())
                    true
                }
                R.id.menu_logs -> {
                    loadFragment(LogsFragment.newInstance())
                    true
                }
                R.id.menu_users -> {
                    loadFragment(UsersFragment.newInstance())
                    true
                }
                R.id.menu_settings -> {
                    loadFragment(SettingsFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Set up back press handler using OnBackPressedDispatcher
     */
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragmentCount = supportFragmentManager.backStackEntryCount
                if (fragmentCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    // At root fragment, close the app
                    finish()
                }
            }
        })
    }

    /**
     * Load and display a fragment
     * @param fragment The fragment to load
     */
    private fun loadFragment(fragment: Fragment) {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load fragment: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Navigate to login screen
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
