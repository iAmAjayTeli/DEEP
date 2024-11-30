package com.example.deep.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.databinding.ItemHospitalManageBinding
import com.example.deep.model.Hospital

class ManageHospitalAdapter(val viewMoreListener: (Hospital) -> Unit) :
    RecyclerView.Adapter<ManageHospitalAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHospitalManageBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<Hospital>() {
        override fun areItemsTheSame(oldItem: Hospital, newItem: Hospital): Boolean {
            return oldItem.emailAddress == newItem.emailAddress
        }

        override fun areContentsTheSame(oldItem: Hospital, newItem: Hospital): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHospitalManageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospital = differ.currentList[position]

        holder.binding.hospitalDetails = hospital

        holder.binding.viewMoreBtn.setOnClickListener {
            viewMoreListener(hospital)
        }

    }

}