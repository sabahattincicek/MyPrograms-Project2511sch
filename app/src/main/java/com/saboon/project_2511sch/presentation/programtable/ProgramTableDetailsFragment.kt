package com.saboon.project_2511sch.presentation.programtable

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.MaterialColors
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentProgramTableDetailsBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.course.DialogFragmentCourse
import com.saboon.project_2511sch.presentation.course.DisplayItemCourse
import com.saboon.project_2511sch.presentation.course.RecyclerAdapterCourse
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.util.ModelColors
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.open
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ProgramTableDetailsFragment : Fragment() {

    private var _binding: FragmentProgramTableDetailsBinding?=null
    private val binding get()=_binding!!
    private val viewModelProgramTable: ViewModelProgramTable by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()
    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini
    private lateinit var recyclerAdapterCourse: RecyclerAdapterCourse
    private val args: ProgramTableDetailsFragmentArgs by navArgs()
    private lateinit var programTable: ProgramTable
    private var uri: Uri? = null

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            this.uri = uri
            val sFile = SFile(
                id = "generate in repository",
                appVersionAtCreation = getString(R.string.app_version),
                title = "generate in repository",
                description = "",
                programTableId = programTable.id,
                courseId = null,
                taskId = null,
                filePath = "generate in repository"
            )
            viewModelSFile.insert(sFile, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgramTableDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelProgramTable.getById(args.programTable.id)

        setupAdapters()
        setupListeners()
        setupObservers()

        binding.topAppBar.setNavigationOnClickListener{
            findNavController().popBackStack()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_toggle_switch -> {
                    menuItem.isChecked = !menuItem.isChecked
                    viewModelProgramTable.activationById(programTable.id, menuItem.isChecked)
                    true
                }
                R.id.action_delete -> {
                    val diaolog = DialogFragmentDeleteConfirmation.newInstance("Delete", "Are you sure?")
                    diaolog.show(childFragmentManager, "Delete Program Table")
                    true
                }
                R.id.action_edit -> {
                    val dialog = DialogFragmentProgramTable.newInstanceForUpdate(programTable)
                    dialog.show(childFragmentManager, "Edit Program Table")
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.fabAdd.setOnClickListener {
            val dialogCourse = DialogFragmentCourse.newInstanceForCreate(programTable)
            dialogCourse.show(childFragmentManager, "Create Course")
        }
        binding.ivFiles.setOnClickListener {
            val action = ProgramTableDetailsFragmentDirections.actionProgramTableDetailsFragmentToFileFragment(programTable)
            findNavController().navigate(action)
        }
    }

    private fun applyDataToView(){
        binding.tvTitle.text = programTable.title
        binding.tvDescription.text = "${getString(R.string.description)}: ${programTable.description}"

        val color = programTable.color

        val customContainerColorAttr = ModelColors.getThemeAttrForCustomContainerColor(color)
        val themeAwareCustomContainerColor = MaterialColors.getColor(requireContext(), customContainerColorAttr, Color.BLACK)

        val onCustomContainerColorAttr = ModelColors.getThemeAttrForOnCustomContainerColor(color)
        val themeAwareOnCustomContainerColor = MaterialColors.getColor(requireContext(), onCustomContainerColorAttr, Color.BLACK)

        binding.llProgramTableContainer.setBackgroundColor(themeAwareCustomContainerColor)
        binding.tvTitle.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvDescription.setTextColor(themeAwareOnCustomContainerColor)
    }

    private fun setupAdapters(){
        recyclerAdapterCourse = RecyclerAdapterCourse()
        recyclerAdapterCourse.onItemClickListener = { course ->
            val action = ProgramTableDetailsFragmentDirections.actionProgramTableDetailsFragmentToCourseDetailsFragment(programTable, course)
            findNavController().navigate(action)
        }
        binding.rvCourses.apply {
            adapter = recyclerAdapterCourse
            layoutManager = LinearLayoutManager(context)
        }
        recyclerAdapterSFileMini = RecyclerAdapterSFileMini()
        recyclerAdapterSFileMini.onItemClickListener = { sFile ->
            sFile.open(requireContext())
        }
        recyclerAdapterSFileMini.onAddItemClickListener = {
            selectFileLauncher.launch(arrayOf("*/*"))
        }
        binding.rvMiniFilePreviews.apply {
            adapter = recyclerAdapterSFileMini
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupListeners(){
        //DELETE
        childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, this) { requestKey, result ->
            val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
            if (isYes) {
                viewModelProgramTable.delete(programTable)
            }
        }
    }

    private fun setupObservers(){
        //PROGRAM TABLE STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.programTableState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            programTable = resource.data!!
                            applyDataToView()
                            val toggleItem = binding.topAppBar.menu.findItem(R.id.action_toggle_switch)
                            toggleItem?.isChecked = programTable.isActive
                            viewModelCourse.updateFilter(programTable)
                            viewModelSFile.updateProgramTable(programTable, false)
                        }
                    }
                }
            }
        }
        //COURSES STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.coursesState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val courseDisplayItemList = resource.data
                            val filteredList = courseDisplayItemList?.filter { it !is DisplayItemCourse.HeaderCourse } // not want header to show
                            recyclerAdapterCourse.submitList(filteredList)
                        }
                    }
                }
            }
        }
        //FILES STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSFile.filesState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val sFileDisplayItemList = resource.data
                            recyclerAdapterSFileMini.submitList(sFileDisplayItemList)
                        }
                    }
                }
            }
        }
        //DELETE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.deleteEvent.collect { event ->
                    when(event){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
        //FILE INSERT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSFile.insertEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {

                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}