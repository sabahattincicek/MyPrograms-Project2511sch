package com.saboon.project_2511sch.domain.repository

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    fun getDarkMode(): Flow<String>
    suspend fun setDarkMode(darkMode: String)
}