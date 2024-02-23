package it.hamy.muza.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import it.hamy.muza.GlobalPreferencesHolder
import it.hamy.muza.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

object DataPreferences : GlobalPreferencesHolder() {
    var topListLength by int(10)
    var topListPeriod by enum(TopListPeriod.AllTime)
    var quickPicksSource by enum(QuickPicksSource.Trending)

    enum class TopListPeriod(val displayName: @Composable () -> String, val duration: Duration? = null) {
        PastDay(displayName = { "Day" }, duration = 1.days),
        PastWeek(displayName = { "Week" }, duration = 7.days),
        PastMonth(displayName = { "Month" }, duration = 30.days),
        PastYear(displayName = { "Year" }, 365.days),
        AllTime(displayName = { "AllTime" })
    }

    enum class QuickPicksSource(val displayName: @Composable () -> String) {
        Trending(displayName = { "Trend" }),
        LastInteraction(displayName = { "LastInteraction" })
    }
}