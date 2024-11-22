package com.example.deep.view.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deep.R
import com.example.deep.adapter.FAQAdapter
import com.example.deep.databinding.FragmentFaqBinding

class Faq : Fragment() {

    private lateinit var binding: FragmentFaqBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_faq, container, false)

        val faqs = listOf(
            requireContext().getString(R.string.faq_q1) to requireContext().getString(R.string.faq_a1),
            requireContext().getString(R.string.faq_q2) to requireContext().getString(R.string.faq_a2),
            requireContext().getString(R.string.faq_q3) to  requireContext().getString(R.string.faq_a3),
            requireContext().getString(R.string.faq_q4) to  requireContext().getString(R.string.faq_a4),
            requireContext().getString(R.string.faq_q5) to  requireContext().getString(R.string.faq_a5),
            requireContext().getString(R.string.faq_q6) to  requireContext().getString(R.string.faq_a6),
            requireContext().getString(R.string.faq_q7) to  requireContext().getString(R.string.faq_a7),
            requireContext().getString(R.string.faq_q8) to  requireContext().getString(R.string.faq_a8),
            requireContext().getString(R.string.faq_q9) to  requireContext().getString(R.string.faq_a9)
        )

        val recyclerView = binding.recyclerViewFAQ
        recyclerView.layoutManager=LinearLayoutManager(requireContext())
        recyclerView.adapter = FAQAdapter(faqs)

        return binding.root
    }

}