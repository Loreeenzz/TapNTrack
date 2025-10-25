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
        // Query tracks where userId equals the user's UID
        firebaseHelper.getTracksReference()
            .orderByChild("userId")
            .equalTo(userId)
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
                            // Sort by timeIn descending (newest first)
                            attendanceRecords = records.sortedByDescending { it.timeIn }
                            view?.displayAttendanceRecords(attendanceRecords)

                            // Calculate statistics AFTER records are loaded
                            calculateUserStatistics(userId)
                        } catch (e: Exception) {
                            view?.showError("Failed to load attendance records: ${e.message}")
                        }
                    } else {
                        attendanceRecords = emptyList()
                        view?.displayAttendanceRecords(emptyList())

                        // Still calculate statistics even if no records
                        calculateUserStatistics(userId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showError("Failed to load attendance records: ${error.message}")
                }
            })
    }

    override fun calculateUserStatistics(userId: String) {
        currentUser?.let { user ->
            // Total Attendance: Count of all attendance records
            val totalAttendance = attendanceRecords.size

            // Attendance Rate: Calculate based on PRESENT and LATE vs total records
            val attendanceRate = if (attendanceRecords.isNotEmpty()) {
                val presentCount = attendanceRecords.count {
                    it.status == "PRESENT" || it.status == "LATE"
                }
                (presentCount.toDouble() / attendanceRecords.size) * 100.0
            } else {
                0.0
            }

            // Last Seen: Get the most recent timeIn from attendance records
            val lastSeen = if (attendanceRecords.isNotEmpty()) {
                attendanceRecords.maxOfOrNull { it.timeIn } ?: 0L
            } else {
                0L
            }

            // Update user's attendance rate in Firebase for future reference
            if (attendanceRecords.isNotEmpty()) {
                firebaseHelper.getUserReference(userId)
                    .updateChildren(mapOf("attendanceRate" to attendanceRate))
            }

            view?.displayUserStatistics(totalAttendance, attendanceRate, lastSeen)
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
