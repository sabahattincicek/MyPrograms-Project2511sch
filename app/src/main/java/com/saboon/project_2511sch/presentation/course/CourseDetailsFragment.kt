package com.saboon.project_2511sch.presentation.course

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.color.MaterialColors
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentCourseDetailsBinding
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.task.RecyclerAdapterTask
import com.saboon.project_2511sch.presentation.task.ViewModelTask
import com.saboon.project_2511sch.util.ModelColors
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.programtable.DialogFragmentProgramTable
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskExam
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskHomework
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskLesson
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.open
import kotlin.getValue

@AndroidEntryPoint
class CourseDetailsFragment : Fragment() {

    private var _binding: FragmentCourseDetailsBinding? =  null
    private val binding get() = _binding!!
    private val args : CourseDetailsFragmentArgs by navArgs()
    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelCourse : ViewModelCourse by viewModels()
    private val viewModelTask: ViewModelTask by viewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()
    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini
    private lateinit var recyclerAdapterTask: RecyclerAdapterTask
    private lateinit var currentUser: User
    private lateinit var programTable: ProgramTable
    private lateinit var course: Course
    private var uri: Uri? = null

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            this.uri = uri
            val sFile = SFile(
                id = "generate in repository",
                createdBy = currentUser.id,
                appVersionAtCreation = getString(R.string.app_version),
                title = "generate in repository",
                description = "",
                programTableId = programTable.id,
                courseId = course.id,
                taskId = null,
                filePath = "generate in repository"
            )
            viewModelSFile.insert(sFile, uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCourseDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupListeners()
        setupObservers()

        programTable = args.programTable
        viewModelCourse.getById(args.course.id) //course initialized in setupObservers() function

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_toggle_switch -> {
                    menuItem.isChecked = !menuItem.isChecked
                    viewModelCourse.activationById(course.id, menuItem.isChecked)
                    true
                }
                R.id.action_delete -> {
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete", "Are you sure?")
                    dialog.show(childFragmentManager, "Delete Course")
                    true
                }
                R.id.action_edit -> {
                    val dialog = DialogFragmentCourse.newInstanceForUpdate(currentUser, programTable, course)
                    dialog.show(childFragmentManager, "Edit Course")
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.fabAdd.setOnClickListener { view ->
            showAddTaskMenu(view)
        }
        binding.ivFiles.setOnClickListener {
            val action = CourseDetailsFragmentDirections.actionCourseDetailsFragmentToFileFragment(programTable, course)
            findNavController().navigate(action)
        }
        binding.btnAbsenceDecrease.setOnClickListener {
            if (course.absence > 0){
                val decrementedCourse = course.copy(
                    absence = course.absence - 1
                )
                viewModelCourse.update(decrementedCourse)
            }
        }
        binding.btnAbsenceIncrease.setOnClickListener {
            val incrementedCourse = course.copy(
                absence = course.absence + 1
            )
            viewModelCourse.update(incrementedCourse)
        }
    }
    private fun applyDataToView(){
        binding.topAppBar.subtitle = course.title
        binding.tvTitleCourse.text = course.title
        binding.tvPersonPrimary.text = course.people.split(",").firstOrNull()?.trim()
        binding.tvPersonSecondary.text = course.people.split(",").drop(1).joinToString(", ") { it.trim() }
        binding.tvDescription.text = course.description
        binding.tvAbsenceCount.text = course.absence.toString()

        val colorName = course.color

        val customContainerColorAttr = ModelColors.getThemeAttrForCustomContainerColor(colorName)
        val themeAwareCustomContainerColor = MaterialColors.getColor(requireContext(), customContainerColorAttr, Color.BLACK)

        val onCustomContainerColorAttr = ModelColors.getThemeAttrForOnCustomContainerColor(colorName)
        val themeAwareOnCustomContainerColor = MaterialColors.getColor(requireContext(), onCustomContainerColorAttr, Color.BLACK)

        binding.llCourseInfo.setBackgroundColor(themeAwareCustomContainerColor)

        binding.tvTitleCourse.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvPersonPrimary.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvPersonSecondary.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvDescription.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvAbsenceLabel.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvAbsenceCount.setTextColor(themeAwareOnCustomContainerColor)
        binding.btnAbsenceDecrease.setColorFilter(themeAwareOnCustomContainerColor)
        binding.btnAbsenceIncrease.setColorFilter(themeAwareOnCustomContainerColor)
    }
    private fun setupAdapters(){
        recyclerAdapterTask = RecyclerAdapterTask()
        recyclerAdapterTask.onItemClickListener = { task ->
            when(task) {
                is Task.Lesson -> {
                    val dialog = DialogFragmentTaskLesson.newInstanceForEdit(currentUser, programTable, course, task)
                    dialog.show(childFragmentManager, "UpdateTaskDialog")
                }
                is Task.Exam -> {
                    val dialog = DialogFragmentTaskExam.newInstanceForEdit(currentUser, programTable, course, task)
                    dialog.show(childFragmentManager, "UpdateTaskDialog")
                }
                is Task.Homework -> {
                    val dialog = DialogFragmentTaskHomework.newInstanceForEdit(currentUser, programTable, course, task)
                    dialog.show(childFragmentManager, "UpdateTaskDialog")
                }
            }
        }
        binding.rvTasks.apply{
            adapter = recyclerAdapterTask
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
    private fun showAddTaskMenu(anchorView: View){
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_add_task, menu)
            setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.action_add_lesson -> {
                        val dialog = DialogFragmentTaskLesson.newInstanceForCreate(currentUser, programTable, course)
                        dialog.show(childFragmentManager, "dialogFragmentTaskLesson")
                        true
                    }
                    R.id.action_add_exam -> {
                        val dialog = DialogFragmentTaskExam.newInstanceForCreate(currentUser, programTable, course)
                        dialog.show(childFragmentManager, "dialogFragmentTaskExam")
                        true
                    }
                    R.id.action_add_homework -> {
                        val dialog = DialogFragmentTaskHomework.newInstanceForCreate(currentUser, programTable, course)
                        dialog.show(childFragmentManager, "dialogFragmentTaskHomework")
                        true
                    }
                    else -> false
                }
            }
            show()
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
        //COURSE STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.courseState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            course = resource.data!!
                            applyDataToView()
                            val toggleItem = binding.topAppBar.menu.findItem(R.id.action_toggle_switch)
                            toggleItem?.isChecked = course.isActive
                            viewModelTask.updateFilter(programTable, course)
                            viewModelSFile.updateProgramTable(programTable)
                            viewModelSFile.updateCourse(course, false)
                        }
                    }
                }
            }
        }
        //TASK STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.tasksState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            recyclerAdapterTask.submitList(resource.data)
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
        //COURSE EVENT: DELETE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.operationEvent.collect { event ->
                    when(event) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
        //FILE EVENT: INSERT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSFile.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {}
                    }
                }
            }
        }

    }

    private fun setupListeners(){
        childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, this) { requestKey, result ->
            val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
            if (isYes) {
                viewModelCourse.delete(course)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }


}