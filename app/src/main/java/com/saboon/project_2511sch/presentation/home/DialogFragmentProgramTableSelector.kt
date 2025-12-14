package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentProgramTableBinding
import com.saboon.project_2511sch.databinding.DialogFragmentProgramTableSelectorBinding
import com.saboon.project_2511sch.domain.model.ProgramTable

class DialogFragmentProgramTableSelector: DialogFragment() {
    private val tag = "DialogFragmentProgramTableSelector"

    private var _binding: DialogFragmentProgramTableSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentProgramTableSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        const val ARG_PROGRAM_TABLE = "program_table_selector_dialog_fragment_arg_program_table"
        const val REQUEST_KEY_SET_ACTIVE = "program_table_selector_dialog_fragment_request_key_set_active"
        const val RESULT_KEY_PROGRAM_TABLE = "program_table_selector_dialog_fragment_result_key_program_table"

        fun newInstance(programTable: ProgramTable?): DialogFragmentProgramTableSelector{
            val fragment = DialogFragmentProgramTableSelector()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_TABLE to programTable
            )
            return fragment
        }
    }
}