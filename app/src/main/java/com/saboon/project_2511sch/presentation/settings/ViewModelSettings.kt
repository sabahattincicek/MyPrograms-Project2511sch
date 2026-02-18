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
            initialValue = SettingsConstants.DarkMode.DEFAULT
        )
    val homeViewRangeState: StateFlow<String> = settingsRepository.getHomeViewRange()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.HomeViewRange.DEFAULT
        )
    val homeListItemColorEnabledState: StateFlow<Boolean> = settingsRepository.getHomeListItemColorEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    val homeListItemColorSourceState: StateFlow<String> = settingsRepository.getHomeListItemColorSource()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.HomeListItemColorSource.DEFAULT
        )

    //ACTIONS
    fun onDarkModeSelected(darkModeValue: String){
        viewModelScope.launch {
            settingsRepository.setDarkMode(darkModeValue)
        }
    }
    fun onHomeViewRangeSelected(viewRange: String) {
        viewModelScope.launch {
            settingsRepository.setHomeViewRange(viewRange)
        }
    }
    fun onHomeListItemColorEnabledChanged(enabled: Boolean){
        viewModelScope.launch {
            settingsRepository.setHomeListItemColorEnabled(enabled)
        }
    }
    fun onHomeListItemColorSourceSelected(source: String) {
        viewModelScope.launch {
            settingsRepository.setHomeListItemColorSource(source)
        }
    }

    //HELPERS
    /**
     * Logic to switch the AppCompatDelegate mode
     */
    fun applyDarkMode(darkModeValue: String) {
        when (darkModeValue) {
            SettingsConstants.DarkMode.OPEN  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SettingsConstants.DarkMode.CLOSE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}