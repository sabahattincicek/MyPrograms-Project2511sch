package com.saboon.project_2511sch.presentation.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

object WidgetHelper {
    fun updateWidgetHome(context: Context){
        MainScope().launch {
            WidgetHome().updateAll(context)
        }
    }
}