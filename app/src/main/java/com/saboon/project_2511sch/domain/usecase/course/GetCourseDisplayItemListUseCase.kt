package com.saboon.project_2511sch.domain.usecase.course

import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.presentation.course.DisplayItemCourse
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetCourseDisplayItemListUseCase @Inject constructor(
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository,
) {
    operator fun invoke(): Flow<Resource<List<DisplayItemCourse>>> {
        return combine(
            tagRepository.getAll(),
            courseRepository.getAll(),
        ){tagRes, courseRes ->
            if (tagRes is Resource.Success && courseRes is Resource.Success){
                val allTags = tagRes.data ?: emptyList()
                val allCourses = courseRes.data ?: emptyList()

                if (allCourses.isEmpty()) {
                    return@combine Resource.Success(emptyList())
                }

                val tagMap = allTags.associateBy { it.id }

                val displayList = mutableListOf<DisplayItemCourse>()

                val groupedCourse = allCourses.groupBy { it.tagId }

                groupedCourse.forEach { (tagId, courseInGroup) ->
                    val tag = tagMap[tagId]
                    if (tag != null){
                        displayList.add(DisplayItemCourse.HeaderCourse(tag.title))
                    }else {
                        //if tag is null set header title untagged
                        displayList.add(DisplayItemCourse.HeaderCourse("Untagged"))
                    }
                    courseInGroup.forEach { course ->
                        displayList.add(DisplayItemCourse.ContentCourse(
                            tag = tag,
                            course = course
                        ))
                    }
                }
                displayList.add(DisplayItemCourse.FooterCourse(allCourses.size))
                Resource.Success(displayList)
            }
            else if (tagRes is Resource.Error || courseRes is Resource.Error){
                Resource.Error("Error occurred while loading data")
            }
            else {
                Resource.Loading()
            }
        }
    }
}