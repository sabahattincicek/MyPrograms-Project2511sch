package com.saboon.project_2511sch.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ViewModelSettings @Inject constructor(
    private val settingsRepository: ISettingsRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {

    // STATES
    val appDarkModeState: StateFlow<String> = settingsRepository.getDarkMode()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.DarkMode.DEFAULT
        )
    val appThemeState: StateFlow<String> = settingsRepository.getAppTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.AppTheme.DEFAULT
        )
    val homeViewRangeState: StateFlow<String> = settingsRepository.getHomeViewRange()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.HomeViewRange.DEFAULT
        )
    val overScrollDaysCountState: StateFlow<Int> = settingsRepository.getOverscrollDaysCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.OverscrollDaysCount.DEFAULT
        )
    val homeListItemColorEnabledState: StateFlow<Boolean> = settingsRepository.getHomeListItemColorEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.HomeListItemColorEnabled.DEFAULT
        )
    val homeListItemColorSourceState: StateFlow<String> = settingsRepository.getHomeListItemColorSource()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.HomeListItemColorSource.DEFAULT
        )
    val selectedCharacterState: StateFlow<String> = settingsRepository.getSelectedCharacter()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.SelectedCharacter.DEFAULT
        )
    val absenceReminderEnabledState: StateFlow<Boolean> = settingsRepository.getAbsenceReminderEnabled()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsConstants.AbsenceReminderEnabled.DEFAULT
        )

    //ACTIONS
    fun onDarkModeSelected(darkModeValue: String){
        viewModelScope.launch {
            settingsRepository.setDarkMode(darkModeValue)
        }
    }
    fun onAppThemeSelected(theme: String){
        viewModelScope.launch {
            settingsRepository.setAppTheme(theme)
        }
    }
    fun onHomeViewRangeSelected(viewRange: String) {
        viewModelScope.launch {
            settingsRepository.setHomeViewRange(viewRange)
        }
    }
    fun onOversrollDaysCountChanged(count: Int){
        viewModelScope.launch {
            settingsRepository.setOverscrollDaysCount(count)
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
    fun onCharacterSelected(id: String){
        viewModelScope.launch {
            settingsRepository.setSelectedCharacter(id)
        }
    }
    fun onAbsenceReminderEnabledChanged(enabled: Boolean){
        viewModelScope.launch {
            settingsRepository.setAbsenceReminderEnabled(enabled)
            val courses = courseRepository.getAllActive().first().data ?: emptyList()
            val tasks = taskRepository.getAll().first().data ?: emptyList()
            alarmScheduler.syncAbsenceAlarms(enabled, courses, tasks)
        }
    }
}