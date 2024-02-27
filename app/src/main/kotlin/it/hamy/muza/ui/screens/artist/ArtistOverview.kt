package it.hamy.muza.ui.screens.artist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.hamy.innertube.Innertube
import it.hamy.innertube.models.NavigationEndpoint
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.LocalPlayerServiceBinder
import it.hamy.muza.R
import it.hamy.muza.ui.components.LocalMenuState
import it.hamy.muza.ui.components.ShimmerHost
import it.hamy.muza.ui.components.themed.Attribution
import it.hamy.muza.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.hamy.muza.ui.components.themed.LayoutWithAdaptiveThumbnail
import it.hamy.muza.ui.components.themed.NonQueuedMediaItemMenu
import it.hamy.muza.ui.components.themed.SecondaryTextButton
import it.hamy.muza.ui.components.themed.TextPlaceholder
import it.hamy.muza.ui.items.AlbumItem
import it.hamy.muza.ui.items.AlbumItemPlaceholder
import it.hamy.muza.ui.items.SongItem
import it.hamy.muza.ui.items.SongItemPlaceholder
import it.hamy.muza.ui.styling.Dimensions
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.asMediaItem
import it.hamy.muza.utils.forcePlay
import it.hamy.muza.utils.secondary
import it.hamy.muza.utils.semiBold

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistOverview(
    youtubeArtistPage: Innertube.ArtistPage?,
    onViewAllSongsClick: () -> Unit,
    onViewAllAlbumsClick: () -> Unit,
    onViewAllSinglesClick: () -> Unit,
    onAlbumClick: (String) -> Unit,
    thumbnailContent: @Composable () -> Unit,
    headerContent: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorPalette, typography) = LocalAppearance.current
    val binder = LocalPlayerServiceBinder.current
    val menuState = LocalMenuState.current
    val windowInsets = LocalPlayerAwareWindowInsets.current

    val endPaddingValues = windowInsets.only(WindowInsetsSides.End).asPaddingValues()

    val sectionTextModifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 24.dp, bottom = 8.dp)

    val scrollState = rememberScrollState()

    LayoutWithAdaptiveThumbnail(
        thumbnailContent = thumbnailContent,
        modifier = modifier
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(colorPalette.background0)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(
                        windowInsets
                            .only(WindowInsetsSides.Vertical)
                            .asPaddingValues()
                    )
            ) {
                Box(modifier = Modifier.padding(endPaddingValues)) {
                    headerContent {
                        youtubeArtistPage?.shuffleEndpoint?.let { endpoint ->
                            SecondaryTextButton(
                                text = stringResource(R.string.shuffle),
                                onClick = {
                                    binder?.stopRadio()
                                    binder?.playRadio(endpoint)
                                }
                            )
                        }
                    }
                }

                thumbnailContent()

                youtubeArtistPage?.let {
                    youtubeArtistPage.songs?.let { songs ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            BasicText(
                                text = stringResource(R.string.songs),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )

                            youtubeArtistPage.songsEndpoint?.let {
                                BasicText(
                                    text = stringResource(R.string.view_all),
                                    style = typography.xs.secondary,
                                    modifier = sectionTextModifier.clickable(onClick = onViewAllSongsClick)
                                )
                            }
                        }

                        songs.forEach { song ->
                            SongItem(
                                song = song,
                                thumbnailSize = Dimensions.thumbnails.song,
                                modifier = Modifier
                                    .combinedClickable(
                                        onLongClick = {
                                            menuState.display {
                                                NonQueuedMediaItemMenu(
                                                    onDismiss = menuState::hide,
                                                    mediaItem = song.asMediaItem
                                                )
                                            }
                                        },
                                        onClick = {
                                            val mediaItem = song.asMediaItem
                                            binder?.stopRadio()
                                            binder?.player?.forcePlay(mediaItem)
                                            binder?.setupRadio(
                                                NavigationEndpoint.Endpoint.Watch(videoId = mediaItem.mediaId)
                                            )
                                        }
                                    )
                                    .padding(endPaddingValues)
                            )
                        }
                    }

                    youtubeArtistPage.albums?.let { albums ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            BasicText(
                                text = stringResource(R.string.albums),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )

                            youtubeArtistPage.albumsEndpoint?.let {
                                BasicText(
                                    text = stringResource(R.string.view_all),
                                    style = typography.xs.secondary,
                                    modifier = sectionTextModifier.clickable(onClick = onViewAllAlbumsClick)
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = endPaddingValues,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = albums,
                                key = Innertube.AlbumItem::key
                            ) { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable(onClick = {
                                        onAlbumClick(album.key)
                                    })
                                )
                            }
                        }
                    }

                    youtubeArtistPage.singles?.let { singles ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(endPaddingValues)
                        ) {
                            BasicText(
                                text = stringResource(R.string.singles),
                                style = typography.m.semiBold,
                                modifier = sectionTextModifier
                            )

                            youtubeArtistPage.singlesEndpoint?.let {
                                BasicText(
                                    text = stringResource(R.string.view_all),
                                    style = typography.xs.secondary,
                                    modifier = sectionTextModifier.clickable(onClick = onViewAllSinglesClick)
                                )
                            }
                        }

                        LazyRow(
                            contentPadding = endPaddingValues,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = singles,
                                key = Innertube.AlbumItem::key
                            ) { album ->
                                AlbumItem(
                                    album = album,
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true,
                                    modifier = Modifier.clickable(onClick = { onAlbumClick(album.key) })
                                )
                            }
                        }
                    }

                    youtubeArtistPage.description?.let { description ->
                        Attribution(
                            text = description,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                        )
                    }
                } ?: ShimmerHost {
                    TextPlaceholder(modifier = sectionTextModifier)

                    repeat(5) {
                        SongItemPlaceholder(thumbnailSize = Dimensions.thumbnails.song)
                    }

                    repeat(2) {
                        TextPlaceholder(modifier = sectionTextModifier)

                        Row {
                            repeat(2) {
                                AlbumItemPlaceholder(
                                    thumbnailSize = Dimensions.thumbnails.album,
                                    alternative = true
                                )
                            }
                        }
                    }
                }
            }

            youtubeArtistPage?.radioEndpoint?.let { endpoint ->
                FloatingActionsContainerWithScrollToTop(
                    scrollState = scrollState,
                    iconId = R.drawable.radio,
                    onClick = {
                        binder?.stopRadio()
                        binder?.playRadio(endpoint)
                    }
                )
            }
        }
    }
}
