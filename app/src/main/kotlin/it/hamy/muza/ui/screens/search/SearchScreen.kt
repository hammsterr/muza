package it.hamy.muza.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import it.hamy.compose.persist.PersistMapCleanup
import it.hamy.compose.routing.RouteHandler
import it.hamy.muza.R
import it.hamy.muza.ui.components.themed.Scaffold
import it.hamy.muza.ui.screens.GlobalRoutes
import it.hamy.muza.ui.screens.Route
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.secondary

@Route
@Composable
fun SearchScreen(
    initialTextInput: String,
    onSearch: (String) -> Unit,
    onViewPlaylist: (String) -> Unit
) {
    val saveableStateHolder = rememberSaveableStateHolder()

    val (tabIndex, onTabChanged) = rememberSaveable { mutableIntStateOf(0) }

    val (textFieldValue, onTextFieldValueChanged) = rememberSaveable(
        initialTextInput,
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(
            TextFieldValue(
                text = initialTextInput,
                selection = TextRange(initialTextInput.length)
            )
        )
    }

    PersistMapCleanup(prefix = "search/")

    RouteHandler(listenToGlobalEmitter = true) {
        GlobalRoutes()

        NavHost {
            val decorationBox: @Composable (@Composable () -> Unit) -> Unit = { innerTextField ->
                Box {
                    AnimatedVisibility(
                        visible = textFieldValue.text.isEmpty(),
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(300)),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        BasicText(
                            text = stringResource(R.string.search_placeholder),
                            maxLines = 1,
                            style = LocalAppearance.current.typography.xxl.secondary
                        )
                    }

                    innerTextField()
                }
            }

            Scaffold(
                topIconButtonId = R.drawable.chevron_back,
                onTopIconButtonClick = pop,
                tabIndex = tabIndex,
                onTabChanged = onTabChanged,
                tabColumnContent = { item ->
                    item(0, stringResource(R.string.online), R.drawable.globe)
                    item(1, stringResource(R.string.library), R.drawable.library)
                }
            ) { currentTabIndex ->
                saveableStateHolder.SaveableStateProvider(currentTabIndex) {
                    when (currentTabIndex) {
                        0 -> OnlineSearch(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            onSearch = onSearch,
                            onViewPlaylist = onViewPlaylist,
                            decorationBox = decorationBox
                        )

                        1 -> LocalSongSearch(
                            textFieldValue = textFieldValue,
                            onTextFieldValueChanged = onTextFieldValueChanged,
                            decorationBox = decorationBox
                        )
                    }
                }
            }
        }
    }
}
