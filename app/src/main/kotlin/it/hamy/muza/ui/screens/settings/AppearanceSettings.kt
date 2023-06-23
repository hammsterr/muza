package it.hamy.muza.ui.screens.settings

import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.enums.ColorPaletteMode
import it.hamy.muza.enums.ColorPaletteName
import it.hamy.muza.enums.ThumbnailRoundness
import it.hamy.muza.ui.components.themed.Header
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.applyFontPaddingKey
import it.hamy.muza.utils.colorPaletteModeKey
import it.hamy.muza.utils.colorPaletteNameKey
import it.hamy.muza.utils.isAtLeastAndroid13
import it.hamy.muza.utils.isShowingThumbnailInLockscreenKey
import it.hamy.muza.utils.rememberPreference
import it.hamy.muza.utils.thumbnailRoundnessKey
import it.hamy.muza.utils.useSystemFontKey
import it.hamy.muza.*


@ExperimentalAnimationApi
@Composable
fun AppearanceSettings() {
    val (colorPalette) = LocalAppearance.current

    var colorPaletteName by rememberPreference(colorPaletteNameKey, ColorPaletteName.Dynamic)
    var colorPaletteMode by rememberPreference(colorPaletteModeKey, ColorPaletteMode.System)
    var thumbnailRoundness by rememberPreference(
        thumbnailRoundnessKey,
        ThumbnailRoundness.Light
    )
    var useSystemFont by rememberPreference(useSystemFontKey, false)
    var applyFontPadding by rememberPreference(applyFontPaddingKey, false)
    var isShowingThumbnailInLockscreen by rememberPreference(
        isShowingThumbnailInLockscreenKey,
        false

    )

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
        Header(title = "Внешний Вид")

        SettingsEntryGroupText(title = "ЦВЕТА")

        EnumValueSelectorSettingsEntry(
            title = "Цвет темы",
            selectedValue = colorPaletteName,
            onValueSelected = { colorPaletteName = it }
        )

        EnumValueSelectorSettingsEntry(
            title = "Ночная тема",
            selectedValue = colorPaletteMode,
            isEnabled = colorPaletteName != ColorPaletteName.PureBlack,
            onValueSelected = { colorPaletteMode = it }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "ФОРМЫ")

        EnumValueSelectorSettingsEntry(
            title = "Скругление",
            selectedValue = thumbnailRoundness,
            onValueSelected = { thumbnailRoundness = it },
            trailingContent = {
                Spacer(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = colorPalette.accent,
                            shape = thumbnailRoundness.shape()
                        )
                        .background(
                            color = colorPalette.background1,
                            shape = thumbnailRoundness.shape()
                        )
                        .size(36.dp)
                )
            }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "ТЕКСТ")

        SwitchSettingEntry(
            title = "Системный шрифт",
            text = "Использовать системный шрифт",
            isChecked = useSystemFont,
            onCheckedChange = { useSystemFont = it }
        )

        SwitchSettingEntry(
            title = "Заполнение шрифта",
            text = "Увеличить пробелы текста",
            isChecked = applyFontPadding,
            onCheckedChange = { applyFontPadding = it }
        )

        if (!isAtLeastAndroid13) {
            SettingsGroupSpacer()

            SettingsEntryGroupText(title = "ЭКРАН БЛОКИРОВКИ")

            SwitchSettingEntry(
                title = "Показывать обложку",
                text = "Использовать обложку в качестве обоев экрана блокировки",
                isChecked = isShowingThumbnailInLockscreen,
                onCheckedChange = { isShowingThumbnailInLockscreen = it }
            )
        }
    }
}


