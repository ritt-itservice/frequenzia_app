package de.rittitservice.frequenzia.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.rittitservice.frequenzia.data.FavoritesDatabase
import de.rittitservice.frequenzia.data.RadioStation
import de.rittitservice.frequenzia.data.StationRepository
import de.rittitservice.frequenzia.data.toFavorite
import de.rittitservice.frequenzia.data.toRecentlyPlayed
import de.rittitservice.frequenzia.playback.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StationRepository()
    private val favoritesDao = FavoritesDatabase.getInstance(application).favoritesDao()
    private val recentlyPlayedDao = FavoritesDatabase.getInstance(application).recentlyPlayedDao()
    val playerController = PlayerController(application)

    private val _searchResults = MutableStateFlow<List<RadioStation>>(emptyList())
    val searchResults: StateFlow<List<RadioStation>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Sender, der tatsächlich im Player geladen ist (nicht nur zur Vorschau
    // angezeigt) – nur der zählt für den Play/Pause-Status und landet im
    // Verlauf "Zuletzt gehört".
    private val _nowPlayingStationId = MutableStateFlow<String?>(null)
    val nowPlayingStationId: StateFlow<String?> = _nowPlayingStationId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val favorites = favoritesDao.getAll()
    val recentlyPlayed = recentlyPlayedDao.getRecent()

    init {
        playerController.connect { controller ->
            controller.addListener(object : androidx.media3.common.Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _isPlaying.value = false
                    _error.value = "Wiedergabe fehlgeschlagen. Sender evtl. nicht erreichbar."
                }
            })
        }
        loadTopStations()
    }

    fun clearError() {
        _error.value = null
    }

    // Merkt sich die zuletzt fehlgeschlagene Aktion, damit der
    // "Wiederholen"-Button auf der Fehlermeldung genau das nochmal versuchen
    // kann (Top-Sender laden oder dieselbe Suche erneut ausführen).
    private var lastAction: (() -> Unit)? = null

    fun retryLastAction() {
        lastAction?.invoke()
    }

    fun loadTopStations() {
        lastAction = ::loadTopStations
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { repository.getTopStations() }
                .onSuccess { _searchResults.value = it }
                .onFailure { _error.value = "Sender konnten nicht geladen werden. Bitte erneut versuchen." }
            _isLoading.value = false
        }
    }

    fun search(query: String, countryCode: String? = null, tag: String? = null) {
        lastAction = { search(query, countryCode, tag) }
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                repository.searchStations(
                    name = query.ifBlank { null },
                    countryCode = countryCode,
                    tag = tag
                )
            }.onSuccess { _searchResults.value = it }
                .onFailure { _error.value = "Suche fehlgeschlagen. Bitte erneut versuchen." }
            _isLoading.value = false
        }
    }

    // Zeigt den Sender im Player an, ohne die Wiedergabe zu starten – für den
    // Tap auf eine Senderzeile (nur Vorschau).
    fun selectStation(station: RadioStation) {
        _currentStation.value = station
    }

    // Startet die tatsächliche Wiedergabe – nur das zählt als "gehört" und
    // landet im Verlauf.
    fun playStation(station: RadioStation) {
        _currentStation.value = station
        _nowPlayingStationId.value = station.stationuuid
        playerController.playStation(station)
        viewModelScope.launch {
            repository.registerClick(station.stationuuid)
            recentlyPlayedDao.insert(station.toRecentlyPlayed())
        }
    }

    fun togglePlayPause() {
        val station = _currentStation.value ?: return
        if (_nowPlayingStationId.value == station.stationuuid) {
            playerController.togglePlayPause()
        } else {
            // Angezeigter Sender ist noch nicht geladen (nur Vorschau) –
            // Play-Druck startet ihn jetzt tatsächlich.
            playStation(station)
        }
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
