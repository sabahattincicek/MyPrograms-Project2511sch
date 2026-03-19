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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CourseRepositoryImp @Inject constructor(
    private val courseDao: CourseDao
) : ICourseRepository {
    override suspend fun insert(course: Course): Resource<Course> {
        try{
            courseDao.insert(course.toEntity())
            return Resource.Success(course)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun delete(course: Course): Resource<Course> {
        try{
            courseDao.delete(course.toEntity())
            return Resource.Success(course)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun update(course: Course): Resource<Course> {
        try{
            courseDao.update(course.toEntity())
            return Resource.Success(course)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun activationById(
        id: String,
        isActive: Boolean
    ): Resource<Unit> {
        try {
            courseDao.activationById(id, isActive)
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getById(id: String): Flow<Resource<Course>> {
        return courseDao.getById(id)
            .map<CourseEntity, Resource<Course>> { courseEntity ->
                Resource.Success(courseEntity.toDomain())
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAll(): Flow<Resource<List<Course>>> {
        return courseDao.getAll()
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntity ->
                Resource.Success(courseEntity.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAllActive(): Flow<Resource<List<Course>>> {
        return courseDao.getAllActive()
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntity ->
                Resource.Success(courseEntity.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAllByTagId(id: String): Flow<Resource<List<Course>>> {
        return courseDao.getAllByTagId(id)
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntities ->
                Resource.Success(courseEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAllByProgramTableIds(ids: List<String>): Flow<Resource<List<Course>>> {
        return courseDao.getAllByProgramTableIds(ids)
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntities ->
                Resource.Success(courseEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getAllActivesByTagIds(ids: List<String>): Flow<Resource<List<Course>>> {
        return courseDao.getAllActivesByProgramTableIds(ids)
            .map<List<CourseEntity>, Resource<List<Course>>> { courseEntities ->
                Resource.Success(courseEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override suspend fun removeTagFromCourses(tagId: String): Resource<Unit> {
        return try {
            val coursesResult = courseDao.getAllByTagId(tagId).first()
            if (coursesResult.isNotEmpty()){
                val currentTime = System.currentTimeMillis()
                val updatedEntities = coursesResult.map { entity ->
                    entity.copy(
                        tagId = null,
                        version = entity.version + 1,
                        updatedAt = currentTime
                    )
                }
                courseDao.updateAll(updatedEntities)
            }
            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }
}