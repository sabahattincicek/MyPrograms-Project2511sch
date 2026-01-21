package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentProgramTableSelectorBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.presentation.programtable.ViewModelProgramTable
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentProgramTableSelector: DialogFragment() {
    private val tag = "DialogFragmentProgramTableSelector"

    private var _binding: DialogFragmentProgramTableSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerAdapterDialogFragmentProgramTableSelector: RecyclerAdapterDialogFragmentProgramTableSelector

    private val viewModelProgramTable: ViewModelProgramTable by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentProgramTableSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerAdapter()
        observeProgramTablesState()

        viewModelProgramTable.getAllProgramTables()

        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupRecyclerAdapter(){
        recyclerAdapterDialogFragmentProgramTableSelector = RecyclerAdapterDialogFragmentProgramTableSelector()
        recyclerAdapterDialogFragmentProgramTableSelector.onItemClickListener = { programTable ->

        }
        recyclerAdapterDialogFragmentProgramTableSelector.onItemCheckedChangeListener = { isChecked, programTable ->
            if (isChecked){
                viewModelProgramTable.setProgramTableActive(programTable)
            }else{
                viewModelProgramTable.setProgramTableInActive(programTable)
            }
        }
        binding.programRecyclerView.apply {
            adapter = recyclerAdapterDialogFragmentProgramTableSelector
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeProgramTablesState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTablesState.collect { resource ->
                    when (resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            resource.data?.let {
                                recyclerAdapterDialogFragmentProgramTableSelector.submitList(resource.data)
                            }
                        }
                    }
                }
            }
        }
    }
}