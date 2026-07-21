package de.rittitservice.frequenzia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.rittitservice.frequenzia.data.RadioStation
import de.rittitservice.frequenzia.data.RecentlyPlayedStation

@Composable
fun RecentlyPlayedScreen(
    recentlyPlayed: List<RecentlyPlayedStation>,
    onStationSelect: (RadioStation) -> Unit,
    onStationPlay: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation) -> Unit,
    isFavorite: (String) -> Boolean,
    onClearHistory: () -> Unit = {}
) {
    if (recentlyPlayed.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Noch nichts gehört.\nSpiel einen Sender ab, er erscheint dann hier.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zuletzt gehört",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showClearConfirmation = true }) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Verlauf leeren",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(recentlyPlayed, key = { it.stationuuid }) { entry ->
                val station = entry.toRadioStation()
                StationRow(
                    station = station,
                    isFavorite = isFavorite(station.stationuuid),
                    onSelect = { onStationSelect(station) },
                    onPlay = { onStationPlay(station) },
                    onFavoriteToggle = { onFavoriteToggle(station) }
                )
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Verlauf leeren?") },
            text = { Text("Der gesamte Wiedergabeverlauf wird endgültig gelöscht. Das kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showClearConfirmation = false
                }) {
                    Text("Leeren", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Abbrechen")
                }
            }
        )
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
