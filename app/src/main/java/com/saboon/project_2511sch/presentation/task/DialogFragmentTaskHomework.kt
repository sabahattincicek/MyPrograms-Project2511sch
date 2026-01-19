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
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTaskHomeworkBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.common.DialogFragmentDeleteConfirmation
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Picker
import com.saboon.project_2511sch.util.toFormattedString

class DialogFragmentTaskHomework: DialogFragment() {

    private var _binding: DialogFragmentTaskHomeworkBinding?=null
    private val binding get() = _binding!!

    private lateinit var dateTimePicker: Picker

    private var course: Course?= null
    private var task: Task.Homework? = null
    private var selectedDueDateMillis: Long = System.currentTimeMillis()
    private var selectedDueTimeMillis: Long = System.currentTimeMillis()
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
        _binding = DialogFragmentTaskHomeworkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)
            task = BundleCompat.getParcelable(it, ARG_TASK, Task.Homework::class.java)
        }

        dateTimePicker = Picker(requireContext(), childFragmentManager)
        setupAdapters()
        setupFragmentResultListeners()

        val isEditMode = task != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit_task)
            binding.toolbar.subtitle = course!!.title
            binding.etTitle.setText(task!!.title)
            binding.etDescription.setText(task!!.description)
            binding.etDueDate.setText(task!!.dueDate.toFormattedString("dd MMMM yyyy EEEE"))
            binding.etDueTime.setText(task!!.dueTime.toFormattedString("HH:mm"))
            binding.actvReminder.setText(mapMinutesToDisplayString(task!!.remindBefore, resources.getStringArray(R.array.reminder_options)), false)

            selectedDueDateMillis = task!!.dueDate
            selectedDueTimeMillis = task!!.dueTime
            selectedRemindBeforeMinutes = task!!.remindBefore
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
                val updatedTask = task!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    dueDate = selectedDueDateMillis,
                    dueTime = selectedDueTimeMillis,
                    remindBefore = selectedRemindBeforeMinutes
                )
                setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(RESULT_KEY_TASK to updatedTask))
                dismiss()
            }else{
                val newTask = Task.Homework(
                    id = IdGenerator.generateTaskId(binding.etTitle.text.toString()),
                    courseId = course!!.id,
                    programTableId = course!!.programTableId,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    dueDate = selectedDueDateMillis,
                    dueTime = selectedDueTimeMillis,
                    remindBefore = selectedRemindBeforeMinutes
                )
                setFragmentResult(REQUEST_KEY_CREATE, bundleOf(RESULT_KEY_TASK to newTask))
                dismiss()
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
    }

    private fun setupFragmentResultListeners(){
        childFragmentManager.setFragmentResultListener(DialogFragmentDeleteConfirmation.REQUEST_KEY, viewLifecycleOwner){ requestKey, result ->
            val isYes = result.getBoolean(DialogFragmentDeleteConfirmation.RESULT_KEY)
            if (isYes){
                setFragmentResult(REQUEST_KEY_DELETE, bundleOf(ARG_TASK to task))
                dismiss()
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
        const val ARG_COURSE = "dialog_task_homework_arg_course"
        const val ARG_TASK = "dialog_task_homework_arg_task"
        const val REQUEST_KEY_CREATE = "dialog_task_homework_request_key_create"
        const val REQUEST_KEY_UPDATE = "dialog_task_homework_request_key_update"
        const val REQUEST_KEY_DELETE = "dialog_task_homework_request_key_delete"
        const val RESULT_KEY_TASK = "dialog_task_homework_result_key_task"

        fun newInstanceForCreate(course: Course): DialogFragmentTaskHomework{
            return DialogFragmentTaskHomework().apply {
                arguments = bundleOf(ARG_COURSE to course)
            }
        }

        fun newInstanceForEdit(course: Course, task: Task.Homework): DialogFragmentTaskHomework{
            return DialogFragmentTaskHomework().apply {
                arguments = bundleOf(ARG_COURSE to course, ARG_TASK to task)
            }
        }
    }

}