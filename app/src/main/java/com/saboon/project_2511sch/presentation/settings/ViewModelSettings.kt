package com.saboon.project_2511sch.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ViewModelSettings @Inject constructor(
    private val settingsRepository: ISettingsRepository
): ViewModel() {

    // STATES
    val appDarkModeState: StateFlow<String> = settingsRepository.getDarkMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "system"
        )

    /**
     * Updates the app theme both in the UI and persistent storage.
     */
    fun onDarkModeSelected(darkModeValue: String){
        viewModelScope.launch {
            settingsRepository.setDarkMode(darkModeValue)
        }
    }
    /**
     * Logic to switch the AppCompatDelegate mode
     */
    fun applyDarkMode(darkModeValue: String) {
        when (darkModeValue) {
            "dark_mode_open" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "dark_mode_close" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}