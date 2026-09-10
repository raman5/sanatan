package com.bhakti.app.core.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.bhakti.app.core.session.SessionManager
import com.bhakti.app.data.repository.AuthRepository
import com.bhakti.app.data.repository.ContentRepository
import com.bhakti.app.data.repository.FakeAuthRepository
import com.bhakti.app.data.repository.FakeContentRepository
import com.bhakti.app.data.repository.FakeUpiPaymentRepository
import com.bhakti.app.data.repository.FavouritesRepository
import com.bhakti.app.data.repository.PaymentRepository
import com.bhakti.app.notifications.AndroidNotificationScheduler
import com.bhakti.app.notifications.NotificationScheduler

/** Hand-rolled composition root - small enough that Hilt/Koin would be overhead. */
class AppContainer(context: Context) {
    val sessionManager = SessionManager(context)
    val authRepository: AuthRepository = FakeAuthRepository()
    val paymentRepository: PaymentRepository = FakeUpiPaymentRepository()
    val contentRepository: ContentRepository = FakeContentRepository()
    val favouritesRepository = FavouritesRepository(sessionManager, contentRepository)
    val notificationScheduler: NotificationScheduler = AndroidNotificationScheduler(context)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided - wrap the app in CompositionLocalProvider(LocalAppContainer provides ...)")
}
