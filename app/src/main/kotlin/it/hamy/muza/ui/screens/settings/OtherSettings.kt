package it.hamy.muza.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import it.hamy.muza.Database
import it.hamy.muza.LocalPlayerAwareWindowInsets
import it.hamy.muza.query
import it.hamy.muza.service.PlayerMediaBrowserService
import it.hamy.muza.ui.components.themed.Header
import it.hamy.muza.ui.styling.LocalAppearance
import it.hamy.muza.utils.isAtLeastAndroid12
import it.hamy.muza.utils.isAtLeastAndroid6
import it.hamy.muza.utils.isIgnoringBatteryOptimizations
import it.hamy.muza.utils.isInvincibilityEnabledKey
import it.hamy.muza.utils.isProxyEnabledKey
import it.hamy.muza.utils.pauseSearchHistoryKey
import it.hamy.muza.utils.proxyHostNameKey
import it.hamy.muza.utils.proxyModeKey
import it.hamy.muza.utils.proxyPortKey
import it.hamy.muza.utils.rememberPreference
import it.hamy.muza.utils.toast
import kotlinx.coroutines.flow.distinctUntilChanged
import java.net.Proxy



@SuppressLint("BatteryLife")
@ExperimentalAnimationApi
@Composable
fun OtherSettings() {
    val context = LocalContext.current
    val (colorPalette) = LocalAppearance.current

    var isAndroidAutoEnabled by remember {
        val component = ComponentName(context, PlayerMediaBrowserService::class.java)
        val disabledFlag = PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val enabledFlag = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        mutableStateOf(
            value = context.packageManager.getComponentEnabledSetting(component) == enabledFlag,
            policy = object : SnapshotMutationPolicy<Boolean> {
                override fun equivalent(a: Boolean, b: Boolean): Boolean {
                    context.packageManager.setComponentEnabledSetting(
                        component,
                        if (b) enabledFlag else disabledFlag,
                        PackageManager.DONT_KILL_APP
                    )
                    return a == b
                }
            }
        )
    }

    var isInvincibilityEnabled by rememberPreference(isInvincibilityEnabledKey, false)

    var isProxyEnabled by rememberPreference(isProxyEnabledKey, false)

    var proxyHost by rememberPreference(proxyHostNameKey, defaultValue = "")

    var proxyPort by rememberPreference(proxyPortKey, defaultValue = 1080)

    var proxyMode by rememberPreference(proxyModeKey, defaultValue = Proxy.Type.HTTP)

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations
        }

    var pauseSearchHistory by rememberPreference(pauseSearchHistoryKey, false)

    val queriesCount by remember {
        Database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

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
        Header(title = "Другое")

        SettingsEntryGroupText(title = "АНДРОИД АВТО")

        SettingsDescription(text = "Включите опцию \"неизвестные источники\" в настройках разработчика в Андроид Авто.")

        SwitchSettingEntry(
            title = "Android Auto",
            text = "Включить поддержку Андроид Авто",
            isChecked = isAndroidAutoEnabled,
            onCheckedChange = { isAndroidAutoEnabled = it }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "ИСТОРИЯ ПОИСКА")

        SwitchSettingEntry(
            title = "Приостановить историю",
            text = "Не сохранять историю поиска",
            isChecked = pauseSearchHistory,
            onCheckedChange = { pauseSearchHistory = it }
        )

        SettingsEntry(
            title = "Очистить историю поиска",
            text = if (queriesCount > 0) {
                "Удалить $queriesCount поисковых запросов"
            } else {
                "История чиста"
            },
            isEnabled = queriesCount > 0,
            onClick = { query(Database::clearQueries) }
        )

        SettingsGroupSpacer()

        SettingsEntryGroupText(title = "ОПТИМИЗАЦИЯ БАТАРЕИ")

        ImportantSettingsDescription(text = "Если включена экономия батареи, воспроизведение может внезапно остановиться!")

        if (isAtLeastAndroid12) {
            SettingsDescription(text = "Android 12+: Обязательно отключите экономию батареи, прежде чем включать опцию \"Invincible service\"!")
        }

        SettingsEntry(
            title = "Игнор. экономии батареи ",
            isEnabled = !isIgnoringBatteryOptimizations,
            text = if (isIgnoringBatteryOptimizations) {
                "Уже игнорируется"
            } else {
                "Отключить остановку приложения в фоне"
            },
            onClick = {
                if (!isAtLeastAndroid6) return@SettingsEntry

                try {
                    activityResultLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                } catch (e: ActivityNotFoundException) {
                    try {
                        activityResultLauncher.launch(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.toast("не найдено настроек батареи! Добавьте приложение в белый список вручную")
                    }
                }
            }
        )

        SwitchSettingEntry(
            title = "Invincible service",
            text = "Обход экономии батареи",
            isChecked = isInvincibilityEnabled,
            onCheckedChange = { isInvincibilityEnabled = it }
        )


        SettingsEntryGroupText(title = "PROXY")

        SwitchSettingEntry(
            title = "Proxy",
            text = "Включить proxy",
            isChecked = isProxyEnabled,
            onCheckedChange = { isProxyEnabled = it }
        )

        AnimatedVisibility(visible = isProxyEnabled) {
            Column {
                EnumValueSelectorSettingsEntry(title = "Proxy",
                    selectedValue = proxyMode, onValueSelected = {proxyMode = it})
                TextDialogSettingEntry(
                    title = "Хост",
                    text = "Введите хост",
                    currentText = proxyHost,
                    onTextSave = { proxyHost = it })
                TextDialogSettingEntry(
                    title = "Порт",
                    text = "Введите порт",
                    currentText = proxyPort.toString(),
                    onTextSave = { proxyPort = it.toIntOrNull() ?: 1080 })
            }
        }

    }
}
