package de.rittitservice.frequenzia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.rittitservice.frequenzia.data.FavoriteStation
import de.rittitservice.frequenzia.data.RadioStation

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteStation>,
    onStationClick: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Noch keine Favoriten gespeichert.\nTippe bei einem Sender auf den Stern.")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(favorites, key = { it.stationuuid }) { fav ->
            val station = fav.toRadioStation()
            StationRow(
                station = station,
                isFavorite = true,
                onClick = { onStationClick(station) },
                onFavoriteToggle = { onFavoriteToggle(station) }
            )
        }
    }
}

private fun FavoriteStation.toRadioStation() = RadioStation(
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
