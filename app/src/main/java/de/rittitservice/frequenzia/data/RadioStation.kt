package de.rittitservice.frequenzia.data

data class RadioStation(
    val stationuuid: String,
    val name: String,
    val url_resolved: String,
    val favicon: String?,
    val countrycode: String?,
    val country: String?,
    val tags: String?,
    val codec: String?,
    val bitrate: Int?
)

data class Country(
    val name: String,
    val iso_3166_1: String,
    val stationcount: Int
)
