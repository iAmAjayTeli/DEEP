package com.example.deep.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.deep.model.Users
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class SharedPreference(private val context: Context) {

    private val sharedPref = context.getSharedPreferences("UserRole", Context.MODE_PRIVATE)

    private val gson = Gson()

    init {
        checkForReInstallation()
    }

    private fun checkForReInstallation() {
        val currentInstallId = sharedPref.getString("install_id", null)
        val savedInstallId = InstallationIdHelper.getInstallationId(context)
        if (currentInstallId == null || currentInstallId != savedInstallId) {
            clearSharedPreferenceDetails()
            saveInstallationId(savedInstallId)
        }
    }
    // Save admin login status
    fun saveAdminLoggedIn(isLoggedIn: Boolean) {
        with(sharedPref.edit()) {
            putBoolean("isAdminLoggedIn", isLoggedIn)
            apply()
        }
    }

    // Check if admin is logged in
    fun isAdminLoggedIn(): Boolean {
        return sharedPref.getBoolean("isAdminLoggedIn", false)
    }

    fun saveUser(user: Users) {
        val userDetails = gson.toJson(user)
        with(sharedPref.edit()) {
            putString("UserRole", userDetails)
            apply()
        }
    }

    fun getUserDetails(): Users? {
        val userDetailsJson = sharedPref.getString("UserRole", null)

        return if (userDetailsJson != null) {
            val userType = object : TypeToken<Users>() {}.type
            val user = gson.fromJson<Users>(userDetailsJson, userType)
            user
        } else {
            null
        }
    }

    private fun saveInstallationId(id: String) {
        with(sharedPref.edit()) {
            putString("install_id", id)
            apply()
        }
    }

     fun clearSharedPreferenceDetails() {
        with(sharedPref.edit()) {
            remove("UserRole")
            apply()
        }

    }

    fun logout(sharedPreference: SharedPreference, activity:Activity) {
        sharedPreference.saveAdminLoggedIn(false)
        context.startActivity(Intent(context, activity::class.java))
        (context as Activity).finish()
    }

}


object InstallationIdHelper {
    fun getInstallationId(context: Context): String {
        val file = context.getFileStreamPath("install_id.txt")
        return if (!file.exists()) {
            val installId = UUID.randomUUID().toString()
            context.openFileOutput("install_id.txt", Context.MODE_PRIVATE).use {
                it.write(installId.toByteArray())
            }
            installId
        } else {
            file.inputStream().bufferedReader().use { it.readLine() }
        }
    }
}