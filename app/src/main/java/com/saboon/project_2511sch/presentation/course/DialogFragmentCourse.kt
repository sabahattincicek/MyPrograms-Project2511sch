package com.saboon.project_2511sch.presentation.course

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentCourseBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.IdGenerator
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.ModelColor
import com.saboon.project_2511sch.util.ModelColorConstats
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class DialogFragmentCourse: DialogFragment() {

    private var _binding: DialogFragmentCourseBinding ?= null
    private val binding get() = _binding!!
    private val viewModelCourse: ViewModelCourse by viewModels()
    private lateinit var currentUser: User
    private lateinit var programTable: ProgramTable
    private var course: Course? = null
    private var selectedColor =  ModelColor()
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
            currentUser = BundleCompat.getParcelable(it, ARG_PROGRAM_USER, User::class.java)!!
            programTable = BundleCompat.getParcelable(it,ARG_PROGRAM_TABLE, ProgramTable::class.java)!!
            course = BundleCompat.getParcelable(it,ARG_COURSE, Course::class.java)
        }

        setupColorCheckers()
        setupObservers()

        val isEditMode = course != null
        Log.d(TAG, "onCreateView: isEditMode: $isEditMode")

        if (isEditMode){
            Log.d(TAG, "onCreateView: Edit mode. Populating fields for course: ${course!!.title}")
            binding.etTitle.setText(course!!.title)
            binding.etDescription.setText(course!!.description)
            binding.etPeople.setText(course!!.people)
            selectedColor = course!!.color
            when(selectedColor.colorHex){
                ModelColorConstats.COLOR_1 -> {clearAllChecks(); binding.ivColorCk1.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_2 -> {clearAllChecks(); binding.ivColorCk2.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_3 -> {clearAllChecks(); binding.ivColorCk3.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_4 -> {clearAllChecks(); binding.ivColorCk4.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_5 -> {clearAllChecks(); binding.ivColorCk5.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_6 -> {clearAllChecks(); binding.ivColorCk6.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_7 -> {clearAllChecks(); binding.ivColorCk7.visibility = View.VISIBLE}
                ModelColorConstats.COLOR_8 -> {clearAllChecks(); binding.ivColorCk8.visibility = View.VISIBLE}
            }
        }else{
            binding.etTitle.requestFocus()
        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "onCreateView: Save button clicked.")
            if (isEditMode){
                val updatedCourse = course!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = selectedColor
                )
                viewModelCourse.update(updatedCourse)
            }else{
                Log.d(TAG, "onCreateView: Creating new course.")
                val newCourse = Course(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    programTableId = programTable.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = selectedColor,
                )
                viewModelCourse.insert(newCourse)
            }

        }

        binding.mcvColor1.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_1)
            clearAllChecks()
            binding.ivColorCk1.visibility = View.VISIBLE
        }
        binding.mcvColor2.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_2)
            clearAllChecks()
            binding.ivColorCk2.visibility = View.VISIBLE
        }
        binding.mcvColor3.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_3)
            clearAllChecks()
            binding.ivColorCk3.visibility = View.VISIBLE
        }
        binding.mcvColor4.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_4)
            clearAllChecks()
            binding.ivColorCk4.visibility = View.VISIBLE
        }
        binding.mcvColor5.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_5)
            clearAllChecks()
            binding.ivColorCk5.visibility = View.VISIBLE
        }
        binding.mcvColor6.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_6)
            clearAllChecks()
            binding.ivColorCk6.visibility = View.VISIBLE
        }
        binding.mcvColor7.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_7)
            clearAllChecks()
            binding.ivColorCk7.visibility = View.VISIBLE
        }
        binding.mcvColor8.setOnClickListener {
            selectedColor = ModelColor(ModelColorConstats.COLOR_8)
            clearAllChecks()
            binding.ivColorCk8.visibility = View.VISIBLE
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

    private fun setupColorCheckers(){
        binding.ivColorBg1.setBackgroundColor(ModelColorConstats.COLOR_1.toColorInt())
        binding.ivColorBg2.setBackgroundColor(ModelColorConstats.COLOR_2.toColorInt())
        binding.ivColorBg3.setBackgroundColor(ModelColorConstats.COLOR_3.toColorInt())
        binding.ivColorBg4.setBackgroundColor(ModelColorConstats.COLOR_4.toColorInt())
        binding.ivColorBg5.setBackgroundColor(ModelColorConstats.COLOR_5.toColorInt())
        binding.ivColorBg6.setBackgroundColor(ModelColorConstats.COLOR_6.toColorInt())
        binding.ivColorBg7.setBackgroundColor(ModelColorConstats.COLOR_7.toColorInt())
        binding.ivColorBg8.setBackgroundColor(ModelColorConstats.COLOR_8.toColorInt())
    }

    private fun setupObservers(){
        //COURSE EVENT: INSERT, UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.operationEvent.collect { event ->
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
    }

    private fun clearAllChecks(){
        binding.ivColorCk1.visibility = View.GONE
        binding.ivColorCk2.visibility = View.GONE
        binding.ivColorCk3.visibility = View.GONE
        binding.ivColorCk4.visibility = View.GONE
        binding.ivColorCk5.visibility = View.GONE
        binding.ivColorCk6.visibility = View.GONE
        binding.ivColorCk7.visibility = View.GONE
        binding.ivColorCk8.visibility = View.GONE
    }


    companion object{
        const val ARG_PROGRAM_USER = "course_dialog_fragment_arg_user"
        const val ARG_PROGRAM_TABLE = "course_dialog_fragment_arg_program_table"
        const val ARG_COURSE = "course_dialog_fragment_arg_course"

        fun newInstanceForCreate(user: User, programTable: ProgramTable):DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_USER to user,
                ARG_PROGRAM_TABLE to programTable
            )
            return fragment
        }
        fun newInstanceForUpdate(user: User, programTable: ProgramTable, course: Course): DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_USER to user,
                ARG_PROGRAM_TABLE to programTable,
                ARG_COURSE to course
            )
            return fragment
        }
    }


}