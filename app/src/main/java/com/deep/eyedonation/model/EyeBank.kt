package com.deep.eyedonation.model

data class EyeBank(
    val name: String,
    val address: String,
    val distance: String,
    val latitude: Double,
    val longitude: Double
)

data class PlacesResponse(
    val results: List<PlaceResult>
)

data class PlaceResult(
    val name: String,
    val vicinity: String,
    val geometry: Geometry
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)



