package de.rittitservice.frequenzia.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Kleine, dekorative Equalizer-Anzeige (kein echtes Audio-Signal, da
 * Internetradio-Streams keine Rohdaten fürs Frontend liefern). Simuliert
 * per gestaffelt animierten Balken den Eindruck von "Sender läuft gerade".
 */
@Composable
fun StationWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5
) {
    val transition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val durationMillis = 500 + (index % 3) * 150
            val staggerOffset = index * 120

            val animatedHeight by transition.animateFloat(
                initialValue = 6f,
                targetValue = if (isPlaying) 28f else 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(staggerOffset, StartOffsetType.Delay)
                ),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
