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
        Log.d(tag, "onCreate: Fragment created.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView: Inflating layout.")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated: View created, setting up UI and observers.")

        setupRecyclerAdapter()
        observeActiveProgramTableState()
        observeDisplayItemsState()
        observeAllProgramTablesState()

        Log.d(tag, "onViewCreated: Requesting active program table from ViewModel.")
        viewModelHome.getActiveProgramTable()


        binding.imDropdownProgramTableList.setOnClickListener {
            viewModelHome.getAllProgramTables()
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentProgramTableSelector.REQUEST_KEY_SELECT_ACTIVE, this){ requestKey, result ->
            val selectedProgramTable = BundleCompat.getParcelable(result,
                DialogFragmentProgramTableSelector.RESULT_KEY_PROGRAM_TABLE, ProgramTable::class.java)
            if (selectedProgramTable != null){
                // TODO: make selected programTable active and reload ui
            }
            else{
                // TODO: get default or recent active programTable from database
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy: Fragment is being destroyed.")
        _binding = null
    }

    private fun observeActiveProgramTableState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(tag, "observeActiveProgramTableState: Collecting active program table state.")
                viewModelHome.activeProgramTable.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "observeActiveProgramTableState: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "observeActiveProgramTableState: State is Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "observeActiveProgramTableState: State is Loading.")
                        }
                        is Resource.Success<*> -> {
                            if (result.data != null) {
                                Log.i(tag, "observeActiveProgramTableState: Success - Active program table found: '${result.data.title}'. Fetching display items.")
                                programTable = result.data
                                binding.tvProgramTable.text = programTable.title
                                viewModelHome.getDisplayItems(programTable)
                            } else {
                                Log.w(tag, "observeActiveProgramTableState: Success, but data is null.")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeAllProgramTablesState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelHome.programTableState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            if (resource.data != null){
                                val dialog = DialogFragmentProgramTableSelector.newInstance(resource.data)
                                dialog.show(childFragmentManager, "ProgramTableSelectorDialog")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeDisplayItemsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(tag, "observeDisplayItemsState: Collecting display items state.")
                viewModelHome.displayItemsState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "observeDisplayItemsState: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "observeDisplayItemsState: State is Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "observeDisplayItemsState: State is Loading.")
                        }
                        is Resource.Success<*> -> {
                            val itemCount = result.data?.size ?: 0
                            Log.i(tag, "observeDisplayItemsState: Success - Submitting $itemCount items to adapter.")
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
    }
}