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
    private var _lastPickedTimeMillis: Long = 0L
    val lastPickedTimeMillis = _lastPickedTimeMillis
    private var _lastPickedDateMillis: Long = 0L
    val lastPickedDateMillis = _lastPickedDateMillis

    fun pickTimeMillis(title: String, setTimeMillis: Long = 0L, callback:(Long) -> Unit){
        val picker = MaterialTimePicker.Builder()
            .setTitleText(title)
            .setTimeFormat(clockFormat)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
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

    fun pickDateMillis(title: String, setDateMillis: Long = 0L, callback: (Long) -> Unit){
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(setDateMillis)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            _lastPickedDateMillis = selection
            callback(selection)
        }
        picker.show(fragmentManager, "Picker_pickDateMillis")
    }
}