package it.hamy.muza.preferences

import it.hamy.muza.GlobalPreferencesHolder
import it.hamy.muza.enums.AlbumSortBy
import it.hamy.muza.enums.ArtistSortBy
import it.hamy.muza.enums.PlaylistSortBy
import it.hamy.muza.enums.SongSortBy
import it.hamy.muza.enums.SortOrder

object OrderPreferences : GlobalPreferencesHolder() {
    var songSortOrder by enum(SortOrder.Descending)
    var localSongSortOrder by enum(SortOrder.Descending)
    var playlistSortOrder by enum(SortOrder.Descending)
    var albumSortOrder by enum(SortOrder.Descending)
    var artistSortOrder by enum(SortOrder.Descending)

    var songSortBy by enum(SongSortBy.DateAdded)
    var localSongSortBy by enum(SongSortBy.DateAdded)
    var playlistSortBy by enum(PlaylistSortBy.DateAdded)
    var albumSortBy by enum(AlbumSortBy.DateAdded)
    var artistSortBy by enum(ArtistSortBy.DateAdded)
}
