package com.nenquit.tapntrack.mvp.dashboard

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nenquit.tapntrack.models.Track
import com.nenquit.tapntrack.models.User
import com.nenquit.tapntrack.utils.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.*

/**
 * DashboardPresenter: Handles dashboard business logic.
 * Fetches and processes data from Firebase for dashboard display.
 */
class DashboardPresenter : DashboardContract.Presenter {
    private var view: DashboardContract.View? = null
    private val firebaseHelper: FirebaseHelper = FirebaseHelper()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("EEEE, MMM dd, yyyy - hh:mm a", Locale.getDefault())

    override fun attach(view: DashboardContract.View) {
        this.view = view
    }

    override fun detach() {
        this.view = null
    }

    override fun loadDashboardData() {
        view?.showLoading()
        updateDateTime()
        loadTodayAttendance()
        loadQuickStats()
        loadRecentActivity()
    }

    override fun refreshData() {
        loadDashboardData()
    }

    override fun onViewAllLogsClick() {
        view?.navigateToLogs()
    }

    override fun onViewAllUsersClick() {
        view?.navigateToUsers()
    }

    override fun onRecentActivityClick(log: Track) {
        view?.navigateToLogDetails(log)
    }

    /**
     * Update current date time display
     */
    private fun updateDateTime() {
        val currentDateTime = dateTimeFormat.format(Date())
        view?.updateDateTime(currentDateTime)
    }

    /**
     * Load today's attendance statistics
     */
    private fun loadTodayAttendance() {
        val today = dateFormat.format(Date())

        firebaseHelper.getTracksReference()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val todayLogs = mutableListOf<Track>()

                            for (logSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val logData = logSnapshot.value as? Map<String, Any> ?: continue
                                val track = Track.fromMap(logData)

                                // Filter for today's logs
                                if (track.date == today) {
                                    todayLogs.add(track)
                                }
                            }

                            // Count by status
                            var presentCount = 0
                            var lateCount = 0
                            var absentCount = 0

                            // Use a set to track unique students (by RFID or name)
                            val uniqueStudents = mutableSetOf<String>()

                            for (log in todayLogs) {
                                val studentId = log.rfidTag.ifEmpty { log.studentName }
                                uniqueStudents.add(studentId)

                                when (log.status) {
                                    "PRESENT" -> presentCount++
                                    "LATE" -> lateCount++
                                    "ABSENT" -> absentCount++
                                }
                            }

                            val totalStudents = uniqueStudents.size
                            view?.displayTodayAttendance(presentCount, lateCount, absentCount, totalStudents)

                        } catch (e: Exception) {
                            view?.showError("Failed to load today's attendance: ${e.message}")
                        }
                    } else {
                        view?.displayTodayAttendance(0, 0, 0, 0)
                    }
                    view?.hideLoading()
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.hideLoading()
                    view?.showError("Failed to load attendance data: ${error.message}")
                }
            })
    }

    /**
     * Load quick statistics (total students, teachers, logs this week)
     */
    private fun loadQuickStats() {
        // Load total students and teachers
        firebaseHelper.getUsersReference()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            var totalStudents = 0
                            var totalTeachers = 0

                            for (userSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val userData = userSnapshot.value as? Map<String, Any> ?: continue
                                val user = User.fromMap(userData)

                                if (user.isActive) {
                                    when (user.role) {
                                        "STUDENT" -> totalStudents++
                                        "TEACHER" -> totalTeachers++
                                    }
                                }
                            }

                            // Now load logs this week
                            loadLogsThisWeek(totalStudents, totalTeachers)

                        } catch (e: Exception) {
                            view?.showError("Failed to load user statistics: ${e.message}")
                        }
                    } else {
                        loadLogsThisWeek(0, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showError("Failed to load user data: ${error.message}")
                }
            })
    }

    /**
     * Load total logs for this week
     */
    private fun loadLogsThisWeek(totalStudents: Int, totalTeachers: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        firebaseHelper.getTracksReference()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            var logsThisWeek = 0

                            for (logSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val logData = logSnapshot.value as? Map<String, Any> ?: continue
                                val track = Track.fromMap(logData)

                                if (track.timeIn >= startOfWeek) {
                                    logsThisWeek++
                                }
                            }

                            view?.displayQuickStats(totalStudents, totalTeachers, logsThisWeek)

                        } catch (e: Exception) {
                            view?.showError("Failed to load log statistics: ${e.message}")
                        }
                    } else {
                        view?.displayQuickStats(totalStudents, totalTeachers, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showError("Failed to load log data: ${error.message}")
                }
            })
    }

    /**
     * Load recent activity (last 5 logs)
     */
    private fun loadRecentActivity() {
        firebaseHelper.getTracksReference()
            .orderByChild("timeIn")
            .limitToLast(5)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            val recentLogs = mutableListOf<Track>()

                            for (logSnapshot in snapshot.children) {
                                @Suppress("UNCHECKED_CAST")
                                val logData = logSnapshot.value as? Map<String, Any> ?: continue
                                recentLogs.add(Track.fromMap(logData))
                            }

                            // Reverse to show most recent first
                            recentLogs.reverse()

                            view?.displayRecentActivity(recentLogs)

                        } catch (e: Exception) {
                            view?.showError("Failed to load recent activity: ${e.message}")
                        }
                    } else {
                        view?.displayRecentActivity(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    view?.showError("Failed to load recent activity: ${error.message}")
                }
            })
    }
}
