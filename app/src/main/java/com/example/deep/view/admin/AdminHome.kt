package com.example.deep.view.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.deep.R
import com.example.deep.databinding.ActivityAdminHomeBinding
import com.example.deep.handler.NavigationDrawerSetup

class AdminHome : AppCompatActivity() {
    private lateinit var binding: ActivityAdminHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=DataBindingUtil.setContentView(this, R.layout.activity_admin_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationDrawer()
        setupClickListener()

    }

    private fun setupClickListener(){
        binding.apply {
            menuIcon.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupNavigationDrawer() {
        val recyclerView = binding.drawerRecyclerview
        NavigationDrawerSetup(true,this, binding.drawerLayout, recyclerView).setupDrawer()
    }
}