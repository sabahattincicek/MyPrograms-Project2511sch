package com.saboon.project_2511sch.presentation.file

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RecyclerListRowFileBinding
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterFile : ListAdapter<File, RecyclerAdapterFile.FileViewHolder>(FileDiffCallback()) {


    var onItemClickListener: ((File) -> Unit)? = null
    var onMenuItemClickListener: ((File, Int) -> Unit)? = null

    class FileViewHolder(val binding: RecyclerListRowFileBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: File){
            binding.tvTitle.text = item.title
            binding.tvDate.text = item.createdAt.toFormattedString("dd.MM.yyyy")

            when{
                item.fileType.startsWith("image/") -> {
                    binding.tvFileType.visibility = View.GONE
                    binding.ivFilePreview.visibility = View.VISIBLE
                    // ÖNEMLİ: Dosya yolundan resim yüklemek için Coil veya Glide gibi
                    // bir kütüphane kullanın. Bu, performansı ve bellek yönetimini
                    // otomatik olarak halleder.

                    // Örnek (Coil ile):
                    // binding.ivFilePreview.load(JavaFile(file.filePath))
                }

                item.fileType == "application/pdf" -> {
                    binding.ivFilePreview.visibility = View.GONE
                    binding.tvFileType.visibility = View.VISIBLE
                    binding.tvFileType.text = "PDF"
                }

                item.fileType == "app/note" -> {
                    binding.ivFilePreview.visibility = View.GONE
                    binding.tvFileType.visibility = View.VISIBLE
                    binding.tvFileType.text = "NOTE"
                }

                item.fileType == "app/link" -> {
                    binding.ivFilePreview.visibility = View.GONE
                    binding.tvFileType.visibility = View.VISIBLE
                    binding.tvFileType.text = "LINK"
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
    }
    class FileDiffCallback : DiffUtil.ItemCallback<File>(){
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileViewHolder {
        val binding = RecyclerListRowFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FileViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }

        holder.binding.ivFileMoreMenu.setOnClickListener { anchorView ->
            val popup = PopupMenu(anchorView.context, anchorView)
            popup.inflate(R.menu.menu_action_delete)
            popup.setOnMenuItemClickListener { menuItem ->
                onMenuItemClickListener?.invoke(item, menuItem.itemId)
                true
            }
            popup.show()
        }
    }
}