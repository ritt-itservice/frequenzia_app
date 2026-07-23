package de.rittitservice.frequenzia

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.rittitservice.frequenzia.data.RadioStation
import de.rittitservice.frequenzia.ui.FavoritesScreen
import de.rittitservice.frequenzia.ui.InfoScreen
import de.rittitservice.frequenzia.ui.MiniPlayer
import de.rittitservice.frequenzia.ui.PlayerScreen
import de.rittitservice.frequenzia.ui.RecentlyPlayedScreen
import de.rittitservice.frequenzia.ui.SearchScreen
import de.rittitservice.frequenzia.ui.StationViewModel

// Ab welcher Fensterbreite neben der Navigationsleiste noch ein eigenes,
// dauerhaftes Player-Panel Platz hat (Googles "Expanded"-Fensterklasse für
// List-Detail-Layouts). Darunter – z. B. kleinere Tablets im Hochformat –
// würde ein festes Panel die Senderliste auf einen unbrauchbaren Streifen
// zusammenquetschen, daher fällt der Player dort wie auf dem Handy auf ein
// Vollbild-Overlay zurück.
private const val PLAYER_PANEL_MIN_WIDTH_DP = 840

// Master-Detail-Layout für Tablets (smallestScreenWidthDp >= 600): feste
// Navigationsleiste statt Bottom-Bar. Ab PLAYER_PANEL_MIN_WIDTH_DP zeigt der
// Player ein dauerhaftes Seitenpanel, darunter ein Vollbild-Overlay wie auf
// dem Handy.
@Composable
fun TabletApp(viewModel: StationViewModel) {
    val navController = rememberNavController()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val nowPlayingStationId by viewModel.nowPlayingStationId.collectAsStateWithLifecycle()
    val isCurrentStationPlaying = isPlaying && currentStation?.stationuuid == nowPlayingStationId
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // Panel/Overlay lässt sich manuell wegklicken, ohne dass die Wiedergabe
    // stoppt; beim Auswählen eines neuen Senders geht es automatisch wieder
    // auf, analog zu isPlayerExpanded auf dem Handy.
    var isPlayerPanelVisible by rememberSaveable { mutableStateOf(true) }
    // screenWidthDp (nicht smallestScreenWidthDp) ändert sich bewusst mit der
    // Rotation – ein 7"-Tablet im Hochformat soll den Player als Overlay
    // zeigen, im Querformat aber das Seitenpanel bekommen.
    val showPlayerAsPanel = LocalConfiguration.current.screenWidthDp >= PLAYER_PANEL_MIN_WIDTH_DP

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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabletNavigationRail(navController)

            Box(modifier = Modifier.weight(1f)) {
                if (showPlayerAsPanel) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.weight(1f)) {
                                TabletNavHost(navController, viewModel, favorites) {
                                    isPlayerPanelVisible = true
                                }
                            }
                            if (!isPlayerPanelVisible) {
                                currentStation?.let { station ->
                                    MiniPlayer(
                                        station = station,
                                        isPlaying = isCurrentStationPlaying,
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onExpand = { isPlayerPanelVisible = true }
                                    )
                                }
                            }
                        }

                        if (isPlayerPanelVisible) {
                            currentStation?.let { station ->
                                val favoriteIds = favorites.map { it.stationuuid }.toSet()
                                Surface(
                                    modifier = Modifier
                                        .width(400.dp)
                                        .fillMaxHeight(),
                                    color = MaterialTheme.colorScheme.background,
                                    tonalElevation = 2.dp
                                ) {
                                    PlayerScreen(
                                        station = station,
                                        isPlaying = isCurrentStationPlaying,
                                        isFavorite = station.stationuuid in favoriteIds,
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onFavoriteToggle = { viewModel.toggleFavorite(station) },
                                        onCollapse = { isPlayerPanelVisible = false },
                                        showCollapseButton = true
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            TabletNavHost(navController, viewModel, favorites) {
                                isPlayerPanelVisible = true
                            }
                        }
                        if (!isPlayerPanelVisible) {
                            currentStation?.let { station ->
                                MiniPlayer(
                                    station = station,
                                    isPlaying = isCurrentStationPlaying,
                                    onTogglePlayPause = { viewModel.togglePlayPause() },
                                    onExpand = { isPlayerPanelVisible = true }
                                )
                            }
                        }
                    }

                    // Voll qualifiziert, da RowScope (vom äußeren Row) hier
                    // noch als impliziter Receiver in Reichweite ist und mit
                    // der RowScope-Variante von AnimatedVisibility kollidiert.
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isPlayerPanelVisible,
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
                                onCollapse = { isPlayerPanelVisible = false },
                                showCollapseButton = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletNavHost(
    navController: NavHostController,
    viewModel: StationViewModel,
    favorites: List<de.rittitservice.frequenzia.data.FavoriteStation>,
    onStationActivated: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route
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
                    onStationActivated()
                },
                onStationPlay = {
                    viewModel.playStation(it)
                    onStationActivated()
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
                    onStationActivated()
                },
                onStationPlay = {
                    viewModel.playStation(it)
                    onStationActivated()
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
                    onStationActivated()
                },
                onStationPlay = {
                    viewModel.playStation(it)
                    onStationActivated()
                },
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                isFavorite = { it in favoriteIds }
            )
        }
        composable(Screen.Info.route) {
            InfoScreen()
        }
    }
}

@Composable
private fun TabletNavigationRail(navController: NavHostController) {
    val items = listOf(Screen.Search, Screen.Favorites, Screen.RecentlyPlayed, Screen.Info)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationRail(
        // Standardbreite (80dp) schneidet "Zuletzt gehört" ab; etwas breiter
        // reicht für zwei Textzeilen ohne Kürzung.
        modifier = Modifier.width(96.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            val icon = when (screen) {
                is Screen.Search -> Icons.Default.Search
                is Screen.Favorites -> Icons.Default.Star
                is Screen.RecentlyPlayed -> Icons.Default.History
                is Screen.Info -> Icons.Default.Info
            }
            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = screen.label) },
                label = {
                    Text(
                        screen.label,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
