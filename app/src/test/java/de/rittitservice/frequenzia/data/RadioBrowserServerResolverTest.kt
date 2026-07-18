package de.rittitservice.frequenzia.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RadioBrowserServerResolverTest {

    // Deckt den 2026-07-18 auf einem echten Gerät gefundenen Bug ab: ein
    // Retry nach HTTP 503 wählte per Zufall wieder denselben (gerade
    // fehlgeschlagenen) Mirror, weil resolveBaseUrl() den vorherigen Host
    // nicht ausschloss.
    @Test
    fun selectMirrorHost_excludesPreviousHost_whenAlternativesExist() {
        val hosts = listOf("de1.api.radio-browser.info", "de2.api.radio-browser.info", "fr1.api.radio-browser.info")

        repeat(50) {
            val chosen = selectMirrorHost(hosts, excluding = "de1.api.radio-browser.info")
            assertFalse(
                "Retry darf nicht wieder den ausgeschlossenen Host wählen",
                chosen == "de1.api.radio-browser.info"
            )
            assertTrue(chosen in hosts)
        }
    }

    @Test
    fun selectMirrorHost_noExclusion_canReturnAnyHost() {
        val hosts = listOf("de1.api.radio-browser.info")
        val chosen = selectMirrorHost(hosts, excluding = null)
        assertEquals("de1.api.radio-browser.info", chosen)
    }

    // Wenn der DNS-Eintrag (aktuell) nur den zuvor fehlgeschlagenen Host
    // zurückgibt, gibt es keine Alternative – dann muss trotzdem ein Ergebnis
    // zurückkommen, statt mit einer leeren Liste abzustürzen.
    @Test
    fun selectMirrorHost_onlyPreviousHostAvailable_fallsBackToIt() {
        val hosts = listOf("de1.api.radio-browser.info")
        val chosen = selectMirrorHost(hosts, excluding = "de1.api.radio-browser.info")
        assertEquals("de1.api.radio-browser.info", chosen)
    }
}
