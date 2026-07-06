package de.rittitservice.frequenzia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SignalRot,
    background = PaperWhite,
    surface = PaperWhite,
    onPrimary = PaperWhite,
    onBackground = InkBlack,
    onSurface = InkBlack,
    surfaceVariant = SurfaceGrey
)

private val DarkColors = darkColorScheme(
    primary = SignalRot,
    background = InkBlack,
    surface = InkBlack,
    onPrimary = PaperWhite,
    onBackground = PaperWhite,
    onSurface = PaperWhite
)

@Composable
fun FrequenziaTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
