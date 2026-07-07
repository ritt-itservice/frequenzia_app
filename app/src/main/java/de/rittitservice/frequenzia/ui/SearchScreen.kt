package de.rittitservice.frequenzia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.rittitservice.frequenzia.R
import de.rittitservice.frequenzia.data.RadioStation
import kotlinx.coroutines.delay

private const val SEARCH_DEBOUNCE_MS = 400L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    stations: List<RadioStation>,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onLoadTop: () -> Unit,
    onStationClick: (RadioStation) -> Unit,
    onFavoriteToggle: (RadioStation) -> Unit,
    isFavorite: (String) -> Boolean
) {
    var query by remember { mutableStateOf("") }

    // Sucht automatisch während des Tippens (entprellt, damit nicht bei
    // jedem Tastendruck eine eigene Netzwerkanfrage rausgeht).
    LaunchedEffect(query) {
        delay(SEARCH_DEBOUNCE_MS)
        if (query.isBlank()) onLoadTop() else onSearch(query)
    }

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
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            placeholder = { Text("Sender, Land oder Genre suchen …") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onSearch = { onSearch(query) }
            ),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            )
        )

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Text(
            text = if (query.isBlank()) "Beliebte Sender" else "Ergebnisse",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(stations, key = { it.stationuuid }) { station ->
                StationRow(
                    station = station,
                    isFavorite = isFavorite(station.stationuuid),
                    onClick = { onStationClick(station) },
                    onFavoriteToggle = { onFavoriteToggle(station) }
                )
            }
        }
    }
}

@Composable
fun StationRow(
    station: RadioStation,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            // Tippen auf die Zeile (z. B. den Sendernamen) spielt den Sender
            // ebenfalls ab und öffnet den Player – nicht nur der Play-Button.
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = station.favicon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                // Sender ohne eigenes Icon (oder mit kaputter Favicon-URL)
                // bekommen das App-Icon als Platzhalter statt eines leeren Feldes.
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(station.country, station.tags).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorit",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        FilledIconButton(
            onClick = onClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Abspielen")
        }
    }
}
