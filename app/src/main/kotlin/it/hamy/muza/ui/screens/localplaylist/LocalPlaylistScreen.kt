package it.hamy.muza.ui.screens.localplaylist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.res.stringResource
import it.hamy.compose.persist.PersistMapCleanup
import it.hamy.compose.routing.RouteHandler
import it.hamy.muza.R
import it.hamy.muza.ui.components.themed.Scaffold
import it.hamy.muza.ui.screens.GlobalRoutes
import it.hamy.muza.ui.screens.Route

@Route
@Composable
fun LocalPlaylistScreen(playlistId: Long) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup(prefix = "localPlaylist/$playlistId/")

    RouteHandler(listenToGlobalEmitter = true) {
        GlobalRoutes()

        NavHost {
            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = 0,
                onTabChanged = { },
                tabColumnContent = { item ->
                    item(0, stringResource(R.string.songs), R.drawable.musical_notes)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> LocalPlaylistSongs(
                            playlistId = playlistId,
                            onDelete = pop
                        )
                    }
                }
            }
        }
    }
}
