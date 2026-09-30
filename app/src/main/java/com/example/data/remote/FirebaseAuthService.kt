package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Service managing Firebase Authentication (Google Sign-In) and Firestore data persistence.
 * Operates with graceful offline fallback when Firebase project credentials are not yet linked.
 */
class FirebaseAuthService(private val context: Context) {

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (e: Exception) {
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
        } catch (e: Exception) {
            null
        }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    val isUserSignedIn: Boolean
        get() = currentUser != null

    /**
     * Signs in anonymously or returns existing session
     */
    suspend fun ensureAuthenticated(): FirebaseUser? {
        val authInstance = auth ?: return null
        return try {
            if (authInstance.currentUser != null) {
                authInstance.currentUser
            } else {
                val result = authInstance.signInAnonymously().await()
                result.user
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Firebase Auth sign-in failed (offline/fallback mode)", e)
            null
        }
    }

    /**
     * Syncs user profile progress to Firestore
     */
    suspend fun syncProfileToFirestore(profile: UserProfile) {
        val db = firestore ?: return
        val user = currentUser ?: return

        try {
            val userMap = hashMapOf(
                "name" to profile.name,
                "email" to profile.email,
                "currentLevel" to profile.currentLevel,
                "levelTitle" to profile.levelTitle,
                "xpPoints" to profile.xpPoints,
                "streakDays" to profile.streakDays,
                "lessonsCompletedCount" to profile.lessonsCompletedCount,
                "quizzesTakenCount" to profile.quizzesTakenCount,
                "averageQuizScore" to profile.averageQuizScore,
                "dailyLearnedMinutes" to profile.dailyLearnedMinutes,
                "dailyTargetMinutes" to profile.dailyTargetMinutes,
                "preferredLanguage" to profile.preferredLanguage.code,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(user.uid)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d("FirebaseAuthService", "Firestore profile sync success for user: ${user.uid}")
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Firestore sync skipped: ${e.message}")
        }
    }

    /**
     * Fetches remote profile from Firestore if available
     */
    suspend fun fetchProfileFromFirestore(): UserProfile? {
        val db = firestore ?: return null
        val user = currentUser ?: return null

        return try {
            val snapshot = db.collection("users").document(user.uid).get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("name") ?: "IKS Scholar"
                val email = snapshot.getString("email") ?: ""
                val currentLevel = snapshot.getLong("currentLevel")?.toInt() ?: 1
                val levelTitle = snapshot.getString("levelTitle") ?: "Brahmachari (Seeker)"
                val xpPoints = snapshot.getLong("xpPoints")?.toInt() ?: 0
                val streakDays = snapshot.getLong("streakDays")?.toInt() ?: 1
                val lessonsCompleted = snapshot.getLong("lessonsCompletedCount")?.toInt() ?: 0
                val quizzesTaken = snapshot.getLong("quizzesTakenCount")?.toInt() ?: 0
                val avgScore = snapshot.getLong("averageQuizScore")?.toInt() ?: 0
                val dailyLearned = snapshot.getLong("dailyLearnedMinutes")?.toInt() ?: 0
                val dailyTarget = snapshot.getLong("dailyTargetMinutes")?.toInt() ?: 20

                UserProfile(
                    name = name,
                    email = email,
                    currentLevel = currentLevel,
                    levelTitle = levelTitle,
                    xpPoints = xpPoints,
                    streakDays = streakDays,
                    lessonsCompletedCount = lessonsCompleted,
                    quizzesTakenCount = quizzesTaken,
                    averageQuizScore = avgScore,
                    dailyLearnedMinutes = dailyLearned,
                    dailyTargetMinutes = dailyTarget
                )
            } else null
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Firestore fetch skipped: ${e.message}")
            null
        }
    }
}
