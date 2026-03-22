package com.saboon.project_2511sch.presentation.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WidgetHomeReceiver : GlanceAppWidgetReceiver(){
    override val glanceAppWidget: GlanceAppWidget
        get() = WidgetHome()
}