package com.saboon.project_2511sch.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.repository.ISettingsRepository
import com.saboon.project_2511sch.domain.usecase.home.GetHomeTimelineUseCase
import com.saboon.project_2511sch.domain.model.FilterTask
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.mapData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for the home timeline.
 * 
 * Holds the shown date range and the task type filter and exposes the timeline rows for them
 * ([displayItemsState]). The range starts as the current week or month (a user setting) and
 * grows a number of days at a time when the user overscrolls the list.
 */
@HiltViewModel
class ViewModelHome @Inject constructor(
    private val getHomeTimelineUseCase: GetHomeTimelineUseCase,
    private val settingsRepository: ISettingsRepository
): ViewModel() {

    private val _dateRange = MutableStateFlow(getInitialRange())
    private val _filterTaskState = MutableStateFlow(FilterTask())

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val displayItemsState = combine(
        _filterTaskState,
        _dateRange,
        settingsRepository.getHomeEmptyHeaderHideEnabled()
    ) { filterTask, range, isHideHeaders ->
        Triple(filterTask, range, isHideHeaders)
    }.flatMapLatest { (filterTask, range, isHideHeaders) ->
        getHomeTimelineUseCase.invoke(filterTask, range.start, range.end, isHideHeaders)
            .map { resource -> resource.mapData { it.toHomeDisplayItems() } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    //FILTER
    /**
     * Changes which task types the timeline shows.
     */
    fun updateFilterTask(filter: FilterTask) {
        Timber.d("Filter changed: lesson=%s, exam=%s, homework=%s", filter.lesson, filter.exam, filter.homework)
        _filterTaskState.value = filter
    }

    /**
     * Resets the range to the initial week or month, unless a non-empty timeline is already shown.
     */
    fun loadData() {
        if (displayItemsState.value is Resource.Success &&
            (displayItemsState.value as Resource.Success).data?.isNotEmpty() == true){
            return
        }
        _dateRange.value = getInitialRange()
    }

    /**
     * Extends the range [daysCount] days into the past.
     */
    fun loadPrevious(daysCount: Int) {
        Timber.d("Extend range to the past: days=%d", daysCount)
        _dateRange.update { current ->
            val newStart = Calendar.getInstance().apply {
                timeInMillis = current.start
                add(Calendar.DAY_OF_YEAR, -daysCount)
            }.timeInMillis
            current.copy(start = getDayStartMillis(newStart))
        }
    }

    /**
     * Extends the range [daysCount] days into the future.
     */
    fun loadNext(daysCount: Int) {
        Timber.d("Extend range to the future: days=%d", daysCount)
        _dateRange.update { current ->
            val newEnd = Calendar.getInstance().apply {
                timeInMillis = current.end
                add(Calendar.DAY_OF_YEAR, daysCount)
            }.timeInMillis
            current.copy(end = getDayEndMillis(newEnd))
        }
    }

    /**
     * Returns the current week (Monday to Sunday) or the current month, depending on the
     * home view range setting.
     */
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
    }

    /**
     * Returns the first millisecond of the day [timestamp] falls on.
     */
    private fun getDayStartMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Returns the last millisecond of the day [timestamp] falls on.
     */
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
