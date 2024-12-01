package com.example.deep.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.deep.R
import com.example.deep.databinding.ActivityOnBoarding2Binding
import com.example.deep.handler.LoadingDialog
import com.example.deep.model.Users
import com.example.deep.model.UsersCrud
import com.example.deep.util.SharedPreference
import com.example.deep.view.admin.AdminLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnBoarding2 : AppCompatActivity() {
    private lateinit var binding: ActivityOnBoarding2Binding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var sharedPreference: SharedPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreference = SharedPreference(this)
        loadingDialog = LoadingDialog(this, "Wait a while, we're setting up your account")
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.apply {
            user.setOnClickListener {
                handleRoleSelection("user")
            }

            admin.setOnClickListener {
                handleRoleSelection("admin")
            }
        }
    }

    private fun handleRoleSelection(role: String) {
        loadingDialog.startLoading()
        val user = Users(role)

        lifecycleScope.launch(Dispatchers.Main) {
            // Save the user role to Firestore and shared preferences
            withContext(Dispatchers.IO) {
                UsersCrud.addUser(user)
            }
            sharedPreference.saveUser(user)

            loadingDialog.isDismiss()

            // Navigate based on the selected role
            when (role) {
                "user" -> {
                    startActivity(Intent(this@OnBoarding2, MainActivity::class.java))
                    finish()
                }
                "admin" -> {
                    startActivity(Intent(this@OnBoarding2, AdminLogin::class.java))
                    finish()
                }
            }
        }
    }
}
