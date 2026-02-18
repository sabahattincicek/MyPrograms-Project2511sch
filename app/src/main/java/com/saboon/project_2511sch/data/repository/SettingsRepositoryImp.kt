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

class SettingsRepositoryImp @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ISettingsRepository {
    companion object {
        const val KEY_APP_THEME = "pref_key_app_theme"
        const val DEFAULT_THEME = "system"
    }

    override fun getDarkMode(): Flow<String> = callbackFlow  {
        // 1. SharedPreferences değişikliklerini dinleyen bir listener oluştur
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { preferences, string ->
            if (string == KEY_APP_THEME){
                val theme = preferences.getString(KEY_APP_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
                trySend(theme)
            }
        }

        // 2. Dinleyiciyi kaydet
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // 3. Flow ilk başladığında mevcut değeri hemen gönder
        val initialTheme = sharedPreferences.getString(KEY_APP_THEME, DEFAULT_THEME)?:DEFAULT_THEME
        trySend(initialTheme)

        // 4. Flow kapandığında (Job iptal edildiğinde) dinleyiciyi kayıttan sil (Bellek sızıntısını önler)
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun setDarkMode(darkMode: String) {
        withContext(Dispatchers.IO){
            sharedPreferences.edit {
                putString(KEY_APP_THEME, darkMode)
            }
        }
    }
}