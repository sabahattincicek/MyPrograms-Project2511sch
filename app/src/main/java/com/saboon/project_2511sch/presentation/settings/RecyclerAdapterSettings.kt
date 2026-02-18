package com.saboon.project_2511sch.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RowSingleTextViewLeftBinding
import com.saboon.project_2511sch.databinding.SettingsRowActionBinding
import com.saboon.project_2511sch.databinding.SettingsRowCategoryBinding
import com.saboon.project_2511sch.databinding.SettingsRowToggleBinding

class RecyclerAdapterSettings : ListAdapter<SettingsItem, RecyclerView.ViewHolder>(SettingsDiffCallback()){
    var onSettingsClick: ((SettingsItem) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            SettingsItem.VIEW_TYPE_CATEGORY -> SettingsCategoryViewHolder(SettingsRowCategoryBinding.inflate(inflater, parent, false))
            SettingsItem.VIEW_TYPE_ACTION -> SettingsActionViewHolder(SettingsRowActionBinding.inflate(inflater, parent, false))
            SettingsItem.VIEW_TYPE_TOGGLE -> SettingsToggleViewHolder(SettingsRowToggleBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        when (holder) {
            is SettingsCategoryViewHolder -> holder.bind(item as SettingsItem.Category)
            is SettingsActionViewHolder -> holder.bind(item as SettingsItem.Action)
            is SettingsToggleViewHolder -> holder.bind(item as SettingsItem.Toggle)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is SettingsItem.Category -> SettingsItem.VIEW_TYPE_CATEGORY
            is SettingsItem.Action -> SettingsItem.VIEW_TYPE_ACTION
            is SettingsItem.Toggle -> SettingsItem.VIEW_TYPE_TOGGLE
        }
    }

    inner class SettingsCategoryViewHolder(private val binding: SettingsRowCategoryBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: SettingsItem.Category){
            binding.tvContent.text = item.title
        }
    }
    inner class SettingsActionViewHolder(private val binding: SettingsRowActionBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: SettingsItem.Action){
            binding.tvTitle.text = item.title
            if (item.summary == null ){
                binding.tvSummary.visibility = View.GONE
            }else{
                binding.tvSummary.visibility = View.VISIBLE
                binding.tvSummary.text = item.summary
            }
            binding.tvValue.visibility = View.VISIBLE
            binding.tvValue.text = item.value.toString()
            binding.root.setOnClickListener { onSettingsClick?.invoke(item) }
        }
    }

    inner class SettingsToggleViewHolder(private val binding: SettingsRowToggleBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: SettingsItem.Toggle){
            binding.tvTitle.text = item.title
            if (item.summary.equals("") && item.summary == null ){
                binding.tvSummary.visibility = View.GONE
            }else{
                binding.tvSummary.visibility = View.VISIBLE
                binding.tvSummary.text = item.summary
            }
            binding.tvValue.visibility = View.VISIBLE
            binding.tvValue.text = item.value.toString()
            binding.swSettings.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                onSettingsClick?.invoke(item)
            }
        }
    }

    open class SettingsDiffCallback: DiffUtil.ItemCallback<SettingsItem>() {
        override fun areItemsTheSame(
            oldItem: SettingsItem,
            newItem: SettingsItem
        ): Boolean {
            // Sealed class olduğu için ID veya Key üzerinden kontrol ederiz
            return when (oldItem) {
                is SettingsItem.Category if newItem is SettingsItem.Category -> oldItem.title == newItem.title
                is SettingsItem.Action if newItem is SettingsItem.Action -> oldItem.key == newItem.key
                is SettingsItem.Toggle if newItem is SettingsItem.Toggle -> oldItem.key == newItem.key
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: SettingsItem,
            newItem: SettingsItem
        ): Boolean {
            return oldItem == newItem
        }

    }

}