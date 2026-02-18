package com.saboon.project_2511sch.presentation.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.core.content.edit

@HiltViewModel
class ViewModelSettings @Inject constructor(
    private val sharedPreferences: SharedPreferences
): ViewModel() {
    fun applyTheme(themeValue: String){
        when(themeValue){
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "system" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        sharedPreferences.edit { putString("pref_key_app_theme", themeValue) }
    }
}