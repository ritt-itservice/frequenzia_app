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

    // Zuletzt gewählter Hostname – wird nach invalidate() von der Auswahl
    // ausgeschlossen, damit ein Retry nicht per Zufall wieder denselben
    // (gerade fehlgeschlagenen) Mirror trifft, wenn Alternativen existieren.
    private var lastHostName: String? = null

    suspend fun resolveBaseUrl(): String {
        cachedBaseUrl?.let { return it }

        val baseUrl = withContext(Dispatchers.IO) {
            try {
                val hostNames = InetAddress.getAllByName(DNS_HOST)
                    .map { it.canonicalHostName }
                    .distinct()
                val chosen = selectMirrorHost(hostNames, excluding = lastHostName)
                lastHostName = chosen
                "https://$chosen/"
            } catch (e: Exception) {
                // Fallback auf einen bekannten, stabilen Mirror
                "https://de1.api.radio-browser.info/"
            }
        }

        cachedBaseUrl = baseUrl
        return baseUrl
    }

    // Erzwingt bei der nächsten Anfrage die Wahl eines neuen Mirrors – z. B.
    // wenn der aktuell gewählte gerade nicht erreichbar war.
    fun invalidate() {
        cachedBaseUrl = null
    }
}

// Als eigenständige, reine Funktion extrahiert (statt Inline-Logik), damit
// sich die Ausschluss-Regel direkt testen lässt, ohne echtes DNS zu benötigen.
internal fun selectMirrorHost(hostNames: List<String>, excluding: String?): String {
    val candidates = excluding
        ?.let { previous -> hostNames.filterNot { it == previous } }
        ?.takeIf { it.isNotEmpty() }
        ?: hostNames
    return candidates.random()
}
