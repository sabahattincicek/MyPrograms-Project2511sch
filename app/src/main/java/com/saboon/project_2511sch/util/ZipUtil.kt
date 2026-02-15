package com.saboon.project_2511sch.util

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtil {
    // Dosyaları ZIP'e ekler
    fun zipFiles(zipFile: File, filesToZip: Map<String, File>){
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOutputStream ->
            filesToZip.forEach { (entryName, file)->
                if (file.exists()){
                    val entry = ZipEntry(entryName)
                    zipOutputStream.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zipOutputStream) }
                    zipOutputStream.closeEntry()
                }
            }
        }
    }
    // ZIP dosyasını belirtilen klasöre açar
    fun unzip(zipFile: File, targetDirectory: File){
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInputStream ->
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null){
                val file = File(targetDirectory, entry.name)
                // Alt klasörler varsa oluştur
                file.parentFile?.mkdirs()
                if (!entry.isDirectory){
                    file.outputStream().use { zipInputStream.copyTo(it) }
                }
                entry = zipInputStream.nextEntry
            }
        }
    }
}