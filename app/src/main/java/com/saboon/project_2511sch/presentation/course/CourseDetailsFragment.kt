package com.saboon.project_2511sch.presentation.course

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskLesson

@AndroidEntryPoint
class CourseDetailsFragment : Fragment() {

    private var _binding: FragmentCourseDetailsBinding? =  null
    private val binding get() = _binding!!

    private val args : CourseDetailsFragmentArgs by navArgs()

    private val viewModelCourse : ViewModelCourse by viewModels()
    private val viewModelTask: ViewModelTask by viewModels()

    private lateinit var programTable: ProgramTable
    private lateinit var course: Course

    private lateinit var taskRecyclerAdapter: RecyclerAdapterTask


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

        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
        
        programTable = args.programTable
        course = args.course
        
        setupRecyclerAdapter()
        setupTaskLessonFragmentResultListeners()
        applyDataToView()
        observeInsertNewScheduleEvent()
        observeUpdateScheduleEvent()
        observeDeleteScheduleEvent()
        observeDeleteCourseEvent()
        observeUpdateCourseEvent()
        observeTaskDisplayItemsState()


        viewModelTask.getTaskDisplayItems(course)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFiles.setOnClickListener {
            val action = CourseDetailsFragmentDirections.actionCourseDetailsFragmentToFileFragment(course)
            findNavController().navigate(action)
        }

