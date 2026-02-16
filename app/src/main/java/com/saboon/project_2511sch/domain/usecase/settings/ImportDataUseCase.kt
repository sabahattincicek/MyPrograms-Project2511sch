package com.saboon.project_2511sch.domain.usecase.settings

import android.content.Context
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.profile.DatabaseBackup
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.ZipUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository,
    private val sFileRepository: ISFileRepository,
    private val json: Json
) {
    suspend fun execute(zipFile: File): Resource<Unit> = withContext(Dispatchers.IO){
        try {
            // 1. Geçici bir klasör oluştur ve ZIP'i oraya çıkart
            val extractFolder = File(context.cacheDir, "extracted_backup")
            if (extractFolder.exists()) extractFolder.deleteRecursively()
            extractFolder.mkdirs()

            ZipUtil.unzip(zipFile, extractFolder)

            // 2. JSON dosyasını oku ve modelleri oluştur
            val jsonFile = File(extractFolder, "backup.json")
            if (!jsonFile.exists()) return@withContext Resource.Error("Not found backup file")

            val jsonString = jsonFile.readText()
            val backup = json.decodeFromString<DatabaseBackup>(jsonString)

            // 3. Fiziksel dosyaları kalıcı klasöre taşı ve yolları güncelle
            val sfilesFolder = File(context.filesDir, "sFiles")
            if (!sfilesFolder.exists()) sfilesFolder.mkdirs()

            val updatedSFiles = backup.sFiles.map { sFile ->
                val extractedPhysicalFile = File(extractFolder, "files/${File(sFile.filePath).name}")
                if (extractedPhysicalFile.exists()){
                    val targetFile = File(sfilesFolder, extractedPhysicalFile.name)
                    extractedPhysicalFile.copyTo(targetFile, overwrite = true)
                    // Veritabanı için yolu bu telefonun dizinine göre güncelle
                    sFile.copy(filePath = targetFile.absolutePath)
                }else{
                    sFile
                }
            }
            // 4. Veritabanına kaydet (Sıralama Önemlidir: Foreign Key kısıtlamaları için)
            // Önce tablolar, sonra kurslar, sonra tasklar ve dosyalar
            backup.programTables.forEach { programTableRepository.insert(it) }
            backup.courses.forEach { courseRepository.insert(it) }
            backup.tasks.forEach { taskRepository.insert(it) }
            updatedSFiles.forEach { sFileRepository.insert(it) }

            // 5. Temizlik (Geçici klasörü sil)
            extractFolder.deleteRecursively()

            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e.message ?: "İçe aktarma sırasında bir hata oluştu")
        }
    }
}