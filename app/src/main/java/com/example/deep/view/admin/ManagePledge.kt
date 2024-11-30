package com.example.deep.view.admin

import android.os.Bundle
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.deep.R
import com.example.deep.databinding.ActivityManagePledgeBinding
import com.example.deep.handler.LoadingDialog
import com.example.deep.view.user.Faq
import com.google.android.material.tabs.TabLayoutMediator

class ManagePledge : AppCompatActivity() {
    private lateinit var binding: ActivityManagePledgeBinding
    private lateinit var searchView: SearchView
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var viewPager: ViewPager

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

        setupViewpager()
        setupClickListener()

    }

    private fun setupViewpager() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager2

        val adapter = PledgeViewPageAdapter(this)
        viewPager.adapter = adapter

        // Link TabLayout and ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pending"
                1 -> "Completed"
                else -> "Tab $position"
            }
        }.attach()

    }

    private fun setupClickListener() {
        binding.apply {
            backBtn.setOnClickListener {
                finish()
            }
        }
    }


    // Pager Adapter for ViewPager2
    class PledgeViewPageAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PendingPledge() // First tab
                1 -> CompletedEyePledge() // Second tab
                else -> Faq()
            }
        }

    }

}