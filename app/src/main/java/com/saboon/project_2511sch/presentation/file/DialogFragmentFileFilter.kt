package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentFileFilterBinding
import com.saboon.project_2511sch.domain.model.BaseModel
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.programtable.ViewModelProgramTable
import com.saboon.project_2511sch.presentation.task.ViewModelTask
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentFileFilter: DialogFragment() {

    private var _binding: DialogFragmentFileFilterBinding?=null
    private val binding get() = _binding!!
    private lateinit var recyclerAdapter: RecyclerAdapterFileFilter
    private val viewModelProgramTable: ViewModelProgramTable by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()
    private val viewModelTask: ViewModelTask by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentFileFilterBinding.inflate(inflater,container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerAdapter()
        setupObservers()

        arguments.let { bundle ->
            val programTableId = bundle!!.getString(ARG_PROGRAM_TABLE_ID)
            val courseId = bundle.getString(ARG_COURSE_ID)

            if (courseId != null){
                viewModelTask.getAllByCourseId(courseId)
            }else if (programTableId != null){
                viewModelCourse.getAllCoursesByProgramTableId(programTableId)
            }else{
                viewModelProgramTable.getAllProgramTables()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupRecyclerAdapter(){
        recyclerAdapter = RecyclerAdapterFileFilter()
        recyclerAdapter.onClickItemListener = { baseModel ->
            setFragmentResult(REQUEST_KEY_BASE_MODEL, bundleOf(RESULT_KEY_BASE_MODEL to baseModel))
            dismiss()
        }
        binding.rvFileFilter.apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    companion object{
        const val ARG_PROGRAM_TABLE_ID = "dialog_fragment_file_filter_arg_program_table_id"
        const val ARG_COURSE_ID = "dialog_fragment_file_filter_arg_course_id"
        const val REQUEST_KEY_BASE_MODEL = "dialog_fragment_file_filter_request_key_base_model"
        const val RESULT_KEY_BASE_MODEL = "dialog_fragment_file_filter_result_key_base_model"

        fun newInstanceFilterProgramTable(): DialogFragmentFileFilter{
            return DialogFragmentFileFilter().apply {
                arguments = bundleOf()
            }
        }
        fun newInstanceFilterCourse(programTableId: String): DialogFragmentFileFilter{
            return DialogFragmentFileFilter().apply {
                arguments = bundleOf(ARG_PROGRAM_TABLE_ID to programTableId)
            }
        }
        fun newInstanceFilterTask(courseId: String): DialogFragmentFileFilter{
            return DialogFragmentFileFilter().apply {
                arguments = bundleOf(ARG_COURSE_ID to courseId)
            }
        }
    }

    private fun setupObservers(){
        //TASK STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.taskState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            recyclerAdapter.submitList(resource.data)
                        }
                    }
                }
            }
        }
        //COURSE STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.coursesState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            recyclerAdapter.submitList(resource.data)
                        }
                    }
                }
            }
        }
        //PROGRAM TABLE STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTableListState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            recyclerAdapter.submitList(resource.data)
                        }
                    }
                }
            }
        }
    }
}