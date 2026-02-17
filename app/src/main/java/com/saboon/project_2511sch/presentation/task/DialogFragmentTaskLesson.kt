package com.saboon.project_2511sch.presentation.task

import android.icu.util.Calendar
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTaskLessonBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.course.DialogFragmentCourse
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Picker
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.open
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class DialogFragmentTaskLesson: DialogFragment() {
    private var _binding: DialogFragmentTaskLessonBinding?= null
    private val binding get() = _binding!!
    private val viewModelTask: ViewModelTask by viewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()

    private lateinit var dateTimePicker: Picker

    private lateinit var currentUser: User
    private lateinit var programTable: ProgramTable
    private lateinit var course: Course
    private var task: Task? = null
    private var lesson: Task.Lesson? = null

    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedRecurrenceRule: RecurrenceRule = RecurrenceRule()
    private var selectedTimeStartMillis: Long = System.currentTimeMillis()
    private var selectedTimeEndMillis: Long = System.currentTimeMillis()
    private var selectedRemindBeforeMinutes: Int = 0

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
        _binding = DialogFragmentTaskLessonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            currentUser = BundleCompat.getParcelable(it,ARG_PROGRAM_USER, User::class.java)!!
            programTable = BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java)!!
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            task = BundleCompat.getParcelable(it, ARG_TASK, Task.Lesson::class.java)
            if (task != null) lesson = task as Task.Lesson
        }

        dateTimePicker = Picker(requireContext(), childFragmentManager)
        setupAdapters()
        setupListeners()
        setupObservers()

        val isEditMode = task != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit_task)
            binding.toolbar.subtitle = course.title
            binding.etTitle.setText(lesson!!.title)
            binding.etDescription.setText(lesson!!.description)
            binding.etDate.setText(lesson!!.date.toFormattedString("dd MMMM yyyy EEEE"))
            binding.actvRepeat.setText(mapRuleToDisplayString(lesson!!.recurrenceRule, resources.getStringArray(R.array.recurrence_options)), false)
            binding.etDateRangeStart.setText(lesson!!.recurrenceRule.dtStart.toFormattedString("dd.MM.yyyy"))
            binding.etDateRangeEnd.setText(lesson!!.recurrenceRule.until.toFormattedString("dd.MM.yyyy"))
            binding.etTimeStart.setText(lesson!!.timeStart.toFormattedString("HH:mm"))
            binding.etTimeEnd.setText(lesson!!.timeEnd.toFormattedString("HH:mm"))
            binding.actvReminder.setText(mapMinutesToDisplayString(lesson!!.remindBefore, resources.getStringArray(R.array.reminder_options)), false)
            binding.etPlace.setText(lesson!!.place)

            //apply files section
            binding.llFilesSection.visibility = View.VISIBLE

            selectedDateMillis = lesson!!.date
            selectedRecurrenceRule = lesson!!.recurrenceRule
            selectedTimeStartMillis = lesson!!.timeStart
            selectedTimeEndMillis = lesson!!.timeEnd
            selectedRemindBeforeMinutes = lesson!!.remindBefore

            viewModelSFile.updateProgramTable(programTable)
            viewModelSFile.updateCourse(course, false)
            viewModelSFile.updateTask(task)
