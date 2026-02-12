package com.saboon.project_2511sch.presentation.course

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentCourseBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.ModelColors
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentCourse: DialogFragment() {

    private var _binding: DialogFragmentCourseBinding ?= null
    private val binding get() = _binding!!
    private val viewModelCourse: ViewModelCourse by viewModels()
    private lateinit var programTable: ProgramTable
    private var course: Course? = null
    private var color: String = ModelColors.MODEL_COLOR_1
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

        arguments?.let {
            programTable = BundleCompat.getParcelable(it,ARG_PROGRAM_TABLE, ProgramTable::class.java)!!
            course = BundleCompat.getParcelable(it,ARG_COURSE, Course::class.java)
        }

        setupObservers()

        val isEditMode = course != null
        Log.d(TAG, "onCreateView: isEditMode: $isEditMode")

        if (isEditMode){
            Log.d(TAG, "onCreateView: Edit mode. Populating fields for course: ${course!!.title}")
            binding.etTitle.setText(course!!.title)
            binding.etDescription.setText(course!!.description)
            binding.etPeople.setText(course!!.people)
            color = course!!.color
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

        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "onCreateView: Save button clicked.")
            if (isEditMode){
                val updatedCourse = course!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = color
                )
                viewModelCourse.update(updatedCourse)
            }else{
                Log.d(TAG, "onCreateView: Creating new course.")
                val newCourse = Course(
                    id = IdGenerator.generateCourseId(binding.etTitle.text.toString()),
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = programTable.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = color,
                )
                viewModelCourse.insert(newCourse)
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

    private fun setupObservers(){
        //INSERT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.insertEvent.collect { event ->
                    when(event){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            dismiss()
                        }
                    }
                }
            }
        }
        //UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.updateEvent.collect { resource ->
                    when(resource){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            dismiss()
                        }
                    }
                }
            }
        }
    }


    companion object{
        const val ARG_PROGRAM_TABLE = "course_dialog_fragment_arg_program_table"
        const val ARG_COURSE = "course_dialog_fragment_arg_course"

        fun newInstanceForCreate(programTable: ProgramTable):DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_TABLE to programTable
            )
            return fragment
        }
        fun newInstanceForUpdate(programTable: ProgramTable, course: Course): DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_TABLE to programTable,
                ARG_COURSE to course
            )
            return fragment
        }
    }


}