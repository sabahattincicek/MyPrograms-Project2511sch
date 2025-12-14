package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.ScheduleDao
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.entity.ScheduleEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Schedule
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScheduleRepositoryImp @Inject constructor(
    private val scheduleDao: ScheduleDao
) : IScheduleRepository {
    override suspend fun insertSchedule(schedule: Schedule): Resource<Schedule> {
        try{
            scheduleDao.insert(schedule.toEntity())
            return Resource.Success(schedule)
        }catch (e: Exception){
            return Resource.Error(e.message.toString())
        }
    }

    override suspend fun updateSchedule(schedule: Schedule): Resource<Schedule> {
        try{
            scheduleDao.update(schedule.toEntity())
            return Resource.Success(schedule)
        }catch (e: Exception){
            return Resource.Error(e.message.toString())
        }
    }

    override suspend fun deleteSchedule(schedule: Schedule): Resource<Schedule> {
        try {
            scheduleDao.delete(schedule.toEntity())
            return Resource.Success(schedule)
        }catch (e: Exception){
            return Resource.Error(e.message.toString())
        }
    }

    override fun getSchedulesByCourseId(id: String): Flow<Resource<List<Schedule>>> {
        return scheduleDao.getSchedulesByCourseId(id)
            .map<List<ScheduleEntity>, Resource<List<Schedule>>> { scheduleEntity ->
                Resource.Success(scheduleEntity.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override fun getSchedulesByProgramTableId(id: String): Flow<Resource<List<Schedule>>> {
        return scheduleDao.getSchedulesByProgramTableId(id)
            .map<List<ScheduleEntity>, Resource<List<Schedule>>> { scheduleEntities ->
                Resource.Success(scheduleEntities.map { it.toDomain() })
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
            }
    }

    override suspend fun deleteSchedulesByCourseId(id: String): Resource<Unit> {
        try{
            scheduleDao.deleteSchedulesByCourseId(id)
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.message.toString())
        }
    }

    override suspend fun deleteSchedulesByProgramTableId(id: String): Resource<Unit> {
        try{
            scheduleDao.deleteSchedulesByProgramTableId(id)
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.message.toString())
        }
    }
}