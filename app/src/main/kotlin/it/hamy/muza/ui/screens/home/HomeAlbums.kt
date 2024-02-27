package it.hamy.muza.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.hamy.compose.persist.persist
import it.hamy.muza.Database
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.R
import it.hamy.muza.enums.AlbumSortBy
import it.hamy.muza.enums.SortOrder
import it.hamy.muza.models.Album
import it.hamy.muza.preferences.OrderPreferences
import it.hamy.muza.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.hamy.muza.ui.components.themed.Header
import it.hamy.muza.ui.components.themed.HeaderIconButton
import it.hamy.muza.ui.items.AlbumItem
import it.hamy.muza.ui.screens.Route
import it.hamy.muza.ui.styling.Dimensions
import it.hamy.muza.ui.styling.LocalAppearance

@OptIn(ExperimentalFoundationApi::class)
@Route
@Composable
fun HomeAlbums(
    onAlbumClick: (Album) -> Unit,
    onSearchClick: () -> Unit
) = with(OrderPreferences) {
    val (colorPalette) = LocalAppearance.current

    var items by persist<List<Album>>(tag = "home/albums", emptyList())

    LaunchedEffect(albumSortBy, albumSortOrder) {
        Database.albums(albumSortBy, albumSortOrder).collect { items = it }
    }

    val sortOrderIconRotation by animateFloatAsState(
        targetValue = if (albumSortOrder == SortOrder.Ascending) 0f else 180f,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = ""
    )

    val lazyListState = rememberLazyListState()

    Box {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End).asPaddingValues(),
            modifier = Modifier
                .background(colorPalette.background0)
                .fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = 0
            ) {
                Header(title = stringResource(R.string.albums)) {
                    HeaderIconButton(
                        icon = R.drawable.calendar,
                        color = if (albumSortBy == AlbumSortBy.Year) colorPalette.text else colorPalette.textDisabled,
                        onClick = { albumSortBy = AlbumSortBy.Year }
                    )

                    HeaderIconButton(
                        icon = R.drawable.text,
                        color = if (albumSortBy == AlbumSortBy.Title) colorPalette.text else colorPalette.textDisabled,
                        onClick = { albumSortBy = AlbumSortBy.Title }
                    )

                    HeaderIconButton(
                        icon = R.drawable.time,
                        color = if (albumSortBy == AlbumSortBy.DateAdded) colorPalette.text
                        else colorPalette.textDisabled,
                        onClick = { albumSortBy = AlbumSortBy.DateAdded }
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    HeaderIconButton(
                        icon = R.drawable.arrow_up,
                        color = colorPalette.text,
                        onClick = { albumSortOrder = !albumSortOrder },
                        modifier = Modifier.graphicsLayer { rotationZ = sortOrderIconRotation }
                    )
                }
            }

            items(
                items = items,
                key = Album::id
            ) { album ->
                AlbumItem(
                    album = album,
                    thumbnailSize = Dimensions.thumbnails.album,
                    modifier = Modifier
                        .clickable(onClick = { onAlbumClick(album) })
                        .animateItemPlacement()
                )
            }
        }

        FloatingActionsContainerWithScrollToTop(
            lazyListState = lazyListState,
            iconId = R.drawable.search,
            onClick = onSearchClick
        )
    }
}
