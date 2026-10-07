package com.bhakti.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.bhakti.app.core.di.AppContainer

class BhaktiApplication : Application(), ImageLoaderFactory {
    val container by lazy { AppContainer(this) }

    /**
     * The app-wide Coil image loader (Coil finds it via [ImageLoaderFactory]).
     * Backend artwork URLs are immutable - replacing an image in Storage
     * gives it a new token URL - so once an image is on the phone it never
     * needs fetching again: ignore server cache headers and keep a generous
     * disk cache, so every screen after the first loads art instantly.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this).maxSizePercent(0.25).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
}
