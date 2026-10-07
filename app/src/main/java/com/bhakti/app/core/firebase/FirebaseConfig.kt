package com.bhakti.app.core.firebase

import android.content.Context
import com.bhakti.app.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.storage.FirebaseStorage

/**
 * Manually initializes Firebase from BuildConfig fields (sourced from the
 * git-ignored firebase.properties - see app/build.gradle.kts) instead of the
 * usual google-services.json + Gradle plugin. Deliberately not using that
 * plugin: it would hard-fail the whole build whenever the config file is
 * absent (every fresh checkout, CI, any machine not cutting a backend-wired
 * build), whereas this degrades to "Firebase unavailable" and
 * [com.bhakti.app.core.di.AppContainer] falls back to the local
 * [com.bhakti.app.data.repository.FakeContentRepository].
 */
object FirebaseConfig {

    /** True once real project config is present - [isAvailable] gates every call site below. */
    val isAvailable: Boolean =
        BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() &&
            BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FIREBASE_APP_ID.isNotBlank()

    private var initialized = false

    /** Idempotent - safe to call from anywhere that needs Firestore/Storage before using them. */
    @Synchronized
    fun ensureInitialized(context: Context) {
        if (!isAvailable || initialized) return
        val options = FirebaseOptions.Builder()
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
            .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
            .build()
        FirebaseApp.initializeApp(context, options)
        initialized = true
    }

    fun firestore(context: Context): FirebaseFirestore {
        ensureInitialized(context)
        return FirebaseFirestore.getInstance().apply {
            // Firestore's own offline cache - content stays browsable (if
            // previously loaded) with a flaky connection or no connection at
            // all, matching how the bundled-resource version never needed
            // network in the first place.
            firestoreSettings = firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings {})
            }
        }
    }

    fun storage(context: Context): FirebaseStorage {
        ensureInitialized(context)
        return FirebaseStorage.getInstance()
    }
}
