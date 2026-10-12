package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.model.CourseGroup
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

/**
 * UseCase that provides all courses grouped by their tag, for the course list screen.
 *
 * Tags and courses are observed together, so the result updates when either changes.
 * Courses without a tag, or whose tag no longer exists, form a group with a null tag.
 * How a group is drawn (headers, footers) is decided by the presentation layer.
 */
class GetCoursesGroupedByTagUseCase @Inject constructor(
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository,
) {
    /**
     * @return A Flow of the course groups, in the order their tags first appear among the courses.
     * Emits an empty list when there is no course.
     */
    operator fun invoke(): Flow<Resource<List<CourseGroup>>> {
        return combine(
            tagRepository.getAll(),
            courseRepository.getAll(),
        ){tagRes, courseRes ->
            if (tagRes is Resource.Success && courseRes is Resource.Success){
                val tagMap = (tagRes.data ?: emptyList()).associateBy { it.id }
                val groups = (courseRes.data ?: emptyList())
                    .groupBy { it.tagId }
                    .map { (tagId, courses) -> CourseGroup(tagMap[tagId], courses) }
                Timber.d("Courses grouped by tag: groups=%d", groups.size)
                Resource.Success(groups)
            }
            else if (tagRes is Resource.Error || courseRes is Resource.Error){
                Timber.w("Courses could not be loaded: tag=%s, course=%s", tagRes.message, courseRes.message)
                Resource.Error("Error occurred while loading data")
            }
            else {
                Resource.Loading()
            }
        }
    }
}
