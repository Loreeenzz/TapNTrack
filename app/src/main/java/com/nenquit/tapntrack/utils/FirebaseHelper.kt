package com.nenquit.tapntrack.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

/**
 * FirebaseHelper: Utility class for managing Firebase Realtime Database references.
 * Provides centralized access to database nodes for users, tracks, and other data.
 */
@Suppress("unused")
class FirebaseHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance("https://tapntrack-00011-default-rtdb.asia-southeast1.firebasedatabase.app")

    /**
     * Get a reference to any path in the database
     * @param path The database path
     * @return DatabaseReference for the specified path
     */
    fun getDatabaseReference(path: String): DatabaseReference {
        return database.getReference(path)
    }

    /**
     * Get a reference to the users node
     * @return DatabaseReference to "users" node
     */
    fun getUsersReference(): DatabaseReference {
        return database.getReference("users")
    }

    /**
     * Get a reference to a specific user by UID
     * @param uid The user's unique identifier
     * @return DatabaseReference to user's data node
     */
    fun getUserReference(uid: String): DatabaseReference {
        return database.getReference("users/$uid")
    }

    /**
     * Get a reference to the tracks node
     * @return DatabaseReference to "tracks" node
     */
    fun getTracksReference(): DatabaseReference {
        return database.getReference("tracks")
    }

    /**
     * Get a reference to a specific track/log by ID
     * @param trackId The track's unique identifier
     * @return DatabaseReference to track's data node
     */
    fun getTrackReference(trackId: String): DatabaseReference {
        return database.getReference("tracks/$trackId")
    }
}
