package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.data.local.dao.UserDao
import com.saboon.project_2511sch.data.local.entity.UserEntity
import com.saboon.project_2511sch.data.local.mapper.toDomain
import com.saboon.project_2511sch.data.local.mapper.toEntity
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IUserRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImp @Inject constructor(
    private val userDao: UserDao
): IUserRepository {
    override suspend fun insert(user: User): Resource<User> {
        try {
            userDao.insert(user.toEntity())
            return Resource.Success(user)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun update(user: User): Resource<User> {
        try {
            userDao.update(user.toEntity())
            return Resource.Success(user)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override suspend fun delete(user: User): Resource<User> {
        try {
            userDao.delete(user.toEntity())
            return Resource.Success(user)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage?:"An unexpected error occurred")
        }
    }

    override fun getById(id: String): Flow<Resource<User>> {
        return userDao.getById(id)
            .map<UserEntity, Resource<User>> { entity ->
                Resource.Success(entity.toDomain())
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override fun getActive(): Flow<Resource<User?>> {
        return userDao.getActive()
            .map<UserEntity?, Resource<User?>> { entity ->
                Resource.Success(entity?.toDomain())
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }
}