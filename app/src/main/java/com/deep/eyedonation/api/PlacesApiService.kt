package com.deep.eyedonation.api

import com.deep.eyedonation.model.PlacesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    @GET("nearbysearch/json")
    fun getNearbyPlaces(
        @Query("location") location: String, // "latitude,longitude"
        @Query("radius") radius: Int, // e.g., 5000 (in meters)
        @Query("type") type: String, // e.g., "hospital"
        @Query("keyword") keyword: String, // e.g., "eye bank"
        @Query("key") apiKey: String // Your Google Places API key
    ): Call<PlacesResponse>
}

