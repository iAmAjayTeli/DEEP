package com.example.deep.view.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.deep.R
import com.example.deep.databinding.ActivityAdminLoginBinding
import com.example.deep.handler.LoadingDialog
import com.example.deep.model.Admin
import com.example.deep.util.SharedPreference
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminLogin : AppCompatActivity() {
    private lateinit var binding: ActivityAdminLoginBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_login)

        // Initialize SharedPreference
        sharedPreference = SharedPreference(this)




        // Check if the admin is already logged in
        if (sharedPreference.isAdminLoggedIn()) {
            navigateToAdminHome()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingDialog = LoadingDialog(this, "Please wait.....")

        setupClickListener()
    }

    private fun setupClickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            forgotPasswordTxt.setOnClickListener {
                startActivity(Intent(this@AdminLogin, ChangeCredentials::class.java))
                intent.putExtra("isChangePassword", true)
                intent.putExtra("isChangeUsername", false)
            }

            login.setOnClickListener {
                val username = userName.text.toString().trim()
                val password = password.text.toString().trim()

                if (username.isEmpty() || password.isEmpty()) {
                    Snackbar.make(
                        binding.root, "Enter username and password", Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    loadingDialog.startLoading()
                    lifecycleScope.launch(Dispatchers.Main) {
                        val adminCredentials =
                            withContext(Dispatchers.IO) { Admin.getAdminCredentials() }
                        Log.e("AdminLogin", "$adminCredentials")

                        if (adminCredentials != null && username == adminCredentials.username && password == adminCredentials.password) {
                            loadingDialog.isDismiss()
                            Snackbar.make(binding.root, "Login Successful", Snackbar.LENGTH_SHORT)
                                .show()
                            delay(1000)

                            // Save admin login status
                            sharedPreference.saveAdminLoggedIn(true)

                            // Navigate to AdminHome
                            navigateToAdminHome()
                        } else {
                            loadingDialog.isDismiss()
                            Snackbar.make(binding.root, "Login Failed", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToAdminHome() {
        startActivity(Intent(this@AdminLogin, AdminHome::class.java))
        finish()
    }
}
