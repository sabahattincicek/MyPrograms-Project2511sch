package com.saboon.project_2511sch.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private val DEFAULT_LOCALE: Locale get() = Locale.getDefault()
private const val DEFAULT_PATTERN = "dd-MM-yyyy HH:mm:ss"

fun Long.toFormattedString(format: String): String{
    val dateFormat = SimpleDateFormat(format, DEFAULT_LOCALE)
    return dateFormat.format(Date(this))
}