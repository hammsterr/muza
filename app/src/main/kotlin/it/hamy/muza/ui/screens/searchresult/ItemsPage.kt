package it.hamy.muza.ui.screens.searchresult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.hamy.compose.persist.persist
import it.hamy.innertube.Innertube
import it.hamy.innertube.utils.plus
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.R
import it.hamy.muza.ui.components.ShimmerHost
import it.hamy.muza.ui.components.themed.FloatingActionsContainerWithScrollToTop
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.center
import it.hamy.muza.utils.secondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
inline fun <T : Innertube.Item> ItemsPage(
    tag: String,
    crossinline header: @Composable (textButton: (@Composable () -> Unit)?) -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemPlaceholderContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialPlaceholderCount: Int = 8,
    continuationPlaceholderCount: Int = 3,
    emptyItemsText: String = stringResource(R.string.no_items_found),
    noinline provider: (suspend (String?) -> Result<Innertube.ItemsPage<T>?>?)? = null
) = ItemsPage(
    tag = tag,
    header = { before, _ -> header(before) },
    itemContent = itemContent,
    itemPlaceholderContent = itemPlaceholderContent,
    modifier = modifier,
    initialPlaceholderCount = initialPlaceholderCount,
    continuationPlaceholderCount = continuationPlaceholderCount,
    emptyItemsText = emptyItemsText,
    provider = provider
)

@Composable
inline fun <T : Innertube.Item> ItemsPage(
    tag: String,
    crossinline header: @Composable (
        beforeContent: (@Composable () -> Unit)?,
        afterContent: (@Composable () -> Unit)?
    ) -> Unit,
    crossinline itemContent: @Composable LazyItemScope.(T) -> Unit,
    noinline itemPlaceholderContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initialPlaceholderCount: Int = 8,
    continuationPlaceholderCount: Int = 3,
    emptyItemsText: String = stringResource(R.string.no_items_found),
    noinline provider: (suspend (String?) -> Result<Innertube.ItemsPage<T>?>?)? = null
) {
    val (_, typography) = LocalAppearance.current
    val updatedProvider by rememberUpdatedState(provider)
    val lazyListState = rememberLazyListState()
    var itemsPage by persist<Innertube.ItemsPage<T>?>(tag)

    val shouldLoad by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }
    }

    LaunchedEffect(shouldLoad, updatedProvider) {
        if (!shouldLoad) return@LaunchedEffect
        val provideItems = updatedProvider ?: return@LaunchedEffect

        withContext(Dispatchers.IO) {
            provideItems(itemsPage?.continuation)
        }?.onSuccess {
            if (it == null) {
                if (itemsPage == null) itemsPage = Innertube.ItemsPage(null, null)
            } else itemsPage += it
        }?.onFailure {
            itemsPage = itemsPage?.copy(continuation = null)
        }?.exceptionOrNull()?.printStackTrace()
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                .asPaddingValues(),
            modifier = Modifier.fillMaxSize()
        ) {
            item(
                key = "header",
                contentType = "header"
            ) {
                header(null, null)
            }

            items(
                items = itemsPage?.items ?: emptyList(),
                key = Innertube.Item::key,
                itemContent = itemContent
            )

            if (itemsPage != null && itemsPage?.items.isNullOrEmpty()) item(key = "empty") {
                BasicText(
                    text = emptyItemsText,
                    style = typography.xs.secondary.center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .fillMaxWidth()
                )
            }

            if (!(itemsPage != null && itemsPage?.continuation == null)) item(key = "loading") {
                val isFirstLoad = itemsPage?.items.isNullOrEmpty()

                ShimmerHost(
                    modifier = if (isFirstLoad) Modifier.fillParentMaxSize() else Modifier
                ) {
                    repeat(if (isFirstLoad) initialPlaceholderCount else continuationPlaceholderCount) {
                        itemPlaceholderContent()
                    }
                }
            }
        }

        FloatingActionsContainerWithScrollToTop(lazyListState = lazyListState)
    }
}
