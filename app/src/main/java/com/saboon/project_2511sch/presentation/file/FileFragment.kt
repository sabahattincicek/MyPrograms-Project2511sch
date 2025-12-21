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

    private lateinit var course: Course

    private val viewModelFile : ViewModelFile by viewModels()

    private lateinit var recyclerAdapter: RecyclerAdapterFile

    private val TAG = "FileFragment"

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        Log.d(TAG, "File picker result received.")
        if (uri != null) {
            Log.i(TAG, "File selected by user with URI: $uri")
            val dialog = DialogFragmentFile.newInstance(course, uri, null)
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
        Log.d(TAG, "onViewCreated: View has been created. Setting up UI and observers.")

        course = args.course

        setupRecyclerAdapter()
        observeFilesState()
        observeInsertNewFileEvent()
        observeDeleteFileEvent()
        observeUpdateFileEvent()

        Log.i(TAG, "onViewCreated: Requesting initial file list for course ID: ${course.id}")
        viewModelFile.getAllFilesByCourseId(course.id)

        binding.toolbar.subtitle = course.title

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar navigation clicked, navigating back.")
            findNavController().popBackStack()
        }

        binding.fabAddNewFile.setOnClickListener { anchorView ->
            Log.d(TAG, "FAB clicked, showing add file menu.")
            val popup = PopupMenu(requireContext(), anchorView)
            popup.menuInflater.inflate(R.menu.add_file_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_add_file -> {
                        Log.d(TAG, "'Add File' menu item clicked. Launching file picker.")
                        selectFileLauncher.launch(arrayOf("*/*"))
                        true
                    }
                    R.id.action_add_note -> {
                        Log.d(TAG, "'Add Note' menu item clicked.")
                        val dialog = DialogFragmentNote.newInstance(course, null)
                        dialog.show(childFragmentManager, "NoteDialogFragment")
                        true
                    }
                    R.id.action_add_link -> {
                        Log.d(TAG, "'Add Link' menu item clicked.")
                        // TODO: add necessary code for "add link" option
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentFile.REQUEST_KEY_CREATE, viewLifecycleOwner){ requestKey, result ->
            Log.d(TAG, "Result received from Create File Dialog with key: $requestKey")
            val newFile = BundleCompat.getParcelable(result, DialogFragmentFile.RESULT_KEY_FILE, File::class.java)
            val uri = BundleCompat.getParcelable(result, DialogFragmentFile.RESULT_KEY_URI, Uri::class.java)

            if(newFile != null && uri != null){
                Log.i(TAG, "Valid file and URI received from dialog. Passing to ViewModel.")
                viewModelFile.insertNewFile(newFile, uri)
            }else {
                Log.w(TAG, "Received null file or URI from Create File Dialog. File: $newFile, URI: $uri")
            }
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentFile.REQUEST_KEY_UPDATE, viewLifecycleOwner){requestKey, result ->
            Log.d(TAG, "Result received from Update File Dialog with key: $requestKey")
            val file = BundleCompat.getParcelable(result, DialogFragmentFile.RESULT_KEY_FILE, File::class.java)
            if (file != null){
                Log.i(TAG, "Valid file received from dialog. Passing to ViewModel for update.")
                viewModelFile.updateFile(file)
            }else{
                Log.w(TAG, "Received null file from Update File Dialog.")
            }
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentNote.REQUEST_KEY_CREATE, viewLifecycleOwner){requestKey, result ->
            Log.d(TAG, "Result received from Create Note Dialog with key: $requestKey")
            val note = BundleCompat.getParcelable(result, DialogFragmentNote.RESULT_KEY_NOTE, File::class.java)
            if (note != null){
                Log.i(TAG, "Valid note file received from dialog. Passing to ViewModel.")
                viewModelFile.insertNewNote(note)
            }else{
                Log.w(TAG, "Received null note from Create Note Dialog.")
            }
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(TAG, "setupRecyclerAdapter: Initializing and setting up RecyclerAdapterFile.")
        recyclerAdapter = RecyclerAdapterFile()

        recyclerAdapter.onItemClickListener = { clickedFile ->
            Log.i(TAG, "File item clicked: ${clickedFile.title}")
            openFile(clickedFile)
        }

        recyclerAdapter.onMenuItemClickListener = { file, clickedItem ->
            when(clickedItem){
                R.id.action_edit -> {
                    Log.d(TAG, "Edit menu clicked for file: ${file.title}")
                    val dialog = DialogFragmentFile.newInstance(course, null, file)
                    dialog.show(childFragmentManager, "UpdateFileDialogFragment")
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: View is being destroyed, nullifying binding.")
        _binding = null
    }
}
