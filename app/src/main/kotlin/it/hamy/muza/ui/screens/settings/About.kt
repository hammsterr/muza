package it.hamy.muza.ui.screens.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import it.hamy.muza.BuildConfig
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.ui.components.themed.Header
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.secondary

@ExperimentalAnimationApi
@Composable
fun About() {
    val (colorPalette, typography) = LocalAppearance.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .background(colorPalette.background0)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
                    .asPaddingValues()
            )
    ) {
        Header(title = "Информация") {
            BasicText(
                text = "v${BuildConfig.VERSION_NAME} by Hamy",
                style = typography.s.secondary
            )
        }

        SettingsEntryGroupText(title = "СОЦИАЛЬНОЕ")

        SettingsEntry(
            title = "GitHub",
            text = "Посмотреть исходный код",
            onClick = {
                uriHandler.openUri("https://github.com/hammsterr/muza")
            }
        )

        SettingsEntry(
            title = "Новости",
            text = "Следите за новостями в группе ВКонтакте",
            onClick = {
                uriHandler.openUri("https://vk.com/hamyack")
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "ДИАГНОСТИКА")

        SettingsEntry(
            title = "Тех. поддержка",
            text = "Сообщайте об ошибках или пожеланиях",
            onClick = {
                uriHandler.openUri("https://hamyack.pages.dev")
            }
        )
    }
}
