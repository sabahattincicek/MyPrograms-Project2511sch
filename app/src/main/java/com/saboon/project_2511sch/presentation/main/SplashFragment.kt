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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
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

    private val viewModelUser : ViewModelUser by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            Log.d(tag, "Permission result received: isGranted = $isGranted")
            viewModelUser.getActive()
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
        checkNotificationPermission()
    }
    private fun checkNotificationPermission(){
        // Sadece Android 13 (Tiramisu) ve üzeri için bildirim izni gerekir.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            when{
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(tag, "Notification permission already granted.")
                    viewModelUser.getActive()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Kullanıcı daha önce izni reddetmiş. Neden önemli olduğunu
                    // açıklayan bir diyalog göstermek en iyi pratiktir.
                    // Şimdilik direkt tekrar izin istiyoruz.
                    Log.d(tag, "Showing rationale and requesting permission again.")
                    Toast.makeText(context, "Notification permission is needed for reminders.", Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.d(tag, "Requesting notification permission for the first time.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        else {
            Log.d(tag, "OS version is below Tiramisu, no permission needed.")
            viewModelUser.getActive()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupObservers(){
        //STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.userState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val user = resource.data
                            if (user != null){
                                val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                                findNavController().navigate(action)
                            }else{
                                val newUser = User(
                                    id = IdGenerator.generateId("default-user"),
                                    createdBy = "",
                                    appVersionAtCreation = getString(R.string.app_version),
                                    updatedBy = "",
                                    userName = "",
                                    email = "",
                                    photoUrl = "",
                                    fullName = "",
                                    role = "",
                                    academicLevel = "",
                                    organisation = ""
                                )
                                viewModelUser.insert(newUser)
                            }
                        }
                    }
                }
            }
        }
        //EVENT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.insertEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }
}