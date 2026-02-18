package com.saboon.project_2511sch.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getDarkMode(): Flow<String>
    suspend fun setDarkMode(darkMode: String)

    fun getHomeViewRange(): Flow<String>
    suspend fun setHomeViewRange(viewRange: String)

    fun getHomeListItemColorEnabled(): Flow<Boolean>
    suspend fun setHomeListItemColorEnabled(enabled: Boolean)
    fun getHomeListItemColorSource(): Flow<String>
    suspend fun setHomeListItemColorSource(source: String)
}