package com.deep.eyedonation.view.user

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.SuccessStoriesAdapter
import com.deep.eyedonation.databinding.ActivityMainBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.handler.NavigationDrawerSetup
import com.deep.eyedonation.model.SuccessStoriesCrud
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loading: LoadingDialog

    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        checkIfAdmin()

        drawerLayout = binding.drawerLayout
        loading = LoadingDialog(this, "Sending...")

        setupNavigationDrawer()
        clickListeners()
        setupStoryRV()
    }

    private fun checkIfAdmin() {
        // Check the preference to determine if the admin is logged in
        val isAdminLoggedIn = sharedPreferences.getBoolean("isAdminLoggedIn", false)

        if (isAdminLoggedIn) {
            // If the admin is logged in, navigate directly to AdminHome
            startActivity(Intent(this, com.deep.eyedonation.view.admin.AdminHome::class.java))
            finish() // Close the current activity (MainActivity)
        }
    }

    private fun clickListeners() {
        binding.apply {
            menuIcon.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            button.setOnClickListener {
                startActivity(Intent(this@MainActivity, PledgeScreen::class.java))
            }

            button2.setOnClickListener {
                startActivity(Intent(this@MainActivity, HospitalRegisteration::class.java))

            }

            submitBtn.setOnClickListener {
                val name = fullName.text.toString()
                val email = emailAddress.text.toString()
                val msg = message.text.toString()

                if (name.isEmpty()) {
                    fullName.error = "Please enter your name"
                }
                if (email.isEmpty()) {
                    emailAddress.error = "Please enter your email"
                }
                if (msg.isEmpty()) {
                    message.error = "Please enter your message"
                }
                if (name.isNotEmpty() && email.isNotEmpty() && msg.isNotEmpty()) {
                    loading.startLoading()
                    sendFormData(name, email, msg)
                }
            }


        }
    }

    private fun sendFormData(name: String, email: String, message: String) {
        val url = "https://formspree.io/f/mlddzwdd"

        val json = JSONObject()
        json.put("_replyto", email) // Use "_replyto" for the email field
        json.put("name", name)
        json.put("message", message)

        Log.d("FormspreeJSON", json.toString()) // Log the JSON for debugging

        // Request body with appropriate media type
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        // Build the request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()

        // Make the network call
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loading.isDismiss()
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to send message: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string() // Read the body ONCE
                Log.d("Formspree Response", responseBodyString ?: "No response body")

                runOnUiThread {
                    loading.isDismiss() // Dismiss loading here to ensure it's dismissed in all cases
                    if (response.isSuccessful) {
                        Log.e("FormspreeResponse", "Response code: ${response.code}")
                        Snackbar.make(
                            binding.root,
                            "Message sent successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()

                        // Handle the redirect (if any)
                        try {
                            val jsonResponse = responseBodyString?.let { JSONObject(it) } // Use the stored string
                            val nextUrl = jsonResponse?.optString("next", "")
                            if (!nextUrl.isNullOrEmpty()) {
                               startActivity(Intent(this@MainActivity, ThanksScreen::class.java))
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "FormspreeRedirectError",
                                "Error parsing or handling redirect: ${e.message}"
                            )
                        }
                    } else {

                        // Log the response for debugging
                        Log.e("FormspreeError", "Response code: ${response.code}, Body: $responseBodyString") // Use the stored string
                        Snackbar.make(
                            binding.root,
                            "Failed to send message: ${response.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }


    private fun setupStoryRV() {
        val recyclerView = binding.successStoriesRV
        val adapter = SuccessStoriesAdapter(true) {
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchStoriesFromDatabase(adapter)
    }

    private fun fetchStoriesFromDatabase(adapter: SuccessStoriesAdapter) {
        lifecycleScope.launch(Dispatchers.Main) {
            val storyList = withContext(Dispatchers.IO) { SuccessStoriesCrud.getStories() }
            Log.e("SuccessStoriesScreen", "fetchStoriesFromDatabase: $storyList")

            if (storyList.isEmpty()) {
                binding.successStoriesTxt.visibility = View.GONE
            } else {
                adapter.diff.submitList(storyList)
            }
        }
    }

    private fun setupNavigationDrawer() {
        recyclerView = binding.drawerRecyclerview
        NavigationDrawerSetup(false, this, drawerLayout, recyclerView).setupDrawer()
    }
}