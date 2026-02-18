package com.saboon.project_2511sch.presentation.home

import androidx.fragment.app.add
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.usecase.home.GetHomeDisplayItemsUseCase
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.presentation.common.FilterTask
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ViewModelHome @Inject constructor(
    private val getHomeDisplayItemsUseCase: GetHomeDisplayItemsUseCase,
    private val settingsRepository: ISettingsRepository
): ViewModel() {

    private val tag = "ViewModelHome"

    private val _dateRange = MutableStateFlow(getInitialRange())

    private val _filterGenericState = MutableStateFlow(FilterGeneric())
    private val _filterTaskState = MutableStateFlow(FilterTask())

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val displayItemsState = combine(
        _filterGenericState,
        _filterTaskState,
        _dateRange
    ) { filterGeneric, filterTask, range ->
        Triple(filterGeneric, filterTask, range)
    }.flatMapLatest { (filterGeneric, filterTask, range) ->
        getHomeDisplayItemsUseCase.invoke(filterGeneric, filterTask, range.start, range.end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    //FILTER
    fun updateFilterProgramTable(programTable: ProgramTable?){
        _filterGenericState.update { current ->
            if (programTable == null) FilterGeneric()
            else current.copy(programTable = programTable)
        }
    }
    fun updateFilterCourse(course: Course?){
        _filterGenericState.update { current ->
            current.copy(course = course)
        }
    }
    fun updateFilterTask(filter: FilterTask) {
        _filterTaskState.value = filter
    }

    // Mevcut haftayı yükle (Pazartesi - Pazar)
    fun loadData() {
        _dateRange.value = getInitialRange()
    }

    // Geriye doğru 30 gün daha ekle
    fun loadPrevious() {
        _dateRange.update { current ->
            val newStart = Calendar.getInstance().apply {
                timeInMillis = current.start
                add(Calendar.DAY_OF_YEAR, -30)
            }.timeInMillis
            current.copy(start = getDayStartMillis(newStart))
        }
    }

    // İleriye doğru 30 gün daha ekle
    fun loadNext() {
        _dateRange.update { current ->
            val newEnd = Calendar.getInstance().apply {
                timeInMillis = current.end
                add(Calendar.DAY_OF_YEAR, 30)
            }.timeInMillis
            current.copy(end = getDayEndMillis(newEnd))
        }
    }

    private fun getInitialRange(): DisplayRange {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val viewRange = runBlocking { settingsRepository.getHomeViewRange().first() }

        return when(viewRange){
            SettingsConstants.HomeViewRange.WEEK -> {
                // Pazartesiye git
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = getDayStartMillis(calendar.timeInMillis)

                // Pazartesi üzerine 6 gün ekle (Pazar)
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val end = getDayEndMillis(calendar.timeInMillis)

                DisplayRange(start, end)
            }
            SettingsConstants.HomeViewRange.MONTH -> {
                // Ayın 1'ine git
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = getDayStartMillis(calendar.timeInMillis)

                // Ayın son gününe git
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = getDayEndMillis(calendar.timeInMillis)

                DisplayRange(start, end)
            }
            else -> {
                // Pazartesiye git
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = getDayStartMillis(calendar.timeInMillis)

                // Pazartesi üzerine 6 gün ekle (Pazar)
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val end = getDayEndMillis(calendar.timeInMillis)

                DisplayRange(start, end)
            }
        }


//        val cal = java.util.Calendar.getInstance()
//        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
//        val daysFromMonday = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - java.util.Calendar.MONDAY
//
//        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysFromMonday)
//        val start = getDayStartMillis(cal.timeInMillis)
//
//        cal.add(java.util.Calendar.DAY_OF_YEAR, 6)
//        val end = getDayEndMillis(cal.timeInMillis)
//
//        return DisplayRange(start, end)
    }

    private fun getDayStartMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getDayEndMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
