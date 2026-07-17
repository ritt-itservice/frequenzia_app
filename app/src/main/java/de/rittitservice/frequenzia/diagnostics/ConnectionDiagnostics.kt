package de.rittitservice.frequenzia.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import de.rittitservice.frequenzia.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Schreibt bei Verbindungsfehlern eine rein lokale Klartext-Datei im
// App-eigenen externen Speicher (kein Netzwerk, keine Übertragung) – nur
// per "adb pull" auslesbar, für die Fehlersuche während der Closed-Test-
// Phase. Komplett abgeschaltet, sobald BuildConfig.CONNECTION_DIAGNOSTICS_ENABLED
// auf false steht (vor dem öffentlichen Produktions-Release umzustellen).
object ConnectionDiagnostics {

    private const val FILE_NAME = "connection-log.txt"
    private const val MAX_SIZE_BYTES = 200 * 1024

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)

    fun logFailure(context: Context, action: String, error: Throwable) {
        if (!BuildConfig.CONNECTION_DIAGNOSTICS_ENABLED) return

        val line = buildString {
            append(timeFormat.format(Date()))
            append(" | ")
            append(action)
            append(" | ")
            append(error::class.java.simpleName)
            append(": ")
            append(error.message ?: "kein Detail")
            append(" | Netzwerk: ")
            append(describeNetwork(context))
        }
        appendLine(context, line)
    }

    private fun describeNetwork(context: Context): String = runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return "ConnectivityManager nicht verfügbar"
        val network = cm.activeNetwork ?: return "keine aktive Verbindung"
        val caps = cm.getNetworkCapabilities(network) ?: return "Capabilities unbekannt"
        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WLAN"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobilfunk"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "unbekannt"
        }
        val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        "$transport (Internet validiert: $validated)"
    }.getOrElse { "Netzwerkstatus nicht ermittelbar (${it.message})" }

    private fun appendLine(context: Context, line: String) {
        runCatching {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val file = File(dir, FILE_NAME)
            // Kappt die älteste Hälfte statt unbegrenzt zu wachsen.
            if (file.exists() && file.length() > MAX_SIZE_BYTES) {
                val lines = file.readLines()
                file.writeText(lines.drop(lines.size / 2).joinToString("\n") + "\n")
            }
            file.appendText(line + "\n")
        }
    }
}
