package com.saboon.project_2511sch.presentation.course_file

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.Formatter
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

class DialogFragmentCourseFile: DialogFragment() {

    private var _binding: DialogFragmentFileBinding?=null
    private val binding get() = _binding!!

    private lateinit var course: Course
    private var uri: Uri? = null
    private var file: File? = null

    private var fileName = ""
    private var fileType = "application/octet-stream"
    private var fileSize = 0L

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
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            uri = BundleCompat.getParcelable(it, ARG_URI, Uri::class.java)
            file = BundleCompat.getParcelable(it, ARG_FILE, File::class.java)
        }

        val isEditMode = file != null
        if (isEditMode){
            binding.etTitle.setText(file!!.title)
            binding.etDescription.setText(file!!.description)
            binding.tvFileType.text = file!!.fileType
            binding.tvFileSize.text = Formatter.formatShortFileSize(context, file!!.sizeInBytes)
        }else{
            readMetaDateFromUri()
        }

        binding.btnSave.setOnClickListener {
            if (isEditMode){
                val updatedFile = file!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString()
                )
                setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(RESULT_KEY_FILE to updatedFile))
                dismiss()
            }else{
                val newFile = File(
                    id = IdGenerator.generateFileId(binding.etTitle.text.toString()),
                    programTableId = course.programTableId,
                    courseId = course.id,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    fileType = this.fileType,
                    filePath = "", //this field will fill in the repository
                    sizeInBytes = this.fileSize
                )
                setFragmentResult(REQUEST_KEY_CREATE, bundleOf(
                    RESULT_KEY_FILE to newFile,
                    RESULT_KEY_URI to uri
                ))
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun readMetaDateFromUri(){
        val contentResolver = requireContext().contentResolver
        uri.let{uri ->
            contentResolver.query(uri!!,null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()){
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }
            fileType = contentResolver.getType(uri) ?: "application/octet-stream"
            binding.etTitle.setText(fileName)
            binding.tvFileType.text = fileType
            binding.tvFileSize.text = Formatter.formatShortFileSize(context, fileSize)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val ARG_COURSE = "file_dialog_fragment_arg_course"
        const val ARG_URI = "file_dialog_fragment_arg_uri"
        const val ARG_FILE = "file_dialog_fragment_arg_file"
        const val REQUEST_KEY_CREATE = "file_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "file_dialog_fragment_request_key_update"
        const val RESULT_KEY_FILE = "file_dialog_fragment_result_key_file"
        const val RESULT_KEY_URI = "file_dialog_fragment_result_key_uri"

        fun newInstance(course: Course, uri: Uri?, file: File?): DialogFragmentCourseFile{
            val fragment = DialogFragmentCourseFile()
            fragment.arguments = bundleOf(
                ARG_COURSE to course,
                ARG_URI to uri,
                ARG_FILE to file
            )
            return fragment
        }
    }
}