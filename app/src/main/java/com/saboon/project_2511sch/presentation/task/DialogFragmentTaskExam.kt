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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTaskExamBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.presentation.sfile.RecyclerAdapterSFileMini
import com.saboon.project_2511sch.presentation.sfile.ViewModelSFile
import com.saboon.project_2511sch.presentation.task.DialogFragmentTaskLesson.Companion.ARG_PROGRAM_TABLE
import com.saboon.project_2511sch.util.IdGenerator
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


    private lateinit var programTable: ProgramTable
    private lateinit var course: Course
    private var task: Task? = null
    private var exam: Task.Exam? = null

    private lateinit var recyclerAdapterSFileMini: RecyclerAdapterSFileMini

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedTimeStartMillis: Long = System.currentTimeMillis()
    private var selectedTimeEndMillis: Long = System.currentTimeMillis()
    private var selectedRemindBeforeMinutes: Int = 0

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
    ): View? {
        _binding = DialogFragmentTaskExamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let{
            programTable = BundleCompat.getParcelable(it, ARG_PROGRAM_TABLE, ProgramTable::class.java)!!
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            task = BundleCompat.getParcelable(it, ARG_TASK, Task.Exam::class.java)
            if (task != null) exam = task as Task.Exam
        }

        dateTimePicker = Picker(requireContext(), childFragmentManager)

        setupAdapters()
        setupListeners()
        setupObservers()

        val isEditMode = task != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit_task)
            binding.toolbar.subtitle = course!!.title
            binding.etTitle.setText(exam!!.title)
            binding.etDescription.setText(exam!!.description)
            binding.etTargetScore.setText(exam!!.targetScore.toString())
            binding.etAchievedScore.setText(exam!!.achievedScore.toString())
            binding.etDate.setText(exam!!.date.toFormattedString("dd MMMM yyyy EEEE"))
            binding.etTimeStart.setText(exam!!.timeStart.toFormattedString("HH:mm"))
            binding.etTimeEnd.setText(exam!!.timeEnd.toFormattedString("HH:mm"))
            binding.actvReminder.setText(mapMinutesToDisplayString(exam!!.remindBefore, resources.getStringArray(R.array.reminder_options)), false)
            binding.etPlace.setText(exam!!.place)

            selectedDateMillis = exam!!.date
            selectedTimeStartMillis = exam!!.timeStart
            selectedTimeEndMillis = exam!!.timeEnd
            selectedRemindBeforeMinutes = exam!!.remindBefore

            viewModelSFile.updateProgramTable(programTable)
            viewModelSFile.updateCourse(course, false)
            viewModelSFile.updateTask(task)
            binding.llFilesSection.visibility = View.VISIBLE
        }else{
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
                val updatedExam = exam!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    targetScore = binding.etTargetScore.text.toString().toIntOrNull(),
                    achievedScore = binding.etAchievedScore.text.toString().toIntOrNull(),
                    date = selectedDateMillis,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString()
                )
                viewModelTask.update(updatedExam)
            }else{
                val newExam = Task.Exam(
                    id = IdGenerator.generateTaskId(binding.etTitle.text.toString()),
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = course!!.programTableId,
                    courseId = course!!.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    date = selectedDateMillis,
                    timeStart = selectedTimeStartMillis,
                    timeEnd = selectedTimeEndMillis,
                    remindBefore = selectedRemindBeforeMinutes,
                    place = binding.etPlace.text.toString(),
                    targetScore = binding.etTargetScore.text.toString().toIntOrNull(),
                    achievedScore = binding.etAchievedScore.text.toString().toIntOrNull()
                )
                viewModelTask.insert(newExam)
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
        //INSERT EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.insertEvent.collect { resource ->
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
        //UPDATE EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.updateEvent.collect { resource ->
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
        //DELETE EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.deleteEvent.collect { resource ->
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
        //INSERT FILE EVENT
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
        const val ARG_PROGRAM_TABLE = "dialog_task_exam_arg_program_table"
        const val ARG_COURSE = "dialog_task_exam_arg_course"
        const val ARG_TASK = "dialog_task_exam_arg_task"

        fun newInstanceForCreate(programTable: ProgramTable, course: Course): DialogFragmentTaskExam{
            return DialogFragmentTaskExam().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_TABLE to programTable,
                    ARG_COURSE to course
                )
            }
        }

        fun newInstanceForEdit(programTable: ProgramTable, course: Course, task: Task): DialogFragmentTaskExam{
            return DialogFragmentTaskExam().apply {
                arguments = bundleOf(
                    ARG_PROGRAM_TABLE to programTable,
                    ARG_COURSE to course,
                    ARG_TASK to task
                )
            }
        }
    }
}