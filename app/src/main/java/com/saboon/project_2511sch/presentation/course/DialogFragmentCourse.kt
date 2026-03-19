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
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.util.IdGenerator
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.tag.DialogFragmentTag
import com.saboon.project_2511sch.presentation.tag.DialogFragmentManageTag
import com.saboon.project_2511sch.presentation.tag.DisplayItemTag
import com.saboon.project_2511sch.presentation.tag.ViewModelTag
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
    private val viewModelTag: ViewModelTag by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()
    private lateinit var currentUser: User
    private var selectedTag: Tag? = null
    private var course: Course? = null
    private var selectedColor =  ModelColor()
    private var tagList = mutableListOf<Tag>()
    private var isActive = true
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
            currentUser = BundleCompat.getParcelable(it, ARG_USER, User::class.java)!!
            course = BundleCompat.getParcelable(it,ARG_COURSE, Course::class.java)
        }

        setupColorCheckers()
        setupListeners()
        setupObservers()

        viewModelTag.getById("MyPrograms_default_tag_id")

        val isEditMode = course != null
        Log.d(TAG, "onCreateView: isEditMode: $isEditMode")

        if (isEditMode){
            Log.d(TAG, "onCreateView: Edit mode. Populating fields for course: ${course!!.title}")
            course?.let { course ->
                binding.etTitle.setText(course.title)
                binding.etDescription.setText(course.description)
                binding.etPeople.setText(course.people)
                selectedColor = course.color
                if (course.tagId != null) viewModelTag.getById(course.tagId)
                else binding.tilTag.isEndIconVisible = false
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
                isActive = course.isActive
                binding.msActivation.isChecked = isActive
            }
        }else{
            binding.etTitle.requestFocus()
            binding.tilTag.isEndIconVisible = false
        }

        binding.btnSave.setOnClickListener {
            Log.d(TAG, "onCreateView: Save button clicked.")
            if (isEditMode){
                val updatedCourse = course!!.copy(
                    isActive = isActive,
                    tagId = selectedTag?.id,
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
                    tagId = selectedTag?.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    people = binding.etPeople.text.toString(),
                    color = selectedColor,
                )
                viewModelCourse.insert(newCourse)
            }
        }

        binding.etTag.setOnClickListener {
            val dialog = DialogFragmentManageTag.newInstanceForSelect()
            dialog.show(childFragmentManager, "DialogFragmentList")
//            if (tagList.isEmpty()){
//                MaterialAlertDialogBuilder(requireContext())
//                    .setTitle("No Tags Found")
//                    .setMessage("There are no tags created yet. Would you like to create a new one now?")
//                    .setNegativeButton("Cancel"){ dialog, which ->
//                        dialog.dismiss()
//                    }
//                    .setPositiveButton("Create New Tag"){ dialog, which ->
//                        val dialogTag = DialogFragmentTag.newInstanceForCreate(currentUser)
//                        dialogTag.show(childFragmentManager, "Dialog Fragment Tag")
//                    }
//                    .show()
//            }else{
//                val dialog = DialogFragmentManageTag.newInstanceForSelect()
//                dialog.show(childFragmentManager, "DialogFragmentList")
//            }
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

        binding.msActivation.setOnCheckedChangeListener { buttonView, isChecked ->
            isActive = isChecked
        }

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "onCreateView: Toolbar navigation clicked. Dismissing dialog.")
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            Log.d(TAG, "onCreateView: Cancel button clicked. Dismissing dialog.")
            dismiss()
        }
        binding.etTag.doOnTextChanged { text, start, before, count ->
            binding.tilTag.isEndIconVisible = !text.isNullOrEmpty()
        }

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

    private fun setupListeners(){
        binding.tilTag.setEndIconOnClickListener {
            binding.etTag.text = null
            selectedTag = null
        }

        //TAG SELECT LISTENER
        childFragmentManager.setFragmentResultListener(DialogFragmentManageTag.REQUEST_TAG, viewLifecycleOwner){ requestKey, bundle ->
            val resultTag = BundleCompat.getParcelable(bundle, DialogFragmentManageTag.RESULT_TAG,Tag::class.java)
            resultTag?.let { tag ->
                selectedTag = tag
                binding.etTag.setText(tag.title)
            }
        }
    }

    private fun setupObservers(){
        // TAGS STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTag.tagsState.collect { resource ->
                    when(resource){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            val displayItemList = resource.data
                            displayItemList?.forEach { item ->
                                if (item is DisplayItemTag.ContentTag){
                                    tagList.add(item.tag)
                                }
                            }
                        }
                    }
                }
            }
        }
        //TAG STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTag.tagState.collect { resource ->
                    when(resource){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            selectedTag = resource.data
                            selectedTag?.let { tag ->
                                binding.etTag.setText(tag.title)
                            }
                        }
                    }
                }
            }
        }
        //COURSE EVENT: INSERT, UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.operationEvent.collect { event ->
                    when(event){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            // eger update islemi yapildiysa ve activation degisitirildiyse bu derse
                            // bagli butun tasklarin alarmlarini sync et
                            val updatedCourse = event.data
                            if (course != null && updatedCourse != null){
                                if (course!!.isActive != updatedCourse.isActive){
                                    Log.d(TAG, "Activation status changed: ${course?.isActive} -> ${updatedCourse.isActive}. Syncing alarms...")
                                    viewModelCourse.syncAlarms(event.data){
                                        dismiss()
                                    }
                                }else{
                                    dismiss()
                                }
                            }else{
                                dismiss()
                            }
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
        const val ARG_USER = "course_dialog_fragment_arg_user"
        const val ARG_COURSE = "course_dialog_fragment_arg_course"

        fun newInstanceForCreate(user: User):DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_USER to user
            )
            return fragment
        }
        fun newInstanceForUpdate(user: User, course: Course): DialogFragmentCourse{
            val fragment = DialogFragmentCourse()
            fragment.arguments = bundleOf(
                ARG_USER to user,
                ARG_COURSE to course
            )
            return fragment
        }
    }


}