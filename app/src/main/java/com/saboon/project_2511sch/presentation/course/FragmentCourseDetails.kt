package com.saboon.project_2511sch.presentation.course

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentCourseDetailsBinding
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.task.RecyclerAdapterTask
import com.saboon.project_2511sch.presentation.task.ViewModelTask
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.tag.ViewModelTag
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskExam
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskHomework
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskLesson
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.open
import kotlin.getValue

@AndroidEntryPoint
class FragmentCourseDetails : Fragment() {

    private var _binding: FragmentCourseDetailsBinding? =  null
    private val binding get() = _binding!!
    private val args : FragmentCourseDetailsArgs by navArgs()
    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelTag: ViewModelTag by viewModels()
    private val viewModelCourse : ViewModelCourse by viewModels()
    private val viewModelTask: ViewModelTask by viewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()
    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini
    private lateinit var recyclerAdapterTask: RecyclerAdapterTask
    private lateinit var currentUser: User
    private lateinit var course: Course
    private var uri: Uri? = null
    private var tag: Tag? = null

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            this.uri = uri
            val sFile = SFile(
                id = "generate in repository",
                createdBy = currentUser.id,
                appVersionAtCreation = getString(R.string.app_version),
                title = "generate in repository",
                description = "",
                courseId = course.id,
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

        viewModelCourse.getById(args.course.id)


        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_delete -> {
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete", "Are you sure?")
                    dialog.show(childFragmentManager, "Delete Course")
                    true
                }
                R.id.action_edit -> {
                    val dialog = DialogFragmentCourse.newInstanceForUpdate(currentUser, course)
                    dialog.show(childFragmentManager, "Edit Course")
                    true
                }
                else -> {
                    false
                }
            }
        }
        binding.llCourseInfo.setOnClickListener {
            val dialog = DialogFragmentCourse.newInstanceForUpdate(currentUser, course)
            dialog.show(childFragmentManager, "Edit Course")
        }
        binding.fabAdd.setOnClickListener { view ->
            showAddTaskMenu(view)
        }
        binding.ivFiles.setOnClickListener {
            val action = FragmentCourseDetailsDirections.actionCourseDetailsFragmentToFileFragment(tag, course)
            findNavController().navigate(action)
        }
    }
    @SuppressLint("UseCompatTextViewDrawableApis") //for tvTag drawable start icon tint
    private fun applyDataToView(){
        binding.tvTitleCourse.text = course.title
        binding.tvPersonPrimary.text = course.people.split(",").firstOrNull()?.trim()
        binding.tvPersonSecondary.text = course.people.split(",").drop(1).joinToString(", ") { it.trim() }
        binding.tvDescription.text = course.description

        val containerColor = course.color.getInt()
        val textColor = course.color.getOnMainTextColor()

        binding.llCourseInfo.setBackgroundColor(containerColor)
        if (course.isActive) binding.mcvIsNotEmpty.visibility = View.GONE
        else binding.mcvIsNotEmpty.visibility = View.VISIBLE

        binding.tvTitleCourse.setTextColor(textColor)
        binding.tvPersonPrimary.setTextColor(textColor)
        binding.tvPersonSecondary.setTextColor(textColor)
        binding.tvDescription.setTextColor(textColor)
        binding.tvTag.setTextColor(textColor)
        binding.tvTag.compoundDrawableTintList = ColorStateList.valueOf(textColor)
    }
    private fun setupAdapters(){
        recyclerAdapterTask = RecyclerAdapterTask()
        recyclerAdapterTask.onContentItemClickListener = { task ->
            when(task) {
                is Task.Lesson -> {
                    val dialog = DialogFragmentTaskLesson.newInstanceForEdit(currentUser, course, task)
                    dialog.show(childFragmentManager, "UpdateTaskDialog")
                }
                is Task.Exam -> {
                    val dialog = DialogFragmentTaskExam.newInstanceForEdit(currentUser, course, task)
                    dialog.show(childFragmentManager, "UpdateTaskDialog")
                }
                is Task.Homework -> {
                    val dialog = DialogFragmentTaskHomework.newInstanceForEdit(currentUser, course, task)
                    dialog.show(childFragmentManager, "UpdateTaskDialog")
                }
            }
        }
        recyclerAdapterTask.onAbsenceButtonClickListener = { taskLesson ->
            viewModelTask.update(course, taskLesson)
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
                        val dialog = DialogFragmentTaskLesson.newInstanceForCreate(currentUser, course)
                        dialog.show(childFragmentManager, "dialogFragmentTaskLesson")
                        true
                    }
                    R.id.action_add_exam -> {
                        val dialog = DialogFragmentTaskExam.newInstanceForCreate(currentUser, course)
                        dialog.show(childFragmentManager, "dialogFragmentTaskExam")
                        true
                    }
                    R.id.action_add_homework -> {
                        val dialog = DialogFragmentTaskHomework.newInstanceForCreate(currentUser, course)
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
                            viewModelTask.loadTasksBy(course)
                            viewModelSFile.loadFilesBy(course)
                            if (course.tagId != null) viewModelTag.getById(course.tagId!!) else binding.tvTag.visibility = View.INVISIBLE
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
                            val taskDisplayItemList = resource.data
                            if (taskDisplayItemList.isNullOrEmpty()){
                                binding.llEmptyList.visibility = View.VISIBLE
                                binding.rvTasks.visibility = View.GONE
                            }else{
                                binding.llEmptyList.visibility = View.GONE
                                binding.rvTasks.visibility = View.VISIBLE

                                recyclerAdapterTask.submitList(resource.data)
                            }
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
        //TAG STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTag.tagState.collect { resource ->
                    when(resource){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            tag = resource.data
                            tag?.let { tag ->
                                binding.tvTag.visibility = View.VISIBLE
                                binding.tvTag.text = tag.title
                            }
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
        // TASK LESSON EVENT: UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {}
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