package com.saboon.project_2511sch.presentation.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding?=null
    private val binding get() = _binding!!

    private val viewModelSettings: ViewModelSettings by viewModels()
    private lateinit var recyclerAdapterSettings: RecyclerAdapterSettings

    private var currentDarkModeValue: String? = null
    private var currentAppThemeValue: String? = null
    private var currentHomeViewRangeValue: String? = null
    private var currentHomeListItemColorEnabledValue: Boolean = true
    private var currentHomeListItemColorSourceValue: String? = null
    private var currentOverscrollDaysCountValue: Int? = null
    private var currentAbsenceReminderEnabledValue: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupObservers()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
        recyclerAdapterSettings = RecyclerAdapterSettings()
        recyclerAdapterSettings.onActionClick = { settingItem ->
            val item = settingItem as SettingsItem.Action
            when(item.key){
                SettingsConstants.PREF_KEY_DARK_MODE -> {
                    val darkModeEntries = resources.getStringArray(R.array.pref_dark_mode)
                    val darkModeValues = SettingsConstants.DarkMode.getValuesAsArray()

                    val checkedItem = darkModeValues.indexOf(currentDarkModeValue)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.darkMode))
                        .setSingleChoiceItems(darkModeEntries, checkedItem) { dialog, which ->
                            val selectedValue = darkModeValues[which]
                            viewModelSettings.onDarkModeSelected(selectedValue)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
                SettingsConstants.PREF_KEY_APP_THEME -> {
                    val appThemeEntries = resources.getStringArray(R.array.pref_app_theme)
                    val appThemeValues = SettingsConstants.AppTheme.getValuesAsArray()

                    val checkedItem = appThemeValues.indexOf(currentAppThemeValue)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.app_theme))
                        .setSingleChoiceItems(appThemeEntries, checkedItem) { dialog, which ->
                            val selectedValue = appThemeValues[which]
                            viewModelSettings.onAppThemeSelected(selectedValue)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
                SettingsConstants.PREF_KEY_HOME_VIEW_RANGE -> {
                    val homeViewRangeEntries = resources.getStringArray(R.array.pref_home_view_range)
                    val homeViewRangeValues = SettingsConstants.HomeViewRange.getValuesAsArray()

                    val checkedItem = homeViewRangeValues.indexOf(currentHomeViewRangeValue)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.loop_tasks))
                        .setSingleChoiceItems(homeViewRangeEntries, checkedItem) { dialog, which ->
                            val selectedValue = homeViewRangeValues[which]
                            viewModelSettings.onHomeViewRangeSelected(selectedValue)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
                SettingsConstants.PREF_KEY_OVERSCROLL_DAYS_COUNT -> {
                    val overscrollDaysCountEntries = resources.getStringArray(R.array.pref_overscroll_days_count)
                    val overscrollDaysCountValues = SettingsConstants.OverscrollDaysCount.getValuesAsArray()

                    val checkedItem = overscrollDaysCountValues.indexOf(currentOverscrollDaysCountValue)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.overscroll))
                        .setSingleChoiceItems(overscrollDaysCountEntries, checkedItem) { dialog, which ->
                            val selectedValue = overscrollDaysCountValues[which]
                            viewModelSettings.onOversrollDaysCountChanged(selectedValue)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
                SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE -> {
                    val homeListItemColorSourceEntries = resources.getStringArray(R.array.pref_home_list_item_color_source)
                    val homeListItemColorSourceValues = SettingsConstants.HomeListItemColorSource.getValuesAsArray()

                    val checkedItem = homeListItemColorSourceValues.indexOf(currentHomeListItemColorSourceValue)

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.show_list_item_colors))
                        .setSingleChoiceItems(homeListItemColorSourceEntries, checkedItem) { dialog, which ->
                            val selectedValue = homeListItemColorSourceValues[which]
                            viewModelSettings.onHomeListItemColorSourceSelected(selectedValue)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }
            }
        }
        recyclerAdapterSettings.onSwitchChange = { settingItem ->
            val item = settingItem as SettingsItem.Toggle
            when(item.key){
                SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED -> {
                    viewModelSettings.onHomeListItemColorEnabledChanged(item.isChecked)
                }
                SettingsConstants.PREF_KEY_ABSENCE_REMINDER_ENABLED -> {
                    viewModelSettings.onAbsenceReminderEnabledChanged(item.isChecked)
                }
            }
        }
        binding.rvSettings.apply {
            adapter = recyclerAdapterSettings
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    private fun renderSettingsList() {
        val settingsList = mutableListOf<SettingsItem>()

        settingsList.add(SettingsItem.Category(getString(R.string.appearance)))
        // DARK MODE
        val darkModeValues = SettingsConstants.DarkMode.getValuesAsArray()
        val darkModeEntries = resources.getStringArray(R.array.pref_dark_mode)
        val darkModeIndex = darkModeValues.indexOf(currentDarkModeValue).coerceAtLeast(0)
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_DARK_MODE,
                title = getString(R.string.darkMode),
                value = darkModeEntries[darkModeIndex]
            )
        )
        // APP THEME
        val appThemeValues = SettingsConstants.AppTheme.getValuesAsArray()
        val appThemeEntries = resources.getStringArray(R.array.pref_app_theme)
        val appThemeIndex = appThemeValues.indexOf(currentAppThemeValue).coerceAtLeast(0)
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_APP_THEME,
                title = getString(R.string.app_theme),
                value = appThemeEntries[appThemeIndex]
            )
        )

        settingsList.add(SettingsItem.Category(getString(R.string.home_page)))
        // HOME VIEW RANGE
        val homeViewRangeValues = SettingsConstants.HomeViewRange.getValuesAsArray()
        val homeViewRangeEntries = resources.getStringArray(R.array.pref_home_view_range)
        val homeViewRangeIndex = homeViewRangeValues.indexOf(currentHomeViewRangeValue).coerceAtLeast(0)
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_HOME_VIEW_RANGE,
                title = getString(R.string.loop_tasks),
                summary = getString(R.string.select_loop_of_task_to_be_shown_on_the_homepage),
                value = homeViewRangeEntries[homeViewRangeIndex]
            )
        )
        // OVERSCROLL DAYS COUNT
        val overscrollValues = SettingsConstants.OverscrollDaysCount.getValuesAsArray().map { it.toString() }
        val overscrollEntries = resources.getStringArray(R.array.pref_overscroll_days_count)
        val overscrollIndex = overscrollValues.indexOf(currentOverscrollDaysCountValue.toString()).coerceAtLeast(0)

        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_OVERSCROLL_DAYS_COUNT,
                title = getString(R.string.overscroll),
                summary = getString(R.string.select_how_many_extra_days_will_be_loaded_with_overscrolling_at_the_beginning_and_end_of_the_list_on_the_homepage),
                value = overscrollEntries[overscrollIndex]
            )
        )
        // HOME LIST ITEM COLOR ENABLED
        settingsList.add(
            SettingsItem.Toggle(
                key = SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED,
                title = getString(R.string.show_list_item_colors),
                summary = getString(R.string.display_the_background_colors_of_list_items_on_the_homepage_if_disabled_the_background_color_will_be_transparent),
                isChecked = currentHomeListItemColorEnabledValue
            )
        )
        //HOME LIST ITEM COLOR SOURCE
        val sourceValues = SettingsConstants.HomeListItemColorSource.getValuesAsArray()
        val sourceEntries = resources.getStringArray(R.array.pref_home_list_item_color_source)
        val sourceIndex = sourceValues.indexOf(currentHomeListItemColorSourceValue).coerceAtLeast(0)

        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE,
                isUIEnabled = currentHomeListItemColorEnabledValue,
                title = getString(R.string.source_of_list_item_colors),
                summary = getString(R.string.select_the_source_from_which_the_background_colors_of_list_items_on_the_homepage_should_be_derived),
                value = sourceEntries[sourceIndex]
            )
        )

        settingsList.add(SettingsItem.Category(getString(R.string.reminder)))
        // ABSENCE REMINDER ENABLED
        settingsList.add(
            SettingsItem.Toggle(
                key = SettingsConstants.PREF_KEY_ABSENCE_REMINDER_ENABLED,
                title = getString(R.string.absence_reminder),
                summary = getString(R.string.receive_a_notification_after_class_sessions_asking_whether_you_attended_the_class),
                isChecked = currentAbsenceReminderEnabledValue
            )
        )

        recyclerAdapterSettings.submitList(settingsList)
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flows = listOf(
                    viewModelSettings.appDarkModeState,
                    viewModelSettings.appThemeState,
                    viewModelSettings.homeViewRangeState,
                    viewModelSettings.overScrollDaysCountState,
                    viewModelSettings.homeListItemColorEnabledState,
                    viewModelSettings.homeListItemColorSourceState,
                    viewModelSettings.absenceReminderEnabledState
                )
                kotlinx.coroutines.flow.combine(flows) { values ->
                    val newAppTheme = values[1] as String
                    // Eğer App Theme değişmişse (ve ilk açılış değilse) activity'yi yeniden başlat
                    if (currentAppThemeValue != null && currentAppThemeValue != newAppTheme) {
                        currentAppThemeValue = newAppTheme
                        activity?.recreate()
                    }

                    currentDarkModeValue = values[0] as String
                    currentAppThemeValue = values[1] as String
                    currentHomeViewRangeValue = values[2] as String
                    currentOverscrollDaysCountValue = values[3] as Int
                    currentHomeListItemColorEnabledValue = values[4] as Boolean
                    currentHomeListItemColorSourceValue = values[5] as String
                    currentAbsenceReminderEnabledValue = values[6] as Boolean
                }.collect {
                    renderSettingsList()
                }
            }
        }
    }
}