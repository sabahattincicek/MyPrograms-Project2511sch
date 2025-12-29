package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentLinkBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.IdGenerator

class DialogFragmentLink: DialogFragment() {

    private var _binding: DialogFragmentLinkBinding?=null
    private val binding get() = _binding!!

    private var course: Course? = null
    private var file: File? = null

    private val TAG = "DialogFragmentLink"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DialogFragment is being created.")
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFragmentLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let{
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)
            file = BundleCompat.getParcelable(it, ARG_LINK, File::class.java)
        }

        val isEditMode = file != null
        if (isEditMode){
            binding.toolbar.title = getString(R.string.edit_link)
            binding.etTitle.setText(file!!.title)
            binding.etUrl.setText(file!!.description)
        }else{
            requireNotNull(course) { "Course must be provided for create mode" }
            binding.toolbar.title = getString(R.string.add_new_link)
        }

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        binding.toolbar.inflateMenu(R.menu.menu_action_save)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.action_save -> {
                    val title = binding.etTitle.text.toString()
                    val url = binding.etUrl.text.toString()

                    if (isEditMode){ //Edit Mode
                        val updatedLinkFile = file!!.copy(
                            title = title,
                            description = url
                        )
                        setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(RESULT_KEY_LINK to updatedLinkFile))
                    }else{ //Create Mode
                        val newLinkFile = File(
                            id = IdGenerator.generateFileId(title),
                            programTableId = course!!.programTableId,
                            courseId = course!!.id,
                            title = title,
                            description = url,
                            fileType = "app/link",
                            filePath = "",
                            sizeInBytes = 0L
                        )
                        setFragmentResult(REQUEST_KEY_CREATE, bundleOf(RESULT_KEY_LINK to newLinkFile))
                    }
                    dismiss()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val ARG_COURSE = "link_dialog_fragment_arg_course"
        const val ARG_LINK = "link_dialog_fragment_arg_link"
        const val REQUEST_KEY_CREATE = "link_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "link_dialog_fragment_request_key_update"
        const val RESULT_KEY_LINK = "link_dialog_fragment_result_key_link"

        fun newInstanceForCreate(course: Course): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf(
                    ARG_COURSE to course
                )
            }
        }
        fun newInstanceForEdit(link: File): DialogFragmentLink{
            return DialogFragmentLink().apply {
                arguments = bundleOf(
                    ARG_LINK to link
                )
            }
        }
    }

}