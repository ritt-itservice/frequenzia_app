package de.rittitservice.frequenzia.ui

import de.rittitservice.frequenzia.data.FavoriteStation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesGroupingTest {

    private fun station(name: String) = FavoriteStation(
        stationuuid = name,
        name = name,
        url_resolved = "https://stream.example/$name",
        favicon = null,
        countrycode = "DE",
        tags = null
    )

    @Test
    fun groupStationsByLetter_groupsByFirstLetter_caseInsensitive() {
        val result = groupStationsByLetter(
            listOf(station("rtl"), station("RMC"), station("Radio Paradise"))
        )

        assertEquals(1, result.size)
        assertEquals("R", result.first().first)
        assertEquals(3, result.first().second.size)
    }

    @Test
    fun groupStationsByLetter_sortsGroupsAlphabetically() {
        val result = groupStationsByLetter(
            listOf(station("Zeta FM"), station("Alpha FM"), station("Mango Radio"))
        )

        assertEquals(listOf("A", "M", "Z"), result.map { it.first })
    }

    @Test
    fun groupStationsByLetter_sortsStationsWithinGroupAlphabetically() {
        val result = groupStationsByLetter(
            listOf(station("RTL"), station("RMC"), station("Radio Paradise"))
        )

        val names = result.first().second.map { it.name }
        assertEquals(listOf("Radio Paradise", "RMC", "RTL"), names)
    }

    // Deckt Sendernamen wie "102.7 KIIS FM" oder "80s80s Radio" ab, die mit
    // Ziffern statt einem Buchstaben beginnen.
    @Test
    fun groupStationsByLetter_nonLetterNames_goIntoHashBucketBeforeA() {
        val result = groupStationsByLetter(
            listOf(station("102.7 KIIS FM"), station("Alpha FM"), station("80s80s Radio"))
        )

        assertEquals("#", result.first().first)
        assertEquals(setOf("102.7 KIIS FM", "80s80s Radio"), result.first().second.map { it.name }.toSet())
        assertEquals("A", result[1].first)
    }

    @Test
    fun groupStationsByLetter_emptyList_returnsEmptyList() {
        assertTrue(groupStationsByLetter(emptyList()).isEmpty())
    }
}
