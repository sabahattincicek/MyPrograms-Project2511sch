package com.saboon.project_2511sch.util

import android.content.Context
import android.text.format.DateFormat
import androidx.fragment.app.FragmentManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class TimePickers(context: Context) {

    private val clockFormat = if(DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    private lateinit var picker: MaterialTimePicker

    fun timePicker(fragmentManager: FragmentManager, title: String, callback:(Long)->Unit){
        picker = MaterialTimePicker.Builder()
            .setTitleText(title)
            .setTimeFormat(clockFormat)
            .setInputMode(INPUT_MODE_CLOCK)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .build()

        picker.addOnPositiveButtonClickListener {
            val selectedHour = picker.hour
            val selectedMinute = picker.minute
            val calendarResult = calendar.apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            callback(calendarResult.timeInMillis)
        }

        picker.show(fragmentManager, "TimePicker")
    }

}