package com.saboon.project_2511sch.presentation.settings

sealed class SettingsItem {
    data class Category(
        val title: String
    ): SettingsItem()

    data class Action(
        val key: String,
        val title: String,
        val summary: String? = null,
        val value: Any
    ): SettingsItem()

    data class Toggle(
        val key: String,
        val title: String,
        val summary: String? = null,
        val value: Any,
        var isChecked: Boolean,
    ): SettingsItem()

    companion object {
        //VIEW TYPES
        const val VIEW_TYPE_CATEGORY = 1
        const val VIEW_TYPE_ACTION = 2
        const val VIEW_TYPE_TOGGLE = 3
    }
}