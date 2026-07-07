package de.rittitservice.frequenzia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.rittitservice.frequenzia.data.RadioStation
import de.rittitservice.frequenzia.data.RecentlyPlayedStation

@Composable
fun RecentlyPlayedScreen(
    recentlyPlayed: List<RecentlyPlayedStation>,
    onStationClick: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation) -> Unit,
    isFavorite: (String) -> Boolean
) {
    if (recentlyPlayed.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Noch nichts gehört.\nSpiel einen Sender ab, er erscheint dann hier.")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(recentlyPlayed, key = { it.stationuuid }) { entry ->
            val station = entry.toRadioStation()
            StationRow(
                station = station,
                isFavorite = isFavorite(station.stationuuid),
                onClick = { onStationClick(station) },
                onFavoriteToggle = { onFavoriteToggle(station) }
            )
        }
    }
}

private fun RecentlyPlayedStation.toRadioStation() = RadioStation(
    stationuuid = stationuuid,
    name = name,
    url_resolved = url_resolved,
    favicon = favicon,
    countrycode = countrycode,
    country = countrycode,
    tags = tags,
    codec = null,
    bitrate = null
)
