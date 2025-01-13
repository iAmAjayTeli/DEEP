package com.deep.eyedonation.view.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.deep.eyedonation.BuildConfig
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.EyeBankAdapter
import com.deep.eyedonation.api.RetrofitClient
import com.deep.eyedonation.databinding.ActivityEyeBankBinding
import com.deep.eyedonation.model.EyeBank
import com.deep.eyedonation.model.PlacesResponse
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.model.LatLng

class EyeBankScreen : AppCompatActivity() {
    private lateinit var binding: ActivityEyeBankBinding
    private lateinit var eyeBankAdapter: EyeBankAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_bank)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (BuildConfig.LOG_ENABLED) {
            Log.d("EyeBankScreen", "Debug message")
        }

        lottieAnimationView = binding.loading
        lottieAnimationView.visibility = View.VISIBLE

        // Request location permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocationAndFetchPlaces()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setUpClickListener()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                fetchNearbyEyeBanks(latitude, longitude)
                // Remove the listener to avoid further updates
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

// In getCurrentLocationAndFetchPlaces() after checking location settings:


    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocationAndFetchPlaces()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }

    private fun getCurrentLocationAndFetchPlaces() {
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(
                LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
            ).build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                startLocationUpdates()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(
                            this@EyeBankScreen,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error
                    }
                }
            }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // Handle the result of the location settings request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // Location settings enabled, start location updates
                startLocationUpdates()
            } else {
                // Location settings not enabled, handle accordingly
                Toast.makeText(this, "Location services are required", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun fetchNearbyEyeBanks(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"
        val radius = 50000
        val type = "hospital"
        val keyword = "eye centre"
        val apiKey = BuildConfig.MAPS_API

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

    private fun setUpClickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun setupRecyclerView(eyeBankList: List<EyeBank>) {
        lottieAnimationView.visibility = View.GONE
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
        if(eyeBanks.isEmpty()){
            binding.noEyeBank.visibility = View.VISIBLE
            return
        }
        else{
            binding.noEyeBank.visibility = View.GONE
            eyeBankAdapter.diff.submitList(eyeBanks)
        }
    }

    private fun openGoogleMaps(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            // Handle the case where Google Maps is not installed
            Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1001
    }
}
