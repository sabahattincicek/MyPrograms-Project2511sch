package com.saboon.project_2511sch.presentation.file

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentNoteBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File

class DialogFragmentNote: DialogFragment() {
    private var _binding : DialogFragmentNoteBinding?=null
    private val binding get() = _binding!!

    private lateinit var course: Course
    private var uri: Uri? = null
    private var file: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogFragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
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
            binding.etNoteTitle.setText(file!!.title)
        }else{

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_COURSE = "note_dialog_fragment_arg_course"
        const val ARG_URI = "note_dialog_fragment_arg_uri"
        const val ARG_FILE = "note_dialog_fragment_arg_file"
        const val REQUEST_KEY_CREATE = "note_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "note_dialog_fragment_request_key_update"
        const val RESULT_KEY_FILE = "note_dialog_fragment_result_key_file"
        const val RESULT_KEY_URI = "note_dialog_fragment_result_key_uri"

        fun newInstance(course: Course, uri: Uri?, file: File?): DialogFragmentNote{
            val fragment = DialogFragmentNote()
            fragment.arguments = bundleOf(
                ARG_COURSE to course,
                ARG_URI to uri,
                ARG_FILE to file
            )
            return fragment
        }

    }
}