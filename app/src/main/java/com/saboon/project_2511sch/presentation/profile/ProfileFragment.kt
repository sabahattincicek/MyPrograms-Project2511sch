package com.saboon.project_2511sch.presentation.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentProfileBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Character
import com.saboon.project_2511sch.util.CharacterManager
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.net.toUri
import com.saboon.project_2511sch.util.AppConstants


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? =null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelProfile: ViewModelProfile by viewModels()
    private val viewModelSettings: ViewModelSettings by viewModels()

    private var exportFile: File? = null

    private lateinit var currentUser: User

    private lateinit var characterManager: CharacterManager
    private lateinit var selectedCharacter: Character
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

        characterManager = CharacterManager(requireContext())
        selectedCharacter = characterManager.getCharacter("av1")!!

//        binding.ivProfilePicture.setOnClickListener {
//            val dialog = DialogFragmentCharacter()
//            dialog.show(childFragmentManager, "dialogFragmentCharacter")
//        }

        binding.ivEditUser.setOnClickListener {
            binding.ivEditUser.visibility = View.GONE
            binding.llTextViewContainer.visibility = View.GONE
            binding.ivSaveUser.visibility = View.VISIBLE
            binding.llEditTextContainer.visibility = View.VISIBLE
        }
        binding.ivSaveUser.setOnClickListener {
            val updatedUser = currentUser.copy(
                fullName = binding.etFullName.text.toString(),
                role = binding.etRole.text.toString(),
                academicLevel = binding.etAcademicLevel.text.toString(),
                institution = binding.etOrganisation.text.toString(),
                aboutMe = binding.etAboutMe.text.toString()
            )
            viewModelUser.update(updatedUser)

            binding.ivEditUser.visibility = View.VISIBLE
            binding.llTextViewContainer.visibility = View.VISIBLE
            binding.ivSaveUser.visibility = View.GONE
            binding.llEditTextContainer.visibility = View.GONE
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
        binding.tvSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, AppConstants.SUPPORT_URL.toUri())
            startActivity(intent)
        }
        binding.tvPrivacyPolicy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, AppConstants.PRIVACY_POLICY_URL.toUri())
            startActivity(intent)
        }
        binding.tvTerms.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, AppConstants.TERMS_OF_USE_URL.toUri())
            startActivity(intent)
        }
        binding.tvAbout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, AppConstants.ABOUT_APP_URL.toUri())
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyDataToView(){
//        binding.ivProfilePicture.load(File(currentUser.photoUrl)){
//            crossfade(true)
//        }
        binding.tvFullName.text = currentUser.fullName
        binding.tvRole.text = currentUser.role
        binding.tvAcademicLevel.text = currentUser.academicLevel
        binding.tvOrganisation.text = currentUser.institution
        binding.tvAboutMe.text = currentUser.aboutMe

        binding.etFullName.setText(currentUser.fullName)
        binding.etRole.setText(currentUser.role)
        binding.etAcademicLevel.setText(currentUser.academicLevel)
        binding.etOrganisation.setText(currentUser.institution)
        binding.etAboutMe.setText(currentUser.aboutMe)
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
        //CHARACTER STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSettings.selectedCharacterState.collect { characterId ->
                    selectedCharacter = characterManager.getCharacter(characterId)!!
//                    binding.ivProfilePicture.load(selectedCharacter.portrait)
//                    binding.ivCharacterVibe.load(selectedCharacter.cover)
//                    binding.tvCharName.text = selectedCharacter.name
//                    binding.tvCharVibe.text = selectedCharacter.activities[0].content["tr"]
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
                        is Resource.Success -> {
                            applyDataToView()
                        }
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
                                .setItems(arrayOf(getString(R.string.share), getString(R.string.save_to_device))){ _, which ->
                                    when(which){
                                        0 -> {shareBackupFile(exportFile!!)} //SHARE
                                        1 -> {exportFileToDeviceLauncher.launch(exportFile!!.name)} //SAVE TO DEVICE
                                    }
                                }
                                .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
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
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), R.string.importFail, Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Idle -> {}
                        is Resource.Loading -> {
                            binding.flLoading.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.flLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), R.string.importSuccessfully, Toast.LENGTH_SHORT).show()
                        }
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
            Toast.makeText(requireContext(), R.string.exportSuccessfully, Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            Log.e("ProfileFragment", "Dosya kaydedilemedi: ${e.message}")
            Toast.makeText(requireContext(), R.string.exportFail, Toast.LENGTH_SHORT).show()
        }finally {
            if (file.exists()) file.delete()
            exportFile = null
        }
    }

//    private fun animateCharacterSelection(show: Boolean){
//        val selectionContent = binding.clCharacterSelectionContent
//        val centerImage = binding.ivSelectedCharacterCenter //char1
//        val petals = listOf(binding.char2, binding.char3, binding.char4, binding.char5, binding.char6)
//
//        if (show){
//            // 1. Önce içeriği görünür yap ve arka planı yavaşça karart
//            selectionContent.visibility = View.VISIBLE
//            selectionContent.alpha = 0f
//            selectionContent.animate().alpha(1f).setDuration(300).start()
//
//            // 2. Merkezdeki resmi "zıplayarak" konumuna getir (Opsiyonel: Eğer ana resimden buraya kaymasını istiyorsan)
//            centerImage.scaleX = 0f
//            centerImage.scaleY = 0f
//            centerImage.animate()
//                .scaleX(1f).scaleY(1f)
//                .setDuration(500)
//                .setInterpolator(OvershootInterpolator())
//                .start()
//
//            // 3. Papatya yapraklarını (Karakterleri) tek tek aç
//            petals.forEachIndexed { index, view ->
//                view.alpha = 0f
//                view.scaleX = 0f
//                view.scaleY = 0f
//
//                // Circular Radius Animasyonu (Merkezden dışarı fırlama efekti)
//                val params = view.layoutParams as ConstraintLayout.LayoutParams
//                val finalRadius = params.circleRadius // XML'deki 150dp değeri
//
//                val radiusAnimator = ValueAnimator.ofInt(0, finalRadius)
//                radiusAnimator.addUpdateListener { animator ->
//                    params.circleRadius = animator.animatedValue as Int
//                    view.layoutParams = params
//                }
//
//                view.animate()
//                    .alpha(1f)
//                    .scaleX(1f)
//                    .scaleY(1f)
//                    .setStartDelay(index * 50L) // Sırayla açılma hissi verir
//                    .setDuration(600)
//                    .setInterpolator(OvershootInterpolator())
//                    .start()
//
//                radiusAnimator.duration = 600
//                radiusAnimator.startDelay = index * 50L
//                radiusAnimator.start()
//            }
//        }else{
//            // Kapatma animasyonu (Geriye toplama)
//            selectionContent.animate()
//                .alpha(0f)
//                .setDuration(300)
//                .withEndAction { selectionContent.visibility = View.GONE }
//                .start()
//        }
//    }
}