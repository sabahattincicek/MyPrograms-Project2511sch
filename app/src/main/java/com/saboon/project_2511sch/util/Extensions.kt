package com.saboon.project_2511sch.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.saboon.project_2511sch.domain.model.SFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.lowercase


private val DEFAULT_LOCALE: Locale get() = Locale.getDefault()
private const val DEFAULT_PATTERN = "dd-MM-yyyy HH:mm:ss"

fun Long.toFormattedString(format: String): String{
    val dateFormat = SimpleDateFormat(format, DEFAULT_LOCALE)
    return dateFormat.format(Date(this))
}

fun SFile.open(context: Context){
    try {
        val fileToOpen = File(this.filePath)
        if (!fileToOpen.exists()){
            Toast.makeText(context, "Error: File not found.", Toast.LENGTH_SHORT).show()
            return
        }
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            fileToOpen
        )
        val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(fileToOpen).toString())
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }catch (e: Exception){
        Toast.makeText(context, "No application available to open this file type.", Toast.LENGTH_SHORT).show()
    }
}