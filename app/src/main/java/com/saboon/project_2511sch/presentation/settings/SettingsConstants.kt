package com.saboon.project_2511sch.presentation.settings

import com.saboon.project_2511sch.presentation.settings.SettingsConstants.HomeListItemColorSource.FROM_COURSE
import com.saboon.project_2511sch.presentation.settings.SettingsConstants.HomeListItemColorSource.FROM_PROGRAM_TABLE

object SettingsConstants {
    //KEYS
    const val PREF_KEY_DARK_MODE = "pref_key_dark_mode"
    const val PREF_KEY_HOME_VIEW_RANGE = "pref_key_home_view_range"
    const val PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED = "pref_key_home_list_item_color_enabled"
    const val PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE = "pref_key_home_list_item_color"
    const val PREF_KEY_OVERSCROLL_DAYS_COUNT = "pref_key_overscroll_days_count"

    //APPEARANCE
    object DarkMode {
        const val OPEN = "open"
        const val CLOSE = "close"
        const val SYSTEM = "system"
        const val DEFAULT = SYSTEM
        /**
         * Returns an array of the available dark mode values in a consistent order.
         */
        fun getValuesAsArray(): Array<String> {
            return arrayOf(OPEN, CLOSE, SYSTEM) // same order with pref_dark_mode in arrays.xml
        }
    }

    //HOME PAGE
    object HomeViewRange {
        const val WEEK = "week"
        const val MONTH = "month"
        const val DEFAULT = WEEK
        fun getValuesAsArray(): Array<String> {
            return arrayOf(WEEK, MONTH) //same order with pref_home_view_range in arrays.xml
        }
    }
    object OverscrollDaysCount {
        const val DAYS_7 = 7
        const val DAYS_14 = 14
        const val DAYS_30 = 30
        const val DAYS_60 = 60
        const val DEFAULT = DAYS_30
        fun getValuesAsArray(): Array<Int>{
            return arrayOf(DAYS_7, DAYS_14, DAYS_30, DAYS_60) // same order with pref_overscroll_days_count in arrays.xml
        }
    }
    object HomeListItemColorEnabled {
        const val DEFAULT = true
    }
    object HomeListItemColorSource {
        const val FROM_COURSE = "course"
        const val FROM_PROGRAM_TABLE = "table"
        const val DEFAULT = FROM_COURSE
        fun getValuesAsArray(): Array<String>{
            return arrayOf(FROM_COURSE, FROM_PROGRAM_TABLE) // same order with pref_home_list_item_color_source in arrays.xml
        }
    }

}