package com.saboon.project_2511sch.presentation.programtable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.model.ProgramTable

class ProgramTablesRecyclerAdapter : ListAdapter<ProgramTable, ProgramTablesRecyclerAdapter.ProgramTableViewHolder>(ProgramTableDiffCallback()) {

    var onMenuItemClickListener: ((ProgramTable, Int) -> Unit)? = null
    var onItemClickListener: ((ProgramTable) -> Unit)? = null
    class ProgramTableViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val programTableTitle: TextView = view.findViewById(R.id.tvProgramTableTitle)
        val programTableDescription: TextView = view.findViewById(R.id.tvProgramTableDescription)

        val moreMenuButton: ImageView= view.findViewById(R.id.ivProgramTableMoreMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramTableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_list_row_program_tables, parent, false)
        return ProgramTableViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgramTableViewHolder, position: Int) {
        val programTable = getItem(position)
        holder.programTableTitle.text = programTable.title
        holder.programTableDescription.text = programTable.description.toString() // We can format this later

        holder.moreMenuButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.recycler_item_menu_program_tables)
            popup.setOnMenuItemClickListener { menuItem ->
                onMenuItemClickListener?.invoke(programTable, menuItem.itemId)
                true
            }
            popup.show()
        }

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(programTable)
        }
    }

    private class ProgramTableDiffCallback : DiffUtil.ItemCallback<ProgramTable>() {
        override fun areItemsTheSame(oldItem: ProgramTable, newItem: ProgramTable): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProgramTable, newItem: ProgramTable): Boolean {
            return oldItem == newItem
        }
    }
}
