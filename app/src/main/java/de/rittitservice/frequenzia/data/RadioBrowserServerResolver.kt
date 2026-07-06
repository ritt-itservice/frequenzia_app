package de.rittitservice.frequenzia.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * Die Radio Browser API besteht aus mehreren gleichwertigen Mirror-Servern.
 * Über den DNS-Eintrag "all.api.radio-browser.info" bekommt man alle aktuell
 * verfügbaren Hosts zurück. Wir wählen daraus zufällig einen aus, statt einen
 * Server fest zu verdrahten (Empfehlung des Radio-Browser-Projekts).
 */
object RadioBrowserServerResolver {

    private const val DNS_HOST = "all.api.radio-browser.info"
    private var cachedBaseUrl: String? = null

    suspend fun resolveBaseUrl(): String {
        cachedBaseUrl?.let { return it }

        val baseUrl = withContext(Dispatchers.IO) {
            try {
                val addresses = InetAddress.getAllByName(DNS_HOST)
                val chosen = addresses.random()
                val hostName = chosen.canonicalHostName
                "https://$hostName/"
            } catch (e: Exception) {
                // Fallback auf einen bekannten, stabilen Mirror
                "https://de1.api.radio-browser.info/"
            }
        }

        cachedBaseUrl = baseUrl
        return baseUrl
    }
}
