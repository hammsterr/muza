package it.hamy.muza.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.hamy.muza.Database
import it.hamy.muza.DatabaseInitializer
import it.hamy.muza.LocalPlayerServiceBinder
import it.hamy.muza.R
import it.hamy.muza.preferences.AppearancePreferences
import it.hamy.muza.preferences.DataPreferences
import it.hamy.muza.preferences.PlayerPreferences
import it.hamy.muza.preferences.isProxyEnabledKey
import it.hamy.muza.preferences.proxyHostNameKey
import it.hamy.muza.preferences.proxyModeKey
import it.hamy.muza.preferences.proxyPortKey
import it.hamy.muza.preferences.rememberPreference
import it.hamy.muza.query
import it.hamy.muza.service.PlayerMediaBrowserService
import it.hamy.muza.ui.components.themed.SecondaryTextButton
import it.hamy.muza.ui.components.themed.SliderDialog
import it.hamy.muza.ui.screens.Route
import it.hamy.muza.utils.findActivity
import it.hamy.muza.utils.isAtLeastAndroid12
import it.hamy.muza.utils.isAtLeastAndroid6
import it.hamy.muza.utils.isIgnoringBatteryOptimizations
import it.hamy.muza.utils.smoothScrollToBottom
import it.hamy.muza.utils.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.net.Proxy
import kotlin.math.roundToInt
import kotlin.system.exitProcess

