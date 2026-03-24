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
import android.widget.Toast
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
import com.saboon.project_2511sch.databinding.DialogFragmentTaskExamBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.PermissionManager
import com.saboon.project_2511sch.util.Picker
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.open
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentTaskExam: DialogFragment() {

    private var _binding: DialogFragmentTaskExamBinding?=null
    private val binding get() = _binding!!
    private val viewModelTask: ViewModelTask by viewModels()
    private val viewModelSFile: ViewModelSFile by viewModels()

    private lateinit var dateTimePicker: Picker

    private lateinit var currentUser: User
    private lateinit var course: Course
    private var task: Task? = null
    private var exam: Task.Exam? = null

    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini

    private var selectedDateMillis: Long = System.currentTimeMillis()
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
    ): View? {
        _binding = DialogFragmentTaskExamBinding.inflate(inflater, container, false)
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

        arguments?.let{
            currentUser = BundleCompat.getParcelable(it, ARG_USER, User::class.java)!!
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            task = BundleCompat.getParcelable(it, ARG_TASK, Task.Exam::class.java)
            if (task != null) exam = task as Task.Exam
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

            binding.etTitle.setText(exam!!.title)
            binding.etDescription.setText(exam!!.description)
            binding.etTargetScore.setText(exam!!.targetScore.toString())
            binding.etAchievedScore.setText(exam!!.achievedScore.toString())
            binding.etDate.setText(exam!!.date.toFormattedString("dd MMMM yyyy EEEE"))
            binding.etTimeStart.setText(exam!!.timeStart.toFormattedString("HH:mm"))
            binding.etTimeEnd.setText(exam!!.timeEnd.toFormattedString("HH:mm"))
            binding.actvReminder.setText(mapReminderToDisplayString(exam!!.remindBefore), false)
            binding.etPlace.setText(exam!!.place)

            selectedDateMillis = exam!!.date
            selectedTimeStartMillis = exam!!.timeStart
            selectedTimeEndMillis = exam!!.timeEnd
            selectedRemindBeforeMinutes = exam!!.remindBefore
        }else{
            binding.toolbar.title = getString(R.string.createTask)

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
                val updatedExam = exam!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    targetScore = binding.etTargetScore.text.toString().toIntOrNull() ?: 0,
                    achievedScore = binding.etAchievedScore.text.toString().toIntOrNull() ?: 0,
                    date = selectedDateMillis,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString()
                )
                viewModelTask.update(course,updatedExam)
            }else{
                val newExam = Task.Exam(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    courseId = course.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    date = selectedDateMillis,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString(),
                    targetScore = binding.etTargetScore.text.toString().toIntOrNull() ?: 0,
                    achievedScore = binding.etAchievedScore.text.toString().toIntOrNull() ?: 0
                )
                viewModelTask.insert(course,newExam)
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.etDate.setOnClickListener {
            dateTimePicker.pickDateMillis("Date"){ result ->
                selectedDateMillis = result
                binding.etDate.setText(selectedDateMillis.toFormattedString("dd MMMM yyyy EEEE"))
            }
        }
        binding.etTimeStart.setOnClickListener {
            dateTimePicker.pickTimeMillis("Start Time", selectedTimeStartMillis) { result ->
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
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
        const val ARG_USER = "dialog_task_exam_arg_user"
        const val ARG_COURSE = "dialog_task_exam_arg_course"
        const val ARG_TASK = "dialog_task_exam_arg_task"

        fun newInstanceForCreate(user: User, course: Course): DialogFragmentTaskExam{
            return DialogFragmentTaskExam().apply {
                arguments = bundleOf(
                    ARG_USER to user,
                    ARG_COURSE to course
                )
            }
        }

        fun newInstanceForEdit(user: User, course: Course, task: Task): DialogFragmentTaskExam{
            return DialogFragmentTaskExam().apply {
                arguments = bundleOf(
                    ARG_USER to user,
                    ARG_COURSE to course,
                    ARG_TASK to task
                )
            }
        }
    }
}