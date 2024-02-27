package it.hamy.muza.ui.screens

import androidx.compose.runtime.Composable
import io.ktor.http.Url
import it.hamy.compose.routing.Route0
import it.hamy.compose.routing.Route1
import it.hamy.compose.routing.Route3
import it.hamy.compose.routing.RouteHandlerScope
import it.hamy.muza.enums.BuiltInPlaylist
import it.hamy.muza.models.Mood
import it.hamy.muza.ui.screens.album.AlbumScreen
import it.hamy.muza.ui.screens.artist.ArtistScreen
import it.hamy.muza.ui.screens.mood.MoodScreen
import it.hamy.muza.ui.screens.pipedplaylist.PipedPlaylistScreen
import it.hamy.muza.ui.screens.playlist.PlaylistScreen
import java.util.UUID

/**
 * Marker class for linters that a composable is a route and should not be handled like a regular
 * composable, but rather as an entrypoint.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class Route

val albumRoute = Route1<String?>("albumRoute")
val artistRoute = Route1<String?>("artistRoute")
val builtInPlaylistRoute = Route1<BuiltInPlaylist>("builtInPlaylistRoute")
val localPlaylistRoute = Route1<Long?>("localPlaylistRoute")
val pipedPlaylistRoute = Route3<String?, String?, String?>("pipedPlaylistRoute")
val playlistRoute = Route3<String?, String?, Int?>("playlistRoute")
val moodRoute = Route1<Mood>("moodRoute")
val searchResultRoute = Route1<String>("searchResultRoute")
val searchRoute = Route1<String>("searchRoute")
val settingsRoute = Route0("settingsRoute")

@Composable
fun RouteHandlerScope.GlobalRoutes() {
    albumRoute { browseId ->
        AlbumScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    artistRoute { browseId ->
        ArtistScreen(
            browseId = browseId ?: error("browseId cannot be null")
        )
    }

    pipedPlaylistRoute { apiBaseUrl, sessionToken, playlistId ->
        PipedPlaylistScreen(
            apiBaseUrl = apiBaseUrl?.let {
                runCatching { Url(it) }.getOrNull()
            } ?: error("apiBaseUrl cannot be null"),
            sessionToken = sessionToken ?: error("sessionToken cannot be null"),
            playlistId = runCatching {
                UUID.fromString(playlistId)
            }.getOrNull() ?: error("playlistId cannot be null")
        )
    }

    playlistRoute { browseId, params, maxDepth ->
        PlaylistScreen(
            browseId = browseId ?: error("browseId cannot be null"),
            params = params,
            maxDepth = maxDepth
        )
    }

    moodRoute { mood ->
        MoodScreen(mood = mood)
    }
}
