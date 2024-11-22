package com.example.deep.view.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.deep.R
import com.example.deep.databinding.FragmentContactUsBinding
import com.google.android.material.snackbar.Snackbar


class ContactUs : Fragment() {
    private lateinit var binding:FragmentContactUsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_contact_us,container,false)


       binding.customerService.setOnClickListener {
            Snackbar.make(binding.root, "Customer Services coming soon", Snackbar.LENGTH_SHORT).show()
        }

   binding.whatsapp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/+919033464399")
            startActivity(intent)
        }

        // Gmail Card
        binding.gmail.setOnClickListener {
            // Open Gmail with predefined email
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("mailto:pkrups78@gmail.com")
            startActivity(intent)
        }

        // Mobile Number Card (Phone Call)
        binding.mobileNum.setOnClickListener {
            // Open dialer with a predefined phone number
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:+91 9033464399")
            startActivity(intent)
        }

        return binding.root
    }


}