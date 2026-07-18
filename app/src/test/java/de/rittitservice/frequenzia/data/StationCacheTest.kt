package de.rittitservice.frequenzia.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StationCacheTest {

    private val station = RadioStation(
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

    // Deckt den 2026-07-18 gefundenen Radio-Browser-Komplettausfall ab:
    // ohne je einen erfolgreichen Abruf gemacht zu haben, muss trotzdem eine
    // nutzbare Liste da sein (die mit der App ausgelieferte Default-Datei).
    @Test
    fun loadCachedOrBundled_noCacheYet_returnsBundledDefaults() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val result = StationCache.loadCachedOrBundled(context)

        assertTrue("Bundled-Default-Liste darf nicht leer sein", result.isNotEmpty())
    }

    @Test
    fun save_thenLoad_returnsExactlyWhatWasSaved() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val stations = listOf(station)

        StationCache.save(context, stations)
        val result = StationCache.loadCachedOrBundled(context)

        assertEquals(stations, result)
    }

    // Ein leeres Ergebnis (z. B. weil eine Suche 0 Treffer hatte) soll nicht
    // versehentlich einen zuvor gespeicherten guten Cache überschreiben.
    @Test
    fun save_withEmptyList_doesNotOverwriteExistingCache() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        StationCache.save(context, listOf(station))

        StationCache.save(context, emptyList())
        val result = StationCache.loadCachedOrBundled(context)

        assertEquals(listOf(station), result)
    }
}
