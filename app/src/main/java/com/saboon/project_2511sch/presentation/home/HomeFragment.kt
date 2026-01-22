package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentHomeBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
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

    private lateinit var recyclerAdapterHome: RecyclerAdapterHome

    private val tag = "HomeFragment"
    private var activeProgramTablesList = mutableListOf<ProgramTable>()

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
        observeActiveProgramTableState()
        observeHomeDisplayItemsState()

        Log.i(tag, "onViewCreated: Triggering initial data load by getting active program table.")
        viewModelProgramTable.getActiveProgramTableList()

        binding.cpProgramTable.setOnClickListener {
            Log.d(tag, "cpProgramTable clicked.")
            binding.cpProgramTable.isChecked = !binding.cpProgramTable.isChecked
            val dialog = DialogFragmentProgramTableSelector()
            dialog.show(childFragmentManager, "ProgramTableSelectorDialog")
        }
        binding.cpCourse.setOnClickListener {
            Log.d(tag, "cpCourse clicked.")
        }
        binding.cpLesson.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpLesson checked state changed: $isChecked")
        }
        binding.cpExam.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpExam checked state changed: $isChecked")
        }
        binding.cpHomework.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpHomework checked state changed: $isChecked")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView: View is being destroyed, nullifying binding to prevent memory leaks.")
        _binding = null
    }

    private fun observeActiveProgramTableState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(tag, "Subscribing to programTablesState flow.")
                viewModelProgramTable.programTablesState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "programTablesState: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "programTablesState: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "programTablesState: Loading.")
                        }
                        is Resource.Success<*> -> {
                            Log.i(tag, "programTablesState: Success received.")
                            result.data?.let { data ->
                                activeProgramTablesList.clear()
                                activeProgramTablesList.addAll(data)
                                Log.d(tag, "activeProgramTablesList updated. Size: ${activeProgramTablesList.size}")

                                Log.i(tag, "Requesting display items for active program tables.")
                                viewModelHome.getDisplayItems(activeProgramTablesList)

                                viewModelProgramTable.getAllProgramTablesCount { resource ->
                                    when(resource) {
                                        is Resource.Error<*> -> {}
                                        is Resource.Idle<*> -> {}
                                        is Resource.Loading<*> -> {}
                                        is Resource.Success<*> -> {
                                            val count = resource.data ?: 0
                                            if (activeProgramTablesList.size == count){ //filter is not applied got all program tables
                                                binding.cpProgramTable.isChecked = false
                                                binding.cpProgramTable.text = getString(R.string.program_table)
                                            }else{ //filter applied got just require program tables
                                                binding.cpProgramTable.isChecked = true
                                                if (activeProgramTablesList.size == 1){
                                                    binding.cpProgramTable.text = activeProgramTablesList.first().title
                                                    Log.d(tag, "Setting single table title: ${activeProgramTablesList.first().title}")
                                                }else if (activeProgramTablesList.size > 1){
                                                    binding.cpProgramTable.text = "${activeProgramTablesList.first().title} ${getString(R.string.and_more)}"
                                                    Log.d(tag, "Setting multi-table title: ${activeProgramTablesList.first().title} and more")
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
        }
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
                            val itemCount = result.data?.size ?: 0
                            Log.i(tag, "displayItemsState: Success - Submitting $itemCount items to adapter.")
                            recyclerAdapterHome.submitList(result.data)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(tag, "setupRecyclerAdapter: Initializing RecyclerAdapterHome.")
        recyclerAdapterHome = RecyclerAdapterHome()
        binding.programRecyclerView.apply {
            adapter = recyclerAdapterHome
            layoutManager = LinearLayoutManager(context)
        }
        recyclerAdapterHome.onItemClickListener = { course ->
            Log.d(tag, "Recycler item clicked. Course: ${course.title}")
            val tableToPass = activeProgramTablesList.first()
            Log.i(tag, "Navigating to CourseDetailsFragment with table: ${tableToPass.title} and course: ${course.title}")
            val action = HomeFragmentDirections.actionHomeFragmentToCourseDetailsFragment(tableToPass, course)
            findNavController().navigate(action)
        }
    }
}
