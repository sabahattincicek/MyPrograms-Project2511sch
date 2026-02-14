package com.saboon.project_2511sch.presentation.common

import android.os.Bundle
import android.view.Display
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
import com.saboon.project_2511sch.databinding.DialogFragmentFilterBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.programtable.ViewModelProgramTable
import com.saboon.project_2511sch.presentation.task.ViewModelTask
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentFilter: DialogFragment() {

    private var _binding: DialogFragmentFilterBinding?=null
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
        _binding = DialogFragmentFilterBinding.inflate(inflater,container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerAdapter()

        arguments.let { bundle ->
            val programTable = bundle?.let { BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java) }
            val course = bundle?.let { BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java) }

            if (course != null){
                observeTaskState()
                viewModelTask.updateFilter(programTable, course)
            }else if (programTable != null){
                observeCourseState()
                viewModelCourse.updateFilter(programTable)
            }else{
                observeProgramTableState()
            }
        }
        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
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
        const val ARG_PROGRAM_TABLE = "dialog_fragment_file_filter_arg_program_table_id"
        const val ARG_COURSE = "dialog_fragment_file_filter_arg_course_id"
        const val REQUEST_KEY_BASE_MODEL = "dialog_fragment_file_filter_request_key_base_model"
        const val RESULT_KEY_BASE_MODEL = "dialog_fragment_file_filter_result_key_base_model"

        fun newInstanceFilterProgramTable(): DialogFragmentFilter{
            return DialogFragmentFilter().apply {
                arguments = bundleOf()
            }
        }
        fun newInstanceFilterCourse(programTable: ProgramTable): DialogFragmentFilter{
            return DialogFragmentFilter().apply {
                arguments = bundleOf(ARG_PROGRAM_TABLE to programTable)
            }
        }
        fun newInstanceFilterTask(programTable: ProgramTable, course: Course): DialogFragmentFilter{
            return DialogFragmentFilter().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_TABLE to programTable,
                    ARG_COURSE to course)
            }
        }
    }

    private fun observeTaskState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.tasksState.collect { resource ->
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
    private fun observeCourseState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.coursesState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            val courses = resource.data
                            recyclerAdapter.submitList(courses)
                        }
                    }
                }
            }
        }
    }
    private fun observeProgramTableState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTablesState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            val programTableDisplayItemList = resource.data
                            recyclerAdapter.submitList(programTableDisplayItemList)
                        }
                    }
                }
            }
        }
    }
}