package com.deep.eyedonation.view.admin

import android.content.Intent
import android.os.Bundle
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
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.SuccessStoriesAdapter
import com.deep.eyedonation.databinding.ActivityManageAddedStoryBinding
import com.deep.eyedonation.model.SuccessStoriesCrud
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageAddedStory : AppCompatActivity() {
    private lateinit var binding: ActivityManageAddedStoryBinding
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var adapter: SuccessStoriesAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_added_story)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = resources.getColor(R.color.action_color, null)
            insets
        }

        loadingAnimation = binding.loading
        loadingAnimation.visibility = View.VISIBLE
        recyclerView = binding.manageStoryRV
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
        adapter = SuccessStoriesAdapter(false) { story ->
            lifecycleScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) { story.id?.let { SuccessStoriesCrud.deleteStory(it) } }
                Snackbar.make(binding.root, "Story Deleted", Snackbar.LENGTH_SHORT).show()
                delay(1000)
                startActivity(Intent(this@ManageAddedStory, AddSuccessStories::class.java))
                finish()
            }
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchStory()
    }

    private fun fetchStory() {
        lifecycleScope.launch(Dispatchers.Main) {
            val stories = withContext(Dispatchers.IO) { SuccessStoriesCrud.getStories() }
            if (stories.isEmpty()) {
                loadingAnimation.visibility = View.GONE
                binding.noStoriesCardView.visibility = View.VISIBLE
            } else {
                binding.noStoriesCardView.visibility = View.GONE
                loadingAnimation.visibility = View.GONE
                adapter.diff.submitList(stories)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchStory()
    }
}
