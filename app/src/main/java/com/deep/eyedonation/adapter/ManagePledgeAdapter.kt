package com.deep.eyedonation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.databinding.PledgeItemBinding
import com.deep.eyedonation.model.Pledges

class ManagePledgeAdapter(
    private val onViewMoreClicked: (Pledges) -> Unit
) : RecyclerView.Adapter<ManagePledgeAdapter.ManagePledgeViewHolder>() {

    class ManagePledgeViewHolder(val binding: PledgeItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    private val diffUtil = object : DiffUtil.ItemCallback<Pledges>() {
        override fun areItemsTheSame(
            oldItem: Pledges,
            newItem: Pledges
        ): Boolean {
            return oldItem.personalDetails.email == newItem.personalDetails.email

        }

        override fun areContentsTheSame(
            oldItem: Pledges,
            newItem: Pledges
        ): Boolean {
            return oldItem == newItem
        }
    }


    val diff = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagePledgeViewHolder {
        return ManagePledgeViewHolder(
            PledgeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: ManagePledgeViewHolder, position: Int) {
        val pledge = diff.currentList[position]

        holder.binding.pledgeDetails = pledge

        holder.binding.viewMoreBtn.setOnClickListener {
            onViewMoreClicked(pledge)
        }

    }


}