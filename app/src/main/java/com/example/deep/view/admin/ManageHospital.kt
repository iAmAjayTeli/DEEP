package com.example.deep.view.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.deep.R
import com.example.deep.adapter.ManageHospitalAdapter
import com.example.deep.databinding.ActivityManageHospitalBinding
import com.example.deep.model.HospitalCrud
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageHospital : AppCompatActivity() {
    private lateinit var binding: ActivityManageHospitalBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageHospitalAdapter
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_hospital)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = resources.getColor(R.color.action_color, null)
            insets
        }

        lottieAnimationView = binding.loading
        lottieAnimationView.visibility = View.VISIBLE

        initRecyclerView()
        clickListener()
    }

    private fun clickListener() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun initRecyclerView() {
        recyclerView = binding.manageHospitalRV
        adapter = ManageHospitalAdapter { hospital ->
            val intent = Intent(this, ManageHospitalDetail::class.java)
            intent.putExtra("hospitalDetails", hospital)
            startActivity(intent)
            finish()
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchHospitalList()
    }

    private fun fetchHospitalList() {
        lifecycleScope.launch(Dispatchers.Main) {
            val hospitalList = withContext(Dispatchers.IO) { HospitalCrud.getAllHospitals() }

            if (hospitalList.isNotEmpty()) {
                lottieAnimationView.visibility = View.GONE
                adapter.differ.submitList(hospitalList)
            } else {
                lottieAnimationView.visibility = View.GONE
                binding.noHospitalCardView.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchHospitalList()
    }
}