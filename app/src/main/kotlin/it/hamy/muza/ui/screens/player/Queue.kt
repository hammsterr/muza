package it.hamy.muza.ui.screens.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import com.valentinilk.shimmer.shimmer
import it.hamy.compose.reordering.ReorderingLazyColumn
import it.hamy.compose.reordering.animateItemPlacement
import it.hamy.compose.reordering.draggedItem
import it.hamy.compose.reordering.rememberReorderingState
import it.hamy.compose.reordering.reorder
import it.hamy.muza.LocalPlayerServiceBinder
import it.hamy.muza.R
import it.hamy.muza.ui.components.BottomSheet
import it.hamy.muza.ui.components.BottomSheetState
import it.hamy.muza.ui.components.LocalMenuState
import it.hamy.muza.ui.components.MusicBars
import it.hamy.muza.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.hamy.muza.ui.components.themed.IconButton
import it.hamy.muza.ui.components.themed.QueuedMediaItemMenu
import it.hamy.muza.ui.items.SongItem
import it.hamy.muza.ui.items.SongItemPlaceholder
import it.hamy.muza.ui.styling.Dimensions
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.ui.styling.onOverlay
import it.hamy.muza.ui.styling.px
import it.hamy.muza.utils.DisposableListener
import it.hamy.muza.utils.medium
import it.hamy.muza.utils.queueLoopEnabledKey
import it.hamy.muza.utils.rememberPreference
import it.hamy.muza.utils.shouldBePlaying
import it.hamy.muza.utils.shuffleQueue
import it.hamy.muza.utils.smoothScrollToTop
import it.hamy.muza.utils.windows
import kotlinx.coroutines.launch

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun Queue(
    backgroundColorProvider: () -> Color,
    layoutState: BottomSheetState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val (colorPalette, typography, thumbnailShape) = LocalAppearance.current

    val windowInsets = WindowInsets.systemBars

    val horizontalBottomPaddingValues = windowInsets
        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()

    BottomSheet(
        state = layoutState,
        modifier = modifier,
        collapsedContent = {
            Box(
                modifier = Modifier
                    .drawBehind { drawRect(backgroundColorProvider()) }
                    .fillMaxSize()
                    .padding(horizontalBottomPaddingValues)
            ) {
                Image(
                    painter = painterResource(R.drawable.playlist),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp)
                )

                content()
            }
        }
    ) {
        val binder = LocalPlayerServiceBinder.current

        binder?.player ?: return@BottomSheet

        val player = binder.player

        var queueLoopEnabled by rememberPreference(queueLoopEnabledKey, defaultValue = true)

        val menuState = LocalMenuState.current

        val thumbnailSizeDp = Dimensions.thumbnails.song
        val thumbnailSizePx = thumbnailSizeDp.px

        var mediaItemIndex by remember {
            mutableStateOf(if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex)
        }

        var windows by remember {
            mutableStateOf(player.currentTimeline.windows)
        }

        var shouldBePlaying by remember {
            mutableStateOf(binder.player.shouldBePlaying)
        }

        player.DisposableListener {
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItemIndex =
                        if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    windows = timeline.windows
                    mediaItemIndex =
                        if (player.mediaItemCount == 0) -1 else player.currentMediaItemIndex
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    shouldBePlaying = binder.player.shouldBePlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    shouldBePlaying = binder.player.shouldBePlaying
                }
            }
        }

        val reorderingState = rememberReorderingState(
            lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = mediaItemIndex),
            key = windows,
            onDragEnd = player::moveMediaItem,
            extraItemCount = 0
        )

        val rippleIndication = rememberRipple(bounded = false)

        val musicBarsTransition = updateTransition(targetState = mediaItemIndex, label = "")

        Column {
            Box(
                modifier = Modifier
                    .background(colorPalette.background1)
                    .weight(1f)
            ) {
                ReorderingLazyColumn(
                    reorderingState = reorderingState,
                    contentPadding = windowInsets
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .asPaddingValues(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .nestedScroll(layoutState.preUpPostDownNestedScrollConnection)

                ) {
                    items(
                        items = windows,
                        key = { it.uid.hashCode() }
                    ) { window ->
                        val isPlayingThisMediaItem = mediaItemIndex == window.firstPeriodIndex
                        var offsetX by remember { mutableStateOf(0f) }

                        SongItem(
                            song = window.mediaItem,
                            thumbnailSizePx = thumbnailSizePx,
                            thumbnailSizeDp = thumbnailSizeDp,
                            onThumbnailContent = {
                                musicBarsTransition.AnimatedVisibility(
                                    visible = { it == window.firstPeriodIndex },
                                    enter = fadeIn(tween(800)),
                                    exit = fadeOut(tween(800)),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .background(
                                                color = Color.Black.copy(alpha = 0.25f),
                                                shape = thumbnailShape
                                            )
                                            .size(Dimensions.thumbnails.song)
                                    ) {
                                        if (shouldBePlaying) {
                                            MusicBars(
                                                color = colorPalette.onOverlay,
                                                modifier = Modifier
                                                    .height(24.dp)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(R.drawable.play),
                                                contentDescription = null,
                                                colorFilter = ColorFilter.tint(colorPalette.onOverlay),
                                                modifier = Modifier
                                                    .size(24.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(
                                    icon = R.drawable.reorder,
                                    color = colorPalette.textDisabled,
                                    indication = rippleIndication,
                                    onClick = {},
                                    modifier = Modifier
                                        .reorder(
                                            reorderingState = reorderingState,
                                            index = window.firstPeriodIndex
                                        )
                                        .size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .combinedClickable(
                                    onLongClick = {
                                        menuState.display {
                                            QueuedMediaItemMenu(
                                                mediaItem = window.mediaItem,
                                                indexInQueue = if (isPlayingThisMediaItem) null else window.firstPeriodIndex,
                                                onDismiss = menuState::hide
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (isPlayingThisMediaItem) {
                                            if (shouldBePlaying) {
                                                player.pause()
                                            } else {
                                                player.play()
                                            }
                                        } else {
                                            player.seekToDefaultPosition(window.firstPeriodIndex)
                                            player.playWhenReady = true
                                        }
                                    }
                                )
                                .animateItemPlacement(reorderingState = reorderingState)
                                .draggedItem(
                                    reorderingState = reorderingState,
                                    index = window.firstPeriodIndex
                                )

                                .draggable(
                                    orientation = Orientation.Horizontal,
                                    state = rememberDraggableState(onDelta = { delta ->
                                        if (isPlayingThisMediaItem) return@rememberDraggableState
                                        offsetX += delta
                                    }),
                                    onDragStopped = { velocity ->
                                        if ((offsetX <= -300.0f && velocity <= -3000.0f) || (offsetX >= 300.0f && velocity >= 3000.0f)) {
                                            binder.player.removeMediaItem(window.firstPeriodIndex)
                                        } else {
                                            offsetX = 0f
                                        }
                                    }
                                )
                                .offset{ IntOffset(offsetX.roundToInt(), 0) }

                        )
                    }

                    item {
                        if (binder.isLoadingRadio) {
                            Column(
                                modifier = Modifier
                                    .shimmer()
                            ) {
                                repeat(3) { index ->
                                    SongItemPlaceholder(
                                        thumbnailSizeDp = thumbnailSizeDp,
                                        modifier = Modifier
                                            .alpha(1f - index * 0.125f)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                FloatingActionsContainerWithScrollToTop(
                    lazyListState = reorderingState.lazyListState,
                    iconId = R.drawable.shuffle,
                    visible = !reorderingState.isDragging,
                    windowInsets = windowInsets.only(WindowInsetsSides.Horizontal),
                    onClick = {
                        reorderingState.coroutineScope.launch {
                            reorderingState.lazyListState.smoothScrollToTop()
                        }.invokeOnCompletion {
                            player.shuffleQueue()
                        }
                    }
                )
            }


            Box(
                modifier = Modifier
                    .clickable(onClick = layoutState::collapseSoft)
                    .background(colorPalette.background2)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(horizontalBottomPaddingValues)
                    .height(64.dp)
            ) {
                BasicText(
                    text = "${windows.size} песни",
                    style = typography.xxs.medium,
                    modifier = Modifier
                        .background(
                            color = colorPalette.background1,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .align(Alignment.CenterStart)
                        .padding(all = 8.dp)
                )

                Image(
                    painter = painterResource(R.drawable.chevron_down),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorPalette.text),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(18.dp)
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { queueLoopEnabled = !queueLoopEnabled }
                        .background(colorPalette.background1)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.CenterEnd)
                        .animateContentSize()
                ) {
                    BasicText(
                        text = "Повтор очереди ",
                        style = typography.xxs.medium,
                    )

                    AnimatedContent(
                        targetState = queueLoopEnabled,
                        transitionSpec = {
                            val slideDirection = if (targetState) AnimatedContentScope.SlideDirection.Up else AnimatedContentScope.SlideDirection.Down

                            ContentTransform(
                                targetContentEnter = slideIntoContainer(slideDirection) + fadeIn(),
                                initialContentExit = slideOutOfContainer(slideDirection) + fadeOut(),
                            )
                        }
                    ) {
                        BasicText(
                            text = if (it) "ВКЛ." else "ВЫКЛ.",
                            style = typography.xxs.medium,
                        )
                    }
                }
            }
        }
    }
}
