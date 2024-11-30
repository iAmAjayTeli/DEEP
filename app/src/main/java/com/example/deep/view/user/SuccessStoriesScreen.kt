package com.example.deep.view.user

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.deep.R
import com.example.deep.adapter.SuccessStoriesAdapter
import com.example.deep.databinding.ActivitySuccessStoriesBinding
import com.example.deep.model.SuccessStoriesCrud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuccessStoriesScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessStoriesBinding
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SuccessStoriesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_success_stories)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = resources.getColor(R.color.action_color, null)
            insets
        }

        lottieAnimationView = binding.loading
        lottieAnimationView.visibility = View.VISIBLE
        recyclerView = binding.successStoryRV
        setupRecyclerView()
        setupClickListener()

    }

    private fun setupClickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }


        }
    }

    private fun setupRecyclerView() {
        adapter = SuccessStoriesAdapter(true) {
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchStoriesFromDatabase()
    }

    private fun fetchStoriesFromDatabase() {
        lifecycleScope.launch(Dispatchers.Main) {
            val storyList = withContext(Dispatchers.IO) { SuccessStoriesCrud.getStories() }
            Log.e("SuccessStoriesScreen", "fetchStoriesFromDatabase: $storyList")

            if (storyList.isEmpty()) {
                lottieAnimationView.visibility = View.GONE
                binding.noStoriesCardView.visibility = View.VISIBLE
            } else {
                lottieAnimationView.visibility = View.GONE
                binding.noStoriesCardView.visibility = View.GONE
                adapter.diff.submitList(storyList)
            }
        }
    }
}