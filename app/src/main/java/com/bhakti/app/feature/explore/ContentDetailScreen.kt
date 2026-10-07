package com.bhakti.app.feature.explore

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.media.BitmapLoader
import com.bhakti.app.core.media.MediaSaver
import com.bhakti.app.core.media.ProfilePhotoStore
import com.bhakti.app.core.media.ShareImageProvider
import com.bhakti.app.core.media.StatusArtRenderer
import com.bhakti.app.core.media.WallpaperSetter
import com.bhakti.app.core.media.rememberJapaChanter
import com.bhakti.app.core.ui.IconBadge
import com.bhakti.app.core.ui.JapaCounter
import com.bhakti.app.core.ui.PlaceholderArt
import com.bhakti.app.core.ui.imageFor
import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.DevotionalContent
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import com.bhakti.app.ui.theme.SaffronDeep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContentDetailScreen(navController: NavHostController, typeName: String, id: String) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contentType = remember(typeName) { ContentType.valueOf(typeName) }

    var item by remember { mutableStateOf<DevotionalContent?>(null) }
    LaunchedEffect(contentType, id) { item = container.contentRepository.byId(contentType, id) }

    val session by container.sessionManager.state.collectAsState(initial = null)
    val isFavourite = session?.favouriteIds?.contains(id) == true

    fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
    }

    fun shareBitmap(bitmap: Bitmap, fileName: String, captionText: String) {
        val uri = ShareImageProvider.uriForBitmap(context, bitmap, fileName)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, captionText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
    }

    fun toastSaveResult(success: Boolean) {
        Toast.makeText(
            context,
            if (success) "Saved to Pictures/Bhakti" else "Couldn't save image",
            Toast.LENGTH_SHORT
        ).show()
    }

    var pendingDownloadAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pendingDownloadAction
        pendingDownloadAction = null
        if (granted && action != null) {
            action()
        } else if (!granted) {
            Toast.makeText(context, "Storage permission needed to save images", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestDownload(action: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            pendingDownloadAction = action
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            action()
        }
    }

    fun downloadBitmap(bitmap: Bitmap, displayName: String) = requestDownload {
        scope.launch { toastSaveResult(MediaSaver.saveBitmapToGallery(context, bitmap, displayName).isSuccess) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contentType == ContentType.STATUS) {
                    TextButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Text("Edit Profile")
                    }
                }
                IconButton(onClick = { scope.launch { container.sessionManager.toggleFavourite(id) } }) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite"
                    )
                }
            }
        }

        when (val current = item) {
            null -> Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            is Wallpaper -> WallpaperDetail(
                current,
                onShare = {
                    scope.launch {
                        val bitmap = BitmapLoader.load(context, current.imageUrl, imageFor(current.deity, current.imageVariant))
                        shareBitmap(bitmap, current.title, "Jai ${current.deity.displayName}! Check out this Bhakti wallpaper: ${current.title}")
                    }
                },
                onDownload = {
                    scope.launch {
                        val bitmap = BitmapLoader.load(context, current.imageUrl, imageFor(current.deity, current.imageVariant))
                        downloadBitmap(bitmap, current.title)
                    }
                }
            )
            // Aarti/bhajans were removed from the app - nothing links here any more,
            // but the content type still exists, so handle any stale link gracefully.
            is Bhajan -> Text("This content is no longer available.", style = MaterialTheme.typography.bodyMedium)
            is Mantra -> MantraDetail(current)
            is WhatsAppStatus -> StatusDetail(
                current,
                userName = session?.user?.displayName?.takeIf { it.isNotBlank() } ?: "Bhakt",
                onShareBitmap = { bitmap -> shareBitmap(bitmap, current.title, current.caption) },
                onDownloadBitmap = { bitmap -> downloadBitmap(bitmap, current.title) }
            )
        }
    }
}

