package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.databinding.FragmentHomeBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.programtable.ViewModelProgramTable
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModelHome: ViewModelHome by viewModels()
    private val viewModelProgramTable: ViewModelProgramTable by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()

    private lateinit var recyclerAdapterHome: RecyclerAdapterHome

    private val tag = "HomeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate: Fragment is being created.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView: Layout is being inflated.")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated: View has been created. Setting up UI and observers.")

        setupRecyclerAdapter()
        observeHomeDisplayItemsState()

        viewModelHome.loadCurrentWeek()

        binding.cpProgramTable.setOnClickListener {
            Log.d(tag, "cpProgramTable clicked.")
            binding.cpProgramTable.isChecked = !binding.cpProgramTable.isChecked
            val dialog = DialogFragmentProgramTableSelector()
            dialog.show(childFragmentManager, "ProgramTableSelectorDialog")
        }
        binding.cpCourse.setOnClickListener {
            Log.d(tag, "cpCourse clicked.")
            binding.cpCourse.isChecked = !binding.cpCourse.isChecked
            val dialog = DialogFragmentCourseSelector()
            dialog.show(childFragmentManager, "CourseSelectorDialog")
        }
        binding.cpLesson.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpLesson checked state changed: $isChecked")
            if (!binding.cpLesson.isChecked && !binding.cpExam.isChecked && !binding.cpHomework.isChecked){
                viewModelHome.updateFilter(FilterTask())
            }else{
                val newFilter = viewModelHome.filterState.value.copy(
                    lesson = isChecked,
                    exam = binding.cpExam.isChecked,
                    homework = binding.cpHomework.isChecked
                )
                viewModelHome.updateFilter(newFilter)
            }
        }
        binding.cpExam.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpExam checked state changed: $isChecked")
            if (!binding.cpLesson.isChecked && !binding.cpExam.isChecked && !binding.cpHomework.isChecked){
                viewModelHome.updateFilter(FilterTask())
            }else{
                val newFilter = viewModelHome.filterState.value.copy(
                    lesson = binding.cpLesson.isChecked,
                    exam = isChecked,
                    homework = binding.cpHomework.isChecked
                )
                viewModelHome.updateFilter(newFilter)
            }
        }
        binding.cpHomework.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpHomework checked state changed: $isChecked")
            if (!binding.cpLesson.isChecked && !binding.cpExam.isChecked && !binding.cpHomework.isChecked){
                viewModelHome.updateFilter(FilterTask())
            }else{
                val newFilter = viewModelHome.filterState.value.copy(
                    lesson = binding.cpLesson.isChecked,
                    exam = binding.cpExam.isChecked,
                    homework = isChecked
                )
                viewModelHome.updateFilter(newFilter)
            }
        }
        binding.osaOverScroll.onActionTriggered = {isTop ->
            if (isTop) {
                Log.d(tag, "overscroll triggered: Top")
                viewModelHome.loadPrevious()
            } else {
                Log.d(tag, "overscroll triggered: Bottom")
                viewModelHome.loadNext()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView: View is being destroyed, nullifying binding to prevent memory leaks.")
        _binding = null
    }
    private fun observeHomeDisplayItemsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(tag, "Subscribing to displayItemsState flow.")
                viewModelHome.displayItemsState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "displayItemsState: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "displayItemsState: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "displayItemsState: Loading.")
                        }
                        is Resource.Success<*> -> {
                            val homeDisplayItemList = result.data
                            val itemCount = homeDisplayItemList?.size ?: 0
                            Log.i(tag, "displayItemsState: Success - Submitting $itemCount items to adapter.")
                            recyclerAdapterHome.submitList(homeDisplayItemList)
                            viewModelProgramTable.getAllProgramTablesCount { resource ->
                                if (resource is Resource.Success){
                                    val allCount = resource.data ?: 0
                                    viewModelProgramTable.getAllActiveProgramTablesCount { resource ->
                                        if (resource is Resource.Success){
                                            val allActivesCount = resource.data ?: 0
                                            binding.cpProgramTable.isChecked = allCount != allActivesCount //filter is not applied got all program tables
                                        }
                                    }
                                }
                            }
                            viewModelCourse.getAllCount { resource ->
                                if (resource is Resource.Success){
                                    val allCount = resource.data ?: 0
                                    viewModelCourse.getAllActivesCount { resource ->
                                        if (resource is Resource.Success){
                                            val allActivesCount = resource.data ?: 0
                                            binding.cpCourse.isChecked = allCount != allActivesCount //filter is not applied got all courses
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(tag, "setupRecyclerAdapter: Initializing RecyclerAdapterHome.")
        recyclerAdapterHome = RecyclerAdapterHome()
        binding.rvHome.apply {
            adapter = recyclerAdapterHome
            layoutManager = LinearLayoutManager(context)
        }
        recyclerAdapterHome.onItemClickListener = { course ->
            Log.d(tag, "Recycler item clicked. Course: ${course.title}")
            val action = HomeFragmentDirections.actionHomeFragmentToCourseDetailsFragment(course)
            findNavController().navigate(action)
        }
    }
}
