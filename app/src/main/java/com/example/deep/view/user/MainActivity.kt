package com.example.deep.view.user

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R
import com.example.deep.databinding.ActivityMainBinding
import com.example.deep.handler.NavigationDrawerSetup

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        drawerLayout = binding.drawerLayout

        val menuIcon=binding.menuIcon

        setupNavigationDrawer()
        clickListeners()
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


        }
    }

    private fun setupNavigationDrawer() {
        recyclerView = binding.drawerRecyclerview
        NavigationDrawerSetup(false,this, drawerLayout, recyclerView).setupDrawer()
    }
}