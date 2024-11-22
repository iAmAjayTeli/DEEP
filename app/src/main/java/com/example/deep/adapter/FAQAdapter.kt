package com.example.deep.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R

class FAQAdapter(private val faqs: List<Pair<String, String>>) :
    RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    private val expandedItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val (question, answer) = faqs[position]
        val isExpanded = expandedItems.contains(position)

        holder.questionText.text = question
        holder.answerText.text = answer
        holder.answerText.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.toggleIcon.setImageResource(
            if (isExpanded) R.drawable.expand_less else R.drawable.expand_more
        )

        holder.itemView.setOnClickListener {
            if (isExpanded) {
                expandedItems.remove(position)
            } else {
                expandedItems.add(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = faqs.size

    class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)
        val answerText: TextView = itemView.findViewById(R.id.answerText)
        val toggleIcon: ImageView = itemView.findViewById(R.id.toggleIcon)
    }
}
