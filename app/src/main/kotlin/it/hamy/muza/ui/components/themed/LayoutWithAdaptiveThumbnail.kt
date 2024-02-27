package it.hamy.muza.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.valentinilk.shimmer.shimmer
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.ui.styling.shimmer
import it.hamy.muza.utils.isLandscape
import it.hamy.muza.utils.px
import it.hamy.muza.utils.thumbnail

@Composable
inline fun LayoutWithAdaptiveThumbnail(
    thumbnailContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) = if (isLandscape) Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
) {
    thumbnailContent()
    content()
} else Box(modifier = modifier) { content() }

fun adaptiveThumbnailContent(
    isLoading: Boolean,
    url: String?,
    shape: Shape? = null
): @Composable () -> Unit = {
    val colorPalette = LocalAppearance.current.colorPalette
    val thumbnailShape = LocalAppearance.current.thumbnailShape

    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val thumbnailSize = if (isLandscape) (maxHeight - 128.dp) else (maxWidth - 64.dp)

        val modifier = Modifier
            .padding(all = 16.dp)
            .clip(shape ?: thumbnailShape)
            .size(thumbnailSize)

        if (isLoading) Spacer(
            modifier = modifier
                .shimmer()
                .background(colorPalette.shimmer)
        ) else AsyncImage(
            model = url?.thumbnail(thumbnailSize.px),
            contentDescription = null,
            modifier = modifier.background(colorPalette.background1)
        )
    }
}
