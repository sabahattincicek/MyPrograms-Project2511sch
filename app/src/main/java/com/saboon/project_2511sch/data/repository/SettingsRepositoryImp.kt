package com.saboon.project_2511sch.data.repository

import android.content.SharedPreferences
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.content.edit
import com.saboon.project_2511sch.presentation.settings.SettingsConstants

class SettingsRepositoryImp @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ISettingsRepository {

    private fun getStringFlow(key: String, defaultValue: String): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { preference, string ->
            if (string == key) trySend(preference.getString(key, defaultValue) ?: defaultValue)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPreferences.getString(key, defaultValue) ?: defaultValue)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    //DARK MODE
    override fun getDarkMode() = getStringFlow(SettingsConstants.PREF_KEY_DARK_MODE, SettingsConstants.DarkMode.DEFAULT)
    override suspend fun setDarkMode(darkMode: String) {
        withContext(Dispatchers.IO){
            sharedPreferences.edit {
                putString(SettingsConstants.PREF_KEY_DARK_MODE, darkMode)
            }
        }
    }

    //HOME VIEW RANGE
    override fun getHomeViewRange() = getStringFlow(SettingsConstants.PREF_KEY_HOME_VIEW_RANGE, SettingsConstants.HomeViewRange.DEFAULT)
    override suspend fun setHomeViewRange(viewRange: String) {
        withContext(Dispatchers.IO){
            sharedPreferences.edit {
                putString(SettingsConstants.PREF_KEY_HOME_VIEW_RANGE, viewRange)
            }
        }
    }
}