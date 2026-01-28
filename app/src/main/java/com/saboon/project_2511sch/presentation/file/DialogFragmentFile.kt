package com.saboon.project_2511sch.presentation.file

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentFileBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentFile: DialogFragment() {

    private val TAG = "DialogFragmentFile"

    private var _binding: DialogFragmentFileBinding?=null
    private val binding get() = _binding!!

    private var programTable: ProgramTable ?= null
    private var course: Course ?= null
    private var task: Task?=null
    private var uri: Uri? = null
    private var file: File? = null

    private val viewModelFile: ViewModelFile by viewModels()

    private var fileName = ""
    private var fileType = "application/octet-stream"
    private var fileSize = 0L
    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        Log.d(TAG, "selectFileLauncher: result received, uri: $uri")
        if (uri != null) {
            this.uri = uri
            readMetaDataFromUri()
            showPreviewFromUri(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: called")
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        _binding = DialogFragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: called")

        arguments?.let {
            programTable = BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java)
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)
            task = BundleCompat.getParcelable(it, ARG_TASK, Task::class.java)
            file = BundleCompat.getParcelable(it, ARG_FILE, File::class.java)
            Log.d(TAG, "onViewCreated: args parsed, file: ${file?.title}, course: ${course?.title}")
        }

        setupObservers()

        val isEditMode = file != null
        if (isEditMode){
            Log.d(TAG, "onViewCreated: Edit mode detected")
            binding.etTitle.setText(file!!.title)
            binding.etDescription.setText(file!!.description)
            binding.tvFileType.text = file!!.fileType
            binding.tvFileSize.text = Formatter.formatShortFileSize(context, file!!.sizeInBytes)
            showPreviewFromPath(file!!.filePath)
        }else{
            Log.d(TAG, "onViewCreated: Create mode detected")
        }

        binding.toolbar.setNavigationOnClickListener { 
            Log.d(TAG, "toolbar: navigation clicked (dismiss)")
            dismiss() 
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    Log.d(TAG, "toolbar menu: delete clicked")
                    file?.let {
                        Log.i(TAG, "toolbar menu: deleting file: ${it.title}")
                        viewModelFile.deleteFile(it)
                    }
                    true
                }
                else -> false
            }
        }

        binding.ivFilePreview.setOnClickListener {
            Log.d(TAG, "ivFilePreview: clicked, file: $file, uri: $uri")
            if (file == null && uri == null) {
                Log.i(TAG, "ivFilePreview: Launching file picker")
                selectFileLauncher.launch(arrayOf("*/*"))
            }
        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "btnSave: clicked, isEditMode: $isEditMode")
            if (isEditMode){
                val updatedFile = file!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString()
                )
                Log.i(TAG, "btnSave: Updating file: ${updatedFile.title}")
                viewModelFile.updateFile(updatedFile)
            }else{
                if (uri == null) {
                    Log.w(TAG, "btnSave: No file selected in create mode, dismissing")
                    dismiss()
                }else{
                    val programTableId = task?.programTableId ?: course?.programTableId ?: programTable?.id
                    val courseId = task?.courseId ?: course?.id
                    val taskId = task?.id

                    val newFile = File(
                        id = IdGenerator.generateFileId(binding.etTitle.text.toString()),
                        appVersionAtCreation = getString(R.string.app_version),
                        programTableId = programTableId,
                        courseId = courseId,
                        taskId = taskId,
                        title = binding.etTitle.text.toString(),
                        description = binding.etDescription.text.toString(),
                        fileType = this.fileType,
                        filePath = "", //this field will fill in the repository
                        sizeInBytes = this.fileSize
                    )
                    Log.i(TAG, "btnSave: Inserting new file: ${newFile.title}, uri: $uri")
                    viewModelFile.insertFile(newFile, uri!!)
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            Log.d(TAG, "btnCancel: clicked")
            dismiss()
        }
    }

    private fun readMetaDataFromUri(){
        Log.d(TAG, "readMetaDataFromUri: called")
        uri?.let {
            val cursor = requireContext().contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    fileName = c.getString(c.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    fileSize = c.getLong(c.getColumnIndexOrThrow(OpenableColumns.SIZE))
                }
            }
            fileType = requireContext().contentResolver.getType(it) ?: "application/octet-stream"
            Log.d(TAG, "readMetaDataFromUri: name: $fileName, size: $fileSize, type: $fileType")
            binding.etTitle.setText(fileName)
            binding.tvFileType.text = fileType
            binding.tvFileSize.text = Formatter.formatShortFileSize(context, fileSize)
        }
    }

    private fun showPreviewFromUri(uri: Uri) {
        Log.d(TAG, "showPreviewFromUri: uri: $uri")
        try {
            val thumbnail = requireContext().contentResolver.loadThumbnail(uri, android.util.Size(512, 512), null)
            binding.ivFilePreview.setImageBitmap(thumbnail)
            binding.ivFilePreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        } catch (e: Exception) {
            Log.e(TAG, "showPreviewFromUri: Error loading thumbnail", e)
            binding.ivFilePreview.setImageResource(R.drawable.baseline_insert_drive_file_24)
        }
    }

    private fun showPreviewFromPath(filePath: String){
        Log.d(TAG, "showPreviewFromPath: path: $filePath")
        val javaFile = java.io.File(filePath)
        if (javaFile.exists()) {
            try {
                val thumbnail = android.media.ThumbnailUtils.createImageThumbnail(
                    javaFile,
                    android.util.Size(512, 512),
                    null
                )
                binding.ivFilePreview.setImageBitmap(thumbnail)
                binding.ivFilePreview.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                Log.e(TAG, "showPreviewFromPath: Error creating thumbnail", e)
                binding.ivFilePreview.setImageResource(R.drawable.baseline_insert_drive_file_24)
                binding.ivFilePreview.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            }
        } else {
            Log.w(TAG, "showPreviewFromPath: File does not exist at path: $filePath")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: called")
        _binding = null
    }

    private fun setupObservers(){
        Log.d(TAG, "setupObservers: called")
        //insert
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.insertNewFileEvent.collect { resource ->
                    Log.d(TAG, "insertNewFileEvent: collected resource: $resource")
                    when(resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "insertNewFileEvent: Error: ${resource.message}")
                        }
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            Log.i(TAG, "insertNewFileEvent: Success, dismissing")
                            dismiss()
                        }
                    }
                }
            }
        }
        //update
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.updateFileEvent.collect { resource ->
                    Log.d(TAG, "updateFileEvent: collected resource: $resource")
                    when(resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "updateFileEvent: Error: ${resource.message}")
                        }
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            Log.i(TAG, "updateFileEvent: Success, dismissing")
                            dismiss()
                        }
                    }
                }
            }
        }
        //delete
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.deleteFileEvent.collect { resource ->
                    Log.d(TAG, "deleteFileEvent: collected resource: $resource")
                    when(resource) {
                        is Resource.Error -> {
                            Log.e(TAG, "deleteFileEvent: Error: ${resource.message}")
                        }
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            Log.i(TAG, "deleteFileEvent: Success, dismissing")
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    companion object{
        const val ARG_PROGRAM_TABLE = "file_dialog_fragment_arg_program_table"
        const val ARG_COURSE = "file_dialog_fragment_arg_course"
        const val ARG_TASK = "file_dialog_fragment_arg_task"
        const val ARG_FILE = "file_dialog_fragment_arg_file"
        const val REQUEST_KEY_CREATE = "file_dialog_fragment_request_key_create"
        const val RESULT_KEY_FILE = "file_dialog_fragment_result_key_file"

        fun newInstanceCreate(): DialogFragmentFile{
            return DialogFragmentFile().apply {
                arguments = bundleOf()
            }
        }
        fun newInstanceCreateForProgramTable(programTable: ProgramTable): DialogFragmentFile{
            return DialogFragmentFile().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_TABLE to programTable
                )
            }
        }
        fun newInstanceCreateForCourse(course: Course): DialogFragmentFile{
            return DialogFragmentFile().apply {
                arguments = bundleOf(
                    ARG_COURSE to course
                )
            }
        }
        fun newInstanceCreateForTask(task: Task): DialogFragmentFile{
            return DialogFragmentFile().apply {
                arguments = bundleOf(
                    ARG_TASK to task
                )
            }
        }
        fun newInstanceEdit(file: File): DialogFragmentFile{
            return DialogFragmentFile().apply {
                arguments = bundleOf(
                    ARG_FILE to file
                )
            }
        }
    }
}
