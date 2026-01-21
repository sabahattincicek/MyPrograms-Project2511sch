package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.CourseDao
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CourseRepositoryImp @Inject constructor(
    private val courseDao: CourseDao
) : ICourseRepository {
    override suspend fun insertCourse(course: Course): Resource<Course> {
        try{
            courseDao.insert(course.toEntity())
            return Resource.Success(course)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun deleteCourse(course: Course): Resource<Course> {
        try{
            courseDao.delete(course.toEntity())
            return Resource.Success(course)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun updateCourse(course: Course): Resource<Course> {
        try{
            courseDao.update(course.toEntity())
            return Resource.Success(course)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getCourseById(id: String): Flow<Resource<Course>> {
        return courseDao.getCourseById(id)
            .map<CourseEntity, Resource<Course>> { courseEntity ->
                Resource.Success(courseEntity.toDomain())
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override suspend fun deleteCoursesByProgramTableId(id: String): Resource<Unit> {
        try {
            courseDao.deleteCoursesByProgramTableId(id)
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getAllCourses(): Flow<Resource<List<Course>>> {
        return courseDao.getAllCourses()
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntity ->
                Resource.Success(courseEntity.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getCoursesByProgramTableId(id: String): Flow<Resource<List<Course>>> {
        return courseDao.getCoursesByProgramTableId(id)
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntities ->
                Resource.Success(courseEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAllCoursesByProgramTableIds(ids: List<String>): Flow<Resource<List<Course>>> {
        return courseDao.getAllCoursesByProgramTableIds(ids)
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntities ->
                Resource.Success(courseEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }
}