package com.saboon.project_2511sch.presentation.course

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentCourseBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.ModelColors
import androidx.core.os.BundleCompat

class DialogFragmentCourse: DialogFragment() {

    private var _binding: DialogFragmentCourseBinding ?= null
    private val binding get() = _binding!!
    private val TAG = "DialogFragmentCourse"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentCourseBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: View created.")

        val programTable = arguments?.let{BundleCompat.getParcelable(it,ARG_PROGRAM_TABLE, ProgramTable::class.java)}
        val course = arguments?.let{BundleCompat.getParcelable(it,ARG_COURSE, Course::class.java)}
        var color: String = ModelColors.MODEL_COLOR_1

        Log.d(TAG, "onCreateView: ProgramTable: $programTable, Course: $course")

        val isEditMode = course != null
        Log.d(TAG, "onCreateView: isEditMode: $isEditMode")

        if (isEditMode){
            Log.d(TAG, "onCreateView: Edit mode. Populating fields for course: ${course.title}")
            binding.etTitle.setText(course.title)
            binding.etDescription.setText(course.description)
            binding.etPeople.setText(course.people)
            color = course.color ?: ModelColors.MODEL_COLOR_1
            when(color){
                ModelColors.MODEL_COLOR_1 -> {binding.radioColor1.isChecked = true}
                ModelColors.MODEL_COLOR_2 -> {binding.radioColor2.isChecked = true}
                ModelColors.MODEL_COLOR_3 -> {binding.radioColor3.isChecked = true}
                ModelColors.MODEL_COLOR_4 -> {binding.radioColor4.isChecked = true}
                ModelColors.MODEL_COLOR_5 -> {binding.radioColor5.isChecked = true}
                ModelColors.MODEL_COLOR_6 -> {binding.radioColor6.isChecked = true}
                ModelColors.MODEL_COLOR_7 -> {binding.radioColor7.isChecked = true}
                ModelColors.MODEL_COLOR_8 -> {binding.radioColor8.isChecked = true}
            }
        }else{
            Log.d(TAG, "onCreateView: Create mode.")
            binding.toolbar.title = getString(R.string.create_new_course)
        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "onCreateView: Save button clicked.")
            if (isEditMode){
                val updatedCourse = course.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = color
                )
                Log.d(TAG, "onCreateView: Updating course: $updatedCourse")
                setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(
                    RESULT_KEY_COURSE to updatedCourse
                ))
                dismiss()
            }else{
                Log.d(TAG, "onCreateView: Creating new course.")
                val newCourse = Course(
                    id = IdGenerator.generateCourseId(binding.etTitle.text.toString()),
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = programTable!!.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = color,
                )

                Log.d(TAG, "onCreateView: New course created: $newCourse")
                setFragmentResult(REQUEST_KEY_CREATE, bundleOf(
                    RESULT_KEY_COURSE to newCourse
                ))
                dismiss()
            }

        }

        binding.rg1.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId){
                R.id.radio_color1 -> {if(binding.radioColor1.isChecked){binding.rg2.clearCheck(); color = ModelColors.MODEL_COLOR_1}}
                R.id.radio_color2 -> {if(binding.radioColor2.isChecked){binding.rg2.clearCheck(); color = ModelColors.MODEL_COLOR_2}}
                R.id.radio_color3 -> {if(binding.radioColor3.isChecked){binding.rg2.clearCheck(); color = ModelColors.MODEL_COLOR_3}}
                R.id.radio_color4 -> {if(binding.radioColor4.isChecked){binding.rg2.clearCheck(); color = ModelColors.MODEL_COLOR_4}}
            }
        }
        binding.rg2.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId){
                R.id.radio_color5 -> {if(binding.radioColor5.isChecked){binding.rg1.clearCheck(); color = ModelColors.MODEL_COLOR_5}}
                R.id.radio_color6 -> {if(binding.radioColor6.isChecked){binding.rg1.clearCheck(); color = ModelColors.MODEL_COLOR_6}}
                R.id.radio_color7 -> {if(binding.radioColor7.isChecked){binding.rg1.clearCheck(); color = ModelColors.MODEL_COLOR_7}}
                R.id.radio_color8 -> {if(binding.radioColor8.isChecked){binding.rg1.clearCheck(); color = ModelColors.MODEL_COLOR_8}}
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "onCreateView: Toolbar navigation clicked. Dismissing dialog.")
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            Log.d(TAG, "onCreateView: Cancel button clicked. Dismissing dialog.")
            dismiss()
        }
        return binding.root
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Dialog destroyed.")
        _binding = null
    }


    companion object{
        const val ARG_PROGRAM_TABLE = "course_dialog_fragment_arg_program_table"
        const val ARG_COURSE = "course_dialog_fragment_arg_course"

        const val REQUEST_KEY_CREATE = "course_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "course_dialog_fragment_request_key_update"

        const val RESULT_KEY_COURSE = "course_dialog_fragment_result_key_course"

        fun newInstance(programTable: ProgramTable, course: Course?): DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_TABLE to programTable,
                ARG_COURSE to course
            )
            return fragment
        }
    }


}