package com.deep.eyedonation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.databinding.ItemEyeBankSelectableBinding
import com.deep.eyedonation.model.EyeBank

class EyeBankSelectableAdapter(
    private val onItemClick: (EyeBank) -> Unit
) : RecyclerView.Adapter<EyeBankSelectableAdapter.EyeBankViewHolder>() {

    inner class EyeBankViewHolder(val binding: ItemEyeBankSelectableBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<EyeBank>() {
        override fun areItemsTheSame(oldItem: EyeBank, newItem: EyeBank): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.address == newItem.address &&
                    oldItem.latitude == newItem.latitude &&
                    oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(oldItem: EyeBank, newItem: EyeBank): Boolean {
            return oldItem == newItem
        }
    }

    val diff = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EyeBankViewHolder {
        return EyeBankViewHolder(
            ItemEyeBankSelectableBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: EyeBankViewHolder, position: Int) {
        val eyeBank = diff.currentList[position]

        holder.binding.apply {
            eyeBankName.text = eyeBank.name
            eyeBankAddress.text = eyeBank.address
            eyeBankDistance.text = eyeBank.distance

            root.setOnClickListener {
                onItemClick(eyeBank)
            }
        }
    }
}
