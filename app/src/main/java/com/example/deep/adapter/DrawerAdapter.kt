package com.example.deep.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deep.R
import com.example.deep.model.DrawerItem

class DrawerAdapter(
    private val items: List<DrawerItem>,
    private val listener: (DrawerItem) -> Unit
) :
    RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.drawer_item, parent, false)
        return DrawerViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrawerViewHolder, position: Int) {

        holder.bind(items[position], listener)
    }

    override fun getItemCount(): Int = items.size

    class DrawerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val title: TextView = itemView.findViewById(R.id.title)

        fun bind(item: DrawerItem, listener: (DrawerItem) -> Unit) = with(itemView) {
            icon.setImageResource(item.icon)
            title.text = item.title
            setOnClickListener { listener(item) }
        }
    }


}

