package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import com.saboon.project_2511sch.databinding.DialogFragmentNoteBinding
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
class DialogFragmentNote: DialogFragment() {
    private var _binding : DialogFragmentNoteBinding?=null
    private val binding get() = _binding!!

    private var programTable: ProgramTable ?= null
    private var course: Course ?= null
    private var task: Task?=null
    private var file: File? = null
    private val viewModelFile: ViewModelFile by viewModels()

    private val TAG = "DialogFragmentNote"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Layout is being inflated.")
        _binding = DialogFragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DialogFragment is being created.")
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created, processing arguments.")

        arguments?.let {
            programTable = BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java)
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)
            task = BundleCompat.getParcelable(it, ARG_TASK, Task::class.java)
            file = BundleCompat.getParcelable(it, ARG_FILE, File::class.java)
        }

        setupObservers()

        val isEditMode = file != null
        if (isEditMode){
            Log.i(TAG, "Operating in Edit Mode.")
            binding.toolbar.title = getString(R.string.edit_note)
            binding.etNoteTitle.setText(file!!.title)
            binding.reEditor.html = file!!.description
        }else{
            Log.i(TAG, "Operating in Create Mode.")
            binding.toolbar.title = getString(R.string.add_new_note)
        }

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Navigation icon clicked, dismissing dialog.")
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

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "btnSave: clicked, isEditMode: $isEditMode")
            val title = binding.etNoteTitle.text.toString()
            val content = binding.reEditor.html ?: ""
            if (isEditMode){
                val updatedNote = file!!.copy(
                    title = title,
                    description = content,
                    sizeInBytes = content.toByteArray().size.toLong()
                )
                viewModelFile.updateFile(updatedNote)
            }else{
                val programTableId = task?.programTableId ?: course?.programTableId ?: programTable?.id
                val courseId = task?.courseId ?: course?.id
                val taskId = task?.id

                val newNote = File(
                    id = IdGenerator.generateFileId(title),
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = programTableId,
                    courseId = courseId,
                    taskId = taskId,
                    title = title,
                    description = content,
                    fileType = "app/note",
                    filePath = "",
                    sizeInBytes = content.toByteArray().size.toLong()
                )
                viewModelFile.insertNote(newNote)
            }
        }
        binding.btnCancel.setOnClickListener {
            Log.d(TAG, "btnCancel: clicked")
            dismiss()
        }

        val typedValue = TypedValue()

        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        binding.reEditor.setEditorBackgroundColor(typedValue.data)
        binding.reEditor.setBackgroundColor(typedValue.data)

        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        binding.reEditor.setEditorFontColor(typedValue.data)

        binding.reEditor.setPlaceholder("Insert text here...");

        binding.actionUndo.setOnClickListener {
            binding.reEditor.undo()
        }
        binding.actionRedo.setOnClickListener {
            binding.reEditor.redo()
        }
        binding.actionBold.setOnClickListener {
            binding.reEditor.setBold()
        }
        binding.actionItalic.setOnClickListener {
            binding.reEditor.setItalic()
        }
        binding.actionUnderline.setOnClickListener {
            binding.reEditor.setUnderline()
        }
        binding.actionStrikethrough.setOnClickListener {
            binding.reEditor.setStrikeThrough()
        }
        binding.actionBullet.setOnClickListener {
            binding.reEditor.setBullets()
        }
        binding.actionIndentIncrease.setOnClickListener {
            binding.reEditor.setIndent()
        }
        binding.actionIndentDecrease.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: View is being destroyed, nullifying binding.")
        _binding = null
    }

    private fun setupObservers(){
        //insert
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.insertNoteEvent.collect { resource ->
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
        const val ARG_PROGRAM_TABLE = "dialog_fragment_note_arg_program_table"
        const val ARG_COURSE = "dialog_fragment_note_arg_course"
        const val ARG_TASK = "dialog_fragment_note_arg_task"
        const val ARG_FILE = "dialog_fragment_note_arg_file"

        fun newInstanceCreate(): DialogFragmentNote{
            return DialogFragmentNote().apply {
                arguments = bundleOf()
            }
        }
        fun newInstanceCreateForProgramTable(programTable: ProgramTable): DialogFragmentNote{
            return DialogFragmentNote().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_TABLE to programTable
                )
            }
        }
        fun newInstanceCreateForCourse(course: Course): DialogFragmentNote{
            return DialogFragmentNote().apply {
                arguments = bundleOf(
                    ARG_COURSE to course
                )
            }
        }
        fun newInstanceCreateForTask(task: Task): DialogFragmentNote{
            return DialogFragmentNote().apply {
                arguments = bundleOf(
                    ARG_TASK to task
                )
            }
        }
        fun newInstanceEdit(file: File): DialogFragmentNote{
            return DialogFragmentNote().apply {
                arguments = bundleOf(
                    ARG_FILE to file
                )
            }
        }
    }
}
