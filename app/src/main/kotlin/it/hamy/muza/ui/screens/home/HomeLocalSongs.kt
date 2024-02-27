package it.hamy.muza.ui.screens.home

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.hamy.muza.Database
import it.hamy.muza.R
import it.hamy.muza.models.Song
import it.hamy.muza.preferences.OrderPreferences
import it.hamy.muza.service.LOCAL_KEY_PREFIX
import it.hamy.muza.transaction
import it.hamy.muza.ui.components.themed.SecondaryTextButton
import it.hamy.muza.ui.screens.Route
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.get
import it.hamy.muza.utils.hasPermission
import it.hamy.muza.utils.isAtLeastAndroid10
import it.hamy.muza.utils.isAtLeastAndroid13
import it.hamy.muza.utils.isCompositionLaunched
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val permission = if (isAtLeastAndroid13) Manifest.permission.READ_MEDIA_AUDIO
else Manifest.permission.READ_EXTERNAL_STORAGE

@Route
@Composable
fun HomeLocalSongs(onSearchClick: () -> Unit) = with(OrderPreferences) {
    val context = LocalContext.current
    val (_, typography) = LocalAppearance.current

    var hasPermission by remember(isCompositionLaunched()) {
        mutableStateOf(context.applicationContext.hasPermission(permission))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasPermission = it }
    )

    LaunchedEffect(hasPermission) {
        context.musicFilesAsFlow().collect()
    }

    if (hasPermission) HomeSongs(
        onSearchClick = onSearchClick,
        songProvider = {
            Database.songs(
                sortBy = localSongSortBy,
                sortOrder = localSongSortOrder,
                isLocal = true
            ).map { songs -> songs.filter { it.durationText != "0:00" } }
        },
        sortBy = localSongSortBy,
        setSortBy = { localSongSortBy = it },
        sortOrder = localSongSortOrder,
        setSortOrder = { localSongSortOrder = it },
        title = stringResource(R.string.local)
    ) else {
        LaunchedEffect(Unit) { launcher.launch(permission) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicText(
                text = stringResource(R.string.media_permission_declined),
                modifier = Modifier.fillMaxWidth(0.5f),
                style = typography.s
            )
            SecondaryTextButton(
                text = stringResource(R.string.open_settings),
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            setData(Uri.fromParts("package", context.packageName, null))
                        }
                    )
                }
            )
        }
    }
}

private val mediaScope = CoroutineScope(Dispatchers.IO + CoroutineName("MediaStore worker"))
fun Context.musicFilesAsFlow(): StateFlow<List<Song>> = flow {
    var version: String? = null

    while (currentCoroutineContext().isActive) {
        val newVersion = MediaStore.getVersion(applicationContext)
        if (version != newVersion) {
            version = newVersion
            val collection =
                if (isAtLeastAndroid10) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
            val albumUriBase = Uri.parse("content://media/external/audio/albumart")

            contentResolver.query(collection, projection, null, null, sortOrder)
                ?.use { cursor ->
                    val isMusicIdx = cursor[MediaStore.Audio.Media.IS_MUSIC]
                    val idIdx = cursor[MediaStore.Audio.Media._ID]
                    val nameIdx = cursor[MediaStore.Audio.Media.DISPLAY_NAME]
                    val durationIdx = cursor[MediaStore.Audio.Media.DURATION]
                    val artistIdx = cursor[MediaStore.Audio.Media.ARTIST]
                    val albumIdIdx = cursor[MediaStore.Audio.Media.ALBUM_ID]

                    buildList {
                        while (cursor.moveToNext()) {
                            if (cursor.getInt(isMusicIdx) == 0) continue
                            val id = cursor.getLong(idIdx)
                            val name = cursor.getString(nameIdx)
                            val duration = cursor.getInt(durationIdx)
                            if (duration == 0) continue
                            val artist = cursor.getString(artistIdx)
                            val albumId = cursor.getLong(albumIdIdx)

                            val albumUri = ContentUris.withAppendedId(albumUriBase, albumId)
                            val durationText =
                                duration.milliseconds.toComponents { minutes, seconds, _ ->
                                    "$minutes:${seconds.toString().padStart(2, '0')}"
                                }

                            add(
                                Song(
                                    id = "$LOCAL_KEY_PREFIX$id",
                                    title = name,
                                    artistsText = artist,
                                    durationText = durationText,
                                    thumbnailUrl = albumUri.toString()
                                )
                            )
                        }
                    }
                }?.let { emit(it) }
        }
        delay(5.seconds)
    }
}.distinctUntilChanged()
    .onEach { songs -> transaction { songs.forEach(Database::insert) } }
    .stateIn(mediaScope, SharingStarted.Eagerly, listOf())
