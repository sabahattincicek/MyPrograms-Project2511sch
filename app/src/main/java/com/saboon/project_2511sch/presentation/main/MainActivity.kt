package com.saboon.project_2511sch.presentation.main

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.ActivityMainBinding
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModelSettings: ViewModelSettings by viewModels()


    private val bottomNavHiddenDestination = setOf(
        R.id.splashFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        applyInitialTheme() // Daha super.onCreate çağrılmadan, veritabanındaki temayı anlık oku ve bas.

        super.onCreate(savedInstanceState)

        observeTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestinationId = navController.currentDestination?.id
                when {
                    // 1. Eğer Splash ekranındaysak veya Home ekranındaysak uygulamayı kapat
                    currentDestinationId == R.id.splashFragment || currentDestinationId == R.id.homeFragment -> {
                        finish()
                    }
                    // 2. Eğer başka bir alt sekmedeysek (ProgramTable, Course, Task vb.) direkt Home'a git
                    // ve aradaki tüm geçmişi temizle
                    else -> {
//                        navController.popBackStack(R.id.homeFragment, false)
                        navController.popBackStack()
                    }
                }
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in bottomNavHiddenDestination) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }
    }

    private fun applyInitialTheme() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val currentTheme = sharedPrefs.getString(SettingsConstants.PREF_KEY_APP_THEME, SettingsConstants.AppTheme.DEFAULT)

        val themeResourceId = when (currentTheme) {
            SettingsConstants.AppTheme.COFFEE -> R.style.Theme_Project_2511sch_Coffee
            SettingsConstants.AppTheme.FOREST -> R.style.Theme_Project_2511sch_Forest
            SettingsConstants.AppTheme.OCEAN -> R.style.Theme_Project_2511sch_Ocean
            SettingsConstants.AppTheme.PONY -> R.style.Theme_Project_2511sch_Pony
            SettingsConstants.AppTheme.VOLCANO -> R.style.Theme_Project_2511sch_Volcano
            else -> R.style.Base_Theme_Project_2511sch
        }
        setTheme(themeResourceId)
    }

    private fun observeTheme(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSettings.appDarkModeState.collect { darkModeValue ->
                    when (darkModeValue) {
                        SettingsConstants.DarkMode.OPEN  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        SettingsConstants.DarkMode.CLOSE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSettings.appThemeState.collect { theme ->

                }
            }
        }
    }
}