package com.saboon.project_2511sch.presentation.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.datepicker.MaterialDatePicker
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentScheduleBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.TimePickers
import com.saboon.project_2511sch.util.toFormattedString

class DialogFragmentTask: DialogFragment() {

    private var _binding: DialogFragmentScheduleBinding?= null
    private val binding get() = _binding!!

    private lateinit var course: Course
    private var task: Task? = null

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedStartTimeMillis: Long = System.currentTimeMillis()
    private var selectedEndTimeMillis: Long = System.currentTimeMillis()
    private var selectedRecurrenceRule: String = ""
    private var selectedRemindBeforeMinutes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentScheduleBinding.inflate(inflater, container, false)

        arguments?.let{
            BundleCompat.getParcelable(it,ARG_COURSE, Course::class.java).let { course ->
                if (course != null) {
                    this.course = course
                }
            }
            BundleCompat.getParcelable(it,ARG_SCHEDULE, Task::class.java).let{ schedule ->
                if (schedule != null){
                    this.task = schedule
                }
            }
        }

        val recurrenceOptions = resources.getStringArray(R.array.recurrence_options)
        val recurrenceAdapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            recurrenceOptions
        )
        binding.actvRepeat.setAdapter(recurrenceAdapter)

        val reminderOptions = resources.getStringArray(R.array.reminder_options)
        val reminderAdapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            reminderOptions
        )
        binding.actvReminder.setAdapter(reminderAdapter)


        val isEditMode = task != null
        if(isEditMode){
//            selectedRecurrenceRule = task!!.recurrenceRule
//            selectedDateMillis = task!!.date
//            selectedStartTimeMillis = task!!.startTime
//            selectedEndTimeMillis = task!!.endTime
//            selectedRemindBeforeMinutes = task!!.remindBefore
//
//            binding.etTitle.setText(task!!.title)
//            binding.etDescription.setText(task!!.description)
//            binding.actvRepeat.setText(mapRuleToDisplayString(selectedRecurrenceRule, recurrenceOptions), false)
//            binding.etDate.setText(selectedDateMillis.toFormattedString("dd MMMM yyyy"))
//            binding.etTimeStart.setText(selectedStartTimeMillis.toFormattedString("HH:mm"))
//            binding.etTimeEnd.setText(selectedEndTimeMillis.toFormattedString("HH:mm"))
//            binding.actvReminder.setText(mapMinutesToDisplayString(selectedRemindBeforeMinutes, reminderOptions), false)
//            binding.etPlace.setText(task!!.place)
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_delete -> {
                    if (task != null){
                        setFragmentResult(REQUEST_KEY_DELETE, bundleOf(RESULT_KEY_SCHEDULE to task))
                        dismiss()
                    }
                    true
                }
                else -> false
            }
        }

        binding.btnSave.setOnClickListener {
            if(isEditMode){
//                val updatedSchedule = task!!.copy(
//                    title = binding.etTitle.text.toString(),
//                    description = binding.etDescription.text.toString(),
//                    date = selectedDateMillis,
//                    startTime = selectedStartTimeMillis,
//                    endTime = selectedEndTimeMillis,
//                    place = binding.etPlace.text.toString(),
//                    remindBefore = selectedRemindBeforeMinutes,
//                    recurrenceRule = selectedRecurrenceRule
//                )
//
//                setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(
//                    RESULT_KEY_SCHEDULE to updatedSchedule
//                ))
//                dismiss()
            }else{
//                val newTask = Task(
//                    id = IdGenerator.generateScheduleId(binding.etTitle.text.toString()),
//                    courseId = course.id,
//                    programTableId = course.programTableId,
//                    title = binding.etTitle.text.toString(),
//                    description = binding.etDescription.text.toString(),
//                    date = selectedDateMillis,
//                    startTime = selectedStartTimeMillis,
//                    endTime = selectedEndTimeMillis,
//                    place = binding.etPlace.text.toString(),
//                    remindBefore = selectedRemindBeforeMinutes,
//                    recurrenceRule = selectedRecurrenceRule
//                )
//
//                setFragmentResult(REQUEST_KEY_CREATE, bundleOf(
//                    RESULT_KEY_SCHEDULE to newTask
//                ))
//                dismiss()
            }
        }

        binding.actvRepeat.setOnItemClickListener { parent, view, position, id ->
            selectedRecurrenceRule = when(position){
                1 -> "FREQ=DAILY"
                2 -> "FREQ=WEEKLY"
                3 -> "FREQ=MONTHLY"
                4 -> "FREQ=YEARLY"
                else -> ""
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
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        binding.etTimeStart.setOnClickListener {
            TimePickers(requireContext()).timePicker(childFragmentManager,"Start Time"){ time ->
                selectedStartTimeMillis = time
                binding.etTimeStart.setText(selectedStartTimeMillis.toFormattedString("HH:mm"))
            }
        }
        binding.etTimeEnd.setOnClickListener {
            TimePickers(requireContext()).timePicker(childFragmentManager, "End Time"){ time ->
                selectedEndTimeMillis = time
                binding.etTimeEnd.setText(selectedEndTimeMillis.toFormattedString("HH:mm"))
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showDatePicker(){
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(selectedDateMillis)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            selectedDateMillis = selection
            binding.etDate.setText(selectedDateMillis.toFormattedString("dd MMMM yyyy"))
        }
        picker.show(childFragmentManager, "DatePicker")
    }

    private fun mapRuleToDisplayString(rule: String, options: Array<String>): String{
        return when(rule){
            "FREQ=DAILY" -> options[1]
            "FREQ=WEEKLY" -> options[2]
            "FREQ=MONTHLY" -> options[3]
            "FREQ=YEARLY" -> options[4]
            else -> options[0] // "Does not repeat"
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
        const val ARG_COURSE = "schedule_dialog_fragment_arg_course"
        const val ARG_SCHEDULE = "schedule_dialog_fragment_arg_schedule"
        const val REQUEST_KEY_CREATE = "schedule_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "schedule_dialog_fragment_request_key_update"
        const val REQUEST_KEY_DELETE = "schedule_dialog_fragment_request_key_delete"
        const val RESULT_KEY_SCHEDULE = "schedule_dialog_fragment_result_key_schedule"

        fun newInstance(course: Course, task: Task?): DialogFragmentTask{
            val fragment = DialogFragmentTask()
            fragment.arguments = bundleOf(
                ARG_COURSE to course,
                ARG_SCHEDULE to task
            )
            return fragment
        }
    }

}