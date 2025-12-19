package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentFileBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.IdGenerator

class DialogFragmentFile: DialogFragment() {

    private var _binding: DialogFragmentFileBinding?=null
    private val binding = _binding!!

    private lateinit var course: Course
    private var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java).let { course ->
                if (course != null) this.course = course
            }
            BundleCompat.getParcelable(it, ARG_FILE, File::class.java).let { file ->
                if (file != null) this.file = file
            }
        }

        val isEditMode = file != null
        if (isEditMode){
            binding.etTitle.setText(file!!.title)
            binding.etDescription.setText(file!!.description)
            binding.tvFileType.text = file!!.fileType
            binding.tvFileSize.text = file!!.sizeInBytes.toString()
        }

        binding.btnSave.setOnClickListener {
            if (isEditMode){
                val updatedFile = file!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString()
                )
                setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(
                    RESULT_KEY_FILE to updatedFile
                ))
                dismiss()
            }else{
                val newFile = File(
                    id = IdGenerator.generateFileId(binding.etTitle.text.toString()),
                    programTableId = course.programTableId,
                    courseId = course.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    fileType = "TODO()",
                    filePath = "TODO()",
                    sizeInBytes = 0L
                )
                setFragmentResult(REQUEST_KEY_CREATE, bundleOf(
                    RESULT_KEY_FILE to newFile
                ))
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val ARG_COURSE = "file_dialog_fragment_arg_course"
        const val ARG_FILE = "file_dialog_fragment_arg_course"
        const val REQUEST_KEY_CREATE = "file_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "file_dialog_fragment_request_key_update"
        const val RESULT_KEY_FILE = "file_dialog_fragment_result_key_file"

        fun newInstance(course: Course, file: File?): DialogFragmentFile{
            val fragment = DialogFragmentFile()
            fragment.arguments = bundleOf(
                ARG_COURSE to course,
                ARG_FILE to file
            )
            return fragment
        }
    }
}