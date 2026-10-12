package com.saboon.project_2511sch.domain.usecase.sfile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.CourseFiles
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.mapData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase that provides attached files for the file screens.
 *
 * [invoke] serves the global file list: the files of every visible course, grouped by course.
 * A course is visible when it is active and either has no tag or its tag is active too.
 * [getByCourse] serves the course details screen: the files of one course.
 * How the result is drawn (headers, footers) is decided by the presentation layer.
 */
class GetFilesGroupedByCourseUseCase @Inject constructor(
    private val sFileRepository: ISFileRepository,
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository
) {

    /**
     * @return A Flow of the files of all visible courses, one group per course that has files.
     * Emits an empty list when there is no visible course or no file.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Resource<List<CourseFiles>>> {
        return combine(
            tagRepository.getAllActive(),
            courseRepository.getAllActive()
        ) { tagResource, courseResource ->
            Pair(tagResource, courseResource)
        }.flatMapLatest { (tagRes, courseRes) ->
            if (tagRes is Resource.Success && courseRes is Resource.Success) {
                val activeTagIds = (tagRes.data ?: emptyList()).map { it.id }.toSet()

                // Filtreleme: Etiketi olmayanlar (null) VEYA etiketi aktif olanlar
                val validCourses = (courseRes.data ?: emptyList()).filter { c ->
                    c.tagId == null || c.tagId in activeTagIds
                }

                if (validCourses.isEmpty()) {
                    return@flatMapLatest flowOf(Resource.Success(emptyList()))
                }

                val courseMap = validCourses.associateBy { it.id }

                sFileRepository.getAllByCourseIds(validCourses.map { it.id }).map { fileResource ->
                    fileResource.mapData { files ->
                        val groups = files.groupBy { it.courseId }.mapNotNull { (courseId, courseFiles) ->
                            courseMap[courseId]?.let { CourseFiles(it, courseFiles) }
                        }
                        Timber.d("Files grouped by course: files=%d, groups=%d", files.size, groups.size)
                        groups
                    }
                }
            } else if (tagRes is Resource.Error || courseRes is Resource.Error) {
                Timber.w("Files could not be loaded: tag=%s, course=%s", tagRes.message, courseRes.message)
                flowOf(Resource.Error("Error loading active tags or courses"))
            } else {
                flowOf(Resource.Loading())
            }
        }
    }

    /**
     * @return A Flow of the files attached to [course].
     */
    fun getByCourse(course: Course): Flow<Resource<List<SFile>>> {
        return sFileRepository.getAllByCourseId(course.id)
    }
}
