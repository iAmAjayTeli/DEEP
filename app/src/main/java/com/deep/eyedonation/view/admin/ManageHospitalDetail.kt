package com.deep.eyedonation.view.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityManageHospitalDetailBinding
import com.deep.eyedonation.handler.LoadingDialog
import com.deep.eyedonation.model.Hospital
import com.deep.eyedonation.model.HospitalCrud
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageHospitalDetail : AppCompatActivity() {
    private lateinit var binding: ActivityManageHospitalDetailBinding
    private lateinit var loading: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_hospital_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val hospitalDetails = intent.getParcelableExtra<Hospital>("hospitalDetails")
        binding.hospitalDetails = hospitalDetails

        loading = LoadingDialog(this, "Please wait...")

        setupClickListener()
    }

    private fun setupClickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            deleteBtn.setOnClickListener {
                loading.startLoading()
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        hospitalDetails?.let {
                            HospitalCrud.deleteHospital(it.emailAddress)
                        }
                        loading.isDismiss()
                        Snackbar.make(it, "Hospital deleted successfully", Snackbar.LENGTH_SHORT)
                            .show()
                        delay(1000)
                        startActivity(Intent(this@ManageHospitalDetail, ManageHospital::class.java))
                        finish()
                    }
                }
            }
        }
    }

}