@Composable
private fun WallpaperDetail(wallpaper: Wallpaper, onShare: () -> Unit, onDownload: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showWallpaperDialog by remember { mutableStateOf(false) }

    PlaceholderArt(deity = wallpaper.deity, label = wallpaper.title, aspectRatio = 9f / 16f, imageVariant = wallpaper.imageVariant, imageUrl = wallpaper.imageUrl)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            wallpaper.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )
        DetailActionsRow(onShare = onShare, onDownload = onDownload, modifier = Modifier)
    }
    Text(
        "${wallpaper.deity.displayName} • ${wallpaper.category} • ${wallpaper.theme} • ${wallpaper.style}",
        style = MaterialTheme.typography.bodyMedium
    )

    Button(
        onClick = { showWallpaperDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
    ) {
        Icon(Icons.Filled.Wallpaper, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
        Text("Set as Phone Wallpaper")
    }

    if (showWallpaperDialog) {
        SetWallpaperDialog(
            onDismiss = { showWallpaperDialog = false },
            onChoose = { target ->
                showWallpaperDialog = false
                scope.launch {
                    val bitmap = BitmapLoader.load(context, wallpaper.imageUrl, imageFor(wallpaper.deity, wallpaper.imageVariant))
                    val result = WallpaperSetter.setAsWallpaper(context, bitmap, target)
                    Toast.makeText(
                        context,
                        if (result.isSuccess) "Wallpaper updated" else "Couldn't set wallpaper",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
private fun SetWallpaperDialog(onDismiss: () -> Unit, onChoose: (Int) -> Unit) {
    val options = listOf(
        "Home screen" to WallpaperSetter.TARGET_HOME,
        "Lock screen" to WallpaperSetter.TARGET_LOCK,
        "Home and lock screen" to WallpaperSetter.TARGET_BOTH
    )
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text(
                    "Set as wallpaper",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                options.forEach { (label, target) ->
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChoose(target) }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MantraDetail(mantra: Mantra) {
    // Same as Naam Japa: each counter tap speaks the mantra aloud in Hindi.
    var japaSoundOn by remember { mutableStateOf(true) }
    val chanter = rememberJapaChanter(devanagariText = mantra.devanagari, recordedUrl = mantra.japaAudioUrl)

    PlaceholderArt(deity = mantra.deity, label = mantra.title, aspectRatio = 16f / 9f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(mantra.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
        IconButton(onClick = { japaSoundOn = !japaSoundOn }) {
            Icon(
                if (japaSoundOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = if (japaSoundOn) "Turn off japa sound" else "Turn on japa sound"
            )
        }
    }

    Row(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(onClick = {}, label = { Text(mantra.purpose) })
        AssistChip(onClick = {}, label = { Text(mantra.category) })
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                mantra.devanagari,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                mantra.transliteration,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }

    mantra.meaning?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Meaning", style = MaterialTheme.typography.titleMedium)
                Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    JapaCounter(
        counterId = mantra.id,
        target = mantra.recommendedCount,
        onTap = { if (japaSoundOn) chanter.chant() },
        routineStepId = "mantra"
    )

    // No separate "Listen" player or share row here - the screen ends at the
    // japa counter, which chants the mantra aloud on every tap.
}

@Composable
private fun StatusDetail(
    status: WhatsAppStatus,
    userName: String,
    onShareBitmap: (Bitmap) -> Unit,
    onDownloadBitmap: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var artBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(status.id, userName) {
        val source = BitmapLoader.load(context, status.imageUrl, imageFor(status.deity, status.imageVariant))
        artBitmap = withContext(Dispatchers.Default) {
            StatusArtRenderer.render(
                source = source,
                greeting = status.greeting,
                shloka = status.shloka,
                userName = userName,
                userPhoto = ProfilePhotoStore.loadBitmap(context)
            )
        }
    }

    val bitmap = artBitmap
    if (bitmap == null) {
        PlaceholderArt(deity = status.deity, label = status.title, aspectRatio = 9f / 16f, imageVariant = status.imageVariant, imageUrl = status.imageUrl)
    } else {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = status.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
        )
    }

    Text(
        "Personalized with your name and photo from Profile.",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 8.dp)
    )

    Text(status.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
    Text("${status.deity.displayName} • ${status.category} • ${status.mediaType.name.lowercase()}", style = MaterialTheme.typography.bodyMedium)
    Text(status.caption, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    DetailActionsRow(
        onShare = { bitmap?.let(onShareBitmap) },
        onDownload = { bitmap?.let(onDownloadBitmap) }
    )
}

@Composable
private fun DetailActionsRow(
    onShare: () -> Unit,
    onDownload: (() -> Unit)? = null,
    modifier: Modifier = Modifier.padding(top = 16.dp)
) {
    Row(modifier = modifier) {
        if (onDownload != null) {
            IconButton(onClick = onDownload) {
                Icon(Icons.Filled.Download, contentDescription = "Download")
            }
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
        }
    }
}
