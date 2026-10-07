import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing credentials live in keystore.properties (git-ignored - never commit it).
// Absent on CI/other machines that aren't cutting a release build, so signing
// config below is skipped gracefully rather than failing the whole build.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Firebase project config (backend for images/audio/mantra text/pricing -
// see FirebaseContentRepository) lives in firebase.properties (git-ignored,
// same pattern as keystore.properties above - never commit it). Absent on a
// fresh checkout, so these BuildConfig fields come back blank and
// AppContainer falls back to FakeContentRepository rather than crashing.
val firebasePropertiesFile = rootProject.file("firebase.properties")
val firebaseProperties = Properties().apply {
    if (firebasePropertiesFile.exists()) {
        firebasePropertiesFile.inputStream().use { load(it) }
    }
}
fun firebaseProp(key: String): String = firebaseProperties.getProperty(key, "")

android {
    namespace = "com.bhakti.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bhaktt.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Off until real payments (Razorpay) are integrated - while off, the paywall and
        // payment screens are never shown and every user gets the full app.
        buildConfigField("boolean", "SUBSCRIPTIONS_ENABLED", "false")

        buildConfigField("String", "FIREBASE_API_KEY", "\"${firebaseProp("apiKey")}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${firebaseProp("appId")}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${firebaseProp("projectId")}\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"${firebaseProp("storageBucket")}\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
