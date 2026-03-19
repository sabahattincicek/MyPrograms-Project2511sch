package com.saboon.project_2511sch.domain.usecase.sfile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.presentation.sfile.DisplayItemSFile
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * UseCase to fetch and format files.
 * If a Course is provided, it returns files for that specific course.
 * If Course is null, it returns all files belonging to all active courses (including untagged ones).
 */
class GetFileDisplayItemListUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository,
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(course: Course? = null): Flow<Resource<List<DisplayItemSFile>>> {

        // --- DURUM 1: SPESİFİK BİR DERS İÇİN DOSYALARI GETİR ---
        if (course != null) {
            return sFileRepository.getAllByCourseId(course.id).map { fileResource ->
                when (fileResource) {
                    is Resource.Success -> {
                        val allSFiles = fileResource.data ?: emptyList()
                        val displayList = allSFiles.map { sFile ->
                            DisplayItemSFile.ContentSFile(course = course, sFile = sFile)
                        }.toMutableList<DisplayItemSFile>()

                        displayList.add(DisplayItemSFile.FooterSFile(displayList.size))
                        Resource.Success(displayList)
                    }
                    is Resource.Error -> Resource.Error(fileResource.message ?: "Files could not be loaded")
                    is Resource.Loading -> Resource.Loading()
                    is Resource.Idle -> Resource.Idle()
                }
            }
        }

        // --- DURUM 2: GENEL LİSTE (HOME MANTIĞI İLE) ---
        return combine(
            tagRepository.getAllActive(),
            courseRepository.getAllActive()
        ) { tagResource, courseResource ->
            Pair(tagResource, courseResource)
        }.flatMapLatest { (tagRes, courseRes) ->
            if (tagRes is Resource.Success && courseRes is Resource.Success) {
                val activeTags = tagRes.data ?: emptyList()
                val activeCourses = courseRes.data ?: emptyList()

                val activeTagsMap = activeTags.associateBy { it.id }

                // Filtreleme: Etiketi olmayanlar (null) VEYA etiketi aktif olanlar
                val validCourses = activeCourses.filter { c ->
                    c.tagId == null || activeTagsMap.containsKey(c.tagId)
                }

                if (validCourses.isEmpty()) {
                    return@flatMapLatest flowOf(Resource.Success(emptyList()))
                }

                val validCourseIds = validCourses.map { it.id }
                val courseMap = validCourses.associateBy { it.id }

                sFileRepository.getAllByCourseIds(validCourseIds).map { fileResource ->
                    when (fileResource) {
                        is Resource.Success -> {
                            val allSFiles = fileResource.data ?: emptyList()

                            if (allSFiles.isEmpty()) return@map Resource.Success(emptyList())

                            val displayList = mutableListOf<DisplayItemSFile>()
                            allSFiles.forEach { sFile ->
                                val associatedCourse = courseMap[sFile.courseId]
                                if (associatedCourse != null) {
                                    // HATA BURADAYDI: displayList.add eklenmemişti.
                                    displayList.add(
                                        DisplayItemSFile.ContentSFile(
                                            course = associatedCourse,
                                            sFile = sFile
                                        )
                                    )
                                }
                            }

                            displayList.add(DisplayItemSFile.FooterSFile(displayList.size))
                            Resource.Success(displayList)
                        }
                        is Resource.Error -> Resource.Error(fileResource.message ?: "Files could not be loaded")
                        is Resource.Loading -> Resource.Loading()
                        is Resource.Idle -> Resource.Idle()
                    }
                }
            } else if (tagRes is Resource.Error || courseRes is Resource.Error) {
                flowOf(Resource.Error("Error loading active tags or courses"))
            } else {
                flowOf(Resource.Loading())
            }
        }
    }
}