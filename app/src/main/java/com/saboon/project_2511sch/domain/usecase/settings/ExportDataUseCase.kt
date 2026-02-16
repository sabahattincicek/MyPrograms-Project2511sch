package com.saboon.project_2511sch.domain.usecase.settings

import android.content.Context
import com.saboon.project_2511sch.presentation.profile.DataTransferPackage
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.ZipUtil
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository,
    private val sFileRepository: ISFileRepository,
    private val json: Json
) {
    suspend fun execute(): Resource<File> = withContext(Dispatchers.IO){
        try {
            // 1. Verileri topla
            val programTables = programTableRepository.getAll().first().data ?: emptyList()
            val courses = courseRepository.getAll().first().data ?: emptyList()
            val tasks = taskRepository.getAll().first().data ?: emptyList()
            val sFiles = sFileRepository.getAll().first().data ?: emptyList()

            val dataTransferPackage = DataTransferPackage(programTables, courses, tasks, sFiles)
            val jsonString = json.encodeToString(dataTransferPackage)

            // 2. Geçici dosyaları hazırla
            val exportFolderTemp = File(context.cacheDir, "export_temp")
            exportFolderTemp.mkdirs()

            val jsonFile = File(exportFolderTemp, "export.json")
            jsonFile.writeText(jsonString)

            // 3. ZIP'e eklenecek dosyaların listesini hazırla
            val filesMap = mutableMapOf<String, File>()
            filesMap["export.json"] = jsonFile

            sFiles.forEach { sFile ->
                val physicalFile = File(sFile.filePath)
                if (physicalFile.exists()){
                    // ZIP içinde 'files/' klasörü altına koyuyoruz
                    filesMap["files/${physicalFile.name}"] = physicalFile
                }
            }

            // 4. Final ZIP dosyasını oluştur
            val zipFile = File(context.cacheDir, "MyProgram_Export_${System.currentTimeMillis().toFormattedString("yyyyMMdd_HHddss")}.zip")
            ZipUtil.zipFiles(zipFile, filesMap)

            // Temizlik
            jsonFile.delete()

            Resource.Success(zipFile)

        }catch (e: Exception){
            Resource.Error(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }
}