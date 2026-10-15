package com.bhakti.app.data.repository

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.bhakti.app.BuildConfig
import com.bhakti.app.core.firebase.FirebaseConfig
import com.bhakti.app.data.model.AuthMethod
import com.bhakti.app.data.model.User
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/** Thrown when the user backs out of the Google account picker - not an error to show. */
class SignInCancelledException : Exception("Sign-in cancelled")

interface AuthRepository {
    /** Shows the Google account picker; needs an Activity to host it. */
    suspend fun signInWithGoogle(activity: Activity): Result<User>

    /** Firebase uid of the signed-in user, or null. */
    fun currentUserId(): String?

    suspend fun signOut()
}

/**
 * Google sign-in via Credential Manager, exchanged for a Firebase Auth
 * session. The Firebase uid is what the payment functions tie a
 * subscription to (see functions/index.js).
 */
class FirebaseAuthRepository(private val context: Context) : AuthRepository {

    private val auth get() = FirebaseConfig.auth(context)

    override suspend fun signInWithGoogle(activity: Activity): Result<User> = runCatching {
        check(BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) {
            "Google sign-in isn't configured for this build (webClientId missing from firebase.properties)."
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build())
            .build()

        val credential = try {
            CredentialManager.create(activity).getCredential(activity, request).credential
        } catch (e: GetCredentialCancellationException) {
            throw SignInCancelledException()
        } catch (e: NoCredentialException) {
            throw IllegalStateException("No Google account found on this phone. Add one in Settings and try again.")
        } catch (e: GetCredentialException) {
            throw IllegalStateException("Google sign-in failed. Please try again.", e)
        }

        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected sign-in response. Please try again."
        }
        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val firebaseUser = auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user
            ?: error("Google sign-in failed. Please try again.")

        User(
            id = firebaseUser.uid,
            displayName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Bhakt",
            email = firebaseUser.email,
            authMethod = AuthMethod.GOOGLE,
            isNewAccount = false
        )
    }

    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun signOut() {
        auth.signOut()
        // Forget the chosen account so the picker shows again next time.
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }
}

/** Stand-in for builds without firebase.properties (fresh checkouts, CI). */
class FakeAuthRepository : AuthRepository {
    private var userId: String? = null

    override suspend fun signInWithGoogle(activity: Activity): Result<User> {
        delay(500)
        val user = User(
            id = "fake-google-user",
            displayName = "Bhakt",
            email = "devotee@gmail.com",
            authMethod = AuthMethod.GOOGLE,
            isNewAccount = false
        )
        userId = user.id
        return Result.success(user)
    }

    override fun currentUserId(): String? = userId

    override suspend fun signOut() {
        userId = null
    }
}
