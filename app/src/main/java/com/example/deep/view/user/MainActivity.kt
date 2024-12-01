package com.example.deep.view.user

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R
import com.example.deep.adapter.SuccessStoriesAdapter
import com.example.deep.databinding.ActivityMainBinding
import com.example.deep.handler.NavigationDrawerSetup
import com.example.deep.model.SuccessStoriesCrud
import com.example.deep.view.admin.AdminHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
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

        val menuIcon = binding.menuIcon

        setupNavigationDrawer()
        clickListeners()
        setupStoryRV()
    }

    private fun checkIfAdmin() {
        // Check the preference to determine if the admin is logged in
        val isAdminLoggedIn = sharedPreferences.getBoolean("isAdminLoggedIn", false)

        if (isAdminLoggedIn) {
            // If the admin is logged in, navigate directly to AdminHome
            startActivity(Intent(this, AdminHome::class.java))
            finish() // Close the current activity (MainActivity)
        }
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

    private fun setupStoryRV() {
        val recyclerView=binding.successStoriesRV
       val  adapter = SuccessStoriesAdapter(true) {
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchStoriesFromDatabase(adapter)
    }

    private fun fetchStoriesFromDatabase(adapter: SuccessStoriesAdapter) {
        lifecycleScope.launch(Dispatchers.Main) {
            val storyList = withContext(Dispatchers.IO) { SuccessStoriesCrud.getStories() }
            Log.e("SuccessStoriesScreen", "fetchStoriesFromDatabase: $storyList")

            if (storyList.isEmpty()) {
                binding.successStoriesTxt.visibility=View.GONE
            } else {
                adapter.diff.submitList(storyList)
            }
        }
    }

    private fun setupNavigationDrawer() {
        recyclerView = binding.drawerRecyclerview
        NavigationDrawerSetup(false, this, drawerLayout, recyclerView).setupDrawer()
    }
}