package com.bhakti.app.core.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.bhakti.app.core.firebase.FirebaseConfig
import com.bhakti.app.core.payments.EntitlementSync
import com.bhakti.app.core.session.SessionManager
import com.bhakti.app.data.repository.AuthRepository
import com.bhakti.app.data.repository.ContentRepository
import com.bhakti.app.data.repository.FakeAuthRepository
import com.bhakti.app.data.repository.FakeContentRepository
import com.bhakti.app.data.repository.FakePaymentRepository
import com.bhakti.app.data.repository.FavouritesRepository
import com.bhakti.app.data.repository.FirebaseAuthRepository
import com.bhakti.app.data.repository.FirebaseContentRepository
import com.bhakti.app.data.repository.PaymentRepository
import com.bhakti.app.data.repository.RazorpayPaymentRepository
import com.bhakti.app.notifications.AndroidNotificationScheduler
import com.bhakti.app.notifications.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Hand-rolled composition root - small enough that Hilt/Koin would be overhead. */
class AppContainer(context: Context) {
    /** Lives as long as the process - for background work like [entitlementSync]. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val sessionManager = SessionManager(context)
    // Firebase is only used once firebase.properties gives it real project
    // config (see app/build.gradle.kts and FirebaseConfig) - every checkout
    // without that file keeps working exactly as before, off local stand-ins.
    val authRepository: AuthRepository =
        if (FirebaseConfig.isAvailable) FirebaseAuthRepository(context) else FakeAuthRepository()
    val paymentRepository: PaymentRepository =
        if (FirebaseConfig.isAvailable) RazorpayPaymentRepository(context) else FakePaymentRepository()
    val contentRepository: ContentRepository =
        if (FirebaseConfig.isAvailable) FirebaseContentRepository(context) else FakeContentRepository()
    val favouritesRepository = FavouritesRepository(sessionManager, contentRepository)
    val notificationScheduler: NotificationScheduler = AndroidNotificationScheduler(context)

    val entitlementSync = EntitlementSync(sessionManager, authRepository, paymentRepository).also {
        it.start(applicationScope)
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided - wrap the app in CompositionLocalProvider(LocalAppContainer provides ...)")
}
