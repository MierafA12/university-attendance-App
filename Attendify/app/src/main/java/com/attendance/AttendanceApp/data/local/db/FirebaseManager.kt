package com.attendance.attendanceapp.data.local.db

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Singleton wrapper that provides pre-configured instances of
 * Firebase Auth and Firestore for the Attendify app.
 *
 * Firebase is auto-initialized by the google-services plugin via
 * FirebaseApp.initializeApp() — this class simply exposes the
 * correctly configured SDK instances.
 */
object FirebaseManager {

    /** Firebase Authentication instance */
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    /** Firestore instance with offline persistence enabled */
    val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance().apply {
                // Settings should be set before any other operations
                val cacheSettings = PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()

                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(cacheSettings)
                    .build()
            }
        } catch (e: Exception) {
            // Fallback to default instance if settings fail
            FirebaseFirestore.getInstance()
        }
    }

    // ── Firestore collection paths ─────────────────────────────────────────

    /** Top-level Firestore collection references */
    val usersCollection        get() = firestore.collection("users")
    val departmentsCollection  get() = firestore.collection("departments")
    val coursesCollection      get() = firestore.collection("courses")
    val studentsCollection     get() = firestore.collection("students")
    val teachersCollection     get() = firestore.collection("teachers")
    val schedulesCollection    get() = firestore.collection("schedules")
    val sessionsCollection     get() = firestore.collection("attendance_sessions")
    val attendanceCollection   get() = firestore.collection("attendance_records")
    val notificationsCollection get() = firestore.collection("notifications")

    // ── Auth helpers ───────────────────────────────────────────────────────

    /** The currently signed-in Firebase user, or null if signed out */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    fun signOut() = auth.signOut()
}
