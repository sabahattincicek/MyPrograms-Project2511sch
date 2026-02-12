package com.saboon.project_2511sch.presentation.programtable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.databinding.FragmentProgramTableListBinding
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramTableListFragment : Fragment() {

    private var _binding: FragmentProgramTableListBinding?= null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "ProgramTableFragment"
    }

    private lateinit var recyclerAdapterProgramTables: RecyclerAdapterProgramTables

    private val viewModelProgramTable: ViewModelProgramTable by viewModels()

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

//        Log.d(TAG, "onViewCreated: Setting up toolbar.")
//        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)

        setupAdapters()
        setupObservers()

        Log.d(TAG, "onViewCreated: Fetching all program tables.")
        viewModelProgramTable.getAllProgramTables()


        binding.fabAdd.setOnClickListener {
            val dialog = DialogFragmentProgramTable.newInstanceForCreate()
            dialog.show(childFragmentManager, "CreateProgramTableDialog")
        }
    }

    private fun setupObservers(){
        //PROGRAM TABLE STATES
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTablesState.collect { resource ->
                    Log.v(TAG, "observeProgramTablesState: New state received: ${resource::class.java.simpleName}")
                    when(resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "observeProgramTablesState: Error state, message: ${resource.message}")
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> Log.v(TAG, "observeProgramTablesState: Idle state.")
                        is Resource.Loading<*> -> Log.i(TAG, "observeProgramTablesState: Loading state.")
                        is Resource.Success<*> -> {
                            Log.i(TAG, "observeProgramTablesState: Success state. Submitting list to adapter. Item count: ${resource.data?.size}")
                            recyclerAdapterProgramTables.submitList(resource.data)
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