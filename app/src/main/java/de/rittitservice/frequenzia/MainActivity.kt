package de.rittitservice.frequenzia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.rittitservice.frequenzia.ui.FavoritesScreen
import de.rittitservice.frequenzia.ui.MiniPlayer
import de.rittitservice.frequenzia.ui.SearchScreen
import de.rittitservice.frequenzia.ui.StationViewModel
import de.rittitservice.frequenzia.ui.theme.FrequenziaTheme
import de.rittitservice.frequenzia.ui.PlayerScreen
import de.rittitservice.frequenzia.ui.RecentlyPlayedScreen

class MainActivity : ComponentActivity() {

    private val viewModel: StationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrequenziaTheme {
                // smallestScreenWidthDp ist geräteabhängig und ändert sich
                // nicht bei Rotation (klassische sw600dp-Tablet-Erkennung) –
                // ein gedrehtes Handy bleibt damit beim Handy-Layout.
                val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
                if (isTablet) {
                    TabletApp(viewModel)
                } else {
                    FrequenziaApp(viewModel)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String) {
    data object Search : Screen("search", "Suche")
    data object Favorites : Screen("favorites", "Favoriten")
    data object RecentlyPlayed : Screen("recently_played", "Zuletzt gehört")
}

@Composable
fun FrequenziaApp(viewModel: StationViewModel) {
    val navController = rememberNavController()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val nowPlayingStationId by viewModel.nowPlayingStationId.collectAsStateWithLifecycle()
    // Nur "wirklich spielt" für den gerade angezeigten Sender, nicht nur
    // zur Vorschau geöffnet.
    val isCurrentStationPlaying = isPlaying && currentStation?.stationuuid == nowPlayingStationId
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val error by viewModel.error.collectAsStateWithLifecycle()
    // rememberSaveable statt remember: sonst geht der aufgeklappte Player bei
    // einer Konfigurationsänderung (z. B. Drehen des Geräts) verloren, weil
    // die Activity dabei neu erstellt wird.
    var isPlayerExpanded by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            val result = snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "Wiederholen",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryLastAction()
            }
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column {
                    currentStation?.let { station ->
                        MiniPlayer(
                            station = station,
                            isPlaying = isCurrentStationPlaying,
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onExpand = { isPlayerExpanded = true }
                        )
                    }
                    BottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Search.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Search.route) {
                    val stations by viewModel.searchResults.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                    val favoriteIds = favorites.map { it.stationuuid }.toSet()

                    SearchScreen(
                        stations = stations,
                        isLoading = isLoading,
                        onSearch = { query -> viewModel.search(query) },
                        onLoadTop = { viewModel.loadTopStations() },
                        onStationSelect = {
                            viewModel.selectStation(it)
                            isPlayerExpanded = true
                        },
                        onStationPlay = {
                            viewModel.playStation(it)
                            isPlayerExpanded = true
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isFavorite = { it in favoriteIds }
                    )
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        favorites = favorites,
                        onStationSelect = {
                            viewModel.selectStation(it)
                            isPlayerExpanded = true
                        },
                        onStationPlay = {
                            viewModel.playStation(it)
                            isPlayerExpanded = true
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
                composable(Screen.RecentlyPlayed.route) {
                    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle(initialValue = emptyList())
                    val favoriteIds = favorites.map { it.stationuuid }.toSet()

                    RecentlyPlayedScreen(
                        recentlyPlayed = recentlyPlayed,
                        onStationSelect = {
                            viewModel.selectStation(it)
                            isPlayerExpanded = true
                        },
                        onStationPlay = {
                            viewModel.playStation(it)
                            isPlayerExpanded = true
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isFavorite = { it in favoriteIds }
                    )
                }
            }
        }

        // Vollbild-Player als sanft einschiebendes Overlay (Slide-up von unten)
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }) + fadeOut()
        ) {
            currentStation?.let { station ->
                val favoriteIds = favorites.map { it.stationuuid }.toSet()
                PlayerScreen(
                    station = station,
                    isPlaying = isCurrentStationPlaying,
                    isFavorite = station.stationuuid in favoriteIds,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onFavoriteToggle = { viewModel.toggleFavorite(station) },
                    onCollapse = { isPlayerExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val items = listOf(Screen.Search, Screen.Favorites, Screen.RecentlyPlayed)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val selected = currentRoute == screen.route
                val icon = when (screen) {
                    is Screen.Search -> Icons.Default.Search
                    is Screen.Favorites -> Icons.Default.Star
                    is Screen.RecentlyPlayed -> Icons.Default.History
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.label,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
