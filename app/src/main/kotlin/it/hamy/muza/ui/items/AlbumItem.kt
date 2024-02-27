package it.hamy.muza.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.hamy.innertube.Innertube
import it.hamy.muza.models.Album
import it.hamy.muza.ui.components.themed.TextPlaceholder
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.ui.styling.shimmer
import it.hamy.muza.utils.px
import it.hamy.muza.utils.secondary
import it.hamy.muza.utils.semiBold
import it.hamy.muza.utils.thumbnail

@Composable
fun AlbumItem(
    album: Album,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) = AlbumItem(
    thumbnailUrl = album.thumbnailUrl,
    title = album.title,
    authors = album.authorsText,
    year = album.year,
    thumbnailSize = thumbnailSize,
    alternative = alternative,
    modifier = modifier
)

@Composable
fun AlbumItem(
    album: Innertube.AlbumItem,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) = AlbumItem(
    thumbnailUrl = album.thumbnail?.url,
    title = album.info?.name,
    authors = album.authors?.joinToString("") { it.name.orEmpty() },
    year = album.year,
    thumbnailSize = thumbnailSize,
    alternative = alternative,
    modifier = modifier
)

@Composable
fun AlbumItem(
    thumbnailUrl: String?,
    title: String?,
    authors: String?,
    year: String?,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) = ItemContainer(
    alternative = alternative,
    thumbnailSize = thumbnailSize,
    modifier = modifier
) {
    val typography = LocalAppearance.current.typography
    val thumbnailShape = LocalAppearance.current.thumbnailShape

    AsyncImage(
        model = thumbnailUrl?.thumbnail(thumbnailSize.px),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .clip(thumbnailShape)
            .size(thumbnailSize)
    )

    ItemInfoContainer {
        BasicText(
            text = title.orEmpty(),
            style = typography.xs.semiBold,
            maxLines = if (alternative) 1 else 2,
            overflow = TextOverflow.Ellipsis
        )

        if (!alternative) authors?.let {
            BasicText(
                text = authors,
                style = typography.xs.semiBold.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        BasicText(
            text = year.orEmpty(),
            style = typography.xxs.semiBold.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun AlbumItemPlaceholder(
    thumbnailSize: Dp,
    modifier: Modifier = Modifier,
    alternative: Boolean = false
) = ItemContainer(
    alternative = alternative,
    thumbnailSize = thumbnailSize,
    modifier = modifier
) {
    val colorPalette = LocalAppearance.current.colorPalette
    val thumbnailShape = LocalAppearance.current.thumbnailShape

    Spacer(
        modifier = Modifier
            .background(color = colorPalette.shimmer, shape = thumbnailShape)
            .size(thumbnailSize)
    )

    ItemInfoContainer {
        TextPlaceholder()
        if (!alternative) TextPlaceholder()
        TextPlaceholder(modifier = Modifier.padding(top = 4.dp))
    }
}
