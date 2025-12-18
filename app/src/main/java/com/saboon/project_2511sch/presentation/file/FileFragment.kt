package com.saboon.project_2511sch.presentation.file

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
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
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileOutputStream
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
            saveFileFromUri(uri)
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

        Log.i(TAG, "onViewCreated: Requesting initial file list for course ID: ${'"'}${course.id}${'"'}")
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
                        // TODO: add necessary code for "add note" option
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
    }

    private fun setupRecyclerAdapter() {
        Log.d(TAG, "setupRecyclerAdapter: Initializing and setting up RecyclerAdapterFile.")
        recyclerAdapter = RecyclerAdapterFile()
        binding.programRecyclerView.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun saveFileFromUri(uri: Uri) {
        Log.d(TAG, "saveFileFromUri: Starting to process URI: $uri")
        val contentResolver = requireContext().contentResolver
        var fileName = "unknown_file"
        var fileSize = 0L

        // 1. Dosyanın adını ve boyutunu ContentResolver ile sorgula.
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                Log.d(TAG, "saveFileFromUri: File metadata retrieved - Name: $fileName, Size: $fileSize bytes")
            }
        }

        try {
            // YORUM SATIRI KALDIRILDI: Bu adımlar dosya kopyalama için kritiktir.
            val inputStream = contentResolver.openInputStream(uri)
            val newFileName = "${'"'}${System.currentTimeMillis()}_${fileName}${'"'}"
            val newFile = JavaFile(requireContext().filesDir, newFileName)
            val outputStream = FileOutputStream(newFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Log.i(TAG, "saveFileFromUri: File successfully copied to internal storage at: ${'"'}${newFile.absolutePath}${'"'}")

            val newFileObject = File(
                id = IdGenerator.generateFileId(fileName),
                programTableId = course.programTableId,
                courseId = course.id,
                title = fileName,
                description = null,
                fileType = contentResolver.getType(uri) ?: "application/octet-stream",
                filePath = newFile.absolutePath, // GÜNCELLENDİ: Gerçek dosya yolu
                sizeInBytes = fileSize
            )

            Log.i(TAG, "saveFileFromUri: File model created. Passing to ViewModel to insert into DB: $newFileObject")
            viewModelFile.insertNewFile(newFileObject)

        } catch (e: Exception) {
            Log.e(TAG, "saveFileFromUri: Failed to save or copy file.", e)
        }
    }

    private fun observeFilesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "Subscribing to filesState flow.")
                viewModelFile.filesState.collect { resource ->
                    when (resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "FilesState: Error - ${'"'}${resource.message}${'"'}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(TAG, "FilesState: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "FilesState: Loading.")
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
                            Log.e(TAG, "InsertFileEvent: Error - ${'"'}${resource.message}${'"'}")
                            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {
                             Log.d(TAG, "InsertFileEvent: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(TAG, "InsertFileEvent: Loading...")
                            // TODO: Show a loading indicator to the user
                        }
                        is Resource.Success<*> -> {
                            Log.i(TAG, "InsertFileEvent: Success - File '${'"'}${resource.data?.title}${'"'}' saved.")
                            Toast.makeText(context, "File saved successfully", Toast.LENGTH_SHORT).show()
                            // The filesState Flow will automatically update the list.
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