package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
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

    private lateinit var programTable: ProgramTable
    private lateinit var activeProgramTablesList: List<ProgramTable>

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
            if (activeProgramTablesList.isNotEmpty()) {
                binding.cpProgramTable.isChecked = true
            }
            val dialog = DialogFragmentProgramTableSelector.newInstance()
            dialog.show(childFragmentManager, "")
        }
        binding.cpCourse.setOnClickListener {
            if (activeProgramTablesList.isNotEmpty()) {
                binding.cpCourse.isChecked = true
            }
        }
        binding.cpLesson.setOnCheckedChangeListener { chip, isChecked ->

        }
        binding.cpExam.setOnCheckedChangeListener { chip, isChecked ->

        }
        binding.cpHomework.setOnCheckedChangeListener { chip, isChecked ->

        }

        childFragmentManager.setFragmentResultListener(DialogFragmentProgramTableSelector.REQUEST_KEY_SELECTED_PROGRAM_TABLE_BOOLEAN, viewLifecycleOwner) { requestKey, result ->
            Log.d(tag, "Result received from ProgramTableSelectorDialog with key: $requestKey")

            val isTrue = result.getBoolean(DialogFragmentProgramTableSelector.RESULT_KEY_PROGRAM_TABLE_BOOLEAN)
            if (isTrue){
                viewModelProgramTable.getActiveProgramTableList()
            }
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
                Log.d(tag, "Subscribing to activeProgramTable state flow.")
                viewModelProgramTable.programTablesState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                        }
                        is Resource.Idle<*> -> {
                        }
                        is Resource.Loading<*> -> {
                        }
                        is Resource.Success<*> -> {
                            result.data?.let {
                                activeProgramTablesList = it
                                if (activeProgramTablesList.isNotEmpty()){
                                    binding.cpProgramTable.isChecked = true
                                    if (activeProgramTablesList.size == 1){
                                        binding.cpProgramTable.text = activeProgramTablesList.first().title
                                    }else if (activeProgramTablesList.size > 1){
                                        binding.cpProgramTable.text = "${activeProgramTablesList.first().title} and more"
                                    }
                                    viewModelHome.getDisplayItems(activeProgramTablesList)
                                }else{
                                    binding.cpProgramTable.isChecked = false
                                    binding.cpProgramTable.text = getString(R.string.program_table)
                                    //get entire task for display
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
                Log.d(tag, "Subscribing to displayItems state flow.")
                viewModelHome.displayItemsState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "DisplayItems State: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "DisplayItems State: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "DisplayItems State: Loading.")
                        }
                        is Resource.Success<*> -> {
                            val itemCount = result.data?.size ?: 0
                            Log.i(tag, "DisplayItems State: Success - Submitting $itemCount items to adapter.")
                            recyclerAdapterHome.submitList(result.data)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(tag, "setupRecyclerAdapter: Initializing and setting up RecyclerAdapterHome.")
        recyclerAdapterHome = RecyclerAdapterHome()
        binding.programRecyclerView.apply {
            adapter = recyclerAdapterHome
            layoutManager = LinearLayoutManager(context)
        }
        recyclerAdapterHome.onItemClickListener = { course ->
            val action = HomeFragmentDirections.actionHomeFragmentToCourseDetailsFragment(programTable, course)
            findNavController().navigate(action)
        }
    }
}