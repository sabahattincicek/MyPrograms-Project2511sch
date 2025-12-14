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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class UserRepositoryImp @Inject constructor(
    private val userDao: UserDao
) : IUserRepository{
    /**
     * Inserts a new user into the local database.
     * Since the application is designed to support only a single local user at a time,
     * this function first deletes any existing user from the table before inserting the new one.
     * @param user The user object to be inserted.
     */
    override suspend fun insertUser(user: User): Resource<Unit> {
        try{
            userDao.setAllUserInactive()
            userDao.insertUser(user.toEntity())
            return Resource.Success(Unit)
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }

    override fun getAllUsers(): Flow<Resource<List<User>>> {
        return userDao.getAllUsers()
            .map<List<UserEntity>, Resource<List<User>>> { userEntities ->
                Resource.Success(userEntities.map{it.toDomain()})
            }
            .catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }

    override suspend fun getActiveUser(): Resource<User> {
        try{
            val userEntity = userDao.getActiveUser()
            return Resource.Success(userEntity.toDomain())
        }catch (e: Exception){
            return Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }
    }
}