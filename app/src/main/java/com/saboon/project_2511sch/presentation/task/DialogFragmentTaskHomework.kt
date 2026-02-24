package com.saboon.project_2511sch.presentation.task

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTaskHomeworkBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskExam.Companion.ARG_PROGRAM_USER
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Picker
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.open
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentTaskHomework: DialogFragment() {

    private var _binding: DialogFragmentTaskHomeworkBinding?=null
    private val binding get() = _binding!!
    private val viewModelTask: ViewModelTask by viewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()

    private lateinit var dateTimePicker: Picker

    private lateinit var currentUser: User
    private lateinit var programTable: ProgramTable
    private lateinit var course: Course
    private var task: Task? = null
    private var homework: Task.Homework? = null

    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini

    private var selectedDueDateMillis: Long = System.currentTimeMillis()
    private var selectedDueTimeMillis: Long = System.currentTimeMillis()
    private var selectedRemindBeforeMinutes: Int = -1 // no reminder

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
                taskId = task!!.id,
                filePath = "generate in repository"
            )
            viewModelSFile.insert(sFile, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentTaskHomeworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            currentUser = BundleCompat.getParcelable(it, ARG_PROGRAM_USER, User::class.java)!!
            programTable = BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java)!!
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            task = BundleCompat.getParcelable(it, ARG_TASK, Task.Homework::class.java)
            if (task != null) homework = task as Task.Homework
        }

        dateTimePicker = Picker(requireContext(), childFragmentManager)
        setupAdapters()
        setupListeners()
        setupObservers()

        val isEditMode = task != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit_task)
            binding.toolbar.subtitle = course.title
            binding.etTitle.setText(homework!!.title)
            binding.etDescription.setText(homework!!.description)
            binding.etDueDate.setText(homework!!.dueDate.toFormattedString("dd MMMM yyyy EEEE"))
            binding.etDueTime.setText(homework!!.dueTime.toFormattedString("HH:mm"))
            binding.actvReminder.setText(mapReminderToDisplayString(homework!!.remindBefore), false)

            selectedDueDateMillis = homework!!.dueDate
            selectedDueTimeMillis = homework!!.dueTime
            selectedRemindBeforeMinutes = homework!!.remindBefore

            viewModelSFile.updateProgramTable(programTable)
            viewModelSFile.updateCourse(course, false)
            viewModelSFile.updateTask(task)
//            binding.llFilesSection.visibility = View.VISIBLE  ------- suanlik database seviyesinde cascade ile otomatik silme islemi yapilamadigi icin tasklara file ekleme islemi engellendi
            binding.llFilesSection.visibility = View.GONE
        }else{
            binding.actvReminder.setText(mapReminderToDisplayString(-1), false)
            binding.llFilesSection.visibility = View.GONE
        }

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.action_delete -> {
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("Delete", "Are you sure?")
                    dialog.show(childFragmentManager, "DeleteConfirmationFragment")
                    true
                }
                else -> false
            }
        }
        binding.btnSave.setOnClickListener {
            if (isEditMode){
                val updatedHomework = homework!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    dueDate = selectedDueDateMillis,
                    dueTime = selectedDueTimeMillis,
                    remindBefore = selectedRemindBeforeMinutes
                )
                viewModelTask.update(updatedHomework)
            }else{
                val newHomework = Task.Homework(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = course.programTableId,
                    courseId = course.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    dueDate = selectedDueDateMillis,
                    dueTime = selectedDueTimeMillis,
                    remindBefore = selectedRemindBeforeMinutes
                )
                viewModelTask.insert(newHomework)
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.etDueDate.setOnClickListener {
            dateTimePicker.pickDateMillis("Due Date", selectedDueDateMillis){ result ->
                selectedDueDateMillis = result
                binding.etDueDate.setText(selectedDueDateMillis.toFormattedString("dd MMMM yyyy EEEE"))
            }
        }
        binding.etDueTime.setOnClickListener {
            dateTimePicker.pickTimeMillis("Due Time", selectedDueTimeMillis){ result ->
                selectedDueTimeMillis = result
                binding.etDueTime.setText(selectedDueTimeMillis.toFormattedString("HH:mm"))
            }
        }
        binding.actvReminder.setOnItemClickListener{ parent, view, position, id ->
            selectedRemindBeforeMinutes = when(position){
                1 -> 0
                2 -> 10
                3 -> 30
                4 -> 60
                5 -> 1440
                else -> -1
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
        binding.actvReminder.setAdapter(
            ArrayAdapter(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.reminder_options)
            )
        )
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
        childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, viewLifecycleOwner){ requestKey, result ->
            val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
            if (isYes){
                viewModelTask.delete(task!!)
            }
        }
    }

    private fun setupObservers(){
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
        //TASK EVENT: INSERT, UPDATE, DELETE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            dismiss()
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
                        is Resource.Success -> {

                        }
                    }
                }
            }
        }
    }

    private fun mapReminderToDisplayString(minutes: Int): String{
        val options = resources.getStringArray(R.array.reminder_options)
        return when(minutes){
            0 -> options[1] // "On Time"
            10 -> options[2] // "10 minutes before"
            30 -> options[3] // "30 minutes before"
            60 -> options[4] // "1 hour before"
            1440 -> options[5] // "1 day before"
            else -> options[0] // "No reminder"
        }
    }

    companion object{
        const val ARG_PROGRAM_USER = "dialog_task_homework_arg_user"
        const val ARG_PROGRAM_TABLE = "dialog_task_homework_arg_program_table"
        const val ARG_COURSE = "dialog_task_homework_arg_course"
        const val ARG_TASK = "dialog_task_homework_arg_task"

        fun newInstanceForCreate(user: User, programTable: ProgramTable, course: Course): DialogFragmentTaskHomework{
            return DialogFragmentTaskHomework().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_USER to user,
                    ARG_PROGRAM_TABLE to programTable,
                    ARG_COURSE to course
                )
            }
        }

        fun newInstanceForEdit(user: User, programTable: ProgramTable, course: Course, task: Task): DialogFragmentTaskHomework{
            return DialogFragmentTaskHomework().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_USER to user,
                    ARG_PROGRAM_TABLE to programTable,
                    ARG_COURSE to course,
                    ARG_TASK to task
                )
            }
        }
    }

}