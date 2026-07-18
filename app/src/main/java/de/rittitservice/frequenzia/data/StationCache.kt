package de.rittitservice.frequenzia.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Lokaler Fallback für die "Beliebte Sender"-Liste beim App-Start: speichert
// die zuletzt erfolgreich vom Server geladene Liste, damit bei einem
// Server-Ausfall (siehe Radio-Browser-Komplettausfall am 2026-07-18) nicht
// einfach eine leere Liste mit Fehlermeldung gezeigt wird. Die eigentliche
// Wiedergabe streamt direkt vom Sender-eigenen Server, ist von einem
// Radio-Browser-Ausfall also gar nicht betroffen – nur die Liste selbst
// bräuchte ohne diesen Cache eine funktionierende Verbindung zu Radio-Browser.
object StationCache {

    private const val CACHE_FILE_NAME = "top-stations-cache.json"
    private const val BUNDLED_ASSET_NAME = "default_stations.json"

    private val gson = Gson()
    private val listType = object : TypeToken<List<RadioStation>>() {}.type

    suspend fun save(context: Context, stations: List<RadioStation>) {
        if (stations.isEmpty()) return
        withContext(Dispatchers.IO) {
            runCatching {
                File(context.filesDir, CACHE_FILE_NAME).writeText(gson.toJson(stations))
            }
        }
    }

    private fun loadCached(context: Context): List<RadioStation>? = runCatching {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) return@runCatching null
        gson.fromJson<List<RadioStation>>(file.readText(), listType)
    }.getOrNull()?.takeIf { it.isNotEmpty() }

    // Fest mit der App ausgelieferte Liste – nur als Notlösung für den
    // allerersten Start, bevor je ein Abruf gelungen ist (dann existiert
    // noch kein loadCached()-Ergebnis).
    private fun loadBundledDefaults(context: Context): List<RadioStation> = runCatching {
        context.assets.open(BUNDLED_ASSET_NAME).bufferedReader().use { reader ->
            gson.fromJson<List<RadioStation>>(reader, listType)
        }
    }.getOrElse { emptyList() }

    // Cache, falls vorhanden, sonst die gebündelte Liste – nie leer, solange
    // die Asset-Datei mitgeliefert wird.
    suspend fun loadCachedOrBundled(context: Context): List<RadioStation> =
        withContext(Dispatchers.IO) {
            loadCached(context) ?: loadBundledDefaults(context)
        }
}
