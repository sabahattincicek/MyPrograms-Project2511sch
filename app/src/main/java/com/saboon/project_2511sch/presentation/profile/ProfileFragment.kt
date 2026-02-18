package com.saboon.project_2511sch.presentation.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import coil3.request.crossfade
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.databinding.FragmentProfileBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? =null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelProfile: ViewModelProfile by viewModels()

    private var exportFile: File? = null

    private lateinit var currentUser: User
    private var isInitialDataLoaded = false


    //select folder to save export file
    private val exportFileToDeviceLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            saveExportFileIntoDevice(it)
        }
    }

    //select file to import
    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){ uri ->
        uri?.let {
            viewModelProfile.importData(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        binding.etFullName.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.fullName) {
                viewModelUser.update(currentUser.copy(fullName = text))
            }
        }
        binding.etRole.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.role) {
                viewModelUser.update(currentUser.copy(role = text))
            }
        }
        binding.etAcademicLevel.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.academicLevel) {
                viewModelUser.update(currentUser.copy(academicLevel = text))
            }
        }
        binding.etOrganization.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.organisation) {
                viewModelUser.update(currentUser.copy(organisation = text))
            }
        }
        binding.etAboutMe.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.aboutMe) {
                viewModelUser.update(currentUser.copy(aboutMe = text))
            }
        }
        binding.tvSettings.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToSettingsFragment()
            findNavController().navigate(action)
        }
        binding.tvExportData.setOnClickListener {
            viewModelProfile.exportData()
        }
        binding.tvImportData.setOnClickListener {
            importFileLauncher.launch("application/zip")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyDataToView(){
        binding.ivProfilePicture.load(File(currentUser.photoUrl)){
            crossfade(true)
        }
        if (!isInitialDataLoaded) {
            binding.etFullName.setText(currentUser.fullName)
            binding.etRole.setText(currentUser.role)
            binding.etAcademicLevel.setText(currentUser.academicLevel)
            binding.etOrganization.setText(currentUser.organisation)
            binding.etAboutMe.setText(currentUser.aboutMe)
            isInitialDataLoaded = true
        }
    }
    private fun setupObservers(){
        //USER STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.currentUser.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            currentUser = resource.data!!
                            applyDataToView()
                        }
                    }
                }
            }
        }
        //USER EVENT: UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {}
                    }
                }
            }
        }
        //EXPORT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProfile.exportEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            exportFile = resource.data!!

                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(exportFile!!.name)
                                .setItems(arrayOf("Share", "Save to device")){ _, which ->
                                    when(which){
                                        0 -> {shareBackupFile(exportFile!!)} //SHARE
                                        1 -> {exportFileToDeviceLauncher.launch(exportFile!!.name)} //SAVE TO DEVICE
                                    }
                                }
                                .setNegativeButton("Cancel") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    }
                }
            }
        }
        //IMPORT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProfile.importEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {}
                    }
                }
            }
        }
    }

    private fun shareBackupFile(file: File){
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Backup"))
    }

    private fun saveExportFileIntoDevice(uri: Uri){
        val file = exportFile ?: return
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }catch (e: Exception){
            Log.e("ProfileFragment", "Dosya kaydedilemedi: ${e.message}")
        }finally {
            if (file.exists()) file.delete()
            exportFile = null
        }
    }
}