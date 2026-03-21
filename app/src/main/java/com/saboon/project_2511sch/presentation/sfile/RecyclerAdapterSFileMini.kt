package com.saboon.project_2511sch.presentation.sfile

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import java.io.File
import androidx.core.graphics.createBitmap

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
                binding.tvContent.text = item.title
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

                val extension = sFile.filePath.substringAfterLast(".", "").lowercase()
                val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "heic", "heif", "bmp", "gif")
                when {
//                    "SNote" -> {
//                        binding.tvFileType.visibility = View.VISIBLE
//                        binding.ivFilePreview.visibility = View.GONE
//                        binding.tvFileType.text = "NOTE"
//                    }
                    extension in imageExtensions -> {
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
                    extension == "pdf" -> {
                        try {
                            binding.tvFileType.visibility = View.GONE
                            binding.ivFilePreview.visibility = View.VISIBLE
                            // 1. PDF dosyasını salt okunur olarak aç
                            val file = File(sFile.filePath)
                            val parcelFileDescriptor = android.os.ParcelFileDescriptor.open(
                                file, android.os.ParcelFileDescriptor.MODE_READ_ONLY
                            )

                            if (parcelFileDescriptor != null) {
                                // 2. PdfRenderer ile dosyayı işle
                                val renderer = android.graphics.pdf.PdfRenderer(parcelFileDescriptor)

                                if (renderer.pageCount > 0) {
                                    // 3. İlk sayfayı (0) aç
                                    val page = renderer.openPage(0)

                                    // 4. Sayfa boyutunda bir bitmap oluştur
                                    val bitmap = createBitmap(page.width, page.height)
                                    bitmap.eraseColor(Color.WHITE)

                                    // 5. PDF sayfasını bitmap'e çiz (render et)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                                    // 6. Sonucu ImageView'a bas
                                    binding.ivFilePreview.setImageBitmap(bitmap)
                                    binding.ivFilePreview.scaleType = ImageView.ScaleType.CENTER_CROP

                                    // 7. Kaynakları serbest bırak (Memory leak önlemek için kritik!)
                                    page.close()
                                }
                                renderer.close()
                                parcelFileDescriptor.close()
                            }
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
                binding.tvTitle.text = binding.root.context.getString(R.string.addFile)
                binding.tvDate.visibility = View.GONE
                binding.tvFileType.visibility = View.GONE
                binding.ivFilePreview.visibility = View.VISIBLE
                binding.ivFilePreview.setImageResource(R.drawable.baseline_add_24)
                binding.ivFilePreview.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }
    }
}