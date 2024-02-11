package it.hamy.muza.ui.screens.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import it.hamy.compose.persist.persistList
import it.hamy.muza.Database
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.R
import it.hamy.muza.enums.BuiltInPlaylist
import it.hamy.muza.enums.PlaylistSortBy
import it.hamy.muza.enums.SortOrder
import it.hamy.muza.models.Playlist
import it.hamy.muza.models.PlaylistPreview
import it.hamy.muza.query
import it.hamy.muza.ui.components.YandexAdsBanner
import it.hamy.muza.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.hamy.muza.ui.components.themed.Header
import it.hamy.muza.ui.components.themed.HeaderIconButton
import it.hamy.muza.ui.components.themed.SecondaryTextButton
import it.hamy.muza.ui.components.themed.TextFieldDialog
import it.hamy.muza.ui.items.PlaylistItem
import it.hamy.muza.ui.styling.Dimensions
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.ui.styling.px
import it.hamy.muza.utils.playlistSortByKey
import it.hamy.muza.utils.playlistSortOrderKey
import it.hamy.muza.utils.rememberPreference


@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable

fun HomePlaylists(
    onBuiltInPlaylist: (BuiltInPlaylist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSearchClick: () -> Unit,
) {
    val (colorPalette) = LocalAppearance.current

    var isCreatingANewPlaylist by rememberSaveable {
        mutableStateOf(false)
    }



    if (isCreatingANewPlaylist) {
        TextFieldDialog(
            hintText = "Введите название плейлиста",
            onDismiss = {
                isCreatingANewPlaylist = false
            },
            onDone = { text ->
                query {
                    Database.insert(Playlist(name = text))
                }
            }
        )
    }

    var sortBy by rememberPreference(playlistSortByKey, PlaylistSortBy.DateAdded)
    var sortOrder by rememberPreference(playlistSortOrderKey, SortOrder.Descending)

    var items by persistList<PlaylistPreview>("home/playlists")

    LaunchedEffect(sortBy, sortOrder) {
        Database.playlistPreviews(sortBy, sortOrder).collect { items = it }
    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (sortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing)
    )

    val thumbnailSizeDp = 108.dp
    val thumbnailSizePx = thumbnailSizeDp.px

    val lazyGridState = rememberLazyGridState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(Dimensions.thumbnails.song * 2 + Dimensions.itemsVerticalPadding * 2),
                contentPadding = LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.itemsVerticalPadding * 2),
                horizontalArrangement = Arrangement.spacedBy(
                    space = Dimensions.itemsVerticalPadding * 2,
                    alignment = Alignment.CenterHorizontally
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorPalette.background0)
            ) {
                item(key = "header", contentType = 0, span = { GridItemSpan(maxLineSpan) }) {
                    Header(title = "Плейлисты") {
                        SecondaryTextButton(
                            text = "Новый плейлист",
                            onClick = { isCreatingANewPlaylist = true }
                        )

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        HeaderIconButton(
                            icon = R.drawable.medical,
                            color = if (sortBy == PlaylistSortBy.SongCount) colorPalette.text else colorPalette.textDisabled,
                            onClick = { sortBy = PlaylistSortBy.SongCount }
                        )

                        HeaderIconButton(
                            icon = R.drawable.text,
                            color = if (sortBy == PlaylistSortBy.Name) colorPalette.text else colorPalette.textDisabled,
                            onClick = { sortBy = PlaylistSortBy.Name }
                        )

                        HeaderIconButton(
                            icon = R.drawable.time,
                            color = if (sortBy == PlaylistSortBy.DateAdded) colorPalette.text else colorPalette.textDisabled,
                            onClick = { sortBy = PlaylistSortBy.DateAdded }
                        )

                        Spacer(
                            modifier = Modifier
                                .width(2.dp)
                        )

                        HeaderIconButton(
                            icon = R.drawable.arrow_up,
                            color = colorPalette.text,
                            onClick = { sortOrder = !sortOrder },
                            modifier = Modifier
                                .graphicsLayer { rotationZ = sortOrderIconRotation }
                        )
                    }
                }

                item(key = "favorites") {
                    PlaylistItem(
                        icon = R.drawable.heart,
                        colorTint = colorPalette.red,
                        name = "Любимые",
                        songCount = null,
                        thumbnailSizeDp = thumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onBuiltInPlaylist(BuiltInPlaylist.Favorites) })
                            .animateItemPlacement()
                    )
                }

                item(key = "offline") {
                    PlaylistItem(
                        icon = R.drawable.airplane,
                        colorTint = colorPalette.blue,
                        name = "Сохранённые",
                        songCount = null,
                        thumbnailSizeDp = thumbnailSizeDp,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onBuiltInPlaylist(BuiltInPlaylist.Offline) })
                            .animateItemPlacement()
                    )
                }

                items(items = items, key = { it.playlist.id }) { playlistPreview ->
                    PlaylistItem(
                        playlist = playlistPreview,
                        thumbnailSizeDp = thumbnailSizeDp,
                        thumbnailSizePx = thumbnailSizePx,
                        alternative = true,
                        modifier = Modifier
                            .clickable(onClick = { onPlaylistClick(playlistPreview.playlist) })
                            .animateItemPlacement()
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 14.dp, end = 10.dp, top = 30.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center,
                    ) {
                        YandexAdsBanner(id = "R-M-5961316-1")
                    }
                }
            }
        }
        FloatingActionsContainerWithScrollToTop(
            lazyGridState = lazyGridState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )
    }
}
