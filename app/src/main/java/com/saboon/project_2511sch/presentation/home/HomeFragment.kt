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
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.databinding.FragmentHomeBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModelHome: ViewModelHome by viewModels()

    private lateinit var recyclerAdapterHome: RecyclerAdapterHome

    private val tag = "HomeFragment"

    private lateinit var programTable: ProgramTable

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
        observeAllProgramTablesState()

        Log.i(tag, "onViewCreated: Triggering initial data load by getting active program table.")
        viewModelHome.getActiveProgramTable()

        binding.imDropdownProgramTableList.setOnClickListener {
            Log.d(tag, "Dropdown icon clicked. Requesting all program tables to show dialog.")
            viewModelHome.getAllProgramTables()
        }


        childFragmentManager.setFragmentResultListener(DialogFragmentProgramTableSelector.REQUEST_KEY_SELECT_ACTIVE, viewLifecycleOwner) { requestKey, result ->
            Log.d(tag, "Result received from ProgramTableSelectorDialog with key: $requestKey")
            val selectedProgramTable = BundleCompat.getParcelable(result,
                DialogFragmentProgramTableSelector.RESULT_KEY_PROGRAM_TABLE, ProgramTable::class.java)

            if (selectedProgramTable != null) {
                Log.i(tag, "Program table selected: '${selectedProgramTable.title}'. Setting it as active and refreshing.")
                viewModelHome.setProgramTableActive(selectedProgramTable)
            } else {
                Log.w(tag, "Received a null program table from the dialog.")
                // TODO: get default or recent active programTable from database
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
                viewModelHome.activeProgramTable.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "ActiveProgramTable State: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "ActiveProgramTable State: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "ActiveProgramTable State: Loading.")
                        }
                        is Resource.Success<*> -> {
                            if (result.data != null) {
                                Log.i(tag, "ActiveProgramTable State: Success - Found active table: '${result.data.title}'.")
                                programTable = result.data
                                binding.tvProgramTable.text = programTable.title
                                viewModelHome.getDisplayItems(programTable)
                            } else {
                                Log.w(tag, "ActiveProgramTable State: Success, but data is null.")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeAllProgramTablesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(tag, "Subscribing to allProgramTables state flow.")
                viewModelHome.programTableState.collect { resource ->
                    when (resource) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "AllProgramTables State: Error - ${resource.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "AllProgramTables State: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "AllProgramTables State: Loading.")
                        }
                        is Resource.Success<*> -> {
                            if (resource.data != null) {
                                Log.i(tag, "AllProgramTables State: Success - Received ${resource.data.size} tables. Showing dialog.")
                                val dialog = DialogFragmentProgramTableSelector.newInstance(resource.data)
                                dialog.show(childFragmentManager, "ProgramSelectorDialogFragment")
                            } else {
                                Log.w(tag, "AllProgramTables State: Success, but data is null.")
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
    }
}