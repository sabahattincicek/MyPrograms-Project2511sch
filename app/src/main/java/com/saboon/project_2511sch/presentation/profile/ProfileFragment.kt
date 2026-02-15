package com.saboon.project_2511sch.presentation.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.setText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import coil3.request.crossfade
import com.saboon.project_2511sch.databinding.FragmentProfileBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? =null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()

    private lateinit var currentUser: User
    private var isInitialDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        binding.etFullName.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.fullName) {
                viewModelUser.update(currentUser.copy(fullName = text))
            }
        }
        binding.etRole.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.role) {
                viewModelUser.update(currentUser.copy(role = text))
            }
        }
        binding.etAcademicLevel.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.academicLevel) {
                viewModelUser.update(currentUser.copy(academicLevel = text))
            }
        }
        binding.etOrganization.doAfterTextChanged {
            val text = it.toString().trim()
            if (text != currentUser.organisation) {
                viewModelUser.update(currentUser.copy(organisation = text))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun applyDataToView(){
        binding.ivProfilePicture.load(File(currentUser.photoUrl)){
            crossfade(true)
        }
        if (!isInitialDataLoaded) {
            binding.etFullName.setText(currentUser.fullName)
            binding.etRole.setText(currentUser.role)
            binding.etAcademicLevel.setText(currentUser.academicLevel)
            binding.etOrganization.setText(currentUser.organisation)
            isInitialDataLoaded = true
        }
    }
    private fun setupObservers(){
        //USER STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.currentUser.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            currentUser = resource.data!!
                            applyDataToView()
                        }
                    }
                }
            }
        }
        //USER EVENT: UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {}
                    }
                }
            }
        }
    }
}