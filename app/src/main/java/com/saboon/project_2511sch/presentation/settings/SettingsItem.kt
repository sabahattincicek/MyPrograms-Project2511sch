package com.saboon.project_2511sch.presentation.settings

sealed class SettingsItem {
    data class Category(
        val title: String
    ): SettingsItem()

    data class Action(
        val key: String,
        val title: String,
        val summary: String,
    ): SettingsItem()

    data class Toggle(
        val key: String,
        val title: String,
        val summary: String,
        var isChecked: Boolean,
    ): SettingsItem()

    companion object {
        const val VIEW_TYPE_CATEGORY = 1
        const val VIEW_TYPE_ACTION = 2
        const val VIEW_TYPE_TOGGLE = 3
    }
}