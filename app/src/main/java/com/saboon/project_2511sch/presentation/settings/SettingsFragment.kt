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

    private lateinit var currentDarkModeValue: String

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
                    if (settingsItem.key == SettingsItem.PREF_KEY_DARK_MODE) {
                        showThemeSelectionDialog()
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
    private fun showThemeSelectionDialog() {
        val darkModeEntries = resources.getStringArray(R.array.pref_dark_mode_entries)
        val darkModeValues = resources.getStringArray(R.array.pref_dark_mode_values)

        // Mevcut seçili olanı bul
        val currentDarkModeValue = viewModelSettings.appDarkModeState.value
        val checkedItem = darkModeValues.indexOf(currentDarkModeValue)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Dark Mode")
            .setSingleChoiceItems(darkModeEntries, checkedItem) { dialog, which ->
                val selectedThemeValue = darkModeValues[which]
                viewModelSettings.onDarkModeSelected(selectedThemeValue)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun renderSettingsList() {
        val settingsList = mutableListOf<SettingsItem>()

        // APPEARANCE SECTION
        settingsList.add(SettingsItem.Category("Appearance"))
        settingsList.add(
            SettingsItem.Action(
                key = SettingsItem.PREF_KEY_DARK_MODE,
                title = "Dark Mode",
                value = currentDarkModeValue,
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
    }
}