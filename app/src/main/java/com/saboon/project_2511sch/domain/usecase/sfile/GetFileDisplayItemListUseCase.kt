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
 * If Course is null, it returns all files belonging to all active courses under active tags.
 */
class GetFileDisplayItemListUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository,
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(course: Course? = null): Flow<Resource<List<DisplayItemSFile>>>{

        // --- CASE 1: SPECIFIC COURSE PROVIDED ---
        // We fetch files directly for this course, ignoring "active" filters of parents.
        if (course != null) {
            return sFileRepository.getAllByCourseId(course.id).map { fileResource ->
                when (fileResource) {
                    is Resource.Error -> Resource.Error(fileResource.message ?: "Files could not be loaded")
                    is Resource.Loading -> Resource.Loading()
                    is Resource.Idle -> Resource.Idle()
                    is Resource.Success -> {
                        val allSFiles = fileResource.data ?: emptyList()

                        // Return empty success immediately if no files exist to trigger Empty State UI
//                        if (allSFiles.isEmpty()) return@map Resource.Success(emptyList())

                        val displayList = allSFiles.map { sFile ->
                            DisplayItemSFile.ContentSFile(
                                course = course,
                                sFile = sFile
                            )
                        }

                        val finalList = displayList.toMutableList<DisplayItemSFile>()
                        // Add footer with total count
                        finalList.add(DisplayItemSFile.FooterSFile(displayList.size))
                        Resource.Success(finalList)
                    }
                }
            }
        }
        // --- CASE 2: NO COURSE PROVIDED (GLOBAL ACTIVE LIST) ---
        // 1. Fetch all active Tags first
        return tagRepository.getAllActive().flatMapLatest { tagResource ->
            when(tagResource){
                is Resource.Error -> flowOf(Resource.Error(tagResource.message ?: "Tags could not be loaded"))
                is Resource.Loading -> flowOf(Resource.Loading())
                is Resource.Idle -> flowOf(Resource.Idle())
                is Resource.Success -> {
                    val activeTags = tagResource.data ?: emptyList()

                    if (activeTags.isEmpty()) return@flatMapLatest flowOf(Resource.Success(emptyList()))

                    val tagIds = activeTags.map { it.id }
                    // 2. Fetch all active Courses belonging to these Tags
                    courseRepository.getAllActivesByTagIds(tagIds).flatMapLatest { courseResource ->
                        when(courseResource){
                            is Resource.Error -> flowOf(Resource.Error(courseResource.message ?: "Courses could not be loaded"))
                            is Resource.Loading -> flowOf(Resource.Loading())
                            is Resource.Idle -> flowOf(Resource.Idle())
                            is Resource.Success -> {
                                val activeCourses = courseResource.data ?: emptyList()

                                if (activeCourses.isEmpty()) return@flatMapLatest flowOf(Resource.Success(emptyList()))

                                val courseIds = activeCourses.map { it.id }

                                // 3. Fetch all files belonging to these active Courses
                                combine(
                                    sFileRepository.getAllByCourseIds(courseIds),
                                    flowOf(activeTags),
                                    flowOf(activeCourses)
                                ){ fileResource, tags, courses ->
                                    when(fileResource){
                                        is Resource.Error -> Resource.Error(fileResource.message ?: "Files could not be loaded")
                                        is Resource.Loading -> Resource.Loading()
                                        is Resource.Idle -> Resource.Idle()
                                        is Resource.Success -> {
                                            val allSFiles = fileResource.data ?: emptyList()

                                            // Early return for empty file list to trigger "Empty UI"
                                            if (allSFiles.isEmpty()) { return@combine Resource.Success(emptyList())}

                                            val courseMap = courses.associateBy { it.id }

                                            val displayList = mutableListOf<DisplayItemSFile>()

                                            allSFiles.forEach { sFile ->
                                                val course = courseMap[sFile.courseId]

                                                if (course != null){
                                                    displayList.add(
                                                        DisplayItemSFile.ContentSFile(
                                                            course = course,
                                                            sFile = sFile
                                                        )
                                                    )
                                                }
                                            }
                                            // Add summary footer
                                            displayList.add(DisplayItemSFile.FooterSFile(displayList.size))
                                            Resource.Success(displayList)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}