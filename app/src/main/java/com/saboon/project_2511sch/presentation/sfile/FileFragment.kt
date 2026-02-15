package com.saboon.project_2511sch.presentation.sfile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.BundleCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentFileBinding
import com.saboon.project_2511sch.domain.model.BaseModel
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.common.DialogFragmentFilter
import com.saboon.project_2511sch.presentation.profile.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.open
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding?=null
    private val binding get() = _binding!!
    private val args : FileFragmentArgs by navArgs()
    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()
    private lateinit var recyclerAdapterSFile: RecyclerAdapterSFile
    private lateinit var currentUser: User
    private var programTable: ProgramTable? = null
    private var course: Course? = null
    private var task: Task? = null
    private var uri: Uri? = null

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            this.uri = uri
            val sFile = SFile(
                id = "generate in repository",
                createdBy = currentUser.id,
                appVersionAtCreation = getString(R.string.app_version),
                title = "generate in repository",
                description = "",
                programTableId = programTable?.id,
                courseId = course?.id,
                taskId = task?.id,
                filePath = "generate in repository"
            )
            viewModelSFile.insert(sFile, uri)
        }
    }


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
        Log.d(tag, "onViewCreated: Initial args received - PT: ${programTable?.title}, Course: ${course?.title}, Task: ${task?.title}")

        applyDataToView()
        setupRecyclerAdapter()
        setupFragmentResultListeners()
        setupObservers()

        Log.d(tag, "onViewCreated: Initializing filters in ViewModel.")
        viewModelSFile.updateProgramTable(programTable)
        viewModelSFile.updateCourse(course)
        viewModelSFile.updateTask(task)

        binding.etSearch.doAfterTextChanged { it ->
            val query = it.toString().trim()
            Log.d(tag, "Search query changed: $query")
            val sFileDisplayList = viewModelSFile.filesState.value.data
            if(sFileDisplayList != null){
                if (query.isNotEmpty()){
                    val filteredList = sFileDisplayList.filter { item ->
                        if (item is DisplayItemSFile.ContentSFile){
                            val sFile = item.sFile
                            val titleMatches = sFile.title.contains(query, ignoreCase = true)
                            val taskTitleMatches = (sFile.taskId ?: "").contains(query, ignoreCase = true)
                            val courseTitleMatches = (sFile.courseId ?: "").contains(query, ignoreCase = true)
                            val programTableTitleMatches = (sFile.programTableId ?: "").contains(query, ignoreCase = true)
                            titleMatches || taskTitleMatches || courseTitleMatches || programTableTitleMatches
                        }else{
                            false
                        }
                    }
                    recyclerAdapterSFile.submitList(filteredList)
                }else{
                    recyclerAdapterSFile.submitList(sFileDisplayList)
                }
            }
        }
         binding.fabAddNewFile.setOnClickListener { anchorView ->
             selectFileLauncher.launch(arrayOf("*/*"))
         }
        binding.cpProgramTable.setOnClickListener {
            Log.d(tag, "cpProgramTable: Chip clicked. Opening ProgramTable filter.")
            binding.cpProgramTable.isChecked = !binding.cpProgramTable.isChecked
            val dialog = DialogFragmentFilter.newInstanceFilterProgramTable()
            dialog.show(childFragmentManager, "DialogFragmentFileFilter")
        }
        binding.cpProgramTable.setOnCloseIconClickListener {
            Log.d(tag, "cpProgramTable: Close icon clicked. Clearing ProgramTable filter.")
            programTable = null
            viewModelSFile.updateProgramTable(programTable)

            binding.cpProgramTable.isChecked = false
            binding.cpProgramTable.isCloseIconVisible = false
            binding.cpProgramTable.text = getString(R.string.program_table)
        }
        binding.cpCourse.setOnClickListener {
            Log.d(tag, "cpCourse: Chip clicked.")
            binding.cpCourse.isChecked = !binding.cpCourse.isChecked
            if(programTable != null){
                Log.d(tag, "cpCourse: Opening Course filter for PT: ${programTable?.id}")
                val dialog = DialogFragmentFilter.newInstanceFilterCourse(programTable!!)
                dialog.show(childFragmentManager, "DialogFragmentFileFilter")
            }else{
                Log.w(tag, "cpCourse: Cannot open Course filter because programTable is null.")
            }
        }
        binding.cpCourse.setOnCloseIconClickListener {
            Log.d(tag, "cpCourse: Close icon clicked. Clearing Course filter.")
            course = null
            viewModelSFile.updateCourse(course)

            binding.cpCourse.isChecked = false
            binding.cpCourse.isCloseIconVisible = false
            binding.cpCourse.text = getString(R.string.course)
        }
        binding.cpTask.setOnClickListener {
            binding.cpTask.isChecked = !binding.cpTask.isChecked
            if (course != null){
                val dialog = DialogFragmentFilter.newInstanceFilterTask(programTable!!, course!!)
                dialog.show(childFragmentManager, "DialogFragmentFileFilter")
            }else{
                Log.w(tag, "cpTask: Cannot open Task filter because course is null.")
            }
        }
        binding.cpTask.setOnCloseIconClickListener {
            Log.d(tag, "cpTask: Close icon clicked. Clearing Task filter.")
            task = null
            viewModelSFile.updateTask(task)

            binding.cpTask.isChecked = false
            binding.cpTask.isCloseIconVisible = false
            binding.cpTask.text = getString(R.string.task)
        }
    }

    private fun applyDataToView(){
        Log.d(tag, "applyDataToView: Applying initial state to UI chips.")
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
        Log.d(tag, "setupRecyclerAdapter: Initializing RecyclerAdapterSFile.")
        recyclerAdapterSFile = RecyclerAdapterSFile()
        recyclerAdapterSFile.onItemClickListener = { clickedFile ->
            Log.i(tag, "onItemClickListener: File clicked - ${clickedFile.title}")
            clickedFile.open(requireContext())
        }
        recyclerAdapterSFile.onMenuItemClickListener = { sFile, clickedItem ->
            Log.d(tag, "onMenuItemClickListener: Menu item $clickedItem clicked for ${sFile.title}")
            when (clickedItem) {
                R.id.action_delete -> {
                    Log.d(tag, "onMenuItemClickListener: Action Delete selected.")
                    val dialog = DialogFragmentDeleteConfirmation.newInstance(
                        "Delete File",
                        "Are you sure you want to delete '${sFile.title}'?"
                    )
                    dialog.show(childFragmentManager, "DeleteDialogFragment")
                    childFragmentManager.setFragmentResultListener(
                        DialogFragmentDeleteConfirmation.REQUEST_KEY,
                        viewLifecycleOwner
                    ) { requestKey, result ->
                        val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
                        Log.d(tag, "DeleteConfirmation result: $isYes")
                        if (isYes) {
                            Log.i(tag, "DeleteConfirmation: Confirmed. Calling ViewModel to delete.")
                            viewModelSFile.delete(sFile)
                        }
                    }
                }
            }
        }
        binding.rvFile.apply {
            adapter = recyclerAdapterSFile
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFragmentResultListeners() {
        Log.d(tag, "setupFragmentResultListeners: Initializing FragmentResultListeners.")

        childFragmentManager.setFragmentResultListener(DialogFragmentFilter.REQUEST_KEY_BASE_MODEL, viewLifecycleOwner){ requestKey, result ->
            val baseModel = BundleCompat.getParcelable(result, DialogFragmentFilter.RESULT_KEY_BASE_MODEL,BaseModel::class.java)
            Log.d(tag, "FragmentResultListener: Filter result received - Type: ${baseModel?.javaClass?.simpleName}")
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

                    Log.d(tag, "FragmentResultListener: Updating filter with selected ProgramTable.")
                    viewModelSFile.updateProgramTable(programTable)
                }
                is Course -> {
                    course = baseModel
                    binding.cpCourse.isChecked = true
                    binding.cpCourse.isCloseIconVisible = true
                    binding.cpCourse.text = course!!.title

                    binding.cpTask.isChecked = false
                    binding.cpTask.isCloseIconVisible = false
                    binding.cpTask.text = getString(R.string.task)

                    Log.d(tag, "FragmentResultListener: Updating filter with selected Course.")
                    viewModelSFile.updateCourse(course)

                }
                is Task -> {
                    task = baseModel
                    binding.cpTask.isChecked = true
                    binding.cpTask.isCloseIconVisible = true
                    binding.cpTask.text = task!!.title

                    Log.d(tag, "FragmentResultListener: Updating filter with selected Task.")
                    viewModelSFile.updateTask(task)
                }
            }
        }
    }

    private fun setupObservers() {
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
                        }
                    }
                }
            }
        }
        //FILES STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSFile.filesState.collect { resource ->
                    Log.d(tag, "fileState: New resource collected - ${resource::class.java.simpleName}")
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val sFileDisplayItemList = resource.data
                            recyclerAdapterSFile.submitList(sFileDisplayItemList)
                        }
                    }
                }
            }
        }
        //FILE EVENT: DELETE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSFile.operationEvent.collect { resource ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView: Cleaning up ViewBinding.")
        _binding = null
    }
}
