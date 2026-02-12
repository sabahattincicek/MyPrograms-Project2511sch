package com.saboon.project_2511sch.presentation.sfile

import android.media.ThumbnailUtils
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import coil3.load
import com.saboon.project_2511sch.R
import coil3.request.crossfade
import com.saboon.project_2511sch.databinding.RowFileBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewLeftBinding
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.toFormattedString
import java.io.File

class RecyclerAdapterSFile :
    ListAdapter<DisplayItemSFile, BaseViewHolder>(BaseDiffCallback<DisplayItemSFile>()) {

    var onItemClickListener: ((SFile) -> Unit)? = null
    var onMenuItemClickListener: ((SFile, Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_HEADER -> {
                val binding = RowSingleTextViewLeftBinding.inflate(inflater, parent, false)
                HeaderSFileViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowFileBinding.inflate(inflater, parent, false)
                ContentSFileViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowSingleTextViewBinding.inflate(inflater, parent, false)
                FooterSFileViewHolder(binding)
            }
            else -> {throw IllegalArgumentException("Unknown viewType")}
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.onItemClickListener = { baseItem ->
            if (baseItem is DisplayItemSFile.ContentSFile){
                onItemClickListener?.invoke(baseItem.sFile)
            }
        }
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    inner class HeaderSFileViewHolder(private val binding: RowSingleTextViewLeftBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemSFile.HeaderSFile){
                binding.tvContent.text = item.header
            }
        }
    }
    inner class ContentSFileViewHolder(private val binding: RowFileBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemSFile.ContentSFile){
                val sFile = item.sFile
                binding.tvTitle.text = sFile.title
                binding.tvDate.text = sFile.createdAt.toFormattedString("MMM yyyy")
                binding.ivFileMoreMenu.setOnClickListener { view ->
                    val popupMenu = PopupMenu(view.context, view)
                    popupMenu.menuInflater.inflate(R.menu.menu_action_delete, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        onMenuItemClickListener?.invoke(sFile, menuItem.itemId)
                        true
                    }
                    popupMenu.show()
                }
                when {
//                    "SNote" -> {
//                        binding.tvFileType.visibility = View.VISIBLE
//                        binding.ivFilePreview.visibility = View.GONE
//                        binding.tvFileType.text = "NOTE"
//                    }
                    sFile.title.endsWith(".jpeg") -> {
                        try {
                            binding.tvFileType.visibility = View.GONE
                            binding.ivFilePreview.visibility = View.VISIBLE
                            binding.ivFilePreview.scaleType = ImageView.ScaleType.CENTER_CROP
                            binding.ivFilePreview.load(File(sFile.filePath)){
                                crossfade(true)
                            }
                        }catch (e: Exception){
                            showFileExtension(sFile.filePath)
                        }
                    }
                    sFile.title.endsWith(".pdf") -> {
                        try {
                            binding.tvFileType.visibility = View.GONE
                            binding.ivFilePreview.visibility = View.VISIBLE
                            val thumbnail = ThumbnailUtils.createImageThumbnail(
                                File(sFile.filePath),
                                Size(512, 512),
                                null
                            )
                            binding.ivFilePreview.setImageBitmap(thumbnail)
                            binding.ivFilePreview.scaleType = ImageView.ScaleType.CENTER_CROP
                        }catch (e: Exception){
                            showFileExtension(sFile.filePath)
                        }
                    }
                    else -> {
                        showFileExtension(sFile.filePath)
                    }
                }

            }
        }
        private fun showFileExtension(text: String) {
            binding.tvFileType.visibility = View.VISIBLE
            binding.ivFilePreview.visibility = View.GONE
            binding.tvFileType.text = ".${text.substringAfterLast(".", text)}"
        }
    }
    inner class FooterSFileViewHolder(private val binding: RowSingleTextViewBinding): BaseViewHolder(binding.root) {
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemSFile.FooterSFile) {
                binding.tvContent.text = "Count: ${item.count}"
            }
        }
    }
}