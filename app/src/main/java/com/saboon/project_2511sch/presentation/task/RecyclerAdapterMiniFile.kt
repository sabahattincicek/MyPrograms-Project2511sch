package com.saboon.project_2511sch.presentation.task

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RecyclerListRowMiniFilePreviewBinding
import com.saboon.project_2511sch.domain.model.File
import java.io.File as JavaFile

class RecyclerAdapterMiniFile :
    ListAdapter<File, RecyclerAdapterMiniFile.MiniFileViewHolder>(MiniFileDiffCallback()) {

    var onItemClickListener: ((File) -> Unit) ?= null
    class MiniFileViewHolder(private val binding: RecyclerListRowMiniFilePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: File){
            when{
                item.fileType.startsWith("image/") -> {
                    binding.tvFileType.visibility = View.GONE
                    binding.ivFilePreview.visibility = View.VISIBLE
                    binding.ivFilePreview.load(JavaFile(item.filePath))
                }

                item.fileType == "application/pdf" -> {
                    binding.tvFileType.visibility = View.GONE
                    binding.ivFilePreview.visibility = View.VISIBLE
                    showFilePreview(item.filePath)

                }

                item.fileType == "app/note" -> {
                    binding.ivFilePreview.visibility = View.GONE
                    binding.tvFileType.visibility = View.VISIBLE
                    binding.tvFileType.text = "NOTE"
                }

                item.fileType == "app/link" -> {
                    binding.tvFileType.visibility = View.GONE
                    binding.ivFilePreview.visibility = View.VISIBLE
                    binding.ivFilePreview.load(item.filePath)
                }

                else -> {
                    binding.ivFilePreview.visibility = View.GONE
                    binding.tvFileType.visibility = View.VISIBLE

                    val extension = item.title?.substringAfterLast('.',"")?.uppercase()
                    if (extension!!.isNotBlank()){
                        binding.tvFileType.text = extension
                    }
                    else {
                        binding.tvFileType.text = "FILE"
                    }
                }
            }
        }

        private fun showFilePreview(filePath: String){
            val javaFile = java.io.File(filePath)
            if (javaFile.exists()) {
                try {
                    val thumbnail = android.media.ThumbnailUtils.createImageThumbnail(
                        javaFile,
                        android.util.Size(512, 512),
                        null
                    )
                    binding.ivFilePreview.setImageBitmap(thumbnail)
                    binding.ivFilePreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                } catch (e: Exception) {
                    binding.ivFilePreview.setImageResource(R.drawable.baseline_insert_drive_file_24)
                    binding.ivFilePreview.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                }
            } else {

            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MiniFileViewHolder {
        val binding = RecyclerListRowMiniFilePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MiniFileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MiniFileViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }

    open class MiniFileDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem == newItem
        }
    }
}