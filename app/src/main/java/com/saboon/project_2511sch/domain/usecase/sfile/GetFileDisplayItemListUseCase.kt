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

class GetFileDisplayItemListUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository,
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository
) {

    /**
     * TÜM DOSYALARI GETİRİR (Filtreleme Mantığı ile)
     * Aktif etiketlere sahip derslerin ve etiketsiz (null tag) derslerin dosyalarını döner.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Resource<List<DisplayItemSFile>>> {
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

                            val filesGroupedByCourse = allSFiles.groupBy { it.courseId }

                            val displayList = mutableListOf<DisplayItemSFile>()
                            filesGroupedByCourse.forEach { (courseId, files) ->
                                val course = courseMap[courseId]
                                if (course != null){
                                    displayList.add(DisplayItemSFile.HeaderSFile(course.title))
                                    files.forEach { sFile ->
                                        displayList.add(
                                            DisplayItemSFile.ContentSFile(
                                                course = course,
                                                sFile = sFile
                                            )
                                        )
                                    }
                                }
                            }

                            displayList.add(DisplayItemSFile.FooterSFile(allSFiles.size))
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

    /**
     * SADECE BELİRLİ BİR DERSE AİT DOSYALARI GETİRİR
     */
    fun getByCourse(course: Course): Flow<Resource<List<DisplayItemSFile>>> {
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
}