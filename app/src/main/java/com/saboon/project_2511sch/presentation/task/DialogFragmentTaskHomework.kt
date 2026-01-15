package com.saboon.project_2511sch.presentation.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTaskHomeworkBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.SubmissionType
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.Picker

class DialogFragmentTaskHomework: DialogFragment() {

    private var _binding: DialogFragmentTaskHomeworkBinding?=null
    private val binding get() = _binding!!

    private lateinit var dateTimePicker: Picker

    private var course: Course?= null
    private var task: Task.Homework? = null

    private var selectedSubmissionType: SubmissionType = SubmissionType.OTHER
    private var selectedDueDateMillis: Long = System.currentTimeMillis()
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


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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