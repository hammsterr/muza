package it.hamy.muza.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalContext
import it.hamy.compose.persist.PersistMapCleanup
import it.hamy.compose.routing.RouteHandler
import it.hamy.compose.routing.defaultStacking
import it.hamy.compose.routing.defaultStill
import it.hamy.compose.routing.defaultUnstacking
import it.hamy.compose.routing.isStacking
import it.hamy.compose.routing.isUnknown
import it.hamy.compose.routing.isUnstacking
import it.hamy.muza.Database
import it.hamy.muza.R
import it.hamy.muza.models.SearchQuery
import it.hamy.muza.query
import it.hamy.muza.ui.components.themed.Scaffold
import it.hamy.muza.ui.screens.albumRoute
import it.hamy.muza.ui.screens.artistRoute
import it.hamy.muza.ui.screens.builtInPlaylistRoute
import it.hamy.muza.ui.screens.builtinplaylist.BuiltInPlaylistScreen
import it.hamy.muza.ui.screens.globalRoutes
import it.hamy.muza.ui.screens.localPlaylistRoute
import it.hamy.muza.ui.screens.localplaylist.LocalPlaylistScreen
import it.hamy.muza.ui.screens.playlistRoute
import it.hamy.muza.ui.screens.search.SearchScreen
import it.hamy.muza.ui.screens.searchResultRoute
import it.hamy.muza.ui.screens.searchRoute
import it.hamy.muza.ui.screens.searchresult.SearchResultScreen
import it.hamy.muza.ui.screens.settings.SettingsScreen
import it.hamy.muza.ui.screens.settingsRoute
import it.hamy.muza.utils.homeScreenTabIndexKey
import it.hamy.muza.utils.pauseSearchHistoryKey
import it.hamy.muza.utils.preferences
import it.hamy.muza.utils.rememberPreference

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun HomeScreen(onPlaylistUrl: (String) -> Unit) {
    val saveableStateHolder = rememberSaveableStateHolder()

    PersistMapCleanup("home/")

    RouteHandler(
        listenToGlobalEmitter = true,
        transitionSpec = {
            when {
                isStacking -> defaultStacking
                isUnstacking -> defaultUnstacking
                isUnknown -> when {
                    initialState.route == searchRoute && targetState.route == searchResultRoute -> defaultStacking
                    initialState.route == searchResultRoute && targetState.route == searchRoute -> defaultUnstacking
                    else -> defaultStill
                }

                else -> defaultStill
            }
        }
    ) {
        globalRoutes()

        settingsRoute {
            SettingsScreen()
        }

        localPlaylistRoute { playlistId ->
            LocalPlaylistScreen(
                playlistId = playlistId ?: error("playlistId cannot be null")
            )
        }

        builtInPlaylistRoute { builtInPlaylist ->
            BuiltInPlaylistScreen(
                builtInPlaylist = builtInPlaylist
            )
        }

        searchResultRoute { query ->
            SearchResultScreen(
                query = query,
                onSearchAgain = {
                    searchRoute(query)
                }
            )
        }

        searchRoute { initialTextInput ->
            val context = LocalContext.current

            SearchScreen(
                initialTextInput = initialTextInput,
                onSearch = { query ->
                    pop()
                    searchResultRoute(query)

                    if (!context.preferences.getBoolean(pauseSearchHistoryKey, false)) {
                        query {
                            Database.insert(SearchQuery(query = query))
                        }
                    }
                },
                onViewPlaylist = onPlaylistUrl
            )
        }

        host {
            val (tabIndex, onTabChanged) = rememberPreference(
                homeScreenTabIndexKey,
                defaultValue = 0
            )

            Scaffold(
                topIconButtonId = R.drawable.equalizer,
                onTopIconButtonClick = { settingsRoute() },
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { Item ->
                    Item(0, "Обзор", R.drawable.sparkles)
                    Item(1, "Песни", R.drawable.musical_notes)
                    Item(2, "Плейлисты", R.drawable.playlist)
                    Item(3, "Исполнители", R.drawable.person)
                    Item(4, "Альбомы", R.drawable.disc)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(key = currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> QuickPicks(
                            onAlbumClick = { albumRoute(it) },
                            onArtistClick = { artistRoute(it) },
                            onPlaylistClick = { playlistRoute(it) },
                            onSearchClick = { searchRoute("") }
                        )

                        1 -> HomeSongs(
                            onSearchClick = { searchRoute("") }
                        )

                        2 -> HomePlaylists(
                            onBuiltInPlaylist = { builtInPlaylistRoute(it) },
                            onPlaylistClick = { localPlaylistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        3 -> HomeArtistList(
                            onArtistClick = { artistRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )

                        4 -> HomeAlbums(
                            onAlbumClick = { albumRoute(it.id) },
                            onSearchClick = { searchRoute("") }
                        )
                    }
                }
            }
        }
    }
}
