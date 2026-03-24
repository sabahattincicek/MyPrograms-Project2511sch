package com.saboon.project_2511sch.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.ActivityMainBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModelUser: ViewModelUser by viewModels()
    private val viewModelSettings: ViewModelSettings by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()

    private lateinit var navController: NavController

    private var isOnboardingCompleted = false
    private var isUserExist = false
    private var isReady = false

    private val bottomNavHiddenDestination = setOf(
        R.id.fragmentOnboarding,
        R.id.fragmentAboutYourself
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        applyInitialTheme()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isReady }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        handleWidgetIntent(intent)
        checkUserAndOnboardingState()
        setupBackPress()
        setupBottomNavVisibility()
        observeFlows()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWidgetIntent(intent)
    }

    // -------------------- SETUP --------------------

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.fragmentHome) {
                    finish()
                } else {
                    navController.popBackStack()
                }
            }
        })
    }

    private fun setupBottomNavVisibility() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                if (destination.id in bottomNavHiddenDestination) View.GONE else View.VISIBLE
        }
    }

    // -------------------- INIT LOGIC --------------------

    private fun checkUserAndOnboardingState() {

        val startDestination = if (isOnboardingCompleted && isUserExist){
            R.id.fragmentHome
        } else{
            R.id.fragmentOnboarding
        }

        val navGraph = navController.navInflater.inflate(R.navigation.main_navigation_graph)
        navGraph.setStartDestination(startDestination)

        navController.graph = navGraph
        binding.bottomNavigationView.setupWithNavController(navController)

        isReady = true
    }

    private fun applyInitialTheme() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = sharedPrefs.getString(
            SettingsConstants.PREF_KEY_APP_THEME,
            SettingsConstants.AppTheme.DEFAULT
        )

        val themeRes = when (theme) {
            SettingsConstants.AppTheme.COFFEE -> R.style.Theme_Project_2511sch_Coffee
            SettingsConstants.AppTheme.FOREST -> R.style.Theme_Project_2511sch_Forest
            SettingsConstants.AppTheme.OCEAN -> R.style.Theme_Project_2511sch_Ocean
            SettingsConstants.AppTheme.PONY -> R.style.Theme_Project_2511sch_Pony
            SettingsConstants.AppTheme.VOLCANO -> R.style.Theme_Project_2511sch_Volcano
            else -> R.style.Base_Theme_Project_2511sch
        }

        setTheme(themeRes)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        val courseId = intent?.getStringExtra("WIDGET_COURSE_ID")
        courseId?.let {
            viewModelCourse.getById(it)
        }
    }

    // -------------------- OBSERVERS --------------------

    private fun observeFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Dark Mode
                launch {
                    viewModelSettings.appDarkModeState.collect { darkMode ->
                        AppCompatDelegate.setDefaultNightMode(
                            when (darkMode) {
                                SettingsConstants.DarkMode.DARK_MODE ->
                                    AppCompatDelegate.MODE_NIGHT_YES

                                SettingsConstants.DarkMode.LIGHT_MODE ->
                                    AppCompatDelegate.MODE_NIGHT_NO

                                else ->
                                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            }
                        )
                    }
                }

                // Theme (log / future usage)
                launch {
                    viewModelSettings.appThemeState.collect {
                        // optional logging
                    }
                }

                // Widget course navigation
                launch {
                    viewModelCourse.courseState.collect { resource ->
                        handleCourseState(resource)
                    }
                }

                //user state
                launch {
                    viewModelUser.currentUser.collect { resource ->
                        when(resource) {
                            is Resource.Error -> {}
                            is Resource.Idle -> {}
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                if (resource.data != null){
                                    isUserExist = true
                                    checkUserAndOnboardingState()
                                }
                            }
                        }
                    }
                }

                //onboarding state
                launch {
                    viewModelSettings.onboardingCompletedState.collect { isCompleted ->
                        isOnboardingCompleted = isCompleted
                        checkUserAndOnboardingState()
                    }
                }
            }
        }
    }

    private fun handleCourseState(resource: Resource<Course>) {
        when (resource) {
            is Resource.Loading -> Unit

            is Resource.Error -> Unit

            is Resource.Success -> {
                resource.data?.let { course ->
                    val bundle = bundleOf("course" to course)
                    navController.navigate(
                        R.id.action_global_fragmentCourseDetails,
                        bundle
                    )
                }
            }

            else -> Unit
        }
    }
}