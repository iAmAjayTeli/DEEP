package com.example.deep.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.databinding.PledgeItemBinding
import com.example.deep.model.Pledges

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
            return oldItem.personalDetails.email == newItem.personalDetails.email &&
                    oldItem.personalDetails.fullName == newItem.personalDetails.fullName &&
                    oldItem.personalDetails.mobileNumber == newItem.personalDetails.mobileNumber &&
                    oldItem.personalDetails.address == newItem.personalDetails.address &&
                    oldItem.personalDetails.age == newItem.personalDetails.age &&
                    oldItem.personalDetails.bloodGroup == newItem.personalDetails.bloodGroup &&
                    oldItem.personalDetails.dateOfBirth == newItem.personalDetails.dateOfBirth &&
                    oldItem.personalDetails.district == newItem.personalDetails.district &&
                    oldItem.personalDetails.pinCode == newItem.personalDetails.pinCode &&
                    oldItem.emergencyContactDetails.emergencyAddress == newItem.emergencyContactDetails.emergencyAddress &&
                    oldItem.emergencyContactDetails.emergencyContactName == newItem.emergencyContactDetails.emergencyContactName &&
                    oldItem.emergencyContactDetails.emergencyContactNumber == newItem.emergencyContactDetails.emergencyContactNumber &&
                    oldItem.eyeBankDetails.eyeBankContact == newItem.eyeBankDetails.eyeBankContact &&
                    oldItem.eyeBankDetails.eyeBankDistrict == newItem.eyeBankDetails.eyeBankDistrict &&
                    oldItem.eyeBankDetails.eyeBankName == newItem.eyeBankDetails.eyeBankName &&
                    oldItem.eyeBankDetails.eyeBankState == newItem.eyeBankDetails.eyeBankState &&
                    oldItem.pledgeConfirmation == newItem.pledgeConfirmation &&
                    oldItem.submittedDate == newItem.submittedDate &&
                    oldItem.pledgeStatus == newItem.pledgeStatus

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