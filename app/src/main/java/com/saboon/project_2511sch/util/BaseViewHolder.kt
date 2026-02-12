package com.saboon.project_2511sch.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    var onItemClickListener: ((BaseDisplayListItem) -> Unit)? = null
    open fun bind(item: BaseDisplayListItem) {
        itemView.isClickable = item.isClickable
        itemView.isFocusable = item.isClickable
        if (item.isClickable) {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        } else {
            itemView.setOnClickListener(null)
        }
    }
}