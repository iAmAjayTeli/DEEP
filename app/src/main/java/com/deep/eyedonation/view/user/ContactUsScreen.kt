package com.deep.eyedonation.view.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityContactUsScreenBinding
import com.google.android.material.snackbar.Snackbar

class ContactUsScreen : AppCompatActivity() {
    private lateinit var binding: ActivityContactUsScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_us_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor=getColor(R.color.action_color)
            insets
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.customerService.setOnClickListener {
            Snackbar.make(binding.root, "Customer Services coming soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.whatsapp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/+919875173692")
            startActivity(intent)
        }

        binding.gmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("mailto:pkrups78@gmail.com")
            startActivity(intent)
        }

        binding.mobileNum.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+91 9875173692")
            startActivity(intent)
        }
    }
}
