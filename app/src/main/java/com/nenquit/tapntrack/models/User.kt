package com.nenquit.tapntrack.models

/**
 * User data model for Firebase Realtime Database.
 * Represents a user account with basic information and activity tracking.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "STUDENT", // ADMIN: manages all users | TEACHER: manages students | STUDENT: role badge only
    val teacherId: String? = null, // Teacher UID if user is a STUDENT, null for ADMIN and TEACHER
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val attendanceRate: Double = 0.0
) {
    /**
     * Convert User to HashMap for Firebase storage
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "name" to name,
            "role" to role,
            "teacherId" to (teacherId ?: ""),
            "isActive" to isActive,
            "createdAt" to createdAt,
            "attendanceRate" to attendanceRate
        )
    }

    companion object {
        /**
         * Create User from HashMap (Firebase data)
         */
        fun fromMap(data: Map<String, Any>): User {
            return User(
                uid = data["uid"] as? String ?: "",
                email = data["email"] as? String ?: "",
                name = data["name"] as? String ?: "",
                role = data["role"] as? String ?: "STUDENT",
                teacherId = (data["teacherId"] as? String)?.takeIf { it.isNotEmpty() },
                isActive = (data["isActive"] as? Boolean) ?: true,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                attendanceRate = (data["attendanceRate"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
