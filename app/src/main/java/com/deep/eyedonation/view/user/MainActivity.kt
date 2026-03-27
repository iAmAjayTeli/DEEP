package com.deep.eyedonation.view.user

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.OurWorkAdapter
import com.deep.eyedonation.databinding.ActivityMainBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.handler.NavigationDrawerSetup
import com.deep.eyedonation.model.OurWork
import com.google.android.material.snackbar.Snackbar
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
        setupOurWorksRV()
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
                startActivity(Intent(this@MainActivity, FamilyDonorRegistration::class.java))
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


    private fun setupOurWorksRV() {
        val recyclerView = binding.ourWorksRV
        val adapter = OurWorkAdapter { work ->
            // Handle click - open full screen image/video viewer
            if (work.mediaType == "video") {
                // Open video in browser or video player
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(work.mediaUrl))
                startActivity(intent)
            } else {
                // Open full screen image viewer
                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, work.mediaUrl)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Static list of Our Works from Cloudinary
        val workList = listOf(
            OurWork(
                id = "1",
                mediaUrl = "https://res.cloudinary.com/de0rfksfd/video/upload/v1771068257/video1_kyiet9.mp4",
                mediaType = "video"
            ),
            OurWork(
                id = "2",
                mediaUrl = "https://res.cloudinary.com/de0rfksfd/image/upload/v1774615365/WhatsApp_Image_2026-02-17_at_1.51.27_PM_lbqmq0.jpg",
                mediaType = "image"
            ),
            OurWork(
                id = "3",
                mediaUrl = "https://res.cloudinary.com/de0rfksfd/image/upload/v1774615365/WhatsApp_Image_2026-02-17_at_1.51.29_PM_wb4bl1.jpg",
                mediaType = "image"
            ),
            OurWork(
                id = "4",
                mediaUrl = "https://res.cloudinary.com/de0rfksfd/image/upload/v1774615364/WhatsApp_Image_2026-02-17_at_1.51.26_PM_gjollt.jpg",
                mediaType = "image"
            ),
            OurWork(
                id = "5",
                mediaUrl = "https://res.cloudinary.com/de0rfksfd/image/upload/v1774615343/WhatsApp_Image_2026-02-17_at_1.51.25_PM_god8ur.jpg",
                mediaType = "image"
            ),

            OurWork(
                id = "6",
                mediaUrl = "https://res.cloudinary.com/de0rfksfd/image/upload/v1774615342/WhatsApp_Image_2026-02-17_at_1.51.25_PM_1_chgck5.jpg",
                mediaType = "image"
            )
        )
        adapter.diff.submitList(workList)
    }

    private fun setupNavigationDrawer() {
        recyclerView = binding.drawerRecyclerview
        NavigationDrawerSetup(false, this, drawerLayout, recyclerView).setupDrawer()
    }
}