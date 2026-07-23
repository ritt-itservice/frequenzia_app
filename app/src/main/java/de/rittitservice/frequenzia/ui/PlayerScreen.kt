package de.rittitservice.frequenzia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.rittitservice.frequenzia.R
import de.rittitservice.frequenzia.data.RadioStation

@Composable
fun PlayerScreen(
    station: RadioStation,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCollapse: () -> Unit,
    showCollapseButton: Boolean = true
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            // Ohne eigenen Pointer-Input-Consumer sind Bereiche ohne Klick-
            // Handler (z. B. neben dem Cover) für Compose beim Hit-Testing
            // unsichtbar – Taps würden sonst zur darunterliegenden Senderliste
            // durchgereicht und dort einen anderen Sender auswählen.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        // Nicht die Geräte-Ausrichtung prüfen, sondern die tatsächlich
        // verfügbare Fläche: Im Tablet-Seitenpanel ist das Gerät oft im
        // Querformat, das Panel selbst aber schmal und hoch – dort muss
        // trotzdem das (schmale) Hochformat-Layout greifen.
        val isWide = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            TopBar(isPlaying, onCollapse, showCollapseButton)

            if (isWide) {
                LandscapePlayerContent(
                    station = station,
                    isPlaying = isPlaying,
                    isFavorite = isFavorite,
                    onTogglePlayPause = onTogglePlayPause,
                    onFavoriteToggle = onFavoriteToggle
                )
            } else {
                PortraitPlayerContent(
                    station = station,
                    isPlaying = isPlaying,
                    isFavorite = isFavorite,
                    onTogglePlayPause = onTogglePlayPause,
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }
    }
}

// Obere Leiste: Einklappen (Sendername bleibt dabei sichtbar – Zurück-Pfeil
// statt Chevron, analog zur Referenz). Im Tablet-Seitenpanel gibt es nichts
// einzuklappen, daher dort ausblendbar.
@Composable
private fun TopBar(isPlaying: Boolean, onCollapse: () -> Unit, showCollapseButton: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showCollapseButton) {
            IconButton(onClick = onCollapse) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Minimieren",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            if (isPlaying) "Wird abgespielt" else "Sender",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(48.dp)) // Balance zur linken Icon-Breite
    }
}

@Composable
private fun Cover(station: RadioStation, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .padding(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = station.favicon,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_launcher_foreground)
        )
    }
}

@Composable
private fun StationInfo(station: RadioStation, textAlign: TextAlign) {
    // Manche Sendernamen sind ungewöhnlich lang (z. B. "-- TOP 100 CHARTS --
    // DJ MIXES --"). Ohne Begrenzung würde das den Player unbegrenzt in die
    // Höhe schieben, genau wie zuvor bei der Beschreibung – daher dieselbe
    // 3-Zeilen-Begrenzung mit Scroll bei Bedarf.
    val nameStyle = MaterialTheme.typography.headlineSmall
    val density = LocalDensity.current
    val maxNameHeight = with(density) { (nameStyle.lineHeight.toPx() * 3).toDp() }

    Text(
        text = station.name,
        style = nameStyle,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = textAlign,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxNameHeight)
            .verticalScroll(rememberScrollState())
    )
    Spacer(modifier = Modifier.height(4.dp))

    // Land/Tags können bei manchen Sendern sehr lang werden (viele Tags).
    // Statt den Player dadurch unbegrenzt in die Höhe zu schieben, wird die
    // Beschreibung auf 3 Zeilen begrenzt und bei Bedarf scrollbar.
    val descriptionStyle = MaterialTheme.typography.bodyMedium
    val maxDescriptionHeight = with(density) { (descriptionStyle.lineHeight.toPx() * 3).toDp() }

    Text(
        text = listOfNotNull(station.country, station.tags).joinToString(" · "),
        style = descriptionStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxDescriptionHeight)
            .verticalScroll(rememberScrollState())
    )
}

@Composable
private fun Controls(
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorit",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }

        FilledIconButton(
            onClick = onTogglePlayPause,
            modifier = Modifier.size(80.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.size(48.dp)) // Balance zum Favoriten-Button
    }
}

@Composable
private fun PortraitPlayerContent(
    station: RadioStation,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Ein rein breitenbasiertes Cover (fillMaxWidth) sprengt in einem
        // schmalen, aber niedrigen Bereich (z. B. Tablet-Seitenpanel) die
        // verfügbare Höhe und drückt Waveform/Steuerung fast übereinander –
        // daher zusätzlich an der Höhe gedeckelt.
        val coverSize = minOf(maxWidth * 0.75f, maxHeight * 0.35f)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Kein Fortschritt/Scrubber – Live-Streams haben keine Position/Dauer.
            Cover(station, modifier = Modifier.size(coverSize))

            Spacer(modifier = Modifier.height(24.dp))
            StationInfo(station, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            StationWaveform(isPlaying = isPlaying)
            Spacer(modifier = Modifier.weight(1f))
            Controls(isPlaying, isFavorite, onTogglePlayPause, onFavoriteToggle)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Im Querformat wäre ein an der Breite orientiertes Cover höher als der
// Bildschirm und würde Name/Steuerung nach unten aus dem sichtbaren Bereich
// schieben – daher hier an der Höhe orientiert und links/rechts aufgeteilt.
@Composable
private fun LandscapePlayerContent(
    station: RadioStation,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTogglePlayPause: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Analog zum Hochformat: zusätzlich an der Breite gedeckelt, damit
        // ein sehr breiter, aber niedriger Bereich das Cover nicht über die
        // gesamte Fläche aufbläst.
        val coverSize = minOf(maxHeight * 0.75f, maxWidth * 0.4f)

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Cover(station, modifier = Modifier.size(coverSize))

            Spacer(modifier = Modifier.width(32.dp))

            Column(modifier = Modifier.weight(1f)) {
                StationInfo(station, textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.height(16.dp))
                StationWaveform(isPlaying = isPlaying)
                Spacer(modifier = Modifier.height(24.dp))
                Controls(isPlaying, isFavorite, onTogglePlayPause, onFavoriteToggle)
            }
        }
    }
}
