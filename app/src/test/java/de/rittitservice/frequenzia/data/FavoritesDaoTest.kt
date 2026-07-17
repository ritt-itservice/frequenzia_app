package de.rittitservice.frequenzia.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FavoritesDaoTest {

    private lateinit var db: FavoritesDatabase

    private val station = FavoriteStation(
        stationuuid = "abc-123",
        name = "Test FM",
        url_resolved = "https://stream.example/test",
        favicon = null,
        countrycode = "DE",
        tags = "pop"
    )

    private val radioStation = RadioStation(
        stationuuid = "abc-123",
        name = "Test FM",
        url_resolved = "https://stream.example/test",
        favicon = null,
        countrycode = "DE",
        country = "Germany",
        tags = "pop",
        codec = null,
        bitrate = null
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FavoritesDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndDeleteFavorite_updatesIsFavoriteAndGetAll() = runTest {
        val dao = db.favoritesDao()

        assertFalse(dao.isFavorite(station.stationuuid))

        dao.insert(station)
        assertTrue(dao.isFavorite(station.stationuuid))
        assertTrue(dao.getAll().first().any { it.stationuuid == station.stationuuid })

        dao.deleteByUuid(station.stationuuid)
        assertFalse(dao.isFavorite(station.stationuuid))
    }

    @Test
    fun insertRecentlyPlayed_showsUpInGetRecent() = runTest {
        val dao = db.recentlyPlayedDao()
        val recentlyPlayed = RecentlyPlayedStation(
            stationuuid = station.stationuuid,
            name = station.name,
            url_resolved = station.url_resolved,
            favicon = station.favicon,
            countrycode = station.countrycode,
            tags = station.tags,
            playedAt = 1_000L
        )

        dao.insert(recentlyPlayed)

        val recent = dao.getRecent().first()
        assertTrue(recent.any { it.stationuuid == station.stationuuid })
    }

    // Deckt die Toggle-Semantik ab, die StationViewModel.toggleFavorite()
    // verwendet: nicht favorisiert -> hinzufügen, bereits favorisiert ->
    // entfernen. Ruft dieselbe toggleFavoriteInDb()-Funktion auf, die auch
    // die App selbst nutzt, statt die DAO-Aufrufe im Test zu duplizieren.
    @Test
    fun toggleFavoriteInDb_addsStation_whenNotYetFavorite() = runTest {
        val dao = db.favoritesDao()
        assertFalse(dao.isFavorite(radioStation.stationuuid))

        toggleFavoriteInDb(dao, radioStation)

        assertTrue(dao.isFavorite(radioStation.stationuuid))
    }

    @Test
    fun toggleFavoriteInDb_removesStation_whenAlreadyFavorite() = runTest {
        val dao = db.favoritesDao()
        dao.insert(station)
        assertTrue(dao.isFavorite(radioStation.stationuuid))

        toggleFavoriteInDb(dao, radioStation)

        assertFalse(dao.isFavorite(radioStation.stationuuid))
    }

    @Test
    fun toggleFavoriteInDb_calledTwice_endsUpNotFavorite() = runTest {
        val dao = db.favoritesDao()

        toggleFavoriteInDb(dao, radioStation)
        assertTrue(dao.isFavorite(radioStation.stationuuid))

        toggleFavoriteInDb(dao, radioStation)
        assertFalse(dao.isFavorite(radioStation.stationuuid))
    }
}
