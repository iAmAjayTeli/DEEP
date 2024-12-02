package com.deep.eyedonation.view.user

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocationAndFetchPlaces()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }

    private fun getCurrentLocationAndFetchPlaces() {
        // Check if the location is available
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val location: Location? = task.result
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        fetchNearbyEyeBanks(latitude, longitude)
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchNearbyEyeBanks(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"
        val radius = 10000 // 10 km radius
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
