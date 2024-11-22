package com.example.deep.view.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.deep.R
import com.example.deep.databinding.ActivityHelpSupportBinding
import com.google.android.material.tabs.TabLayoutMediator

class HelpSupport : AppCompatActivity() {
    private lateinit var binding: ActivityHelpSupportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
       binding=DataBindingUtil.setContentView(this, R.layout.activity_help_support)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = binding.backButton
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        backButton.setOnClickListener {
            onBackPressed()
        }


        val adapter = HelpSupportPagerAdapter(this)
        viewPager.adapter = adapter

        // Link TabLayout and ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "FAQ"
                1 -> "Contact Us"
                else -> "Tab $position"
            }
        }.attach()
    }

    // Pager Adapter for ViewPager2
    class HelpSupportPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2 // Number of tabs

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> Faq() // First tab
                1 -> ContactUs() // Second tab
                else-> Faq()
            }
        }
    }
}