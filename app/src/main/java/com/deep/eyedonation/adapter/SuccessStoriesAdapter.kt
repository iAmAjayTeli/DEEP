package com.deep.eyedonation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.deep.eyedonation.databinding.SuccessStoriesItemBinding
import com.deep.eyedonation.model.Stories

class SuccessStoriesAdapter(
    private val isDelete: Boolean,
    private val onDelete: (Stories) -> Unit
) :
    RecyclerView.Adapter<SuccessStoriesAdapter.SuccessStoriesViewHolder>() {
    inner class SuccessStoriesViewHolder(val binding: SuccessStoriesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffUtil = object : DiffUtil.ItemCallback<Stories>() {
        override fun areItemsTheSame(oldItem: Stories, newItem: Stories): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.comments == newItem.comments &&
                    oldItem.date == newItem.date &&
                    oldItem.rating == newItem.rating &&
                    oldItem.personName == newItem.personName
        }

        override fun areContentsTheSame(oldItem: Stories, newItem: Stories): Boolean {
            return oldItem == newItem
        }

    }

    val diff = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuccessStoriesViewHolder {
        return SuccessStoriesViewHolder(
            SuccessStoriesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return diff.currentList.size
    }

    override fun onBindViewHolder(holder: SuccessStoriesViewHolder, position: Int) {
        val story = diff.currentList[position]
        holder.binding.storyDetails = story

        holder.binding.isDelete = isDelete

        holder.binding.deleteBtn.setOnClickListener {
            onDelete(story)
        }


    }


}