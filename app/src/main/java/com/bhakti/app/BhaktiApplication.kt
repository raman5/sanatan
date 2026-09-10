package com.bhakti.app

import android.app.Application
import com.bhakti.app.core.di.AppContainer

class BhaktiApplication : Application() {
    val container by lazy { AppContainer(this) }
}
