package com.saboon.project_2511sch.presentation.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.key
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private var currentDarkModeValue: String = SettingsConstants.DarkMode.DEFAULT
    private var currentHomeViewRangeValue: String = SettingsConstants.HomeViewRange.DEFAULT
    private var currentHomeListItemColorSource: String = SettingsConstants.HomeListItemColorSource.DEFAULT

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
        recyclerAdapterSettings = RecyclerAdapterSettings()
        recyclerAdapterSettings.onSettingsClick = { settingsItem ->
            when(settingsItem){
                is SettingsItem.Action -> {
                    when(settingsItem.key){
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

                        SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE -> {
                            val homeListItemColorSourceEntries = resources.getStringArray(R.array.pref_home_list_item_color_source)
                            val homeListItemColorSourceValues = SettingsConstants.HomeListItemColorSource.getValuesAsArray()

                            val checkedItem = homeListItemColorSourceValues.indexOf(currentHomeListItemColorSource)

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
                is SettingsItem.Toggle -> {

                }
                else -> {}
            }
        }
        binding.rvSettings.apply {
            adapter = recyclerAdapterSettings
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    private fun renderSettingsList() {
        val settingsList = mutableListOf<SettingsItem>()

        // APPEARANCE SECTION
        settingsList.add(SettingsItem.Category("Appearance"))
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_DARK_MODE,
                title = "Dark Mode",
                value = currentDarkModeValue,
            )
        )

        // HOME PAGE
        settingsList.add(SettingsItem.Category("Home Page"))
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_HOME_VIEW_RANGE,
                title = "Display Items View Range",
                summary = "Ana sayfada listelenecek gorevlerin tarih araligini secin",
                value = currentHomeViewRangeValue
            )
        )
        settingsList.add(
            SettingsItem.Action(
                key = SettingsConstants.PREF_KEY_HOME_LIST_ITEM_COLOR_SOURCE,
                title = "Color Source",
                summary = "Ana sayfadaki listelerin arkaplan renklerinin kaynagi",
                value = currentHomeListItemColorSource
            )
        )

        recyclerAdapterSettings.submitList(settingsList)
    }
    private fun setupObservers() {
        // DARK MODE STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelSettings.appDarkModeState.collect { darkModeValue ->
                    currentDarkModeValue = darkModeValue
                    renderSettingsList()
                }
            }
        }
        // HOME VIEW RANGE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelSettings.homeViewRangeState.collect { homeViewRangeValue ->
                    currentHomeViewRangeValue = homeViewRangeValue
                    renderSettingsList()
                }
            }
        }
        //HOME LIST ITEM COLOR SOURCE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelSettings.homeListItemColorSourceState.collect { homeListItemColorSourceValue ->
                    currentHomeListItemColorSource = homeListItemColorSourceValue
                    renderSettingsList()
                }
            }
        }
    }
}