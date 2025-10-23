package com.nenquit.tapntrack.mvp.users

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.utils.FirebaseHelper

/**
 * UserDetailsPresenter: Handles user details and attendance business logic.
 * Manages loading user profile, attendance records, and user statistics.
 */
class UserDetailsPresenter : UserDetailsContract.Presenter {
    private var view: UserDetailsContract.View? = null
    private val firebaseHelper: FirebaseHelper = FirebaseHelper()
    private var currentUser: User? = null
    private var attendanceRecords: List<Track> = emptyList()

    override fun attach(view: UserDetailsContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun loadUserDetails(userId: String) {
        view?.showLoading()

        firebaseHelper.getUserReference(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    view?.hideLoading()
                    if (snapshot.exists()) {
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val userData = snapshot.value as? Map<String, Any> ?: return
                            currentUser = User.fromMap(userData)
                            currentUser?.let { view?.displayUserProfile(it) }
                        } catch (e: Exception) {
                            view?.showError("Failed to load user profile: ${e.message}")
                        }
                    } else {
                        view?.showError("User not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.hideLoading()
                    view?.showError("Failed to load user profile: ${error.message}")
                }
            })
    }

    override fun loadAttendanceRecords(userId: String) {
        firebaseHelper.getUserTracksReference(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val records = mutableListOf<Track>()
                            for (trackSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val trackData = trackSnapshot.value as? Map<String, Any> ?: continue
                                records.add(Track.fromMap(trackData))
                            }
                            // Sort by timestamp descending (newest first)
                            attendanceRecords = records.sortedByDescending { it.timestamp }
                            view?.displayAttendanceRecords(attendanceRecords)
                        } catch (e: Exception) {
                            view?.showError("Failed to load attendance records: ${e.message}")
                        }
                    } else {
                        attendanceRecords = emptyList()
                        view?.displayAttendanceRecords(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showError("Failed to load attendance records: ${error.message}")
                }
            })
    }

    override fun calculateUserStatistics(userId: String) {
        currentUser?.let { user ->
            val totalAttendance = attendanceRecords.size
            val attendanceRate = user.attendanceRate
            val lastSeen = user.lastLoginTime
            val totalLogins = user.loginCount

            view?.displayUserStatistics(totalAttendance, attendanceRate, lastSeen, totalLogins)
        }
    }

    override fun deactivateUser(userId: String) {
        view?.showLoading()
        firebaseHelper.getUserReference(userId)
            .updateChildren(mapOf("isActive" to false))
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("User deactivated successfully")
                loadUserDetails(userId)
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to deactivate user: ${exception.message}")
            }
    }

    override fun activateUser(userId: String) {
        view?.showLoading()
        firebaseHelper.getUserReference(userId)
            .updateChildren(mapOf("isActive" to true))
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("User activated successfully")
                loadUserDetails(userId)
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to activate user: ${exception.message}")
            }
    }

    override fun deleteUser(userId: String) {
        view?.showLoading()
        firebaseHelper.getUserReference(userId)
            .removeValue()
            .addOnSuccessListener {
                view?.hideLoading()
                view?.showSuccess("User deleted successfully")
                view?.navigateBack()
            }
            .addOnFailureListener { exception ->
                view?.hideLoading()
                view?.showError("Failed to delete user: ${exception.message}")
            }
    }

    override fun onBackPressed() {
        view?.navigateBack()
    }
}
