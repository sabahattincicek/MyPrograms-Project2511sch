package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentLinkBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class DialogFragmentLink: DialogFragment() {

    private var _binding: DialogFragmentLinkBinding?=null
    private val binding get() = _binding!!

    private var programTable: ProgramTable ?= null
    private var course: Course ?= null
    private var task: Task?=null
    private var file: File? = null

    private val viewModelFile: ViewModelFile by viewModels()

    private val TAG = "DialogFragmentLink"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DialogFragment is being created.")
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFragmentLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            programTable = BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java)
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)
            task = BundleCompat.getParcelable(it, ARG_TASK, Task::class.java)
            file = BundleCompat.getParcelable(it, ARG_FILE, File::class.java)
        }

        setupObservers()

        val isEditMode = file != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit_link)
            binding.etTitle.setText(file!!.title)
            binding.etUrl.setText(file!!.description)
        }else{
            binding.toolbar.title = getString(R.string.add_new_link)
        }

        binding.toolbar.setNavigationOnClickListener {
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
            // TODO: buraya link icin preview eklenecek
        }
        binding.btnSave.setOnClickListener {
            Log.d(TAG, "btnSave: clicked, isEditMode: $isEditMode")
            val title = binding.etTitle.text.toString()
            val url = binding.etUrl.text.toString()
            if (isEditMode){
                val updatedLink = file!!.copy(
                    title = title,
                    description = url
                )
                Log.i(TAG, "btnSave: Updating file: ${updatedLink.title}")
                viewModelFile.updateFile(updatedLink)
            }else{
                val programTableId = task?.programTableId ?: course?.programTableId ?: programTable?.id
                val courseId = task?.courseId ?: course?.id
                val taskId = task?.id

                val newLink = File(
                    id = IdGenerator.generateFileId(title),
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = programTableId,
                    courseId = courseId,
                    taskId = taskId,
                    title = title,
                    description = url,
                    fileType = "app/link",
                    filePath = "",
                    sizeInBytes = 0L
                )
                viewModelFile.insertLink(newLink)
            }
        }

        binding.btnCancel.setOnClickListener {
            Log.d(TAG, "btnCancel: clicked")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers(){
        Log.d(TAG, "setupObservers: called")
        //insert
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.insertLinkEvent.collect { resource ->
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
        const val ARG_PROGRAM_TABLE = "dialog_fragment_link_arg_program_table"
        const val ARG_COURSE = "dialog_fragment_link_arg_course"
        const val ARG_TASK = "dialog_fragment_link_arg_task"
        const val ARG_FILE = "dialog_fragment_link_arg_file"

        fun newInstanceCreate(): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf()
            }
        }
        fun newInstanceCreateForProgramTable(programTable: ProgramTable): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_TABLE to programTable
                )
            }
        }
        fun newInstanceCreateForCourse(course: Course): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf(
                    ARG_COURSE to course
                )
            }
        }
        fun newInstanceCreateForTask(task: Task): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf(
                    ARG_TASK to task
                )
            }
        }
        fun newInstanceEdit(file: File): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf(
                    ARG_FILE to file
                )
            }
        }
    }

}