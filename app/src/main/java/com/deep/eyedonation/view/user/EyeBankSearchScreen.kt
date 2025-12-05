package com.deep.eyedonation.view.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.deep.eyedonation.BuildConfig
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.EyeBankSelectableAdapter
import com.deep.eyedonation.api.RetrofitClient
import com.deep.eyedonation.databinding.ActivityEyeBankSearchBinding
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

class EyeBankSearchScreen : AppCompatActivity() {

    private lateinit var binding: ActivityEyeBankSearchBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var eyeBankAdapter: EyeBankSelectableAdapter
    private var allEyeBanks: List<EyeBank> = emptyList()

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1001
        const val EXTRA_EYE_BANK_NAME = "eye_bank_name"
        const val EXTRA_EYE_BANK_ADDRESS = "eye_bank_address"
        const val EXTRA_EYE_BANK_DISTANCE = "eye_bank_distance"
        const val EXTRA_EYE_BANK_LATITUDE = "eye_bank_latitude"
        const val EXTRA_EYE_BANK_LONGITUDE = "eye_bank_longitude"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_eye_bank_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()
        setupClickListeners()
        setupSearchFilter()

        // Auto-fetch eye banks on screen load
        checkLocationPermissionAndFetch()
    }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocationAndFetchEyeBanks()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
                binding.loading.visibility = View.GONE
                binding.infoText.visibility = View.VISIBLE
                binding.infoText.text = "Location permission is required to find nearby eye banks"
            }
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation
            if (location != null) {
                fetchNearbyEyeBanks(location.latitude, location.longitude)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }

    private fun setupRecyclerView() {
        eyeBankAdapter = EyeBankSelectableAdapter { eyeBank ->
            // Return selected eye bank to previous screen
            val resultIntent = Intent().apply {
                putExtra(EXTRA_EYE_BANK_NAME, eyeBank.name)
                putExtra(EXTRA_EYE_BANK_ADDRESS, eyeBank.address)
                putExtra(EXTRA_EYE_BANK_DISTANCE, eyeBank.distance)
                putExtra(EXTRA_EYE_BANK_LATITUDE, eyeBank.latitude)
                putExtra(EXTRA_EYE_BANK_LONGITUDE, eyeBank.longitude)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.eyeBankRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@EyeBankSearchScreen)
            adapter = eyeBankAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupSearchFilter() {
        binding.searchEditText.addTextChangedListener { editable ->
            val query = editable.toString().trim().lowercase()
            if (query.isEmpty()) {
                eyeBankAdapter.diff.submitList(allEyeBanks)
            } else {
                val filteredList = allEyeBanks.filter {
                    it.name.lowercase().contains(query) ||
                            it.address.lowercase().contains(query)
                }
                eyeBankAdapter.diff.submitList(filteredList)

                if (filteredList.isEmpty() && allEyeBanks.isNotEmpty()) {
                    binding.noEyeBankCard.visibility = View.VISIBLE
                    binding.eyeBankRecyclerview.visibility = View.GONE
                } else {
                    binding.noEyeBankCard.visibility = View.GONE
                    binding.eyeBankRecyclerview.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocationAndFetchEyeBanks()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocationAndFetchEyeBanks() {
        binding.loading.visibility = View.VISIBLE
        binding.infoText.visibility = View.GONE
        binding.noEyeBankCard.visibility = View.GONE
        binding.eyeBankRecyclerview.visibility = View.GONE

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
                            this@EyeBankSearchScreen,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        binding.loading.visibility = View.GONE
                        binding.infoText.visibility = View.VISIBLE
                        binding.infoText.text = "Failed to enable location services"
                    }
                } else {
                    binding.loading.visibility = View.GONE
                    binding.infoText.visibility = View.VISIBLE
                    binding.infoText.text = "Location settings error"
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
            binding.loading.visibility = View.GONE
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
            } else {
                binding.loading.visibility = View.GONE
                binding.infoText.visibility = View.VISIBLE
                binding.infoText.text = "Location services are required to find nearby eye banks"
            }
        }
    }

    private fun fetchNearbyEyeBanks(latitude: Double, longitude: Double) {
        val location = "$latitude,$longitude"
        val radius = 50000
        val type = "hospital"
        val keyword = "eye centre"

        val apiKey = BuildConfig.PLACES_WEB_API_KEY
        Log.d("EyeBankSearch", "Fetching nearby eye banks at: $location")

        RetrofitClient.retrofit.getNearbyPlaces(location, radius, type, keyword, apiKey)
            .enqueue(object : retrofit2.Callback<PlacesResponse> {
                override fun onResponse(
                    call: retrofit2.Call<PlacesResponse>,
                    response: retrofit2.Response<PlacesResponse>
                ) {
                    binding.loading.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        val userLocation = LatLng(latitude, longitude)

                        val eyeBanks = response.body()!!.results.map { result ->
                            val targetLocation = LatLng(
                                result.geometry.location.lat,
                                result.geometry.location.lng
                            )
                            val distance = calculateDistance(userLocation, targetLocation)
                            val distanceValue = distance.replace(" km", "").toDoubleOrNull() ?: 0.0

                            Pair(
                                EyeBank(
                                    name = result.name,
                                    address = result.vicinity,
                                    distance = distance,
                                    latitude = result.geometry.location.lat,
                                    longitude = result.geometry.location.lng
                                ),
                                distanceValue
                            )
                        }.sortedBy { it.second } // Sort by distance ascending
                            .map { it.first }

                        Log.d("EyeBankSearch", "Found ${eyeBanks.size} eye banks")
                        allEyeBanks = eyeBanks
                        displayEyeBanks(eyeBanks)
                    } else {
                        Log.e("EyeBankSearch", "Error: ${response.errorBody()?.string()}")
                        binding.infoText.visibility = View.VISIBLE
                        binding.infoText.text = "Failed to fetch eye banks. Please try again."
                    }
                }

                override fun onFailure(call: retrofit2.Call<PlacesResponse>, t: Throwable) {
                    binding.loading.visibility = View.GONE
                    Log.e("EyeBankSearch", "Failure: ${t.message}")
                    binding.infoText.visibility = View.VISIBLE
                    binding.infoText.text = "Network error. Please check your connection."
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
        return "${(results[0] / 1000.0).format(2)} km"
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun displayEyeBanks(eyeBanks: List<EyeBank>) {
        if (eyeBanks.isEmpty()) {
            binding.noEyeBankCard.visibility = View.VISIBLE
            binding.eyeBankRecyclerview.visibility = View.GONE
            binding.infoText.visibility = View.GONE
        } else {
            binding.noEyeBankCard.visibility = View.GONE
            binding.eyeBankRecyclerview.visibility = View.VISIBLE
            binding.infoText.visibility = View.GONE
            eyeBankAdapter.diff.submitList(eyeBanks)
        }
    }
}
