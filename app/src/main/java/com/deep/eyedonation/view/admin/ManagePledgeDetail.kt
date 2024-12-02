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
import com.deep.eyedonation.databinding.ActivityManagePledgeDetailBinding
import com.deep.eyedonation.model.PledgeCrud
import com.deep.eyedonation.model.Pledges
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManagePledgeDetail : AppCompatActivity() {
    private lateinit var binding: ActivityManagePledgeDetailBinding
    private lateinit var pledge: Pledges
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_pledge_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        pledge = intent.getParcelableExtra<Pledges>("pledgeList")!!
        binding.pledgeDetails = pledge

        clickListener()
    }

    private fun clickListener() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressed()
            }

            deleteBtn.setOnClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) { PledgeCrud.deletePledge(pledge.personalDetails.email) }
                    Snackbar.make(it, "Pledge Deleted Successfully", Snackbar.LENGTH_SHORT).show()
                    delay(1000)
                    startActivity(Intent(this@ManagePledgeDetail, ManagePledge::class.java))
                    finish()
                }
            }

            completedBtn.setOnClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        PledgeCrud.updatePledgeStatus(
                            pledge.personalDetails.email,
                            "Completed"
                        )
                    }
                    Snackbar.make(it, "Pledge Marked as Completed", Snackbar.LENGTH_SHORT).show()
                    delay(1000)
                    startActivity(Intent(this@ManagePledgeDetail, ManagePledge::class.java))
                    finish()
                }
            }

        }
    }
}