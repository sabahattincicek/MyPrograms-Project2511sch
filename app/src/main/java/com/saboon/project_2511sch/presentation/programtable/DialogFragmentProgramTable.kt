package com.saboon.project_2511sch.presentation.programtable

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentProgramTableBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.ModelColors
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class DialogFragmentProgramTable: DialogFragment() {

    private val TAG = "CreateProgramTableDialog"

    private var _binding: DialogFragmentProgramTableBinding? = null
    private val binding get() = _binding!!
    private val viewModelProgramTable: ViewModelProgramTable by viewModels()
    private lateinit var currentUser: User
    private var programTable: ProgramTable? = null
    private var color: String = ModelColors.MODEL_COLOR_1

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        arguments?.let {
            currentUser = BundleCompat.getParcelable(it, ARG_PROGRAM_USER, User::class.java)!!
            programTable = BundleCompat.getParcelable(it,ARG_PROGRAM_TABLE, ProgramTable::class.java)
        }

        setupObservers()

        val isEditMode = programTable != null
        if (isEditMode){
            binding.etTitle.setText(programTable!!.title)
            binding.etDescription.setText(programTable!!.description)
            color = programTable!!.color
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
            if(isEditMode){
                val updatedProgramTable = programTable!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = color
                )

                viewModelProgramTable.update(updatedProgramTable)
            }else{
                val newProgramTable = ProgramTable(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = color,
                )
                viewModelProgramTable.insert(newProgramTable)
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
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.d(TAG, "onDestroy: called, binding set to null")
    }
    private fun setupObservers(){
        //PROFRAM TABLE EVENT: INSERT, UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelProgramTable.operationEvent.collect { event ->
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

    companion object {
        const val ARG_PROGRAM_USER = "program_table_dialog_fragment_arg_user"
        const val ARG_PROGRAM_TABLE = "program_table_dialog_fragment_arg_program_table"

        fun newInstanceForCreate(user: User):DialogFragmentProgramTable{
            val fragment = DialogFragmentProgramTable()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_USER to user
            )
            return fragment
        }
        fun newInstanceForUpdate(user: User, programTable: ProgramTable): DialogFragmentProgramTable {
            val fragment = DialogFragmentProgramTable()
            fragment.arguments = bundleOf(
                ARG_PROGRAM_USER to user,
                ARG_PROGRAM_TABLE to programTable
            )
            return fragment
        }
    }
}