@SuppressLint("BatteryLife")
@Route
@Composable
fun OtherSettings() {
    val context = LocalContext.current
    val binder = LocalPlayerServiceBinder.current
    val uriHandler = LocalUriHandler.current

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

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

    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(context.isIgnoringBatteryOptimizations)
    }

    val activityResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { isIgnoringBatteryOptimizations = context.isIgnoringBatteryOptimizations }
    )

    val queriesCount by remember {
        Database.queriesCount().distinctUntilChanged()
    }.collectAsState(initial = 0)

    var proxyMode by rememberPreference(proxyModeKey, defaultValue = Proxy.Type.HTTP)

    var isProxyEnabled by rememberPreference(isProxyEnabledKey, false)

    var proxyHost by rememberPreference(proxyHostNameKey, defaultValue = "")

    var proxyPort by rememberPreference(proxyPortKey, defaultValue = 3128)

    SettingsCategoryScreen(
        title = stringResource(R.string.other),
        scrollState = scrollState
    ) {
        SettingsGroup(title = stringResource(R.string.android_auto)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.android_auto),
                text = stringResource(R.string.android_auto_description),
                isChecked = isAndroidAutoEnabled,
                onCheckedChange = { isAndroidAutoEnabled = it }
            )

            AnimatedVisibility(visible = isAndroidAutoEnabled) {
                SettingsDescription(text = stringResource(R.string.android_auto_warning))
            }
        }
        SettingsGroup(title = stringResource(R.string.search_history)) {
            SwitchSettingsEntry(
                title = stringResource(R.string.pause_search_history),
                text = stringResource(R.string.pause_search_history_description),
                isChecked = DataPreferences.pauseSearchHistory,
                onCheckedChange = { DataPreferences.pauseSearchHistory = it }
            )

            AnimatedVisibility(visible = !(DataPreferences.pauseSearchHistory && queriesCount == 0)) {
                SettingsEntry(
                    title = stringResource(R.string.clear_search_history),
                    text = if (queriesCount > 0) stringResource(
                        R.string.format_clear_search_history_amount,
                        queriesCount
                    )
                    else stringResource(R.string.empty_history),
                    onClick = { query(Database::clearQueries) },
                    isEnabled = queriesCount > 0
                )
            }
        }
        SettingsGroup(title = stringResource(R.string.built_in_playlists)) {
            IntSettingsEntry(
                title = stringResource(R.string.top_list_length),
                text = stringResource(R.string.top_list_length_description),
                currentValue = DataPreferences.topListLength,
                setValue = { DataPreferences.topListLength = it },
                defaultValue = 10,
                range = 1..500
            )
        }
        SettingsGroup(title = stringResource(R.string.quick_picks)) {
            EnumValueSelectorSettingsEntry(
                title = stringResource(R.string.quick_picks_source),
                selectedValue = DataPreferences.quickPicksSource,
                onValueSelected = { DataPreferences.quickPicksSource = it },
                valueText = { it.displayName() }
            )
        }
        SettingsGroup(title = stringResource(R.string.dynamic_thumbnails)) {
            var selectingThumbnailSize by remember { mutableStateOf(false) }
            SettingsEntry(
                title = stringResource(R.string.max_dynamic_thumbnail_size),
                text = stringResource(R.string.max_dynamic_thumbnail_size_description),
                onClick = {
                    selectingThumbnailSize = true
                }
            )
            if (selectingThumbnailSize) SliderDialog(
                onDismiss = { selectingThumbnailSize = false },
                title = stringResource(R.string.max_dynamic_thumbnail_size),
                provideState = {
                    remember(AppearancePreferences.maxThumbnailSize) {
                        mutableFloatStateOf(AppearancePreferences.maxThumbnailSize.toFloat())
                    }
                },
                onSlideCompleted = { AppearancePreferences.maxThumbnailSize = it.roundToInt() },
                min = 16f,
                max = 2160f,
                toDisplay = {
                    stringResource(
                        R.string.format_px,
                        it.roundToInt()
                    )
                }
            )
        }

        SettingsGroup(title = stringResource(R.string.proxy)) {


            SwitchSettingsEntry(
                title = stringResource(R.string.proxy),
                text = stringResource(R.string.proxy_desc),
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

        SettingsGroup(title = stringResource(R.string.service_lifetime)) {
            AnimatedVisibility(visible = !isIgnoringBatteryOptimizations) {
                SettingsDescription(
                    text = stringResource(R.string.service_lifetime_warning),
                    important = true
                )
            }

            if (isAtLeastAndroid12) SettingsDescription(
                text = stringResource(R.string.service_lifetime_warning_android_12)
            )

            SettingsEntry(
                title = stringResource(R.string.ignore_battery_optimizations),
                text = if (isIgnoringBatteryOptimizations) stringResource(R.string.ignoring_battery_optimizations)
                else stringResource(R.string.ignore_battery_optimizations_action),
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
                            activityResultLauncher.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        } catch (e: ActivityNotFoundException) {
                            context.toast(context.getString(R.string.no_battery_optimization_settings_found))
                        }
                    }
                },
                isEnabled = !isIgnoringBatteryOptimizations
            )

            AnimatedVisibility(!isAtLeastAndroid12 || isIgnoringBatteryOptimizations) {
                SwitchSettingsEntry(
                    title = stringResource(R.string.invincible_service),
                    text = stringResource(R.string.invincible_service_description),
                    isChecked = PlayerPreferences.isInvincibilityEnabled,
                    onCheckedChange = { PlayerPreferences.isInvincibilityEnabled = it }
                )
            }

            SettingsEntry(
                title = stringResource(R.string.need_help),
                text = stringResource(R.string.need_help_description),
                onClick = {
                    uriHandler.openUri("https://dontkillmyapp.com/")
                }
            )

            SettingsDescription(text = stringResource(R.string.service_lifetime_report_issue))
        }

        var showTroubleshoot by rememberSaveable { mutableStateOf(false) }

        AnimatedContent(showTroubleshoot, label = "") { show ->
            if (show) SettingsGroup(
                title = stringResource(R.string.troubleshooting),
                description = stringResource(R.string.troubleshooting_warning),
                important = true
            ) {
                val troubleshootScope = rememberCoroutineScope()
                var reloading by rememberSaveable { mutableStateOf(false) }

                SecondaryTextButton(
                    text = stringResource(R.string.reload_app_internals),
                    onClick = {
                        if (!reloading) troubleshootScope.launch {
                            reloading = true
                            binder?.restartForegroundOrStop()
                            DatabaseInitializer.reload()
                            reloading = false
                        }
                    },
                    enabled = !reloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                SecondaryTextButton(
                    text = stringResource(R.string.kill_app),
                    onClick = {
                        binder?.stopRadio()
                        binder?.invincible = false
                        context.findActivity().finishAndRemoveTask()
                        binder?.restartForegroundOrStop()
                        troubleshootScope.launch {
                            delay(500L)
                            Handler(Looper.getMainLooper()).postAtFrontOfQueue { exitProcess(0) }
                        }
                    },
                    enabled = !reloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .padding(horizontal = 16.dp)
                )
            } else SecondaryTextButton(
                text = stringResource(R.string.show_troubleshoot_section),
                onClick = {
                    coroutineScope.launch {
                        delay(500)
                        scrollState.smoothScrollToBottom()
                    }
                    showTroubleshoot = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
