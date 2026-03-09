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
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.common.DialogFragmentFilter
import com.saboon.project_2511sch.presentation.user.ViewModelUser
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
    private var programTable: Tag? = null
    private var course: Course? = null
    private var task: Task? = null
    private var uri: Uri? = null

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            this.uri = uri
            course?.let { course ->
                val sFile = SFile(
                    id = "generate in repository",
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    title = "generate in repository",
                    description = "",
                    courseId = course.id,
                    filePath = "generate in repository"
                )
                viewModelSFile.insert(sFile, uri)
            }
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

        course = args.course
        task = args.task
        Log.d(tag, "onViewCreated: Initial args received - PT: ${programTable?.title}, Course: ${course?.title}, Task: ${task?.title}")

        setupRecyclerAdapter()
        setupFragmentResultListeners()
        setupObservers()

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
                            val courseTitleMatches = (sFile.courseId ?: "").contains(query, ignoreCase = true)
                            titleMatches || courseTitleMatches
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

//        childFragmentManager.setFragmentResultListener(DialogFragmentFilter.REQUEST_KEY_BASE_MODEL, viewLifecycleOwner){ requestKey, result ->
//            val baseModel = BundleCompat.getParcelable(result, DialogFragmentFilter.RESULT_KEY_BASE_MODEL,BaseModel::class.java)
//            Log.d(tag, "FragmentResultListener: Filter result received - Type: ${baseModel?.javaClass?.simpleName}")
//            when(baseModel){
//                is Tag -> {
//                    programTable = baseModel
//                    binding.cpProgramTable.isChecked = true
//                    binding.cpProgramTable.isCloseIconVisible = true
//                    binding.cpProgramTable.text = programTable!!.title
//
//                    binding.cpCourse.isChecked = false
//                    binding.cpCourse.isCloseIconVisible = false
//                    binding.cpCourse.text = getString(R.string.course)
//
//                    binding.cpTask.isChecked = false
//                    binding.cpTask.isCloseIconVisible = false
//                    binding.cpTask.text = getString(R.string.task)
//
//                    Log.d(tag, "FragmentResultListener: Updating filter with selected Tag.")
//                    viewModelSFile.updateProgramTable(programTable)
//                }
//                is Course -> {
//                    course = baseModel
//                    binding.cpCourse.isChecked = true
//                    binding.cpCourse.isCloseIconVisible = true
//                    binding.cpCourse.text = course!!.title
//
//                    binding.cpTask.isChecked = false
//                    binding.cpTask.isCloseIconVisible = false
//                    binding.cpTask.text = getString(R.string.task)
//
//                    Log.d(tag, "FragmentResultListener: Updating filter with selected Course.")
//                    viewModelSFile.updateCourse(course)
//
//                }
//                is Task -> {
//                    task = baseModel
//                    binding.cpTask.isChecked = true
//                    binding.cpTask.isCloseIconVisible = true
//                    binding.cpTask.text = task!!.title
//
//                    Log.d(tag, "FragmentResultListener: Updating filter with selected Task.")
//                    viewModelSFile.updateTask(task)
//                }
//            }
//        }
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
                            if (sFileDisplayItemList != null){
                                if (sFileDisplayItemList.size <= 1){ // 1 for footer
                                    binding.llEmptyList.visibility = View.VISIBLE
                                    binding.rvFile.visibility = View.GONE
                                }else{
                                    binding.llEmptyList.visibility = View.GONE
                                    binding.rvFile.visibility = View.VISIBLE

                                    recyclerAdapterSFile.submitList(sFileDisplayItemList)
                                }
                            }
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
