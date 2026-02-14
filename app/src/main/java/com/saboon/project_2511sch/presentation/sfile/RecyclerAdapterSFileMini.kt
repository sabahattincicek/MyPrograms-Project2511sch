package com.saboon.project_2511sch.presentation.sfile

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import coil3.load
import coil3.request.crossfade
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RowFileMiniBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewLeftBinding
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.toFormattedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.launch

class RecyclerAdapterSFileMini :
    ListAdapter<DisplayItemSFile, BaseViewHolder>(BaseDiffCallback<DisplayItemSFile>()) {

    var onItemClickListener: ((SFile) -> Unit) ?= null
    var onAddItemClickListener: ((Unit) -> Unit) ?= null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_HEADER -> {
                val binding = RowSingleTextViewLeftBinding.inflate(inflater, parent, false)
                HeaderFileMiniViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowFileMiniBinding.inflate(inflater, parent, false)
                ContentFileMiniViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowFileMiniBinding.inflate(inflater, parent, false)
                FooterFileMiniViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.onItemClickListener = { baseItem ->
            when(baseItem){
                is DisplayItemSFile.ContentSFile -> onItemClickListener?.invoke(baseItem.sFile)
                is DisplayItemSFile.FooterSFile -> onAddItemClickListener?.invoke(Unit)
            }
        }
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    inner class HeaderFileMiniViewHolder(private val binding: RowSingleTextViewLeftBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item) //for click logic
            if (item is DisplayItemSFile.HeaderSFile){
                binding.tvContent.text = item.header
            }
        }
    }
    inner class ContentFileMiniViewHolder(private val binding: RowFileMiniBinding): BaseViewHolder(binding.root){

        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)

            if (item is DisplayItemSFile.ContentSFile){
                val sFile = item.sFile
                binding.tvTitle.text = sFile.title
                binding.tvDate.text = sFile.createdAt.toFormattedString("MMM yyyy")

                // Varsayılan olarak her şeyi temizle
                binding.ivFilePreview.setImageDrawable(null)
                when {
//                    "SNote" -> {
//                        binding.tvFileType.visibility = View.VISIBLE
//                        binding.ivFilePreview.visibility = View.GONE
//                        binding.tvFileType.text = "NOTE"
//                    }
                    sFile.title.endsWith(".jpeg", ignoreCase = true) ||
                            sFile.title.endsWith(".jpg", ignoreCase = true) -> {
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
                    sFile.title.endsWith(".pdf", ignoreCase = true) -> {
                        try {
                            binding.tvFileType.visibility = View.GONE
                            binding.ivFilePreview.visibility = View.VISIBLE
                            showFileExtension(sFile.filePath)
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
    inner class FooterFileMiniViewHolder(private val binding: RowFileMiniBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemSFile.FooterSFile){
                binding.tvTitle.visibility = View.GONE
                binding.tvDate.visibility = View.GONE
                binding.tvFileType.visibility = View.GONE
                binding.ivFilePreview.visibility = View.VISIBLE
                binding.ivFilePreview.setImageResource(R.drawable.baseline_add_24)
                binding.ivFilePreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }
    }
}