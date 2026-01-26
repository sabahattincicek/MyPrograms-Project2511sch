package com.saboon.project_2511sch.presentation.programtable

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentProgramTableBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProgramTableFragment : Fragment() {

    private var _binding: FragmentProgramTableBinding?= null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "ProgramTableFragment"
    }

    private lateinit var programTablesRecyclerAdapter: ProgramTablesRecyclerAdapter

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
        _binding = FragmentProgramTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created.")

        Log.d(TAG, "onViewCreated: Setting up toolbar.")
        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)

        setupRecyclerView()
        observeProgramTablesState()
        observeInsertNewProgramTableEvent()
        observeDeleteProgramTableEvent()
        observeUpdateProgramTableEvent()

        Log.d(TAG, "onViewCreated: Fetching all program tables.")
        viewModelProgramTable.getAllProgramTables()



        val menuHost: MenuHost = requireActivity()
        Log.d(TAG, "onViewCreated: Adding menu provider to host.")
        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {
                menuInflater.inflate(R.menu.menu_action_add, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.action_add -> {
                        Log.i(TAG, "onMenuItemSelected: Add program menu item clicked")

                        val dialog = DialogFragmentProgramTable.newInstance(null)
                        dialog.show(childFragmentManager, "CreateProgramTableDialog")
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        childFragmentManager.setFragmentResultListener(DialogFragmentProgramTable.REQUEST_KEY_CREATE, this){ requestKey, result ->

            val newProgramTable = BundleCompat.getParcelable(result,DialogFragmentProgramTable.RESULT_KEY_PROGRAM_TABLE, ProgramTable::class.java)
            if (newProgramTable != null){
                viewModelProgramTable.insertNewProgramTable(newProgramTable)
            }
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentProgramTable.REQUEST_KEY_UPDATE, this){ requestKey, result ->
            val updatedProgramTable = BundleCompat.getParcelable(result,DialogFragmentProgramTable.RESULT_KEY_PROGRAM_TABLE, ProgramTable::class.java)
            if (updatedProgramTable != null){
                viewModelProgramTable.updateProgramTable(updatedProgramTable)
            }
        }
    }
    private fun observeProgramTablesState(){
        Log.d(TAG, "observeProgramTablesState: Starting to observe programTablesState.")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTableListState.collect { resource ->
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
                            programTablesRecyclerAdapter.submitList(resource.data)
                        }
                    }
                }
            }
        }
    }
    private fun observeInsertNewProgramTableEvent(){
        Log.d(TAG, "observeAddNewProgramTableEvent: Starting to observe addNewProgramTableEvent")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.insertNewProgramTableEvent.collect { event ->
                    Log.v(TAG, "observeAddNewProgramTableEvent: New event collected: ${event::class.java.simpleName}")
                    when(event){
                        is Resource.Error<*> -> { Log.e(TAG, "observeAddNewProgramTableEvent: Error event received, message: ${event.message}")
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Idle<*> -> Log.v(TAG, "observeAddNewProgramTableEvent: Idle event received.")
                        is Resource.Loading<*> -> Log.i(TAG, "observeAddNewProgramTableEvent: Loading event received.")
                        is Resource.Success -> {
                            Log.i(TAG, "observeAddNewProgramTableEvent: Success event received. Program table added.")
                            Toast.makeText(requireContext(), getString(R.string.program_table_added_successfully), Toast.LENGTH_LONG).show()
                            val action = ProgramTableFragmentDirections.actionProgramTableFragmentToCourseFragment(event.data!!)
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }

    private fun observeDeleteProgramTableEvent(){
        Log.d(TAG, "observeDeleteProgramTableEvent: Starting to observe deleteProgramTableEvent.")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.deleteProgramTableEvent.collect { event ->
                    Log.v(TAG, "observeDeleteProgramTableEvent: New event received: ${event::class.java.simpleName}")
                    when(event) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "observeDeleteProgramTableEvent: Error event, message: ${event.message}")
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> Log.v(TAG, "observeDeleteProgramTableEvent: Idle event.")
                        is Resource.Loading<*> -> Log.i(TAG, "observeDeleteProgramTableEvent: Loading event.")
                        is Resource.Success<*> -> {
                            Log.i(TAG, "observeDeleteProgramTableEvent: Success event. Program table deleted.")
                            Toast.makeText(requireContext(), getString(R.string.program_table_deleted_successfully), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeUpdateProgramTableEvent(){
        Log.d(TAG, "observeUpdateProgramTableEvent: Starting to observe updateProgramTableEvent.")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.updateProgramTableEvent.collect { event ->
                    Log.v(TAG, "observeUpdateProgramTableEvent: New event received: ${event::class.java.simpleName}")
                    when(event) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "observeUpdateProgramTableEvent: Error event, message: ${event.message}")
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> Log.v(TAG, "observeUpdateProgramTableEvent: Idle event.")
                        is Resource.Loading<*> -> Log.i(TAG, "observeUpdateProgramTableEvent: Loading event.")
                        is Resource.Success<*> -> {
                            Log.i(TAG, "observeUpdateProgramTableEvent: Success event. Program table updated.")
                            Toast.makeText(requireContext(), getString(R.string.program_table_updated_successfully),Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(){
        Log.d(TAG, "setupRecyclerView: Setting up RecyclerView and its adapter.")
        programTablesRecyclerAdapter = ProgramTablesRecyclerAdapter()
        programTablesRecyclerAdapter.onItemClickListener = { programTable ->
            val action = ProgramTableFragmentDirections.actionProgramTableFragmentToCourseFragment(programTable)
            findNavController().navigate(action)
        }
        programTablesRecyclerAdapter.onMenuItemClickListener = { programTable, menuItemId ->
            when(menuItemId){
                R.id.action_edit -> {
                    Log.i(TAG, "onMenuItemClickListener: Edit clicked for program table ID: ${programTable.id}")
                    val dialog = DialogFragmentProgramTable.newInstance(programTable)
                    dialog.show(childFragmentManager, "UpdateProgramTableDialog")
                }
                R.id.action_delete -> {
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete Program", "Are you sure?")
                    Log.i(TAG, "onMenuItemClickListener: Delete clicked for program table ID: ${programTable.id}. Showing confirmation dialog.")
                    dialog.show(childFragmentManager, "DeleteInformationDialog")
                    childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, this){ requestKey, result ->
                        val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
                        if (isYes) {
                            Log.i(TAG, "Confirmation received to delete program table ID: ${programTable.id}. Calling ViewModel.")
                            viewModelProgramTable.deleteProgramTable(programTable)
                        }
                    }
                }
            }
        }
        binding.programRecyclerView.apply {
            adapter = programTablesRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: Fragment destroyed, binding set to null")
    }

}