        binding.btnAddSchedule.setOnClickListener { anchorView ->
            showAddTaskMenu(anchorView)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {
                menuInflater.inflate(R.menu.menu_action_edit_delete, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.action_edit -> {
                        val dialog = DialogFragmentCourse.newInstance(programTable, course)
                        dialog.show(childFragmentManager, "EditCourseDialog")
                        true
                    }
                    R.id.action_delete -> {
                        val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete Course", "Are you sure?")
                        dialog.show(childFragmentManager, "DeleteCourseDialog")
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, this) { requestKey, result ->
            val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
            if (isYes) {
                 viewModelCourse.deleteCourse(course)
            }
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentCourse.REQUEST_KEY_UPDATE, this){requestKey, result ->
            val updatedCourse = BundleCompat.getParcelable(result, DialogFragmentCourse.RESULT_KEY_COURSE,Course::class.java)
            if (updatedCourse != null){
                viewModelCourse.updateCourse(updatedCourse)
            }
        }



        binding.btnAbsenceDecrease.setOnClickListener {
            viewModelCourse.decrementAbsence(course)
        }
        binding.btnAbsenceIncrease.setOnClickListener {
            viewModelCourse.incrementAbsence(course)
        }
    }

    private fun setupRecyclerAdapter(){
        taskRecyclerAdapter = RecyclerAdapterTask()
        taskRecyclerAdapter.onItemClickListener = { task ->
            when(task) {
                is Task.Lesson -> {
                    val dialog = DialogFragmentTaskLesson.newInstanceForEdit(course, task)
                    dialog.show(childFragmentManager, "UpdateScheduleDialog")
                }
                is Task.Exam -> {}
                is Task.Homework -> {}
            }
        }
        binding.rvSchedules.apply{
            adapter = taskRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupTaskLessonFragmentResultListeners(){
        childFragmentManager.setFragmentResultListener(DialogFragmentTaskLesson.REQUEST_KEY_CREATE, this){ requestKey, result ->
            val newTask = BundleCompat.getParcelable(result, DialogFragmentTaskLesson.RESULT_KEY_TASK, Task.Lesson::class.java)
            if (newTask != null){
                viewModelTask.insertNewTask(newTask)
            }
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentTaskLesson.REQUEST_KEY_UPDATE, this){ requestKey, result ->
            val updatedTask = BundleCompat.getParcelable(result, DialogFragmentTaskLesson.RESULT_KEY_TASK,Task.Lesson::class.java)
            if(updatedTask != null){
                viewModelTask.updateTask(updatedTask)
            }
        }

        childFragmentManager.setFragmentResultListener(DialogFragmentTaskLesson.REQUEST_KEY_DELETE, this){ requestKey, result ->
            val deletedTask = BundleCompat.getParcelable(result, DialogFragmentTaskLesson.RESULT_KEY_TASK, Task.Lesson::class.java)
            if (deletedTask != null){
                viewModelTask.deleteTask(deletedTask)
            }
        }
    }

    private fun applyDataToView(){
        binding.topAppBar.subtitle = course.title
        binding.tvTitleCourse.text = course.title
        binding.tvPersonPrimary.text = course.people?.split(",")?.firstOrNull()?.trim()?:""
        binding.tvPersonSecondary.text = course.people?.split(",")?.drop(1)?.joinToString(", ") { it.trim() } ?: ""
        binding.tvDescription.text = course.description
        binding.tvAbsenceCount.text = course.absence.toString()

        val colorName = course.color

        val customContainerColorAttr = ModelColors.getThemeAttrForCustomContainerColor(colorName)
        val themeAwareCustomContainerColor = MaterialColors.getColor(requireContext(), customContainerColorAttr, Color.BLACK)

        val onCustomContainerColorAttr = ModelColors.getThemeAttrForOnCustomContainerColor(colorName)
        val themeAwareOnCustomContainerColor = MaterialColors.getColor(requireContext(), onCustomContainerColorAttr, Color.BLACK)

        binding.llCourseInfo.setBackgroundColor(themeAwareCustomContainerColor)
        binding.llFilesContainer.setBackgroundColor(themeAwareCustomContainerColor)

        binding.tvTitleCourse.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvPersonPrimary.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvPersonSecondary.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvDescription.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvAbsenceLabel.setTextColor(themeAwareOnCustomContainerColor)
        binding.tvAbsenceCount.setTextColor(themeAwareOnCustomContainerColor)
        binding.btnAbsenceDecrease.setColorFilter(themeAwareOnCustomContainerColor)
        binding.btnAbsenceIncrease.setColorFilter(themeAwareOnCustomContainerColor)

        binding.btnFiles.setBackgroundColor(themeAwareOnCustomContainerColor)
        binding.btnFiles.setTextColor(themeAwareCustomContainerColor)
        binding.tvFile.setTextColor(themeAwareCustomContainerColor)
    }

    private fun showAddTaskMenu(anchorView: View){
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_add_task, menu)
            setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.action_add_lesson -> {
                        val dialog = DialogFragmentTaskLesson.newInstanceForCreate(course)
                        dialog.show(childFragmentManager, "dialogFragmentTaskLesson")
                        true
                    }
                    R.id.action_add_exam -> {

                        true
                    }
                    R.id.action_add_homework -> {

                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun observeUpdateCourseEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.updateCourseEvent.collect { event ->
                    when(event) {
                        is Resource.Error<*> -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            course = event.data!!
                            applyDataToView()
                        }
                    }
                }
            }
        }
    }
    private fun observeDeleteCourseEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.deleteCourseEvent.collect { event ->
                    when(event) {
                        is Resource.Error<*> -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            Toast.makeText(context, getString(R.string.course_deleted_successfully), Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }
    private fun observeInsertNewScheduleEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.insertNewScheduleEvent.collect { event ->
                    when(event) {
                        is Resource.Error<*> -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            viewModelTask.setupAlarmForSchedule(programTable, course, event.data!!)
                            Toast.makeText(context, getString(R.string.schedule_added_successfully), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    private fun observeUpdateScheduleEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.updateScheduleEvent.collect { event ->
                    when(event) {
                        is Resource.Error<*> -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            viewModelTask.setupAlarmForSchedule(programTable, course, event.data!!)
                            Toast.makeText(context, getString(R.string.schedule_updated_successfully), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeDeleteScheduleEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.deleteScheduleEvent.collect { event ->
                    when(event) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeTaskDisplayItemsState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.taskDisplayItemsState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            taskRecyclerAdapter.submitList(resource.data)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }


}