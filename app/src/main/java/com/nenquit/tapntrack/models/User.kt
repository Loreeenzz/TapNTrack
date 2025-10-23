package com.nenquit.tapntrack.models

/**
 * User data model for Firebase Realtime Database.
 * Represents a user account with basic information and activity tracking.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "STUDENT", // ADMIN, TEACHER, STUDENT
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val lastLoginTime: Long = 0L,
    val loginCount: Int = 0,
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
            "isActive" to isActive,
            "createdAt" to createdAt,
            "lastLoginTime" to lastLoginTime,
            "loginCount" to loginCount,
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
                isActive = (data["isActive"] as? Boolean) ?: true,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                lastLoginTime = (data["lastLoginTime"] as? Number)?.toLong() ?: 0L,
                loginCount = (data["loginCount"] as? Number)?.toInt() ?: 0,
                attendanceRate = (data["attendanceRate"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
