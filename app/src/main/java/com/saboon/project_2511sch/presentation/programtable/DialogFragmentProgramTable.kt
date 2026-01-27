package com.saboon.project_2511sch.presentation.programtable

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentProgramTableBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.ModelColors

class DialogFragmentProgramTable: DialogFragment() {

    private val TAG = "CreateProgramTableDialog"

    private var _binding: DialogFragmentProgramTableBinding? = null
    private val binding get() = _binding!!

    private val allRadioButtonIds = listOf(
        R.id.radio_color1, R.id.radio_color2, R.id.radio_color3, R.id.radio_color4,
        R.id.radio_color5, R.id.radio_color6, R.id.radio_color7, R.id.radio_color8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: called")
        _binding = DialogFragmentProgramTableBinding.inflate(inflater, container, false)

        val programTable = arguments?.let{BundleCompat.getParcelable(it,ARG_PROGRAM_TABLE, ProgramTable::class.java)}
        var color: String = ModelColors.MODEL_COLOR_1

        val allRadioButtons = allRadioButtonIds.map{ binding.root.findViewById<RadioButton>(it)}
        //burasi radio buttonlardan biri secilince digerlerini false yapmak icin var
        allRadioButtons.forEach { radioButton ->
            radioButton.setOnClickListener { clickedButton ->
                allRadioButtons.forEach { otherButton ->
                    if(otherButton.id != clickedButton.id){
                        otherButton.isChecked = false
                    }
                }
                val selectedButton = clickedButton as RadioButton
                selectedButton.isChecked = true
                color = selectedButton.tag.toString()
            }
        }

        val isEditMode = programTable != null

        if (isEditMode){
            binding.etTitle.setText(programTable.title)
            binding.etDescription.setText(programTable.description)
            //burasi database de secili olan rengi radio buttonlarda hangisine uyuyorsa onu secen kod
            allRadioButtons.forEach { it.isChecked = false }
            val radioButtonToSelect = allRadioButtons.find { it.tag.toString() == color }
            if (radioButtonToSelect != null) {
                radioButtonToSelect.isChecked = true
            } else {
                Log.w(TAG, "onCreateView: No radio button found for color '$color'. Defaulting to the first one (red).")
                allRadioButtons.firstOrNull()?.isChecked = true
                color = allRadioButtons.firstOrNull()?.tag?.toString() ?: ModelColors.MODEL_COLOR_1
            }
        }

        binding.btnSave.setOnClickListener {
            if(isEditMode){
                val updatedProgramTable = programTable.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = color
                )

                setFragmentResult(REQUEST_KEY_UPDATE, bundleOf(
                    RESULT_KEY_PROGRAM_TABLE to updatedProgramTable
                ))
                dismiss()
            }else{
                val newProgramTable = ProgramTable(
                    id = IdGenerator.generateProgramTableId(binding.etTitle.text.toString()),
                    appVersionAtCreation = getString(R.string.app_version),
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = color,
                )

                setFragmentResult(REQUEST_KEY_CREATE, bundleOf(
                    RESULT_KEY_PROGRAM_TABLE to newProgramTable
                ))
                dismiss()
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.d(TAG, "onDestroy: called, binding set to null")
    }

    companion object {
        const val ARG_PROGRAM_TABLE = "program_table_dialog_fragment_arg_program_table"
        const val REQUEST_KEY_CREATE = "program_table_dialog_fragment_request_key_create"
        const val REQUEST_KEY_UPDATE = "program_table_dialog_fragment_request_key_update"
        const val RESULT_KEY_PROGRAM_TABLE = "program_table_dialog_fragment_result_key_program_table"

        fun newInstance(programTable: ProgramTable?): DialogFragmentProgramTable {
            val fragment = DialogFragmentProgramTable()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_TABLE to programTable
            )
            return fragment
        }
    }
}