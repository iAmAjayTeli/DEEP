package com.deep.eyedonation.view.admin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
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
import com.deep.eyedonation.adapter.PledgeAdapter
import com.deep.eyedonation.databinding.ActivityManagePledgeBinding
import com.deep.eyedonation.model.PledgeCrud
import com.deep.eyedonation.model.Pledges
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManagePledge : AppCompatActivity() {
    private lateinit var binding: ActivityManagePledgeBinding
    private lateinit var searchView: SearchView
    private lateinit var loading: LottieAnimationView
    private lateinit var adapter: PledgeAdapter
    private lateinit var recyclerView: RecyclerView
    private var pledgeList = listOf<Pledges>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_pledge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = resources.getColor(R.color.action_color, null)
            insets
        }

        // Initialize views
        searchView = binding.searchView
        loading = binding.loading
        recyclerView = binding.completedPledgeRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list
        adapter = PledgeAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        // Set up search functionality
        setupSearchView()

        // Fetch and display pledges
        getPledgeList()
        setupClickListener()
    }

    private fun setupClickListener(){
        binding.apply {
            backBtn.setOnClickListener {
                onBackPressed()
            }
        }
    }


    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Not used
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrEmpty()) {
                    pledgeList
                } else {
                    pledgeList.filter {
                        it.personalDetails.fullName.contains(newText, ignoreCase = true) ||
                                it.personalDetails.email.contains(newText, ignoreCase = true)
                    }
                }
                adapter.updatePledgeList(filteredList)
                return true
            }
        })
    }

    private fun getPledgeList() {
        loading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val pledges = withContext(Dispatchers.IO) { PledgeCrud.getPledges() }
                if (pledges.isNotEmpty()) {
                    pledgeList = pledges
                    adapter.updatePledgeList(pledgeList)
                    binding.noPledgeCardView.visibility = View.GONE
                } else {
                    adapter.updatePledgeList(emptyList())
                    binding.noPledgeCardView.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("ManagePledge", "Error fetching pledges", e)
                binding.noPledgeCardView.visibility = View.VISIBLE
            } finally {
                loading.visibility = View.GONE
            }
        }
    }

}
