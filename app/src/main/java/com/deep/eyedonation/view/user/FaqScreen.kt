package com.deep.eyedonation.view.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.deep.eyedonation.R
import com.deep.eyedonation.adapter.FAQAdapter
import com.deep.eyedonation.databinding.ActivityFaqScreenBinding

class FaqScreen : AppCompatActivity() {
    private lateinit var binding: ActivityFaqScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faq_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor=getColor(R.color.action_color)
            insets
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        setupFaqList()
    }

    private fun setupFaqList() {
        val faqs = listOf(
            getString(R.string.faq_q1) to getString(R.string.faq_a1),
            getString(R.string.faq_q2) to getString(R.string.faq_a2),
            getString(R.string.faq_q3) to getString(R.string.faq_a3),
            getString(R.string.faq_q4) to getString(R.string.faq_a4),
            getString(R.string.faq_q5) to getString(R.string.faq_a5),
            getString(R.string.faq_q6) to getString(R.string.faq_a6),
            getString(R.string.faq_q7) to getString(R.string.faq_a7),
            getString(R.string.faq_q8) to getString(R.string.faq_a8),
            getString(R.string.faq_q9) to getString(R.string.faq_a9)
        )

        binding.recyclerViewFAQ.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFAQ.adapter = FAQAdapter(faqs)
    }
}
