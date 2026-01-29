package com.saboon.project_2511sch.presentation.file

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.os.BundleCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentFileBinding
import com.saboon.project_2511sch.domain.model.BaseModel
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
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

    private var programTable: ProgramTable? = null
    private var course: Course? = null
    private var task: Task? = null

    private val viewModelFile : ViewModelFile by viewModels()

    private lateinit var recyclerAdapter: RecyclerAdapterFile

    private val tag = "FileFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate: Fragment initialized.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView: Inflating layout.")
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated: View hierarchy created.")

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        programTable = args.programTable
        course = args.course
        task = args.task

        Log.d(tag, "onViewCreated: Arguments - ProgramTable: ${programTable?.id}, Course: ${course?.id}, Task: ${task?.id}")

        viewModelFile.updateProgramTableFilter(programTable)
        viewModelFile.updateCourseFilter(course)
        viewModelFile.updateTaskFilter(task)

        applyDataToView()
        setupRecyclerAdapter()
        setupFragmentResultListeners()
        setupObservers()

        binding.etSearch.doAfterTextChanged {
            val query = it.toString().trim()
            Log.d(tag, "Search query changed: $query")
            val originalList = (viewModelFile.filesState.value as? Resource.Success<List<File>>)?.data
            if(originalList != null){
                if (query.isNotEmpty()){
                    val filteredList = originalList.filter { file ->
                        val titleMatches = file.title.contains(query, true)
                        val descriptionMatches = file.title.contains(query, true)
                        val taskTitleMatches = (file.taskId ?: "").contains(query, true)
                        val courseTitleMatches = (file.courseId ?: "").contains(query, true)
                        val programTableTitleMatches = (file.programTableId ?: "").contains(query, true)
                        titleMatches || descriptionMatches || taskTitleMatches || courseTitleMatches || programTableTitleMatches
                    }
                    Log.d(tag, "Filtering list. Original size: ${originalList.size}, Filtered size: ${filteredList.size}")
                    recyclerAdapter.submitList(filteredList)
                }else{
                    Log.d(tag, "Query empty, submitting original list.")
                    recyclerAdapter.submitList(originalList)
                }
            }
        }
         binding.fabAddNewFile.setOnClickListener { anchorView ->
            showAddFileMenu(anchorView)
         }
        binding.cpProgramTable.setOnClickListener {
            Log.d(tag, "Chip ProgramTable clicked.")
            binding.cpProgramTable.isChecked = !binding.cpProgramTable.isChecked
            val dialog = DialogFragmentFileFilter.newInstanceFilterProgramTable()
            dialog.show(childFragmentManager, "DialogFragmentFileFilter")
        }
        binding.cpProgramTable.setOnCloseIconClickListener {
            programTable = null
            viewModelFile.updateProgramTableFilter(programTable)

            binding.cpProgramTable.isChecked = false
            binding.cpProgramTable.isCloseIconVisible = false
            binding.cpProgramTable.text = getString(R.string.program_table)
        }
        binding.cpCourse.setOnClickListener {
            Log.d(tag, "Chip Course clicked.")
            binding.cpCourse.isChecked = !binding.cpCourse.isChecked
            if(programTable != null){
                val dialog = DialogFragmentFileFilter.newInstanceFilterCourse(programTable!!.id)
                dialog.show(childFragmentManager, "DialogFragmentFileFilter")
            }else{
                Log.w(tag, "Course filter requested but programTable is null.")
            }
        }
        binding.cpCourse.setOnCloseIconClickListener {
            course = null
            viewModelFile.updateCourseFilter(course)

            binding.cpCourse.isChecked = false
            binding.cpCourse.isCloseIconVisible = false
            binding.cpCourse.text = getString(R.string.course)
        }
        binding.cpTask.setOnClickListener {
            Log.d(tag, "Chip Task clicked.")
            binding.cpTask.isChecked = !binding.cpTask.isChecked
            if (course != null){
                val dialog = DialogFragmentFileFilter.newInstanceFilterTask(course!!.id)
                dialog.show(childFragmentManager, "DialogFragmentFileFilter")
            }else{
                Log.w(tag, "Task filter requested but course is null.")
            }
        }
        binding.cpTask.setOnCloseIconClickListener {
            task = null
            viewModelFile.updateTaskFilter(task)

            binding.cpTask.isChecked = false
            binding.cpTask.isCloseIconVisible = false
            binding.cpTask.text = getString(R.string.task)
        }
    }

    private fun applyDataToView(){
        Log.d(tag, "applyDataToView: Updating UI chips based on initial state.")
        if (programTable != null){
            binding.cpProgramTable.isChecked = true
            binding.cpProgramTable.isCloseIconVisible = true
            binding.cpProgramTable.text = programTable!!.title
        }
        if (course != null){
            binding.cpCourse.isChecked = true
            binding.cpCourse.isCloseIconVisible = true
            binding.cpCourse.text = course!!.title
        }
        if (task != null){
            binding.cpTask.isChecked = true
            binding.cpTask.isCloseIconVisible = true
            binding.cpTask.text = task!!.title
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(tag, "setupRecyclerAdapter: Configuring RecyclerView and Adapter.")
        recyclerAdapter = RecyclerAdapterFile()

        recyclerAdapter.onItemClickListener = { clickedFile ->
            Log.i(tag, "onItemClickListener: File ID: ${clickedFile.id}, Type: ${clickedFile.fileType}")
            when (clickedFile.fileType){
                "app/note" -> {
                    Log.d(tag, "Opening DialogFragmentNote for edit.")
                    val dialog = DialogFragmentNote.newInstanceEdit(clickedFile)
                    dialog.show(childFragmentManager, "NoteDialogFragment_editMode")
                }
                "app/link" -> {
                    Log.d(tag, "Opening DialogFragmentLink for edit.")
                    val dialog = DialogFragmentLink.newInstanceEdit(clickedFile)
                    dialog.show(childFragmentManager, "LinkDialogFragment_editMode")
                }
                else -> {
                    Log.d(tag, "Calling openFile for external viewer.")
                    val dialog = DialogFragmentFile.newInstanceEdit(clickedFile)
                    dialog.show(childFragmentManager, "FileDialogFragment_editMode")
                }
            }
        }
        recyclerAdapter.onFilePreviewClickListener = { clickedFile ->
            when (clickedFile.fileType){
                "app/note" -> {}
                "app/link" -> {}
                else -> {
                    openFile(clickedFile)
                }
            }
        }

        recyclerAdapter.onMenuItemClickListener = { file, clickedItem ->
            when(clickedItem){
                R.id.action_delete -> {
                    Log.d(tag, "onMenuItemClickListener: Delete action triggered for file: ${file.id}")
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete File", "Are you sure you want to delete '${file.title}'?")
                    dialog.show(childFragmentManager, "DeleteDialogFragment")
                    childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, viewLifecycleOwner){requestKey, result ->
                        val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
                        Log.d(tag, "DeleteConfirmation result: $isYes")
                        if (isYes) {
                            viewModelFile.deleteFile(file)
                        }
                    }
                }
            }
        }

        binding.rvFile.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showAddFileMenu(anchorView: View) {
        Log.d(tag, "showAddFileMenu: Displaying PopupMenu.")
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.add_file_menu, menu)
            setOnMenuItemClickListener { item ->
                Log.d(tag, "AddFileMenu item clicked: ${item.itemId}")
                when (item.itemId) {
                    R.id.action_add_file -> {
                        if (task != null) DialogFragmentFile.newInstanceCreateForTask(task!!).show(childFragmentManager, "DialogFragmentFile")
                        else if (course != null) DialogFragmentFile.newInstanceCreateForCourse(course!!).show(childFragmentManager, "DialogFragmentFile")
                        else if (programTable != null) DialogFragmentFile.newInstanceCreateForProgramTable(programTable!!).show(childFragmentManager, "DialogFragmentFile")
                        else DialogFragmentFile.newInstanceCreate().show(childFragmentManager, "DialogFragmentFile")
                        true
                    }
                    R.id.action_add_note -> {
                        if (task != null) DialogFragmentNote.newInstanceCreateForTask(task!!).show(childFragmentManager, "DialogFragmentNote")
                        else if (course != null) DialogFragmentNote.newInstanceCreateForCourse(course!!).show(childFragmentManager, "DialogFragmentNote")
                        else if (programTable != null) DialogFragmentNote.newInstanceCreateForProgramTable(programTable!!).show(childFragmentManager, "DialogFragmentNote")
                        else DialogFragmentNote.newInstanceCreate().show(childFragmentManager, "DialogFragmentNote")
                        true
                    }
                    R.id.action_add_link -> {
                        if (task != null) DialogFragmentLink.newInstanceCreateForTask(task!!).show(childFragmentManager, "DialogFragmentNote")
                        else if (course != null) DialogFragmentLink.newInstanceCreateForCourse(course!!).show(childFragmentManager, "DialogFragmentNote")
                        else if (programTable != null) DialogFragmentLink.newInstanceCreateForProgramTable(programTable!!).show(childFragmentManager, "DialogFragmentNote")
                        else DialogFragmentLink.newInstanceCreate().show(childFragmentManager, "DialogFragmentNote")
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun setupFragmentResultListeners() {
        Log.d(tag, "setupFragmentResultListeners: Initializing listeners.")

        childFragmentManager.setFragmentResultListener(DialogFragmentFileFilter.REQUEST_KEY_BASE_MODEL, viewLifecycleOwner){ _, result ->
            val baseModel = BundleCompat.getParcelable(result, DialogFragmentFileFilter.RESULT_KEY_BASE_MODEL,BaseModel::class.java)
            Log.d(tag, "Result: Filter selection - Type: ${baseModel?.javaClass?.simpleName}")
            when(baseModel){
                is ProgramTable -> {
                    programTable = baseModel
                    binding.cpProgramTable.isChecked = true
                    binding.cpProgramTable.isCloseIconVisible = true
                    binding.cpProgramTable.text = programTable!!.title

                    binding.cpCourse.isChecked = false
                    binding.cpCourse.isCloseIconVisible = false
                    binding.cpCourse.text = getString(R.string.course)

                    binding.cpTask.isChecked = false
                    binding.cpTask.isCloseIconVisible = false
                    binding.cpTask.text = getString(R.string.task)

                    viewModelFile.updateProgramTableFilter(programTable)
                }
                is Course -> {
                    course = baseModel
                    binding.cpCourse.isChecked = true
                    binding.cpCourse.isCloseIconVisible = true
                    binding.cpCourse.text = course!!.title

                    binding.cpTask.isChecked = false
                    binding.cpTask.isCloseIconVisible = false
                    binding.cpTask.text = getString(R.string.task)

                    viewModelFile.updateCourseFilter(course)
                }
                is Task -> {
                    task = baseModel
                    binding.cpTask.isChecked = true
                    binding.cpTask.isCloseIconVisible = true
                    binding.cpTask.text = task!!.title

                    viewModelFile.updateTaskFilter(task)
                }
            }
        }
    }

    private fun setupObservers() {
        Log.d(tag, "setupObservers: Launching lifecycle-aware collectors.")

        //file state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelFile.filesState.collect { resource ->
                    Log.d(tag, "Observer: filesState resource type: ${resource::class.java.simpleName}")
                    when (resource) {
                        is Resource.Success-> {
                            Log.i(tag, "Observer: Loaded ${resource.data?.size ?: 0} files.")
                            recyclerAdapter.submitList(resource.data)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun openFile(file: File){
        Log.d(tag, "openFile: Attempting to open path: ${file.filePath}")
        try {
            val fileToOpen = JavaFile(file.filePath)
            if (!fileToOpen.exists()){
                Log.e(tag, "openFile: File does not exist on disk: ${file.filePath}")
                Toast.makeText(context, "Error: File not found.", Toast.LENGTH_SHORT).show()
                return
            }

            val fileUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                fileToOpen
            )
            Log.d(tag, "openFile: Generated FileProvider URI: $fileUri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, file.fileType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Log.i(tag, "openFile: Launching Intent for type: ${file.fileType}")
            startActivity(intent)
        }catch (e: Exception) {
            Log.e(tag, "openFile: Exception occurred: ${e.message}", e)
            Toast.makeText(requireContext(), "No application available to open this file type.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView: Cleaning up view binding.")
        _binding = null
    }
}
