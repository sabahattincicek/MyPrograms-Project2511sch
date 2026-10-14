package com.saboon.project_2511sch.presentation.profile

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogEditProfileBinding
import com.saboon.project_2511sch.databinding.FragmentProfileBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.AppConstants
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * The "Profile" tab of the bottom navigation (presentation layer).
 *
 * Shows a header card with the user's initials, name, "title · class · school" line and
 * "about me" text, a random Atatürk quote, and option groups: settings, backup export / import,
 * and links to support, privacy policy, terms, about and the donation page.
 *
 * Design notes:
 * - The user is read from the activity scoped [ViewModelUser], so the header always reflects the
 *   active user; edits go back through [ViewModelUser.update].
 * - Profile details are edited in a [MaterialAlertDialogBuilder] dialog
 *   (`dialog_edit_profile.xml`) instead of in place, which keeps the page itself read-only.
 * - Texts for the header are built by [ProfileHeaderFormatter] so the rules can be unit tested.
 * - Backup export / import run in [ViewModelProfile]; this fragment only picks files and shows results.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelProfile: ViewModelProfile by viewModels()

    /** Backup file produced by the last export, kept until it is shared or saved. */
    private var exportFile: File? = null

    /** The active user; null until the first [Resource.Success] arrives. */
    private var currentUser: User? = null

    /** Lets the user pick where to save the export file. */
    private val exportFileToDeviceLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) {
            Timber.d("Save export file cancelled by the user")
            return@registerForActivityResult
        }
        saveExportFileIntoDevice(uri)
    }

    /** Lets the user pick a backup file to import. */
    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            Timber.d("Import file selection cancelled by the user")
            return@registerForActivityResult
        }
        Timber.i("Import file selected, starting import")
        viewModelProfile.importData(uri)
    }

    /** Inflates the profile layout with view binding. */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    /** Fills the static texts and wires up observers and click listeners. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")

        val quotes = resources.getStringArray(R.array.ataturk_quotes)
        binding.tvAtaturkQuotes.text = "\"${quotes.random()}\""
        binding.tvAppVersion.text = getString(R.string.version_label, getString(R.string.app_version))

        setupObservers()
        setupListeners()
    }

    /** Releases the binding when the view is destroyed. */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Connects every button and option row on the page to its action. */
    private fun setupListeners() {
        binding.btnEditUser.setOnClickListener { showEditProfileDialog() }
        binding.tvSettings.setOnClickListener {
            Timber.d("Navigating to settings")
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToSettingsFragment())
        }
        binding.tvExportData.setOnClickListener {
            Timber.i("Export requested")
            viewModelProfile.exportData()
        }
        binding.tvImportData.setOnClickListener {
            importFileLauncher.launch("application/zip")
        }
        binding.tvSupport.setOnClickListener { openUrl(AppConstants.SUPPORT_URL) }
        binding.tvPrivacyPolicy.setOnClickListener { openUrl(AppConstants.PRIVACY_POLICY_URL) }
        binding.tvTerms.setOnClickListener { openUrl(AppConstants.TERMS_OF_USE_URL) }
        binding.tvAbout.setOnClickListener { openUrl(AppConstants.ABOUT_APP_URL) }
        binding.tvSupportProject.setOnClickListener { openUrl(AppConstants.DONATE_PAGE_URL) }
    }

    /** Opens [url] in the browser; logs and ignores the click when no app can open it. */
    private fun openUrl(url: String) {
        Timber.d("Opening url: %s", url)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "No app found to open url: %s", url)
        }
    }

    /** Shows the header card for [user]: initials avatar, name, details line and "about me". */
    private fun bindHeader(user: User) {
        val initials = ProfileHeaderFormatter.initials(user.fullName)
        binding.tvAvatarInitials.text = initials
        binding.ivAvatarPlaceholder.visibility = if (initials.isEmpty()) View.VISIBLE else View.GONE

        binding.tvFullName.text = user.fullName.trim().ifEmpty { getString(R.string.profile_name_placeholder) }

        binding.tvProfileSubtitle.text = ProfileHeaderFormatter
            .details(user.role, user.academicLevel, user.institution)
            .ifEmpty { getString(R.string.profile_details_placeholder) }

        val aboutMe = user.aboutMe.trim()
        binding.tvAboutMe.text = aboutMe
        binding.tvAboutMe.visibility = if (aboutMe.isEmpty()) View.GONE else View.VISIBLE
    }

    /** Opens the "Edit profile" dialog filled with the current user and saves the changes on confirm. */
    private fun showEditProfileDialog() {
        val user = currentUser
        if (user == null) {
            Timber.w("Edit profile clicked before the user was loaded")
            return
        }
        Timber.d("Opening edit profile dialog: id=%s", user.id)

        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        dialogBinding.etFullName.setText(user.fullName)
        dialogBinding.etRole.setText(user.role)
        dialogBinding.etAcademicLevel.setText(user.academicLevel)
        dialogBinding.etOrganisation.setText(user.institution)
        dialogBinding.etAboutMe.setText(user.aboutMe)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_profile)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                Timber.d("Edit profile cancelled")
                dialog.dismiss()
            }
            .setPositiveButton(R.string.save) { _, _ ->
                val updatedUser = user.copy(
                    fullName = dialogBinding.etFullName.text.toString().trim(),
                    role = dialogBinding.etRole.text.toString().trim(),
                    academicLevel = dialogBinding.etAcademicLevel.text.toString().trim(),
                    institution = dialogBinding.etOrganisation.text.toString().trim(),
                    aboutMe = dialogBinding.etAboutMe.text.toString().trim()
                )
                Timber.i("Saving profile: id=%s", user.id)
                viewModelUser.update(updatedUser)
            }
            .show()
    }

    /** Collects the user state and the update, export and import events while the view is started. */
    private fun setupObservers() {
        //USER STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelUser.currentUser.collect { resource ->
                    when (resource) {
                        is Resource.Error -> Timber.e("Failed to load user: %s", resource.message)
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val user = resource.data
                            if (user == null) {
                                Timber.w("No active user found")
                            } else {
                                Timber.d("User loaded: id=%s", user.id)
                                currentUser = user
                                bindHeader(user)
                            }
                        }
                    }
                }
            }
        }
        //USER EVENT: UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelUser.operationEvent.collect { resource ->
                    when (resource) {
                        is Resource.Error -> Timber.e("User update failed: %s", resource.message)
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val user = resource.data!!.data
                            Timber.i("User updated: id=%s", user.id)
                            currentUser = user
                            bindHeader(user)
                        }
                    }
                }
            }
        }
        //EXPORT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelProfile.exportEvent.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            Timber.e("Export failed: %s", resource.message)
                            Toast.makeText(requireContext(), R.string.exportFail, Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val file = resource.data!!
                            Timber.i("Export file ready")
                            exportFile = file
                            showExportOptions(file)
                        }
                    }
                }
            }
        }
        //IMPORT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelProfile.importEvent.collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            Timber.e("Import failed: %s", resource.message)
                            binding.flLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), R.string.importFail, Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Idle -> {}
                        is Resource.Loading -> {
                            binding.flLoading.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            Timber.i("Import finished")
                            binding.flLoading.visibility = View.GONE
                            Toast.makeText(requireContext(), R.string.importSuccessfully, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /** Asks whether the export [file] should be shared or saved to the device. */
    private fun showExportOptions(file: File) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(file.name)
            .setItems(arrayOf(getString(R.string.share), getString(R.string.save_to_device))) { _, which ->
                when (which) {
                    0 -> shareBackupFile(file)
                    1 -> exportFileToDeviceLauncher.launch(file.name)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /** Opens the system share sheet for the backup [file] through the app's FileProvider. */
    private fun shareBackupFile(file: File) {
        Timber.d("Sharing export file")
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

    /** Copies the export file into the document at [uri] chosen by the user, then deletes the temporary file. */
    private fun saveExportFileIntoDevice(uri: Uri) {
        val file = exportFile
        if (file == null) {
            Timber.w("Save requested but there is no export file")
            return
        }
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Timber.i("Export file saved to device")
            Toast.makeText(requireContext(), R.string.exportSuccessfully, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Timber.e(e, "Failed to save export file")
            Toast.makeText(requireContext(), R.string.exportFail, Toast.LENGTH_SHORT).show()
        } finally {
            if (file.exists()) file.delete()
            exportFile = null
        }
    }
}
