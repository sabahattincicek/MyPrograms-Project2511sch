package com.saboon.project_2511sch.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.saboon.project_2511sch.data.local.dao.FileDao
import com.saboon.project_2511sch.data.local.entity.FileEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.repository.IFileRepository
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import java.io.File as JavaFile

class FileRepositoryImp @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDao: FileDao,
): IFileRepository {
    override suspend fun insertFileFromUri(file: File, uri: Uri): Resource<File> {
        val contentResolver = context.contentResolver

        // Güvenlik ve tutarlılık için, dosya adını tekrar Uri'den okuyalım.
        var originalFileName = "unknown_file"
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()){
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) originalFileName = cursor.getString(nameIndex)
            }
        }

       try {
           // 1. Fiziksel dosyayı uygulamanın özel alanına kopyala
           val inputStream = contentResolver.openInputStream(uri)
           val newPhysicalFileName = "${System.currentTimeMillis()}_${originalFileName}"
           val newLocalFile = JavaFile(context.filesDir, newPhysicalFileName)
           val outputStream = FileOutputStream(newLocalFile)
           inputStream?.copyTo(outputStream)
           inputStream?.close()
           outputStream.close()

           val finalFileToSave = file.copy(
               filePath = newLocalFile.absolutePath,
               // Kullanıcı başlığı değiştirebilir, o yüzden orijinal adı değil,
               // diyalogdan gelen 'title'ı kullanmaya devam etmeliyiz.
               // Eğer diyalogdan gelen title boş ise, orijinal adı kullanabiliriz.
               title = file.title?.takeIf { it.isNotBlank() } ?: originalFileName
           )

           // 3. Nihai nesneyi veritabanına kaydet
           fileDao.insert(finalFileToSave.toEntity())

           return Resource.Success(file)
        }catch (e: Exception){
           return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun deleteFile(file: File): Resource<File> {
        try {
            val fileToDelete = JavaFile(file.filePath)
            if (fileToDelete.exists() && !fileToDelete.delete()){
                return Resource.Error("Failed to delete physical file at ${file.filePath}")
            }
            fileDao.delete(file.toEntity())
            return Resource.Success(file)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun updateFile(file: File): Resource<File> {
        try {
            fileDao.update(file.toEntity())
            return Resource.Success(file)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun insertNoteFile(note: File): Resource<File> {
        val content = note.description ?: ""

        try {
            // 1. Yeni bir .html dosyası için isim oluştur.
            val newFileName = "${System.currentTimeMillis()}_${note.title}.html"
            val newLocalFile = JavaFile(context.filesDir, newFileName)

            // 2. FileOutputStream kullanarak HTML içeriğini bu yeni dosyaya yaz.
            val outputStream = FileOutputStream(newLocalFile)
            outputStream.write(content.toByteArray())
            outputStream.close()

            // 3. Veritabanına kaydedilecek NİHAİ nesneyi, oluşturduğumuz
            //    dosyanın kalıcı yolu ile güncelle.
            val finalNoteToSave = note.copy(
                filePath = newLocalFile.absolutePath
            )

            // 4. Bu nihai nesneyi veritabanına kaydet.
            fileDao.insert(finalNoteToSave.toEntity())
            return Resource.Success(finalNoteToSave)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun insertLinkFile(link: File): Resource<File> {
        try {
            fileDao.insert(link.toEntity())
            return Resource.Success(link)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getFilesByCourseId(id: String): Flow<Resource<List<File>>> {
        return fileDao.getFilesByCourseId(id)
            .map<List<FileEntity>, Resource<List<File>>> { entities ->
                Resource.Success( entities.map { it.toDomain() } )
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getFilesByProgramTableId(id: String): Flow<Resource<List<File>>> {
        return fileDao.getFilesByProgramTableId(id)
            .map<List<FileEntity>, Resource<List<File>>> { entities ->
                Resource.Success(entities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAllFiles(): Flow<Resource<List<File>>> {
        return fileDao.getAllFiles()
            .map<List<FileEntity>, Resource<List<File>>> { entities ->
                Resource.Success(entities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }
}