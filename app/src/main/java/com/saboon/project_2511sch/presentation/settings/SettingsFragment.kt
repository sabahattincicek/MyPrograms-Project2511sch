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
    private var currentHomeViewRangeValue: String? = null
    private var currentHomeListItemColorEnabledValue: Boolean? = null
    private var currentHomeListItemColorSourceValue: String? = null
    private var currentOverscrollDaysCountValue: Int? = null
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
                        .setTitle("Dark Mode")
                        .setSingleChoiceItems(darkModeEntries, checkedItem) { dialog, which ->
                            val selectedValue = darkModeValues[which]
                            viewModelSettings.onDarkModeSelected(selectedValue)
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
                        .setTitle("Home View Range")
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
                        .setTitle("Overscroll Days Count")
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
                        .setTitle("Home List Item Color Source")
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
            }
        }
        binding.rvSettings.apply {
            adapter = recyclerAdapterSettings
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    private fun renderSettingsList() {
        val settingsList = mutableListOf<SettingsItem>()

        // DARK MODE
        settingsList.add(SettingsItem.Category("Appearance"))
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_DARK_MODE,
                title = "Dark Mode",
                value = currentDarkModeValue!!
            )
        )

        // HOME VIEW RANGE
        settingsList.add(SettingsItem.Category("Home Page"))
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_HOME_VIEW_RANGE,
                title = "Display Items View Range",
                summary = "Ana sayfada listelenecek görevlerin tarih aralığını seçin",
                value = currentHomeViewRangeValue!!
            )
        )

        // OVERSCROLL DAYS COUNT
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_OVERSCROLL_DAYS_COUNT,
                title = "Overscroll days count",
                summary = "How many extra days to load when scrolling",
                value = currentOverscrollDaysCountValue!!
            )
        )

        // HOME LIST ITEM COLOR ENABLED
        settingsList.add(
            SettingsItem.Toggle(
                key = SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_ENABLED,
                title = "Show List Item Colors",
                summary = "Enable or disable background colors in the home list",
                isChecked = currentHomeListItemColorEnabledValue!! // Artık kesinlikle doğru değer
            )
        )

        //HOME LIST ITEM COLOR SOURCE
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE,
                isUIEnabled = currentHomeListItemColorEnabledValue!!,
                title = "List Item Color Source",
                summary = "Determine the source of background colors for home list items",
                value = currentHomeListItemColorSourceValue?.replaceFirstChar { it.uppercase() } ?: ""
            )
        )

        recyclerAdapterSettings.submitList(settingsList)
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Tüm ayar akışlarını birleştiriyoruz
                kotlinx.coroutines.flow.combine(
                    viewModelSettings.appDarkModeState,
                    viewModelSettings.homeViewRangeState,
                    viewModelSettings.overScrollDaysCountState,
                    viewModelSettings.homeListItemColorEnabledState,
                    viewModelSettings.homeListItemColorSourceState
                ) { darkMode, viewRange, overscrollDaysCount, isColorEnabled, colorSource ->
                    // Değerleri güncelle
                    currentDarkModeValue = darkMode
                    currentHomeViewRangeValue = viewRange
                    currentOverscrollDaysCountValue = overscrollDaysCount
                    currentHomeListItemColorEnabledValue = isColorEnabled
                    currentHomeListItemColorSourceValue = colorSource
                }.collect {
                    // SADECE tüm veriler atandıktan sonra listeyi çiz
                    renderSettingsList()
                }
            }
        }
    }
}