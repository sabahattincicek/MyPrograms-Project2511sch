package com.saboon.project_2511sch.presentation.task

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTaskExamBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Picker
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.toFormattedString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentTaskExam: DialogFragment() {

    private var _binding: DialogFragmentTaskExamBinding?=null
    private val binding get() = _binding!!
    private val viewModelTask: ViewModelTask by viewModels()

    private lateinit var dateTimePicker: Picker

    private var course: Course?= null
    private var task: Task? = null
    private var exam: Task.Exam? = null

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedTimeStartMillis: Long = System.currentTimeMillis()
    private var selectedTimeEndMillis: Long = System.currentTimeMillis()
    private var selectedRemindBeforeMinutes: Int = 0

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
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)
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
        }else{

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
                val updatedTask = exam!!.copy(
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
                viewModelTask.update(updatedTask)
            }else{
                val newTask = Task.Exam(
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
                viewModelTask.insert(newTask)
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
        const val ARG_COURSE = "dialog_task_exam_arg_course"
        const val ARG_TASK = "dialog_task_exam_arg_task"
        const val REQUEST_KEY_CREATE = "dialog_task_exam_request_key_create"
        const val REQUEST_KEY_UPDATE = "dialog_task_exam_request_key_update"
        const val REQUEST_KEY_DELETE = "dialog_task_exam_request_key_delete"
        const val RESULT_KEY_TASK = "dialog_task_exam_result_key_task"

        fun newInstanceForCreate(course: Course): DialogFragmentTaskExam{
            return DialogFragmentTaskExam().apply {
                arguments = bundleOf(ARG_COURSE to course)
            }
        }

        fun newInstanceForEdit(course: Course, task: Task): DialogFragmentTaskExam{
            return DialogFragmentTaskExam().apply {
                arguments = bundleOf(ARG_COURSE to course, ARG_TASK to task)
            }
        }
    }
}