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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                FrequenziaApp(viewModel)
            }
        }
    }
}

private sealed class Screen(val route: String, val label: String) {
    data object Search : Screen("search", "Suche")
    data object Favorites : Screen("favorites", "Favoriten")
    data object RecentlyPlayed : Screen("recently_played", "Zuletzt gehört")
}

@Composable
fun FrequenziaApp(viewModel: StationViewModel) {
    val navController = rememberNavController()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
    val error by viewModel.error.collectAsStateWithLifecycle()
    var isPlayerExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
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
                            isPlaying = isPlaying,
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
                        onStationClick = { viewModel.playStation(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isFavorite = { it in favoriteIds }
                    )
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        favorites = favorites,
                        onStationClick = { viewModel.playStation(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) }
                    )
                }
                composable(Screen.RecentlyPlayed.route) {
                    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle(initialValue = emptyList())
                    val favoriteIds = favorites.map { it.stationuuid }.toSet()

                    RecentlyPlayedScreen(
                        recentlyPlayed = recentlyPlayed,
                        onStationClick = { viewModel.playStation(it) },
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
                    isPlaying = isPlaying,
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

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    val icon = when (screen) {
                        is Screen.Search -> Icons.Default.Search
                        is Screen.Favorites -> Icons.Default.Star
                        is Screen.RecentlyPlayed -> Icons.Default.History
                    }
                    Icon(imageVector = icon, contentDescription = screen.label)
                },
                label = { Text(screen.label) }
            )
        }
    }
}
