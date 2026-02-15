package com.deep.eyedonation.view.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding

    companion object {
        const val EXTRA_IMAGE_URL = "image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_screen_image)

        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        // Load the image
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.eye)
                .error(R.drawable.eye)
                .into(binding.fullScreenImage)
        }

        // Close button click
        binding.closeBtn.setOnClickListener {
            finish()
        }

        // Also close when clicking anywhere on the image
        binding.fullScreenImage.setOnClickListener {
            finish()
        }
    }
}
