package com.saboon.project_2511sch.presentation.task

import android.Manifest
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.data.worker.WidgetUpdateWorker
import com.saboon.project_2511sch.databinding.DialogFragmentTaskLessonBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.PermissionManager
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
    private lateinit var course: Course
    private var task: Task? = null
    private var lesson: Task.Lesson? = null

    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedRecurrenceRule: RecurrenceRule = RecurrenceRule(
        freq = RecurrenceRule.Frequency.WEEKLY,
        dtStart = selectedDateMillis,
        until = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            add(Calendar.MONTH, 9)
        }.timeInMillis)
    private var selectedTimeStartMillis: Long = System.currentTimeMillis()
    private var selectedTimeEndMillis: Long = System.currentTimeMillis()
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
                courseId = course.id,
                filePath = "generate in repository"
            )
            viewModelSFile.insert(sFile, uri)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){

        }else{
            PermissionManager.NotificationPermission.showPermissionRationale(this){ // onNegativeClick
                selectedRemindBeforeMinutes = -1
                binding.actvReminder.setText(mapReminderToDisplayString(-1), false)
            }
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

        // Sistemin çubuklarının (StatusBar ve NavBar) yüksekliğini al ve layout'a padding olarak ekle
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // v (yani senin root view'ın) sistem çubukları kadar içeri itilir
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,    // Üstteki bildirim çubuğunu kurtarır
                right = systemBars.right,
                bottom = systemBars.bottom // Alttaki navigasyon çubuğunu kurtarır
            )

            insets
        }

        arguments?.let {
            currentUser = BundleCompat.getParcelable(it,ARG_USER, User::class.java)!!
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            task = BundleCompat.getParcelable(it, ARG_TASK, Task.Lesson::class.java)
            if (task != null) lesson = task as Task.Lesson
        }

        dateTimePicker = Picker(requireContext(), childFragmentManager)
        setupAdapters()
        setupListeners()
        setupObservers()

        checkAndRequestNotificationPermission()

        val isEditMode = task != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit)
            binding.toolbar.subtitle = course.title

            binding.etTitle.setText(lesson!!.title)
            binding.etDescription.setText(lesson!!.description)
            binding.etDate.setText(lesson!!.date.toFormattedString("dd MMMM yyyy EEEE"))
            binding.actvRepeat.setText(mapRuleToDisplayString(lesson!!.recurrenceRule), false)
            binding.etDateRangeStart.setText(lesson!!.recurrenceRule.dtStart.toFormattedString("dd.MM.yyyy"))
            binding.etDateRangeEnd.setText(lesson!!.recurrenceRule.until.toFormattedString("dd.MM.yyyy"))
            binding.etTimeStart.setText(lesson!!.timeStart.toFormattedString("HH:mm"))
            binding.etTimeEnd.setText(lesson!!.timeEnd.toFormattedString("HH:mm"))
            binding.actvReminder.setText(mapReminderToDisplayString(lesson!!.remindBefore), false)
            binding.etPlace.setText(lesson!!.place)

            selectedDateMillis = lesson!!.date
            selectedRecurrenceRule = lesson!!.recurrenceRule
            selectedTimeStartMillis = lesson!!.timeStart
            selectedTimeEndMillis = lesson!!.timeEnd
            selectedRemindBeforeMinutes = lesson!!.remindBefore
        }else{
            binding.toolbar.title = getString(R.string.createTask)

            binding.actvRepeat.setText(mapRuleToDisplayString(selectedRecurrenceRule), false)
            binding.actvReminder.setText(mapReminderToDisplayString(-1), false)
            binding.llFilesSection.visibility = View.GONE

            binding.etTitle.requestFocus()

            binding.toolbar.menu.clear()
        }

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.action_delete -> {
                    val dialog = DialogFragmentDeleteConfirmation.newInstance("${binding.root.context.getString(R.string.delete)}", "${binding.root.context.getString(R.string.areYouSure)}")
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
                viewModelTask.update(course,updatedLesson)
            }else{
                val newLesson = Task.Lesson(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    courseId = course.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    date = selectedDateMillis,
                    recurrenceRule = selectedRecurrenceRule,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    absence = listOf<Long>(),
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString(),
                )
                viewModelTask.insert(course, newLesson)
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
            if (position > 0) checkAndRequestNotificationPermission()
            selectedRemindBeforeMinutes = when(position){
                0 -> -1    // "No reminder" -> Index 0
                1 -> 0     // "On Time" -> Index 1
                2 -> 10    // "10 minutes before" -> Index 2
                3 -> 30    // "30 minutes before" -> Index 3
                4 -> 60    // "1 hour before" -> Index 4
                5 -> 1440  // "1 day before" -> Index 5
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
                viewModelTask.delete(course, task!!)
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
                            WidgetUpdateWorker.enqueueUpdate(requireContext().applicationContext)
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
    private fun mapRuleToDisplayString(rule: RecurrenceRule): String{
        val options = resources.getStringArray(R.array.recurrence_options)
        return when(rule.freq){
            RecurrenceRule.Frequency.ONCE -> options[0] // "Does not repeat/ONCE"
            RecurrenceRule.Frequency.DAILY -> options[1] // "Repeats daily"
            RecurrenceRule.Frequency.WEEKLY -> options[2] // "Repeats weekly"
            RecurrenceRule.Frequency.MONTHLY -> options[3] // "Repeats monthly"
            RecurrenceRule.Frequency.YEARLY -> options[4] // "Repeats yearly"
            else -> options[0] // "Does not repeat/ONCE"
        }
    }
    private fun mapReminderToDisplayString(minutes: Int): String{
        val options = resources.getStringArray(R.array.reminder_options)
        return when(minutes){
            -1 -> options[0] // "No reminder"
            0 -> options[1] // "On Time"
            10 -> options[2] // "10 minutes before"
            30 -> options[3] // "30 minutes before"
            60 -> options[4] // "1 hour before"
            1440 -> options[5] // "1 day before"
            else -> options[0] // "No reminder"
        }
    }
    private fun checkAndRequestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            //system grant notification permission already
        }
    }

    companion object{
        const val ARG_USER = "dialog_task_lesson_arg_user"
        const val ARG_TAG = "dialog_task_lesson_arg_tag"
        const val ARG_COURSE = "dialog_task_lesson_arg_course"
        const val ARG_TASK = "dialog_task_lesson_arg_task"

        fun newInstanceForCreate(user: User, course: Course): DialogFragmentTaskLesson{
            return DialogFragmentTaskLesson().apply {
                arguments = bundleOf(
                    ARG_USER to user,
                    ARG_COURSE to course
                )
            }
        }

        fun newInstanceForEdit(user: User, course: Course, task: Task): DialogFragmentTaskLesson{
            return DialogFragmentTaskLesson().apply {
                arguments = bundleOf(
                    ARG_USER to user,
                    ARG_COURSE to course,
                    ARG_TASK to task
                )
            }
        }
    }
}