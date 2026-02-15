package com.saboon.project_2511sch.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentSplashBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.IdGenerator
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val tag = "SplashFragment"

    private val viewModelUser : ViewModelUser by activityViewModels()

    private var isPermissionProcessDone = false
    private var isUserReady = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            Log.d(tag, "Permission result received: isGranted = $isGranted")
            onPermissionProcessFinished()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated called")
        checkNotificationPermission()
        setupObservers()
    }
    private fun checkNotificationPermission() {
        Log.d(tag, "checkNotificationPermission started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(tag, "Notification permission already granted")
                    onPermissionProcessFinished()
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    Log.d(tag, "Showing notification permission rationale")
                    requestPermissionLauncher.launch(permission)
                }
                else -> {
                    Log.d(tag, "Requesting notification permission")
                    requestPermissionLauncher.launch(permission)
                }
            }
        } else {
            Log.d(tag, "SDK version < Tiramisu, skipping notification permission")
            onPermissionProcessFinished()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    private fun onPermissionProcessFinished() {
        Log.d(tag, "onPermissionProcessFinished")
        isPermissionProcessDone = true
        tryFinalNavigation()
    }
    private fun tryFinalNavigation() {
        Log.d(tag, "tryFinalNavigation: isPermissionProcessDone=$isPermissionProcessDone, isUserReady=$isUserReady")
        if (isPermissionProcessDone && isUserReady) {
            if (findNavController().currentDestination?.id == R.id.splashFragment) {
                Log.d(tag, "Navigating to HomeFragment")
                val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                findNavController().navigate(action)
            } else {
                Log.d(tag, "Navigation condition met but current destination is not SplashFragment")
            }
        }
    }

    private fun setupObservers(){
        Log.d(tag, "setupObservers called")
        //STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.currentUser.collect { resource ->
                    Log.d(tag, "currentUser resource state: ${resource::class.simpleName}")
                    when(resource) {
                        is Resource.Error -> {
                            Log.e(tag, "currentUser Error: ${resource.message}")
                        }
                        is Resource.Idle -> {
                            Log.d(tag, "currentUser Idle")
                        }
                        is Resource.Loading -> {
                            Log.d(tag, "currentUser Loading")
                        }
                        is Resource.Success -> {
                            Log.d(tag, "currentUser Success - data is ${if (resource.data != null) "not null" else "null"}")
                            if (resource.data != null){
                                isUserReady = true
                                tryFinalNavigation()
                            }else{
                                Log.d(tag, "User not found, generating a new one")
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
                                    organisation = "",
                                    aboutMe = ""
                                )
                                viewModelUser.insert(newUser)
                            }
                        }
                    }
                }
            }
        }
        //USER EVENT: INSERT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.operationEvent.collect { resource ->
                    Log.d(tag, "operationEvent resource state: ${resource::class.simpleName}")
                    when(resource) {
                        is Resource.Error -> {
                            Log.e(tag, "operationEvent Error: ${resource.message}")
                        }
                        is Resource.Idle -> {
                            Log.d(tag, "operationEvent Idle")
                        }
                        is Resource.Loading -> {
                            Log.d(tag, "operationEvent Loading")
                        }
                        is Resource.Success -> {
                            Log.d(tag, "operationEvent Success - User inserted")
                            isUserReady = true
                            tryFinalNavigation()
                        }
                    }
                }
            }
        }
    }
}
