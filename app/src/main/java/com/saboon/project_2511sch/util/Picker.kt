package com.saboon.project_2511sch.util

import android.content.Context
import android.text.format.DateFormat
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class Picker(context: Context, private val fragmentManager: FragmentManager) {
    private val clockFormat = if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
    private val calendar = Calendar.getInstance()
    private var _lastPickedTimeMillis: Long = System.currentTimeMillis()
    val lastPickedTimeMillis = _lastPickedTimeMillis
    private var _lastPickedDateMillis: Long = System.currentTimeMillis()
    val lastPickedDateMillis = _lastPickedDateMillis

    fun pickTimeMillis(title: String, callback:(Long) -> Unit){
        calendar.timeInMillis = _lastPickedTimeMillis
        val picker = MaterialTimePicker.Builder()
            .setTitleText(title)
            .setTimeFormat(clockFormat)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()
        picker.addOnPositiveButtonClickListener {
            val pickedTimeMillis = calendar.apply {
                set(Calendar.HOUR_OF_DAY, picker.hour)
                set(Calendar.MINUTE, picker.minute)
            }.timeInMillis
            _lastPickedTimeMillis = pickedTimeMillis
            callback(pickedTimeMillis)
        }
        picker.show(fragmentManager, "Picker_pickTimeMillis")
    }

    fun setPickedTimeMillis(timeMillis: Long){
        _lastPickedTimeMillis = timeMillis
    }

    fun pickDateMillis(title: String, callback: (Long) -> Unit){
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(_lastPickedDateMillis)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            _lastPickedDateMillis = selection
            callback(selection)
        }
        picker.show(fragmentManager, "Picker_pickDateMillis")
    }

    fun setPickedDateMillis(dateMillis: Long){
        _lastPickedDateMillis = dateMillis
    }
}