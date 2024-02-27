package it.hamy.muza.preferences

import it.hamy.muza.GlobalPreferencesHolder

object UIStatePreferences : GlobalPreferencesHolder() {
    var homeScreenTabIndex by int(0)
    var searchResultScreenTabIndex by int(0)

    var artistScreenTabIndexProperty = int(0)
    var artistScreenTabIndex by artistScreenTabIndexProperty
}
