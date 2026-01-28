package com.saboon.project_2511sch.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.usecase.home.GetHomeDisplayItemsUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ViewModelHome @Inject constructor(
    private val getHomeDisplayItemsUseCase: GetHomeDisplayItemsUseCase,
): ViewModel() {

    private val tag = "ViewModelHome"

    // 1. State'ler: Filtre ve Tarih Aralığı
    private val _filterState = MutableStateFlow(FilterTask())
    val filterState = _filterState.asStateFlow()
    private val _dateRange = MutableStateFlow(getInitialWeekRange())
    @OptIn(ExperimentalCoroutinesApi::class)
    val displayItemsState = combine(
        _filterState,
        _dateRange
    ) { filter, range ->
        filter to range
    }.flatMapLatest { (filter, range) ->
        Log.d(tag, "Fetching items for range: ${range.start} - ${range.end}")
        getHomeDisplayItemsUseCase.invoke(filter, range.start, range.end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    // Mevcut haftayı yükle (Pazartesi - Pazar)
    fun loadCurrentWeek() {
        _dateRange.value = getInitialWeekRange()
    }

    // Geriye doğru 30 gün daha ekle
    fun loadPrevious() {
        _dateRange.update { current ->
            val newStart = java.util.Calendar.getInstance().apply {
                timeInMillis = current.start
                add(java.util.Calendar.DAY_OF_YEAR, -30)
            }.timeInMillis
            current.copy(start = getDayStartMillis(newStart))
        }
    }

    // İleriye doğru 30 gün daha ekle
    fun loadNext() {
        _dateRange.update { current ->
            val newEnd = java.util.Calendar.getInstance().apply {
                timeInMillis = current.end
                add(java.util.Calendar.DAY_OF_YEAR, 30)
            }.timeInMillis
            current.copy(end = getDayEndMillis(newEnd))
        }
    }

    // Chip filtrelerini güncellemek için
    fun updateFilter(filter: FilterTask) {
        _filterState.value = filter
    }

    private fun getInitialWeekRange(): DisplayRange {
        val cal = java.util.Calendar.getInstance()
        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else dayOfWeek - java.util.Calendar.MONDAY

        cal.add(java.util.Calendar.DAY_OF_YEAR, -daysFromMonday)
        val start = getDayStartMillis(cal.timeInMillis)

        cal.add(java.util.Calendar.DAY_OF_YEAR, 6)
        val end = getDayEndMillis(cal.timeInMillis)

        return DisplayRange(start, end)
    }

    private fun getDayStartMillis(timestamp: Long): Long {
        return java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getDayEndMillis(timestamp: Long): Long {
        return java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

//    fun getDisplayItems(filterTask: FilterTask){
//        viewModelScope.launch {
//            try {
//                _displayItemsState.value = Resource.Loading()
//                Log.d(tag, "getDisplayItems: State set to Loading")
//
//                getHomeDisplayItemsUseCase.invoke(filterTask).collect { resource ->
//                    Log.d(tag, "getDisplayItems: Resource received: ${resource::class.java.simpleName}")
//                    _displayItemsState.value = resource
//                }
//            } catch (e: Exception){
//                Log.e(tag, "getDisplayItems: Error occurred", e)
//                _displayItemsState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
//            }
//        }
//    }
}
