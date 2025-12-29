package com.saboon.project_2511sch.presentation.file

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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentFileBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File as JavaFile

@AndroidEntryPoint
class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding?=null
    private val binding get() = _binding!!

    private val args : FileFragmentArgs by navArgs()

    private var course: Course? = null

    private val viewModelFile : ViewModelFile by viewModels()

    private lateinit var recyclerAdapter: RecyclerAdapterFile

    private val TAG = "FileFragment"

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        Log.d(TAG, "File picker result received.")
        if (uri != null) {
            Log.i(TAG, "File selected by user with URI: $uri")
            val dialog = DialogFragmentFile.newInstanceForCreate(course!!, uri)
            dialog.show(childFragmentManager, "CreateFileFragmentDialog")
        } else {
            Log.d(TAG, "File selection was cancelled by the user.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment is being created.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Layout is being inflated.")
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View has been created.")

        course = args.course

        setupToolbar()
        setupRecyclerAdapter()
        setupFragmentResultListeners()
        setupObservers()

        // Configure the fragment's behavior based on whether a course was passed.
        if (course != null) {
            configureForCourseMode(course!!)
        } else {
            configureForAllFilesMode()
        }
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation clicked, navigating back.")
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(TAG, "setupRecyclerAdapter: Initializing and setting up RecyclerAdapterFile.")
        recyclerAdapter = RecyclerAdapterFile()

        recyclerAdapter.onItemClickListener = { clickedFile ->
            Log.i(TAG, "File item clicked: ${clickedFile.title}")
            when (clickedFile.fileType){
                "app/note" -> {
                    Log.i(TAG, "Note item clicked: ${clickedFile.title}. Opening Note Editor.")
                    val dialog = DialogFragmentNote.newInstanceForEdit(clickedFile)
                    dialog.show(childFragmentManager, "NoteDialogFragment_editMode")
                }
                "app/link" -> {
                    val dialog = DialogFragmentLink.newInstanceForEdit(clickedFile)
                    dialog.show(childFragmentManager, "LinkDialogFragment_editMode")
                }
                else -> {
                    Log.i(TAG, "File item clicked: ${clickedFile.title}. Opening with system viewer.")
                    openFile(clickedFile)
                }
            }
        }

        recyclerAdapter.onMenuItemClickListener = { file, clickedItem ->
            when(clickedItem){
                R.id.action_delete -> {
                    Log.d(TAG, "Delete menu clicked for file: ${file.title}")
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete File", "Are you sure you want to delete '${file.title}'?")
                    dialog.show(childFragmentManager, "DeleteDialogFragment")
                    childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, viewLifecycleOwner){requestKey, result ->
                        val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
                        if (isYes) {
                            Log.i(TAG, "Deletion confirmed for file: ${file.title}. Calling ViewModel.")
                            viewModelFile.deleteFile(file)
                        } else {
                            Log.d(TAG, "Deletion cancelled for file: ${file.title}.")
                        }
                    }
                }
            }
        }

        binding.programRecyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Configures the UI when a specific course is provided.
     * Shows the FAB and fetches files for that course.
     */
    private fun configureForCourseMode(currentCourse: Course) {
        Log.d(TAG, "Configuring UI for course-specific mode: ${currentCourse.title}")
        binding.toolbar.title = "Files" // Or keep it as it is
        binding.toolbar.subtitle = currentCourse.title
        binding.fabAddNewFile.visibility = View.VISIBLE

        binding.fabAddNewFile.setOnClickListener { anchorView ->
            Log.d(TAG, "FAB clicked, showing add file menu.")
            showAddFileMenu(anchorView, currentCourse)
        }

        Log.i(TAG, "Requesting initial file list for course ID: ${currentCourse.id}")
        viewModelFile.getAllFilesByCourseId(currentCourse.id)
    }

    /**
     * Configures the UI for the generic "all files" mode.
     * Hides the FAB and fetches all files from the repository.
     */
    private fun configureForAllFilesMode() {
        Log.d(TAG, "Configuring UI for all-files mode.")
        binding.toolbar.title = "All Files"
        binding.toolbar.subtitle = null
        binding.fabAddNewFile.visibility = View.GONE

        Log.i(TAG, "Requesting all files list.")
        viewModelFile.getAllFiles()
    }

    private fun showAddFileMenu(anchorView: View, currentCourse: Course) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.add_file_menu, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_add_file -> {
                        Log.d(TAG, "'Add File' menu item clicked. Launching file picker.")
                        selectFileLauncher.launch(arrayOf("*/*"))
                        true
                    }
                    R.id.action_add_note -> {
                        Log.d(TAG, "'Add Note' menu item clicked.")
                        val dialog = DialogFragmentNote.newInstanceForCreate(currentCourse)
                        dialog.show(childFragmentManager, "NoteDialogFragment")
                        true
                    }
                    R.id.action_add_link -> {
                        Log.d(TAG, "'Add Link' menu item clicked.")
                        val dialog = DialogFragmentLink.newInstanceForCreate(currentCourse)
                        dialog.show(childFragmentManager, "LinkDialogFragment_createLink")
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun setupFragmentResultListeners() {
        // Listener for creating a physical file
        childFragmentManager.setFragmentResultListener(DialogFragmentFile.REQUEST_KEY_CREATE, viewLifecycleOwner) { _, result ->
            val newFile = BundleCompat.getParcelable(result, DialogFragmentFile.RESULT_KEY_FILE, File::class.java)
            val uri = BundleCompat.getParcelable(result, DialogFragmentFile.RESULT_KEY_URI, Uri::class.java)
            if (newFile != null && uri != null) viewModelFile.insertNewFile(newFile, uri)
        }

        // Listener for updating any file type
        childFragmentManager.setFragmentResultListener(DialogFragmentFile.REQUEST_KEY_UPDATE, viewLifecycleOwner) { _, result ->
            val updatedFile = BundleCompat.getParcelable(result, DialogFragmentFile.RESULT_KEY_FILE, File::class.java)
            updatedFile?.let { viewModelFile.updateFile(it) }
        }

        // Listener for creating a note
        childFragmentManager.setFragmentResultListener(DialogFragmentNote.REQUEST_KEY_CREATE, viewLifecycleOwner) { _, result ->
            val newNote = BundleCompat.getParcelable(result, DialogFragmentNote.RESULT_KEY_NOTE, File::class.java)
            newNote?.let { viewModelFile.insertNewNote(it) }
        }

        // Listener for updating a note
        childFragmentManager.setFragmentResultListener(DialogFragmentNote.REQUEST_KEY_UPDATE, viewLifecycleOwner) { _, result ->
            val updatedNote = BundleCompat.getParcelable(result, DialogFragmentNote.RESULT_KEY_NOTE, File::class.java)
            updatedNote?.let { viewModelFile.updateFile(it) }
        }

        // Listener for creating a link
        childFragmentManager.setFragmentResultListener(DialogFragmentLink.REQUEST_KEY_CREATE, viewLifecycleOwner) { _, result ->
            val newLink = BundleCompat.getParcelable(result, DialogFragmentLink.RESULT_KEY_LINK, File::class.java)
            newLink?.let { viewModelFile.insertNewLink(it) }
        }

        // Listener for updating a link
        childFragmentManager.setFragmentResultListener(DialogFragmentLink.REQUEST_KEY_UPDATE, viewLifecycleOwner) { _, result ->
            val updatedLink = BundleCompat.getParcelable(result, DialogFragmentLink.RESULT_KEY_LINK, File::class.java)
            updatedLink?.let { viewModelFile.updateFile(it) }
        }
    }

    private fun setupObservers() {
        observeFilesState()
        observeInsertNewFileEvent()
        observeDeleteFileEvent()
        observeUpdateFileEvent()
        observeInsertNewNoteEvent()
        observeInsertNewLinkEvent()
    }

    private fun openFile(file: File){
        try {
            // 1. Modelimizdeki dosya yolundan bir java.io.File nesnesi oluştur.
            val fileToOpen = JavaFile(file.filePath)

            if (!fileToOpen.exists()){
                Log.e(TAG, "File not found at path: ${file.filePath}")
                Toast.makeText(context, "Error: File not found.", Toast.LENGTH_SHORT).show()
                return
            }

            // 2. FileProvider kullanarak güvenli, paylaşılabilir bir content:// Uri'si al.
            // Buradaki "authorities" string'i, Manifest'teki ile birebir aynı olmalıdır.
            val fileUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                fileToOpen
            )

            // 3. Dosyayı görüntülemek için bir ACTION_VIEW Intent'i oluştur.
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, file.fileType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Log.i(TAG, "Attempting to open file with URI: $fileUri and type: ${file.fileType}")
            startActivity(intent)
        }catch (e: Exception) {
            // Cihazda bu dosya türünü açacak bir uygulama yüklü değilse bu hata alınır.
            Log.e(TAG, "Error opening file: ${file.title}", e)
            Toast.makeText(requireContext(), "No application available to open this file type.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeFilesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "Subscribing to filesState flow.")
                viewModelFile.filesState.collect { resource ->
                    when (resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "FilesState: Error - ${resource.message}")
                            // Optionally show a persistent error message in the UI
                        }
                        is Resource.Idle<*> -> {
                            Log.d(TAG, "FilesState: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "FilesState: Loading.")
                            // TODO: Show a loading indicator, e.g., a ProgressBar
                        }
                        is Resource.Success<*> -> {
                            val itemCount = resource.data?.size ?: 0
                            Log.i(TAG, "FilesState: Success - Submitting $itemCount files to adapter.")
                            recyclerAdapter.submitList(resource.data)
                        }
                    }
                }
            }
        }
    }
    private fun observeUpdateFileEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.updateFileEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "UpdateFileEvent: Error - ${resource.message}")
                            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {
                            Log.d(TAG, "UpdateFileEvent: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "UpdateFileEvent: Loading...")
                            Toast.makeText(context, "Updating file...", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Success<*> -> {
                            Log.i(TAG, "UpdateFileEvent: Success - '${resource.data?.title}' was updated.")
                            Toast.makeText(context, "File updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    private fun observeDeleteFileEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.deleteFileEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "DeleteFileEvent: Error - ${resource.message}")
                            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {
                             Log.d(TAG, "DeleteFileEvent: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "DeleteFileEvent: Loading...")
                            Toast.makeText(context, "Deleting file...", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Success<*> -> {
                            Log.i(TAG, "DeleteFileEvent: Success - '${resource.data?.title}' was deleted.")
                            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    private fun observeInsertNewFileEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.insertNewFileEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "InsertFileEvent: Error - ${resource.message}")
                            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {
                            Log.d(TAG, "InsertFileEvent: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "InsertFileEvent: Loading...")
                            Toast.makeText(context, "Saving file...", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Success<*> -> {
                            Log.i(TAG, "InsertFileEvent: Success - File '${resource.data?.title}' saved.")
                            Toast.makeText(context, "File saved successfully", Toast.LENGTH_SHORT).show()
                            // The filesState Flow will automatically update the list.
                        }
                    }
                }
            }
        }
    }
    private fun observeInsertNewNoteEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.insertNewNoteEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "InsertNoteEvent: Error - ${resource.message}")
                            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {
                            Log.d(TAG, "InsertNoteEvent: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "InsertNoteEvent: Loading...")
                            Toast.makeText(context, "Saving note...", Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Success<*> -> {
                            Log.i(TAG, "InsertNoteEvent: Success - Note '${resource.data?.title}' saved.")
                            Toast.makeText(context, "Note saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    private fun observeInsertNewLinkEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.insertNewLinkEvent.collect { resource ->
                    when (resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: View is being destroyed, nullifying binding.")
        _binding = null
    }
}
