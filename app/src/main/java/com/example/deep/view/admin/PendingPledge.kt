package com.example.deep.view.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.example.deep.R
import com.example.deep.adapter.ManagePledgeAdapter
import com.example.deep.databinding.FragmentPendingPledgeBinding
import com.example.deep.handler.LoadingDialog
import com.example.deep.model.PledgeCrud
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PendingPledge : Fragment() {
    private lateinit var binding: FragmentPendingPledgeBinding
    private  lateinit var adapter: ManagePledgeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var loading: LottieAnimationView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater, R.layout.fragment_pending_pledge, container, false)

        loading=binding.loading
        loading.visibility=View.VISIBLE



        setupRecyclerview()

        return binding.root
    }

    private fun setupRecyclerview(){
        recyclerView=binding.pendingPledgeRecycler
        adapter=ManagePledgeAdapter{pledge->
            val intent= Intent(requireContext(), ManagePledgeDetail::class.java)
            intent.putExtra("pledgeList", pledge)
            startActivity(intent)
        }

        recyclerView.adapter=adapter
        recyclerView.layoutManager= LinearLayoutManager(requireContext())

        lifecycleScope.launch(Dispatchers.Main) {
            val pledgeList= withContext(Dispatchers.IO){ PledgeCrud.getPledgeByStatus("Pending")}
            Log.e("ManagePledge", "setupRecyclerview: $pledgeList")

            if(pledgeList.isNotEmpty()){
                loading.visibility=View.GONE
                adapter.diff.submitList(pledgeList)
            }

        }


    }

}