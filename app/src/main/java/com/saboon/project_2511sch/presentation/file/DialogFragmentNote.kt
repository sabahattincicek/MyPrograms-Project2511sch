package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentNoteBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.IdGenerator

class DialogFragmentNote: DialogFragment() {
    private var _binding : DialogFragmentNoteBinding?=null
    private val binding get() = _binding!!

    private lateinit var course: Course
    private var file: File? = null

    private val TAG = "DialogFragmentNote"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Layout is being inflated.")
        _binding = DialogFragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DialogFragment is being created.")
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View created, processing arguments.")

        arguments?.let {
            course = BundleCompat.getParcelable(it, ARG_COURSE, Course::class.java)!!
            file = BundleCompat.getParcelable(it, ARG_NOTE, File::class.java)
            Log.d(TAG, "Arguments received. Course: ${course.title}, Existing Note: ${file?.title ?: "null"}")
        }

        val isEditMode = file != null
        if (isEditMode){
            Log.i(TAG, "Operating in Edit Mode.")
            binding.toolbar.title = getString(R.string.edit_note)
            binding.etNoteTitle.setText(file!!.title)
            binding.reEditor.html = file!!.description
        }else{
            Log.i(TAG, "Operating in Create Mode.")
            binding.toolbar.title = getString(R.string.add_new_note)
        }

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Navigation icon clicked, dismissing dialog.")
            dismiss()
        }

        binding.toolbar.inflateMenu(R.menu.menu_action_save)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId){
                R.id.action_save -> {
                    Log.d(TAG, "Save action clicked.")
                    val title = binding.etNoteTitle.text.toString()
                    val content = binding.reEditor.html ?: ""

                    if (file != null) { // Edit Mode
                        val updatedNoteFile = file!!.copy(
                            title = title,
                            description = content,
                            sizeInBytes = content.toByteArray().size.toLong()
                        )
                        Log.i(TAG, "Sending update result for note: ${updatedNoteFile.title}")
                        setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(RESULT_KEY_NOTE to updatedNoteFile))
                    } else { // Create Mode
                        val newNoteFile = File(
                            id = IdGenerator.generateFileId(title),
                            programTableId = course.programTableId,
                            courseId = course.id,
                            title = title,
                            description = content,
                            fileType = "app/note",
                            filePath = "",
                            sizeInBytes = content.toByteArray().size.toLong()
                        )
                        Log.i(TAG, "Sending create result for new note: ${newNoteFile.title}")
                        setFragmentResult(REQUEST_KEY_CREATE, bundleOf(RESULT_KEY_NOTE to newNoteFile))
                    }

                    dismiss()
                    true
                }
                else -> false
            }
        }

        val typedValue = TypedValue()

        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        binding.reEditor.setEditorBackgroundColor(typedValue.data)
        binding.reEditor.setBackgroundColor(typedValue.data)

        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        binding.reEditor.setEditorFontColor(typedValue.data)

        binding.reEditor.setPlaceholder("Insert text here...");

        binding.actionUndo.setOnClickListener {
            binding.reEditor.undo()
        }
        binding.actionRedo.setOnClickListener {
            binding.reEditor.redo()
        }
        binding.actionBold.setOnClickListener {
            binding.reEditor.setBold()
        }
        binding.actionItalic.setOnClickListener {
            binding.reEditor.setItalic()
        }
        binding.actionUnderline.setOnClickListener {
            binding.reEditor.setUnderline()
        }
        binding.actionStrikethrough.setOnClickListener {
            binding.reEditor.setStrikeThrough()
        }
        binding.actionBullet.setOnClickListener {
            binding.reEditor.setBullets()
        }
        binding.actionIndentIncrease.setOnClickListener {
            binding.reEditor.setIndent()
        }
        binding.actionIndentDecrease.setOnClickListener {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: View is being destroyed, nullifying binding.")
        _binding = null
    }

    companion object {
        const val ARG_COURSE = "note_dialog_fragment_arg_course"
        const val ARG_NOTE = "note_dialog_fragment_arg_note"
        const val REQUEST_KEY_CREATE = "note_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "note_dialog_fragment_request_key_update"
        const val RESULT_KEY_NOTE = "note_dialog_fragment_result_key_note"

        fun newInstance(course: Course, note: File?): DialogFragmentNote{
            val fragment = DialogFragmentNote()
            fragment.arguments = bundleOf(
                ARG_COURSE to course,
                ARG_NOTE to note
            )
            return fragment
        }
    }
}
