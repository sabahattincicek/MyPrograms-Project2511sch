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

    // HOME LIST ITEM COLOR ENABLED
    override fun getHomeListItemColorEnabled(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED) {
                trySend(prefs.getBoolean(SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED, true))
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(sharedPreferences.getBoolean(SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED, true))
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    override suspend fun setHomeListItemColorEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit {
                putBoolean( SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED, enabled)
            }
        }
    }

    //HOME LIST ITEM COLOR SOURCE
    override fun getHomeListItemColorSource() = getStringFlow(SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE, SettingsConstants.HomeListItemColorSource.DEFAULT)
    override suspend fun setHomeListItemColorSource(source: String) {
        withContext(Dispatchers.IO){
            sharedPreferences.edit{
                putString(SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE, source)
            }
        }
    }
}