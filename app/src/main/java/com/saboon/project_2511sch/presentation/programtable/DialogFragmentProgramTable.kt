package com.saboon.project_2511sch.presentation.programtable

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentProgramTableBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.ModelColor
import com.saboon.project_2511sch.util.ModelColorConstats
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
    private var selectedColor =  ModelColor()

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

        setupColorCheckers()
        setupObservers()

        val isEditMode = programTable != null
        if (isEditMode){
            binding.etTitle.setText(programTable!!.title)
            binding.etDescription.setText(programTable!!.description)
            selectedColor = programTable!!.color
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
            if(isEditMode){
                val updatedProgramTable = programTable!!.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = selectedColor
                )

                viewModelProgramTable.update(updatedProgramTable)
            }else{
                val newProgramTable = ProgramTable(
                    id = IdGenerator.generateId(binding.etTitle.text.toString()),
                    createdBy = currentUser.id,
                    appVersionAtCreation = getString(R.string.app_version),
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    color = selectedColor,
                )
                viewModelProgramTable.insert(newProgramTable)
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