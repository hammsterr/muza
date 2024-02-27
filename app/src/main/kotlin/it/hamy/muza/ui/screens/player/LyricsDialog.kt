package it.hamy.muza.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import it.hamy.muza.Database
import it.hamy.muza.LocalPlayerServiceBinder
import it.hamy.muza.service.isLocal
import it.hamy.muza.ui.modifiers.PinchDirection
import it.hamy.muza.ui.modifiers.onSwipe
import it.hamy.muza.ui.modifiers.pinchToToggle
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.forceSeekToNext
import it.hamy.muza.utils.forceSeekToPrevious
import it.hamy.muza.utils.px
import it.hamy.muza.utils.thumbnail

@Composable
fun LyricsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) = Dialog(onDismissRequest = onDismiss) {
    val appearance = LocalAppearance.current
    val (colorPalette) = appearance
    val thumbnailShape = appearance.thumbnailShape

    val player = LocalPlayerServiceBinder.current?.player ?: return@Dialog
    val (window, error) = currentWindow()

    LaunchedEffect(window, error) {
        if (window == null || window.mediaItem.isLocal || error != null) onDismiss()
    }

    window ?: return@Dialog

    AnimatedContent(
        targetState = window,
        transitionSpec = {
            if (initialState.mediaItem.mediaId == targetState.mediaItem.mediaId)
                return@AnimatedContent ContentTransform(
                    targetContentEnter = EnterTransition.None,
                    initialContentExit = ExitTransition.None
                )

            val direction = if (targetState.firstPeriodIndex > initialState.firstPeriodIndex)
                AnimatedContentTransitionScope.SlideDirection.Left
            else AnimatedContentTransitionScope.SlideDirection.Right

            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = direction,
                    animationSpec = tween(500)
                ),
                initialContentExit = slideOutOfContainer(
                    towards = direction,
                    animationSpec = tween(500)
                ),
                sizeTransform = null
            )
        },
        label = ""
    ) { currentWindow ->
        BoxWithConstraints(
            modifier = modifier
                .padding(all = 36.dp)
                .padding(vertical = 32.dp)
                .clip(thumbnailShape)
                .fillMaxSize()
                .background(colorPalette.background1)
                .pinchToToggle(
                    direction = PinchDirection.In,
                    threshold = 0.9f,
                    onPinch = { onDismiss() }
                )
                .onSwipe(
                    onSwipeLeft = {
                        player.forceSeekToNext()
                    },
                    onSwipeRight = {
                        player.seekToDefaultPosition()
                        player.forceSeekToPrevious()
                    }
                )
        ) {
            val thumbnailHeight = maxHeight

            if (currentWindow.mediaItem.mediaMetadata.artworkUri != null) AsyncImage(
                model = currentWindow.mediaItem.mediaMetadata.artworkUri.thumbnail((thumbnailHeight - 64.dp).px),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorPalette.background0)
            )

            Lyrics(
                mediaId = currentWindow.mediaItem.mediaId,
                isDisplayed = true,
                onDismiss = { },
                height = thumbnailHeight,
                mediaMetadataProvider = currentWindow.mediaItem::mediaMetadata,
                durationProvider = player::getDuration,
                ensureSongInserted = { Database.insert(currentWindow.mediaItem) },
                onMenuLaunched = onDismiss
            )
        }
    }
}
