package de.rittitservice.frequenzia.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.rittitservice.frequenzia.data.FavoriteStation
import de.rittitservice.frequenzia.data.RadioStation
import kotlinx.coroutines.launch
import java.util.Locale

private val ALPHABET_RAIL_LETTERS = listOf("#") + ('A'..'Z').map { it.toString() }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    favorites: List<FavoriteStation>,
    onStationSelect: (RadioStation) -> Unit,
    onStationPlay: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation) -> Unit
) {
    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Noch keine Favoriten gespeichert.\nTippe bei einem Sender auf den Stern.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            placeholder = { Text("Favoriten durchsuchen …") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        // Rein lokales Filtern über die bereits geladenen Favoriten – kein
        // Netzwerk-Aufruf nötig, daher auch kein Debounce wie in der
        // Sendersuche.
        val filtered = remember(favorites, query) {
            if (query.isBlank()) {
                favorites
            } else {
                favorites.filter { it.name.contains(query, ignoreCase = true) }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Keine Favoriten gefunden für „$query“.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@FavoritesScreen
        }

        val grouped = remember(filtered) { groupStationsByLetter(filtered) }
        val groupStartIndices = remember(grouped) { computeGroupStartIndices(grouped) }
        val availableLetters = remember(grouped) { grouped.map { it.first }.toSet() }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Row(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                state = listState
            ) {
                grouped.forEach { (letter, stations) ->
                    stickyHeader {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    items(stations, key = { it.stationuuid }) { fav ->
                        val station = fav.toRadioStation()
                        StationRow(
                            station = station,
                            isFavorite = true,
                            onSelect = { onStationSelect(station) },
                            onPlay = { onStationPlay(station) },
                            onFavoriteToggle = { onFavoriteToggle(station) }
                        )
                    }
                }
            }

            // A–Z-Schnellzugriff: Tippen auf einen Buchstaben springt zum
            // jeweiligen Abschnitt. Buchstaben ohne eigene Favoriten sind
            // abgeblendet und nicht antippbar. Jeder Buchstabe bekommt sein
            // eigenes clickable statt einer selbstgebauten Gestenerkennung
            // über die ganze Spalte – zuverlässiger und großzügigere
            // Trefferfläche pro Buchstabe.
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ALPHABET_RAIL_LETTERS.forEach { letter ->
                    val isAvailable = letter in availableLetters
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .then(
                                if (isAvailable) {
                                    Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        val targetIndex = groupStartIndices.getValue(letter)
                                        coroutineScope.launch { listState.scrollToItem(targetIndex) }
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = 10.sp,
                            fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAvailable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Gruppiert nach Anfangsbuchstabe (Großschreibung); Sendernamen ohne
// führenden Buchstaben (Ziffern, Sonderzeichen) landen gemeinsam im
// "#"-Abschnitt, der vor "A" einsortiert wird. Als eigenständige Funktion
// extrahiert, damit sie sich ohne Compose/UI direkt testen lässt.
internal fun groupStationsByLetter(stations: List<FavoriteStation>): List<Pair<String, List<FavoriteStation>>> {
    val sorted = stations.sortedBy { it.name.trim().uppercase(Locale.GERMAN) }
    val grouped = sorted.groupBy { station ->
        val first = station.name.trim().firstOrNull()?.uppercaseChar()
        if (first != null && first.isLetter()) first.toString() else "#"
    }
    return grouped.entries
        .sortedWith(compareBy { if (it.key == "#") "" else it.key })
        .map { it.key to it.value }
}

// Berechnet für jeden Buchstaben-Abschnitt den Index seines stickyHeader-
// Eintrags innerhalb der flachen LazyColumn-Item-Liste (Header + Zeilen pro
// Gruppe), damit LazyListState.scrollToItem() gezielt dorthin springen kann.
internal fun computeGroupStartIndices(grouped: List<Pair<String, List<FavoriteStation>>>): Map<String, Int> {
    var index = 0
    val result = mutableMapOf<String, Int>()
    grouped.forEach { (letter, stations) ->
        result[letter] = index
        index += 1 + stations.size
    }
    return result
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
