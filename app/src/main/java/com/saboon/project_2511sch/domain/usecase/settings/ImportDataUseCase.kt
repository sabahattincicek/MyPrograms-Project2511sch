package com.saboon.project_2511sch.domain.usecase.settings

import android.content.Context
import android.net.Uri
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.profile.DataTransferPackage
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
    suspend fun execute(uri: Uri): Resource<Unit> = withContext(Dispatchers.IO){
        // Geçici dosyaları try bloğu dışında tanımlıyoruz ki finally içinde silebiliriz
        var tempZipFile: File? = null
        val extractFolder = File(context.cacheDir, "extracted_backup_${System.currentTimeMillis()}")

        try {
            // 1. Uri içeriğini geçici bir ZIP dosyasına kopyala
            tempZipFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.zip")
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempZipFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext Resource.Error("Dosya okunamadı (InputStream null)")

            // 2. ZIP'i çıkart
            if (extractFolder.exists()) extractFolder.deleteRecursively()
            extractFolder.mkdirs()
            ZipUtil.unzip(tempZipFile, extractFolder)

            // 2. JSON dosyasını oku ve modelleri oluştur
            val jsonFile = File(extractFolder, "backup.json")
            if (!jsonFile.exists()) return@withContext Resource.Error("Not found backup file")
            val jsonString = jsonFile.readText()
            val dataTransferPackage = json.decodeFromString<DataTransferPackage>(jsonString)

            // 3. Fiziksel dosyaları kalıcı klasöre taşı ve yolları güncelle
            val sFilesFolder = File(context.filesDir, "sFiles")
            if (!sFilesFolder.exists()) sFilesFolder.mkdirs()

            val updatedSFiles = dataTransferPackage.sFiles.map { sFile ->
                val extractedPhysicalFile = File(extractFolder, "files/${File(sFile.filePath).name}")
                if (extractedPhysicalFile.exists()){
                    val targetFile = File(sFilesFolder, extractedPhysicalFile.name)
                    extractedPhysicalFile.copyTo(targetFile, overwrite = true)
                    // Veritabanı için yolu bu telefonun dizinine göre güncelle
                    sFile.copy(filePath = targetFile.absolutePath)
                }else{
                    sFile
                }
            }
            // 4. Veritabanına kaydet (Sıralama Önemlidir: Foreign Key kısıtlamaları için)
            // Önce tablolar, sonra kurslar, sonra tasklar ve dosyalar
            dataTransferPackage.programTables.forEach { programTableRepository.insert(it) }
            dataTransferPackage.courses.forEach { courseRepository.insert(it) }
            dataTransferPackage.tasks.forEach { taskRepository.insert(it) }
            updatedSFiles.forEach { sFileRepository.insert(it) }

            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e.message ?: "İçe aktarma sırasında bir hata oluştu")
        }finally {
            try {
                tempZipFile?.deleteRecursively()
                extractFolder.deleteRecursively()
            }catch (e: Exception){

            }
        }
    }
}