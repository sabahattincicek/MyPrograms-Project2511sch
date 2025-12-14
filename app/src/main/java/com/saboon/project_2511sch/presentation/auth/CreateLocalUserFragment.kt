package com.saboon.project_2511sch.presentation.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.databinding.FragmentCreateLocalUserBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateLocalUserFragment : Fragment() {

    private var _binding: FragmentCreateLocalUserBinding ?= null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    private val TAG = "CreateLocalUserFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCreateLocalUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeCreateLocalUserEvent()

        binding.btnCreateLocalUser.setOnClickListener {
            Log.d(TAG, "btnCreateLocalUser Pressed")
            validateInputs()
        }
        binding.btnCreateRemoteUser.setOnClickListener {
            val action = CreateLocalUserFragmentDirections.actionCreateLocalUserFragmentToCreateRemoteUserFragment()
            findNavController().navigate(action)
        }
    }

    private fun observeCreateLocalUserEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                    authViewModel.createUserEvent.collect { event ->
                        when(event){
                            is Resource.Error<*> -> {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                                Log.e(TAG, event.message.toString())
                            }
                            is Resource.Idle<*> -> TODO()
                            is Resource.Loading<*> -> TODO()
                            is Resource.Success<*> -> {
                                Toast.makeText(requireContext(), "User created successfully", Toast.LENGTH_LONG).show()
                                Log.d(TAG, "user created successfully")

                            }
                        }
                    }
            }
        }
    }

    private fun validateInputs(){
        var isValid = true

        val firstName = binding.etFirstName.text.toString().trim()
        val secondName = binding.etSecondName.text.toString().trim()
        val role = binding.etUserRole.text.toString().trim()
        val organization = binding.etOrganization.text.toString().trim()

        if(firstName.isEmpty()){
            binding.tilFirstName.error = "This field can't be empty"
            binding.tilFirstName.isErrorEnabled = true
            isValid = false
        }
        else {
            binding.tilFirstName.error = null
            binding.tilFirstName.isErrorEnabled = false
            isValid = true
        }

        if(isValid){
            val newUser = User(
                id = IdGenerator.generateUserId(firstName),
                authProviderId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false,
                rowVersion = 1,
                isActive = true,
                email = null,
                userName = null,
                firstName = firstName,
                secondName = secondName,
                photoUrl = null,
                userRole = role,
                academicLevel = null,
                organization = organization,
                lastLoginAt = null,
                lastLoginIp = null,
                isVerified = false,
            )
            authViewModel.createLocalUser(newUser)
        }
    }

    private fun showRemoteUserDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remote User?")
            .setMessage("Creating a remote user allows you to sync your data across multiple devices and access your account from anywhere. This requires an internet connection for initial setup and synchronization.")
            .setCancelable(false)
            .setPositiveButton("Yes"){dialog, which ->
                Log.d(TAG, "User choose created remote user")
                val action = CreateLocalUserFragmentDirections.actionCreateLocalUserFragmentToCreateRemoteUserFragment()
                findNavController().navigate(action)
            }
            .setNegativeButton("Later"){dialog, which ->
                Log.d(TAG, "User choose dismiss create remote user")
                dialog.dismiss()
            }
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}