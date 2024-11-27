package com.example.deep.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.databinding.ItemEyeBankBinding
import com.example.deep.model.EyeBank

class EyeBankAdapter(
    private val onDirectionClick: (EyeBank) -> Unit
) : RecyclerView.Adapter<EyeBankAdapter.EyeBankViewHolder>() {
    inner class EyeBankViewHolder(val binding:ItemEyeBankBinding) : RecyclerView.ViewHolder(binding.root)

    private  val diffUtil=object: DiffUtil.ItemCallback<EyeBank>(){
        override fun areItemsTheSame(oldItem: EyeBank, newItem: EyeBank): Boolean {
              return oldItem.name==newItem.name &&
                      oldItem.address==newItem.address &&
                      oldItem.distance==newItem.distance &&
                      oldItem.latitude==newItem.latitude &&
                      oldItem.longitude==newItem.longitude
        }

        override fun areContentsTheSame(oldItem: EyeBank, newItem: EyeBank): Boolean {
           return  oldItem==newItem
        }

    }

    val diff= AsyncListDiffer(this,diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EyeBankViewHolder {
       return EyeBankViewHolder(ItemEyeBankBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: EyeBankViewHolder, position: Int) {
        val eyeBank=diff.currentList[position]

        holder

        holder.binding.getDirectionBtn.setOnClickListener {
            onDirectionClick(eyeBank)
        }
    }
}