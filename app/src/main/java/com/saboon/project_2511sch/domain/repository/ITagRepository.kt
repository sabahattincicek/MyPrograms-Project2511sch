package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface ITagRepository {
    suspend fun insert(tag: Tag) : Resource<Tag>
    suspend fun delete(tag: Tag) : Resource<Tag>
    suspend fun update(tag: Tag) : Resource<Tag>
    suspend fun activationById(id: String, isActive: Boolean): Resource<Unit>
    fun getById(id: String): Flow<Resource<Tag>>
    fun getAll(): Flow<Resource<List<Tag>>>
    fun getAllActive(): Flow<Resource<List<Tag>>>
}