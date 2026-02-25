package com.saboon.project_2511sch.presentation.programtable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.databinding.FragmentProgramTableListBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ProgramTableListFragment : Fragment() {

    private var _binding: FragmentProgramTableListBinding?= null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelProgramTable: ViewModelProgramTable by viewModels()

    private lateinit var currentUser: User

    companion object {
        private const val TAG = "ProgramTableFragment"
    }

    private lateinit var recyclerAdapterProgramTables: RecyclerAdapterProgramTables


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment created.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflating layout")
        _binding = FragmentProgramTableListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created.")

        setupAdapters()
        setupObservers()

        binding.fabAdd.setOnClickListener {
            val dialog = DialogFragmentProgramTable.newInstanceForCreate(currentUser)
            dialog.show(childFragmentManager, "CreateProgramTableDialog")
        }
    }

    private fun setupObservers(){
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
        //PROGRAM TABLE STATES
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTablesState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val displayItemProgramTableList = resource.data
                            if (displayItemProgramTableList.isNullOrEmpty()){
                                binding.llEmptyList.visibility = View.VISIBLE
                                binding.rvProgramTable.visibility = View.GONE
                            }else{
                                binding.llEmptyList.visibility = View.GONE
                                binding.rvProgramTable.visibility = View.VISIBLE

                                recyclerAdapterProgramTables.submitList(resource.data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupAdapters(){
        Log.d(TAG, "setupRecyclerView: Setting up RecyclerView and its adapter.")
        recyclerAdapterProgramTables = RecyclerAdapterProgramTables()
        recyclerAdapterProgramTables.onItemClickListener = { programTable ->
            val action = ProgramTableListFragmentDirections.actionProgramTableFragmentToProgramTableDetailsFragment(programTable)
            findNavController().navigate(action)
        }
        binding.rvProgramTable.apply {
            adapter = recyclerAdapterProgramTables
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: Fragment destroyed, binding set to null")
    }

}