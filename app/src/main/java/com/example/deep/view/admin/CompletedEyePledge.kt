package com.example.deep.view.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.deep.R
import com.example.deep.adapter.ManagePledgeAdapter
import com.example.deep.databinding.FragmentCompletedEyePledgeBinding
import com.example.deep.model.PledgeCrud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompletedEyePledge : Fragment() {
    private lateinit var binding: FragmentCompletedEyePledgeBinding
    private lateinit var adapter: ManagePledgeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loading: LottieAnimationView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_completed_eye_pledge,
                container,
                false
            )

        loading = binding.loading
        loading.visibility = View.VISIBLE



        setupRecyclerview()

        return binding.root
    }

    private fun setupRecyclerview() {
        recyclerView = binding.completedPledgeRecycler
        adapter = ManagePledgeAdapter { pledge ->
            val intent = Intent(requireContext(), ManagePledgeDetail::class.java)
            intent.putExtra("pledgeList", pledge)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getPledgeList()

    }

    private fun getPledgeList() {
        lifecycleScope.launch(Dispatchers.Main) {
            val pledgeList =
                withContext(Dispatchers.IO) { PledgeCrud.getPledgeByStatus("Completed") }
            Log.e("ManagePledge", "setupRecyclerview: $pledgeList")

            if (pledgeList.isNotEmpty()) {
                loading.visibility = View.GONE
                adapter.diff.submitList(pledgeList)
            } else {
                loading.visibility = View.GONE
                binding.noPledgeCardView.visibility = View.VISIBLE
            }

        }
    }


    override fun onResume() {
        super.onResume()
        getPledgeList()
    }


}