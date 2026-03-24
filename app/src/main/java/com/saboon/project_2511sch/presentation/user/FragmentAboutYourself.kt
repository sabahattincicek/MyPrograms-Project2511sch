package com.saboon.project_2511sch.presentation.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentAboutYourselfBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.OperationType
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.launch
import kotlin.getValue

class FragmentAboutYourself : Fragment() {

    private var _binding: FragmentAboutYourselfBinding?=null
    private val binding get() = _binding!!
    private val viewModelUser : ViewModelUser by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutYourselfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()

        binding.btnStart.setOnClickListener {
            val newUser = User(
                id = IdGenerator.generateId(binding.etFullName.text.toString()),
                createdBy = "",
                appVersionAtCreation = getString(R.string.app_version),
                userName = "",
                email = "",
                photoUrl = "",
                fullName = binding.etFullName.text.toString(),
                role = binding.etRole.text.toString(),
                academicLevel = binding.etAcademicLevel.text.toString(),
                institution = binding.etSchool.text.toString(),
                aboutMe = binding.etAboutMe.text.toString()
            )
            viewModelUser.insert(newUser)
        }
        binding.btnSkip.setOnClickListener {
            val newUser = User(
                id = IdGenerator.generateId("default-user"),
                createdBy = "",
                appVersionAtCreation = getString(R.string.app_version),
                userName = "",
                email = "",
                photoUrl = "",
                fullName = "",
                role = "",
                academicLevel = "",
                institution = "",
                aboutMe = ""
            )
            viewModelUser.insert(newUser)
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupObserver(){
        //USER EVENT: INSERT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val operationResult = resource.data //BaseVmOperationResult<User>
                            val user = operationResult?.data
                            val type = operationResult?.operationType
                            when(type) {
                                OperationType.INSERT -> {
                                    goToHome()
                                }
                                OperationType.UPDATE -> {}
                                OperationType.DELETE -> {}
                                null -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun goToHome(){
        val action = FragmentAboutYourselfDirections.actionFragmentAboutYourselfToFragmentHome()
        findNavController().navigate(action)
    }
}