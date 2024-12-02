package com.deep.eyedonation.view.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityAdminHomeBinding
import com.deep.eyedonation.handler.NavigationDrawerSetup

class AdminHome : AppCompatActivity() {
    private lateinit var binding: ActivityAdminHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }



        setupNavigationDrawer()
        setupClickListener()

    }

    private fun setupClickListener() {
        binding.apply {
            menuIcon.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            pledgeCardView.setOnClickListener {
                navigateTo(com.deep.eyedonation.view.admin.ManagePledge())
            }

            hospitalCardView.setOnClickListener {
                navigateTo(com.deep.eyedonation.view.admin.ManageHospital())
            }

            successStoriesCardView.setOnClickListener {
                navigateTo(com.deep.eyedonation.view.admin.AddSuccessStories())
            }

            settingCardView.setOnClickListener {
                navigateTo(com.deep.eyedonation.view.admin.AdminSetting())
            }


        }
    }

    private fun setupNavigationDrawer() {
        val recyclerView = binding.drawerRecyclerview
        NavigationDrawerSetup(true, this, binding.drawerLayout, recyclerView).setupDrawer()
    }

    private fun navigateTo(activity: Activity) {
        startActivity(Intent(this, activity::class.java))
    }
}