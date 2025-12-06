package com.deep.eyedonation.view.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityAdminSettingBinding
import com.deep.eyedonation.util.SharedPreference
import com.deep.eyedonation.view.user.MainActivity
import com.google.firebase.auth.FirebaseAuth

class AdminSetting : AppCompatActivity() {
    private lateinit var binding: ActivityAdminSettingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.action_color)
            insets
        }


        setupClickListener()
    }

    private fun setupClickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }

            changeUsernameLayout.setOnClickListener {
                val intent = Intent(this@AdminSetting, ChangeCredentials::class.java)
                intent.putExtra("isChangePassword", false)
                intent.putExtra("isChangeUsername", true)
                startActivity(intent)
                finish()
            }

            changePasswordLayout.setOnClickListener {
                val intent = Intent(this@AdminSetting, ChangeCredentials::class.java)
                intent.putExtra("isChangePassword", true)
                intent.putExtra("isChangeUsername", false)
                intent.putExtra("isAdminSetting", true)
                startActivity(intent)
                finish()
            }

            logoutLayout.setOnClickListener {
                SharedPreference(this@AdminSetting).logout(
                    SharedPreference(this@AdminSetting),
                    MainActivity()
                )
            }

//            deleteLayout.setOnClickListener {
//                val firebaseAuth = FirebaseAuth.getInstance()
//                val currentUser = firebaseAuth.currentUser
//
//                currentUser?.delete()
//                    ?.addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Toast.makeText(this@AdminSetting, "Account deleted successfully", Toast.LENGTH_SHORT)
//                                .show()
//                            // Navigate to LoginActivity or any other screen
//                            val intent = Intent(this@AdminSetting, MainActivity::class.java)
//                            intent.flags =
//                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                            startActivity(intent)
//                            finish()
//                        } else {
//                            val errorMessage = task.exception?.message ?: "Account deletion failed"
//                            Toast.makeText(this@AdminSetting, errorMessage, Toast.LENGTH_LONG).show()
//                        }
//                    }
//            }


        }
    }
}