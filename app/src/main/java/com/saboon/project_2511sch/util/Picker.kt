package com.saboon.project_2511sch.util

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class Picker(context: Context, private val fragmentManager: FragmentManager) {
    
    private val TAG = "Picker"
    private val clockFormat = if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
    private val calendar = Calendar.getInstance()
    private var _lastPickedTimeMillis: Long = System.currentTimeMillis()
    val lastPickedTimeMillis get() = _lastPickedTimeMillis
    private var _lastPickedDateMillis: Long = System.currentTimeMillis()
    val lastPickedDateMillis get() = _lastPickedDateMillis

    fun pickTimeMillis(title: String, callback:(Long) -> Unit){
        Log.d(TAG, "pickTimeMillis: Showing time picker with title: $title")
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
            Log.i(TAG, "pickTimeMillis: Time picked: $pickedTimeMillis")
            callback(pickedTimeMillis)
        }
        picker.show(fragmentManager, "Picker_pickTimeMillis")
    }

    fun pickDateMillis(title: String, callback: (Long) -> Unit){
        Log.d(TAG, "pickDateMillis: Showing date picker with title: $title")
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(_lastPickedDateMillis)
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            selection?.let {
                _lastPickedDateMillis = it
                Log.i(TAG, "pickDateMillis: Date picked: $it")
                callback(it)
            } ?: Log.w(TAG, "pickDateMillis: Selection was null")
        }
        picker.show(fragmentManager, "Picker_pickDateMillis")
    }
}
