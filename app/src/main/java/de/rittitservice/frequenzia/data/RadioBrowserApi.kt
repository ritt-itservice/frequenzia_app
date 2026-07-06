package de.rittitservice.frequenzia.data

import retrofit2.http.GET
import retrofit2.http.Query

interface RadioBrowserApi {

    @GET("json/stations/search")
    suspend fun searchStations(
        @Query("name") name: String? = null,
        @Query("country") country: String? = null,
        @Query("countrycode") countrycode: String? = null,
        @Query("tag") tag: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true
    ): List<RadioStation>

    @GET("json/stations/topclick/50")
    suspend fun getTopStations(): List<RadioStation>

    @GET("json/countries")
    suspend fun getCountries(): List<Country>

    // Meldet Klick an die API zurück (unterstützt das Radio-Browser-Projekt / Statistik)
    @GET("json/url/{uuid}")
    suspend fun registerClick(@retrofit2.http.Path("uuid") uuid: String)
}
