package de.rittitservice.frequenzia.data

import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

private class FakeRadioBrowserApi(private val onCall: () -> List<RadioStation>) : RadioBrowserApi {
    override suspend fun searchStations(
        name: String?,
        country: String?,
        countrycode: String?,
        tag: String?,
        limit: Int,
        hideBroken: Boolean,
        order: String,
        reverse: Boolean
    ): List<RadioStation> = onCall()

    override suspend fun getTopStations(): List<RadioStation> = onCall()

    override suspend fun getCountries(): List<Country> = emptyList()

    override suspend fun registerClick(uuid: String) {}
}

class StationRepositoryTest {

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

    // Deckt genau das Verhalten aus StationRepository.withRetry ab: ein
    // einzelner transienter Fehler (z. B. kurzer Netzwerkaussetzer) darf
    // nicht sofort als Fehler beim Nutzer ankommen, sondern soll einen
    // zweiten Versuch mit frisch aufgebautem Client bekommen.
    @Test
    fun getTopStations_transientFailure_retriesOnceWithFreshClientAndSucceeds() = runTest {
        var factoryCalls = 0
        var apiCalls = 0
        val repository = StationRepository(apiFactory = {
            factoryCalls++
            FakeRadioBrowserApi {
                apiCalls++
                if (apiCalls == 1) throw IOException("connection reset")
                listOf(station)
            }
        })

        val result = repository.getTopStations()

        assertEquals(listOf(station), result)
        assertEquals("Client sollte für den Retry neu aufgebaut werden (Mirror-Wechsel)", 2, factoryCalls)
        assertEquals(2, apiCalls)
    }

    @Test
    fun searchStations_transientFailure_retriesOnceAndSucceeds() = runTest {
        var apiCalls = 0
        val repository = StationRepository(apiFactory = {
            FakeRadioBrowserApi {
                apiCalls++
                if (apiCalls == 1) throw IOException("connection reset")
                listOf(station)
            }
        })

        val result = repository.searchStations(name = "Test")

        assertEquals(listOf(station), result)
        assertEquals(2, apiCalls)
    }

    // Ein zweiter, ebenfalls fehlschlagender Versuch darf nicht endlos weiter
    // wiederholt werden – der Fehler muss nach genau einem Retry nach oben
    // durchgereicht werden, damit der Nutzer die "Sender konnten nicht
    // geladen werden"-Meldung sieht statt einer hängenden App.
    @Test
    fun getTopStations_persistentFailure_propagatesAfterExactlyOneRetry() = runTest {
        var apiCalls = 0
        val repository = StationRepository(apiFactory = {
            FakeRadioBrowserApi {
                apiCalls++
                throw IOException("mirror still down")
            }
        })

        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getTopStations() }
        }
        assertEquals("Genau ein Erstversuch + ein Retry, kein Retry-Loop", 2, apiCalls)
    }

    @Test
    fun getTopStations_success_reusesSameClientWithoutRebuilding() = runTest {
        var factoryCalls = 0
        val repository = StationRepository(apiFactory = {
            factoryCalls++
            FakeRadioBrowserApi { listOf(station) }
        })

        repository.getTopStations()
        repository.getTopStations()

        assertEquals("Client wird zwischengespeichert, solange nichts fehlschlägt", 1, factoryCalls)
    }
}
