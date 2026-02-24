package com.saboon.project_2511sch.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    // APPEARANCE
    fun getDarkMode(): Flow<String>
    suspend fun setDarkMode(darkMode: String)
    fun getAppTheme(): Flow<String>
    suspend fun setAppTheme(theme: String)

    // HOME PAGE
    fun getHomeViewRange(): Flow<String>
    suspend fun setHomeViewRange(viewRange: String)
    fun getOverscrollDaysCount(): Flow<Int>
    suspend fun setOverscrollDaysCount(count: Int)
    fun getHomeListItemColorEnabled(): Flow<Boolean>
    suspend fun setHomeListItemColorEnabled(enabled: Boolean)
    fun getHomeListItemColorSource(): Flow<String>
    suspend fun setHomeListItemColorSource(source: String)
}