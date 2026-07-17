package de.rittitservice.frequenzia.data

import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// apiFactory baut standardmäßig den echten Retrofit-Client gegen einen
// Radio-Browser-Mirror auf (siehe buildDefaultApi unten). Für Tests lässt
// sich hier eine Fake-Implementierung von RadioBrowserApi einsetzen, ohne
// echtes Netzwerk/DNS zu benötigen.
class StationRepository(
    private val apiFactory: suspend () -> RadioBrowserApi = ::buildDefaultApi
) {

    private var api: RadioBrowserApi? = null

    private suspend fun getApi(): RadioBrowserApi {
        api?.let { return it }
        val created = apiFactory()
        api = created
        return created
    }

    // Ein automatischer, kurzer Wiederholungsversuch für transiente
    // Netzwerkfehler (z. B. kurzer Signalabriss unterwegs) – bevor der
    // Nutzer überhaupt eine Fehlermeldung sieht. Der zweite Versuch wählt
    // zusätzlich einen anderen Radio-Browser-Mirror, falls der erste gerade
    // nicht erreichbar war.
    private suspend fun <T> withRetry(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            delay(600)
            RadioBrowserServerResolver.invalidate()
            api = null
            block()
        }
    }

    suspend fun searchStations(
        name: String? = null,
        countryCode: String? = null,
        tag: String? = null
    ): List<RadioStation> = withRetry {
        getApi().searchStations(name = name, countrycode = countryCode, tag = tag)
    }

    suspend fun getTopStations(): List<RadioStation> = withRetry { getApi().getTopStations() }

    suspend fun getCountries(): List<Country> = getApi().getCountries()

    suspend fun registerClick(uuid: String) {
        runCatching { getApi().registerClick(uuid) }
    }
}

private suspend fun buildDefaultApi(): RadioBrowserApi {
    val baseUrl = RadioBrowserServerResolver.resolveBaseUrl()

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(logging)
        // Radio Browser bittet höflich um einen aussagekräftigen User-Agent
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Frequenzia/0.1 (Android)")
                .build()
            chain.proceed(request)
        }
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(RadioBrowserApi::class.java)
}
