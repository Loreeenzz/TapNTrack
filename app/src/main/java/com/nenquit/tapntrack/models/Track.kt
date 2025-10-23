package com.nenquit.tapntrack.models

/**
 * Track data model for Firebase Realtime Database.
 * Represents a tracked activity or attendance record (check-in/check-out).
 */
data class Track(
    val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val title: String = "",
    val description: String = "",
    val eventType: String = "", // CHECK_IN or CHECK_OUT
    val timestamp: Long = 0L,
    val location: String = ""
) {
    /**
     * Convert Track to HashMap for Firebase storage
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "studentName" to studentName,
            "title" to title,
            "description" to description,
            "eventType" to eventType,
            "timestamp" to timestamp,
            "location" to location
        )
    }

    companion object {
        /**
         * Create Track from HashMap (Firebase data)
         */
        fun fromMap(data: Map<String, Any>): Track {
            return Track(
                id = data["id"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                eventType = data["eventType"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
                location = data["location"] as? String ?: ""
            )
        }
    }
}
