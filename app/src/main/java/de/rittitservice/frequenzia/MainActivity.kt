package de.rittitservice.frequenzia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
}

@Composable
fun FrequenziaApp(viewModel: StationViewModel) {
    val navController = rememberNavController()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Column {
                currentStation?.let { station ->
                    MiniPlayer(
                        station = station,
                        isPlaying = isPlaying,
                        onTogglePlayPause = { viewModel.togglePlayPause() }
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
                val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
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
                val favorites by viewModel.favorites.collectAsStateWithLifecycle(initialValue = emptyList())
                FavoritesScreen(
                    favorites = favorites,
                    onStationClick = { viewModel.playStation(it) },
                    onFavoriteToggle = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val items = listOf(Screen.Search, Screen.Favorites)
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
                    Icon(
                        imageVector = if (screen is Screen.Search) Icons.Default.Search else Icons.Default.Star,
                        contentDescription = screen.label
                    )
                },
                label = { Text(screen.label) }
            )
        }
    }
}
