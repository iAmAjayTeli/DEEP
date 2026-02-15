package com.deep.eyedonation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.deep.eyedonation.R
import com.deep.eyedonation.databinding.OurWorkItemBinding
import com.deep.eyedonation.model.OurWork

class OurWorkAdapter(
    private val onItemClick: (OurWork) -> Unit = {}
) : RecyclerView.Adapter<OurWorkAdapter.OurWorkViewHolder>() {

    inner class OurWorkViewHolder(val binding: OurWorkItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<OurWork>() {
        override fun areItemsTheSame(oldItem: OurWork, newItem: OurWork): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OurWork, newItem: OurWork): Boolean {
            return oldItem == newItem
        }
    }

    val diff = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OurWorkViewHolder {
        return OurWorkViewHolder(
            OurWorkItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = diff.currentList.size

    override fun onBindViewHolder(holder: OurWorkViewHolder, position: Int) {
        val work = diff.currentList[position]

        holder.binding.apply {
            // Load media using Glide
            Glide.with(mediaImage.context)
                .load(work.mediaUrl)
                .placeholder(R.drawable.eye)
                .error(R.drawable.eye)
                .centerCrop()
                .into(mediaImage)

            // Show play icon for videos
            if (work.mediaType == "video") {
                playIcon.visibility = View.VISIBLE
            } else {
                playIcon.visibility = View.GONE
            }

            // Handle click to view full image/video
            root.setOnClickListener {
                onItemClick(work)
            }
        }
    }
}
