package com.bhakti.app.data.repository

import com.bhakti.app.core.session.SessionManager
import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import kotlinx.coroutines.flow.first

data class FavouriteContent(
    val wallpapers: List<Wallpaper>,
    val bhajans: List<Bhajan>,
    val mantras: List<Mantra>,
    val statuses: List<WhatsAppStatus>
) {
    val isEmpty: Boolean
        get() = wallpapers.isEmpty() && bhajans.isEmpty() && mantras.isEmpty() && statuses.isEmpty()
}

/** Resolves the favourite ids stored in [SessionManager] against the catalogue. */
class FavouritesRepository(
    private val sessionManager: SessionManager,
    private val contentRepository: ContentRepository
) {
    suspend fun favourites(): FavouriteContent {
        val ids = sessionManager.state.first().favouriteIds
        return FavouriteContent(
            wallpapers = contentRepository.wallpapers().filter { it.id in ids },
            bhajans = contentRepository.bhajans().filter { it.id in ids },
            mantras = contentRepository.mantras().filter { it.id in ids },
            statuses = contentRepository.statuses().filter { it.id in ids }
        )
    }

    suspend fun toggle(id: String) = sessionManager.toggleFavourite(id)
}
