package de.rittitservice.frequenzia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.rittitservice.frequenzia.data.FavoritesDatabase
import de.rittitservice.frequenzia.data.RadioStation
import de.rittitservice.frequenzia.data.StationRepository
import de.rittitservice.frequenzia.data.toFavorite
import de.rittitservice.frequenzia.playback.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StationRepository()
    private val favoritesDao = FavoritesDatabase.getInstance(application).favoritesDao()
    val playerController = PlayerController(application)

    private val _searchResults = MutableStateFlow<List<RadioStation>>(emptyList())
    val searchResults: StateFlow<List<RadioStation>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    val favorites = favoritesDao.getAll()

    init {
        playerController.connect { controller ->
            controller.addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
        }
        loadTopStations()
    }

    fun loadTopStations() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repository.getTopStations() }
                .onSuccess { _searchResults.value = it }
            _isLoading.value = false
        }
    }

    fun search(query: String, countryCode: String? = null, tag: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                repository.searchStations(
                    name = query.ifBlank { null },
                    countryCode = countryCode,
                    tag = tag
                )
            }.onSuccess { _searchResults.value = it }
            _isLoading.value = false
        }
    }

    fun playStation(station: RadioStation) {
        _currentStation.value = station
        playerController.playStation(station)
        viewModelScope.launch { repository.registerClick(station.stationuuid) }
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun toggleFavorite(station: RadioStation) {
        viewModelScope.launch {
            if (favoritesDao.isFavorite(station.stationuuid)) {
                favoritesDao.deleteByUuid(station.stationuuid)
            } else {
                favoritesDao.insert(station.toFavorite())
            }
        }
    }

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
