package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentHomeFilterSelectorBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class DialogFragmentCourseSelector: DialogFragment() {
    private val tag = "DialogFragmentCourseSelector"

    private var _binding: DialogFragmentHomeFilterSelectorBinding ?= null
    private val binding get() = _binding!!

    private lateinit var recyclerAdapterDialogFragmentSelector: RecyclerAdapterDialogFragmentSelector

    private val viewModelCourse: ViewModelCourse by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentHomeFilterSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerAdapter()
        observeProgramTablesState()
        viewModelCourse.updateFilter(null)
        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupRecyclerAdapter(){
        recyclerAdapterDialogFragmentSelector = RecyclerAdapterDialogFragmentSelector()
        recyclerAdapterDialogFragmentSelector.onItemCheckedChangeListener = { isChecked, baseModel ->
            if (baseModel is Course){
                viewModelCourse.activationById(baseModel.id, isChecked)
            }
        }
        binding.rvSelector.apply {
            adapter = recyclerAdapterDialogFragmentSelector
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeProgramTablesState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.coursesState.collect { resource ->
                    when (resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            resource.data?.let {
//                                recyclerAdapterDialogFragmentSelector.submitList(resource.data)
                            }
                        }
                    }
                }
            }
        }
    }

}