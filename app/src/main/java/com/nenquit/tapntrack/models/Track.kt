package com.nenquit.tapntrack.models

import java.text.SimpleDateFormat
import java.util.*

/**
 * Track data model for Firebase Realtime Database.
 * Represents an attendance/RFID tracking record with time-in and time-out.
 *
 * This model is used for:
 * - RFID school ID scans
 * - Student attendance tracking
 * - Time-in and time-out logging
 */
data class Track(
    val id: String = "",
    val userId: String = "", // User UID (student or teacher who tapped RFID)
    val studentName: String = "",
    val rfidTag: String = "", // RFID card number
    val timeIn: Long = 0L, // Timestamp in milliseconds
    val timeOut: Long? = null, // Timestamp in milliseconds (null if not yet out)
    val date: String = "", // Format: yyyy-MM-dd for easy filtering
    val status: String = "PRESENT", // PRESENT, LATE, ABSENT, HALF_DAY
    val teacherId: String = "", // Assigned teacher's UID
    val location: String = "", // e.g., "Main Gate", "Classroom A"
    val remarks: String = "", // Optional notes
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Legacy fields for backward compatibility
    @Deprecated("Use timeIn instead") val timestamp: Long = 0L,
    @Deprecated("Use status instead") val eventType: String = "",
    @Deprecated("Use remarks instead") val title: String = "",
    @Deprecated("Use remarks instead") val description: String = ""
) {
    /**
     * Calculate duration between time-in and time-out in milliseconds
     * Returns null if student hasn't timed out yet
     */
    fun getDuration(): Long? {
        return timeOut?.let { it - timeIn }
    }

    /**
     * Get formatted duration string (e.g., "8h 30m")
     */
    fun getFormattedDuration(): String {
        val duration = getDuration() ?: return "In Progress"
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }

    /**
     * Check if student is still in (no time-out)
     */
    fun isStillIn(): Boolean {
        return timeOut == null
    }

    /**
     * Convert to HashMap for Firebase
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "studentName" to studentName,
            "rfidTag" to rfidTag,
            "timeIn" to timeIn,
            "timeOut" to timeOut,
            "date" to date,
            "status" to status,
            "teacherId" to teacherId,
            "location" to location,
            "remarks" to remarks,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        /**
         * Create Track from HashMap (Firebase data)
         */
        fun fromMap(data: Map<String, Any>): Track {
            // Read userId from Firebase, fallback to studentId for backward compatibility
            val userId = data["userId"] as? String ?: data["studentId"] as? String ?: ""

            return Track(
                id = data["id"] as? String ?: "",
                userId = userId,
                studentName = data["studentName"] as? String ?: "",
                rfidTag = data["rfidTag"] as? String ?: "",
                timeIn = (data["timeIn"] as? Number)?.toLong() ?: (data["timestamp"] as? Number)?.toLong() ?: 0L,
                timeOut = (data["timeOut"] as? Number)?.toLong(),
                date = data["date"] as? String ?: getTodayDate(),
                status = data["status"] as? String ?: "PRESENT",
                teacherId = data["teacherId"] as? String ?: "",
                location = data["location"] as? String ?: "",
                remarks = data["remarks"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                // Legacy fields
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
                eventType = data["eventType"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: ""
            )
        }

        /**
         * Get today's date in yyyy-MM-dd format
         */
        fun getTodayDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        /**
         * Format timestamp to readable time (e.g., "08:15 AM")
         */
        fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        /**
         * Format date to readable format (e.g., "Oct 25, 2025")
         */
        fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
