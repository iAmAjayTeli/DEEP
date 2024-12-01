package com.example.deep.view.user

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.deep.R
import com.example.deep.model.Users
import com.example.deep.util.SharedPreference
import com.example.deep.view.admin.AdminLogin

class SplashScreen : AppCompatActivity() {

    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spalsh_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreference = SharedPreference(this)

        // Delay for splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2000) // 2-second delay
    }

    private fun navigateToNextScreen() {
        // Check if the app is launching for the first time
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            // First time launching the app → Navigate to OnBoarding
            startActivity(Intent(this, OnBoarding::class.java))
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        } else {
            // Check the saved role from SharedPreference
            val userDetails: Users? = sharedPreference.getUserDetails()

            when {
                userDetails == null -> {
                    // No role saved → Navigate to OnBoarding2 (User/Admin Selection)
                    startActivity(Intent(this, OnBoarding2::class.java))
                }
                userDetails.role == "user" -> {
                    // Role is "user" → Navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                }
                userDetails.role == "admin" -> {
                    // Role is "admin" → Navigate to AdminLogin
                    startActivity(Intent(this, AdminLogin::class.java))
                }
            }
        }

        finish() // Finish the splash screen activity
    }
}
