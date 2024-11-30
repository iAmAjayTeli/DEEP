package com.example.deep.view.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.deep.R
import com.example.deep.databinding.ActivityAdminSettingBinding

class AdminSetting : AppCompatActivity() {
    private lateinit var binding: ActivityAdminSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=DataBindingUtil.setContentView(this, R.layout.activity_admin_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor=getColor(R.color.action_color)
            insets
        }

        setupClickListener()
    }

    private fun setupClickListener(){
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }

            changeUsernameLayout.setOnClickListener {
                val intent=Intent(this@AdminSetting,ChangeCredentials::class.java)
                intent.putExtra("isChangePassword",false)
                intent.putExtra("isChangeUsername",true)
                startActivity(intent)
                finish()
            }

            changePasswordLayout.setOnClickListener {
                val intent=Intent(this@AdminSetting,ChangeCredentials::class.java)
                intent.putExtra("isChangePassword",true)
                intent.putExtra("isChangeUsername",false)
                intent.putExtra("isAdminSetting",true)
                startActivity(intent)
                finish()
            }
        }



    }
}