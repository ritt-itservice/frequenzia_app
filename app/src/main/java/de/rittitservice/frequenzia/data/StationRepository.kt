package de.rittitservice.frequenzia.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StationRepository {

    private var api: RadioBrowserApi? = null

    private suspend fun getApi(): RadioBrowserApi {
        api?.let { return it }

        val baseUrl = RadioBrowserServerResolver.resolveBaseUrl()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
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

        val created = retrofit.create(RadioBrowserApi::class.java)
        api = created
        return created
    }

    suspend fun searchStations(
        name: String? = null,
        countryCode: String? = null,
        tag: String? = null
    ): List<RadioStation> {
        return getApi().searchStations(name = name, countrycode = countryCode, tag = tag)
    }

    suspend fun getTopStations(): List<RadioStation> = getApi().getTopStations()

    suspend fun getCountries(): List<Country> = getApi().getCountries()

    suspend fun registerClick(uuid: String) {
        runCatching { getApi().registerClick(uuid) }
    }
}
