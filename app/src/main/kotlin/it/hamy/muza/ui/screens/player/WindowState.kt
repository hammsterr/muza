package it.hamy.muza.ui.screens.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import it.hamy.muza.LocalPlayerServiceBinder
import it.hamy.muza.utils.DisposableListener
import it.hamy.muza.utils.currentWindow

@Composable
fun currentWindow(): Pair<Timeline.Window?, PlaybackException?> {
    val player = LocalPlayerServiceBinder.current?.player ?: return null to null
    var window by remember { mutableStateOf(player.currentWindow) }
    var error by remember { mutableStateOf<PlaybackException?>(player.playerError) }

    player.DisposableListener {
        object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                window = player.currentWindow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                error = player.playerError
            }

            override fun onPlayerError(playbackException: PlaybackException) {
                error = playbackException
            }
        }
    }

    return window to error
}
