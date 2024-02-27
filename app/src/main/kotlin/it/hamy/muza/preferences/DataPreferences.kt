package it.hamy.muza.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.hamy.muza.GlobalPreferencesHolder
import it.hamy.muza.R
import it.hamy.muza.enums.CoilDiskCacheSize
import it.hamy.muza.enums.ExoPlayerDiskCacheSize
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

object DataPreferences : GlobalPreferencesHolder() {
    var coilDiskCacheMaxSize by enum(CoilDiskCacheSize.`128MB`)
    var exoPlayerDiskCacheMaxSize by enum(ExoPlayerDiskCacheSize.`2GB`)
    var pauseHistory by boolean(false)
    var pausePlaytime by boolean(false)
    var pauseSearchHistory by boolean(false)
    val topListLengthProperty = int(10)
    var topListLength by topListLengthProperty
    val topListPeriodProperty = enum(TopListPeriod.AllTime)
    var topListPeriod by topListPeriodProperty
    var quickPicksSource by enum(QuickPicksSource.Trending)

    enum class TopListPeriod(val displayName: @Composable () -> String, val duration: Duration? = null) {
        PastDay(displayName = { stringResource(R.string.past_24_hours) }, duration = 1.days),
        PastWeek(displayName = { stringResource(R.string.past_week) }, duration = 7.days),
        PastMonth(displayName = { stringResource(R.string.past_month) }, duration = 30.days),
        PastYear(displayName = { stringResource(R.string.past_year) }, 365.days),
        AllTime(displayName = { stringResource(R.string.all_time) })
    }

    enum class QuickPicksSource(val displayName: @Composable () -> String) {
        Trending(displayName = { stringResource(R.string.trending) }),
        LastInteraction(displayName = { stringResource(R.string.last_interaction) })
    }
}