//            binding.llFilesSection.visibility = View.VISIBLE  ------- suanlik database seviyesinde cascade ile otomatik silme islemi yapilamadigi icin tasklara file ekleme islemi engellendi
        }else{
//            viewModelSFile.updateProgramTable(programTable)
//            viewModelSFile.updateCourse(course, false)
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
                val updatedLesson = lesson!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    date = selectedDateMillis,
                    recurrenceRule = selectedRecurrenceRule,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString()
                )
                viewModelTask.update(updatedLesson)
            }else{
                val newLesson = Task.Lesson(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = course.programTableId,
                    courseId = course.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    date = selectedDateMillis,
                    recurrenceRule = selectedRecurrenceRule,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString(),
                )
                viewModelTask.insert(newLesson)
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.actvRepeat.setOnItemClickListener { parentFragment, view, position, id ->
            selectedRecurrenceRule.freq = when(position){
                0 -> RecurrenceRule.Frequency.ONCE
                1 -> RecurrenceRule.Frequency.DAILY
                2 -> RecurrenceRule.Frequency.WEEKLY
                3 -> RecurrenceRule.Frequency.MONTHLY
                4 -> RecurrenceRule.Frequency.YEARLY
                else -> selectedRecurrenceRule.freq
            }
        }
        binding.actvReminder.setOnItemClickListener { parentFragment, view, position, id ->
            selectedRemindBeforeMinutes = when(position){
                1 -> 0
                2 -> 10
                3 -> 30
                4 -> 60
                5 -> 1440
                else -> -1
            }
        }
        binding.etDate.setOnClickListener {
            dateTimePicker.pickDateMillis("Date", selectedDateMillis){ result ->
                selectedDateMillis = result
                binding.etDate.setText(selectedDateMillis.toFormattedString("dd.MM.yyyy"))
                selectedRecurrenceRule.dtStart = selectedDateMillis
                binding.etDateRangeStart.setText(selectedRecurrenceRule.dtStart.toFormattedString("dd.MM.yyyy"))
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedRecurrenceRule.dtStart
                    add(Calendar.MONTH, 9)
                }
                selectedRecurrenceRule.until = cal.timeInMillis // add 9 month
                binding.etDateRangeEnd.setText(selectedRecurrenceRule.until.toFormattedString("dd.MM.yyyy"))
            }
        }
        binding.etDateRangeStart.setOnClickListener {
            dateTimePicker.pickDateMillis("Start Date", selectedRecurrenceRule.dtStart){result ->
                selectedRecurrenceRule.dtStart = result
                binding.etDateRangeStart.setText(selectedRecurrenceRule.dtStart.toFormattedString("dd.MM.yyyy"))
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedRecurrenceRule.dtStart
                    add(Calendar.MONTH, 9)
                }
                selectedRecurrenceRule.until = cal.timeInMillis // add 9 month
                binding.etDateRangeEnd.setText(selectedRecurrenceRule.until.toFormattedString("dd.MM.yyyy"))
            }
        }
        binding.etDateRangeEnd.setOnClickListener {
            dateTimePicker.pickDateMillis("End Date", selectedRecurrenceRule.until){result ->
                selectedRecurrenceRule.until = result
                binding.etDateRangeEnd.setText(selectedRecurrenceRule.until.toFormattedString("dd.MM.yyyy"))
            }
        }
        binding.etTimeStart.setOnClickListener {
            dateTimePicker.pickTimeMillis("Start Time", selectedTimeStartMillis){ result ->
                selectedTimeStartMillis = result
                binding.etTimeStart.setText(selectedTimeStartMillis.toFormattedString("HH:mm"))
                val cal = Calendar.getInstance().apply {
                    timeInMillis = selectedTimeStartMillis
                    add(Calendar.HOUR_OF_DAY, 1)
                }
                selectedTimeEndMillis = cal.timeInMillis //add 1 hour
                binding.etTimeEnd.setText(selectedTimeEndMillis.toFormattedString("HH:mm"))
            }
        }
        binding.etTimeEnd.setOnClickListener {
            dateTimePicker.pickTimeMillis("End Time", selectedTimeEndMillis){ result ->
                selectedTimeEndMillis = result
                binding.etTimeEnd.setText(selectedTimeEndMillis.toFormattedString("HH:mm"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setupAdapters(){
        binding.actvRepeat.setAdapter(
            ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.recurrence_options))
        )
        binding.actvReminder.setAdapter(
            ArrayAdapter(requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.reminder_options))
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
    private fun mapRuleToDisplayString(rule: RecurrenceRule, options: Array<String>): String{
        return when(rule.freq){
            RecurrenceRule.Frequency.ONCE -> options[0]
            RecurrenceRule.Frequency.DAILY -> options[1]
            RecurrenceRule.Frequency.WEEKLY -> options[2]
            RecurrenceRule.Frequency.MONTHLY -> options[3]
            RecurrenceRule.Frequency.YEARLY -> options[4]
            else -> options[0] // "Does not repeat/ONCE"
        }
    }
    private fun mapMinutesToDisplayString(minutes: Int, options: Array<String>): String{
        return when(minutes){
            0 -> options[1]
            10 -> options[2]
            30 -> options[3]
            60 -> options[4]
            1440 -> options[5]
            else -> options[0]
        }
    }

    companion object{
        const val ARG_PROGRAM_USER = "dialog_task_lesson_arg_user"
        const val ARG_PROGRAM_TABLE = "dialog_task_lesson_arg_program_table"
        const val ARG_COURSE = "dialog_task_lesson_arg_course"
        const val ARG_TASK = "dialog_task_lesson_arg_task"

        fun newInstanceForCreate(user: User, programTable: ProgramTable, course: Course): DialogFragmentTaskLesson{
            return DialogFragmentTaskLesson().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_USER to user,
                    ARG_PROGRAM_TABLE to programTable,
                    ARG_COURSE to course
                )
            }
        }

        fun newInstanceForEdit(user: User, programTable: ProgramTable, course: Course, task: Task): DialogFragmentTaskLesson{
            return DialogFragmentTaskLesson().apply {
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