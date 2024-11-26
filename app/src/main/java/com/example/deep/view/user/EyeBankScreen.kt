package com.example.deep.view.user

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.deep.R
import com.example.deep.adapter.EyeBankAdapter
import com.example.deep.api.RetrofitClient
import com.example.deep.databinding.ActivityEyeBankBinding
import com.example.deep.model.EyeBank
import com.example.deep.model.PlacesResponse
import com.example.deep.util.Constants
import com.google.android.gms.maps.model.LatLng

class EyeBankScreen : AppCompatActivity() {
    private lateinit var binding: ActivityEyeBankBinding
    private lateinit var eyeBankAdapter: EyeBankAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=DataBindingUtil.setContentView(this, R.layout.activity_eye_bank)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getCurrentLocationAndFetchPlaces()
        setUpClickListener()

    }


    private fun setUpClickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun fetchNearbyEyeBanks(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"
        val radius = 10000 // 10 km radius
        val type = "hospital"
        val keyword = "eye bank"
        val apiKey = Constants.MAPS_API

        RetrofitClient.retrofit.getNearbyPlaces(location, radius, type, keyword, apiKey)
            .enqueue(object : retrofit2.Callback<PlacesResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PlacesResponse>,
                    response: retrofit2.Response<PlacesResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val userLocation = LatLng(latitude, longitude)

                        val eyeBanks = response.body()!!.results.map { result ->
                            val targetLocation = LatLng(
                                result.geometry.location.lat,
                                result.geometry.location.lng
                            )
                            val distance = calculateDistance(userLocation, targetLocation)

                            EyeBank(
                                name = result.name,
                                address = result.vicinity,
                                distance = distance,
                                latitude = result.geometry.location.lat,
                                longitude = result.geometry.location.lng
                            )
                        }

                        Log.e("API_RESPONSE", "Response: $eyeBanks")

                        setupRecyclerView(eyeBanks)
                    } else {
                        Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<PlacesResponse>, t: Throwable) {
                    Log.e("API_ERROR", "Failure: ${t.message}")
                }
            })
    }

    private fun calculateDistance(userLocation: LatLng, targetLocation: LatLng): String {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            targetLocation.latitude, targetLocation.longitude,
            results
        )
        return "${(results[0] / 1000.0).format(2)} km" // Convert meters to kilometers with two decimal places
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun getCurrentLocationAndFetchPlaces() {
        val latitude = 12.971598
        val longitude = 77.594566

        fetchNearbyEyeBanks(latitude, longitude)
    }

    private fun setupRecyclerView(eyeBankList: List<EyeBank>) {
        eyeBankAdapter = EyeBankAdapter { eyeBank ->
            // Handle "Get Directions" click
            openGoogleMaps(eyeBank.latitude, eyeBank.longitude)
        }

        binding.eyeBankRecyclerview.apply {
            layoutManager = GridLayoutManager(this@EyeBankScreen, 2)
            adapter = eyeBankAdapter
        }

        displayEyeBanks(eyeBankList)
    }


    private fun displayEyeBanks(eyeBanks: List<EyeBank>) {
        eyeBankAdapter.diff.submitList(eyeBanks)
    }

    private fun openGoogleMaps(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }


}

