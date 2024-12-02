package com.deep.eyedonation.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.databinding.PledgeItemBinding
import com.deep.eyedonation.model.Pledges
import com.deep.eyedonation.view.admin.ManagePledgeDetail

class PledgeAdapter(private var pledgeList: List<Pledges>, private val context: Context) :
    RecyclerView.Adapter<PledgeAdapter.PledgeViewHolder>() {

    inner class PledgeViewHolder(val binding: PledgeItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PledgeAdapter.PledgeViewHolder {
        return PledgeViewHolder(
            PledgeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PledgeAdapter.PledgeViewHolder, position: Int) {
        val pledge = pledgeList[position]

        holder.binding.pledgeDetails = pledge

        holder.binding.viewMoreBtn.setOnClickListener {
            context.startActivity(
                Intent(context, ManagePledgeDetail::class.java).putExtra(
                    "pledgeList",
                    pledge
                )
            )
        }

    }

    override fun getItemCount(): Int {
        return pledgeList.size
    }

    fun updatePledgeList(newPledgeList: List<Pledges>) {
        pledgeList = newPledgeList
        notifyDataSetChanged()
    }
}