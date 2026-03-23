package com.saboon.project_2511sch.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.ActivityMainBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.presentation.course.FragmentCourseDetailsDirections
import com.saboon.project_2511sch.presentation.course.FragmentCourseList
import com.saboon.project_2511sch.presentation.course.FragmentCourseListDirections
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModelSettings: ViewModelSettings by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()


    private val bottomNavHiddenDestination = setOf(
        R.id.splashFragment,
        R.id.fragmentOnboarding
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        applyInitialTheme() // Daha super.onCreate çağrılmadan, veritabanındaki temayı anlık oku ve bas.
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleWidgetIntent(intent)

        observeTheme()



        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestinationId = navController.currentDestination?.id
                when {
                    // 1. Eğer Splash ekranındaysak veya Home ekranındaysak uygulamayı kapat
                    currentDestinationId == R.id.splashFragment || currentDestinationId == R.id.fragmentHome -> {
                        finish()
                    }
                    // 2. Eğer başka bir alt sekmedeysek (Tag, Course, Task vb.) direkt Home'a git
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWidgetIntent(intent)
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

    private fun handleWidgetIntent(intent: Intent?){
        val courseId = intent?.getStringExtra("WIDGET_COURSE_ID")
        if (courseId != null) viewModelCourse.getById(courseId)
    }
    private fun observeTheme(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSettings.appDarkModeState.collect { darkModeValue ->
                    when (darkModeValue) {
                        SettingsConstants.DarkMode.DARK_MODE  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        SettingsConstants.DarkMode.LIGHT_MODE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.courseState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val course = resource.data
                            if (course != null){
                                val bundle = bundleOf("course" to course)
                                findNavController(R.id.fragmentContainerView).navigate(R.id.action_global_fragmentCourseDetails, bundle)
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSettings.onboardingCompletedState.collect { completed ->
                    if (!completed){
//                        findNavController(R.id.fragmentContainerView).navigate(R.id.onboardingFragment)
                    }
                }
            }
        }
    }
}