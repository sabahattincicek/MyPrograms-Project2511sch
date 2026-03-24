package com.saboon.project_2511sch.presentation.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentSplashBinding
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentSplash : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by viewModels()
    private val viewModelSettings: ViewModelSettings by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObserves()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupObserves() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val userFlow = viewModelUser.currentUser.filter { it is Resource.Success || it is Resource.Error }
                val onboardingFlow = viewModelSettings.onboardingCompletedState

                combine(userFlow, onboardingFlow) { userRes, onboardingDone ->
                    userRes to onboardingDone
                }.first().let { (userRes, onboardingDone) ->

                    val userExist = userRes.data != null

                    // 2. Navigasyon Çökmesini Engellemek İçin Güvenlik Kontrolü
                    if (findNavController().currentDestination?.id == R.id.fragmentSplash) {
                        when {
                            // Senin isteğin: Onboarding bitmemişse VEYA User yoksa Onboarding'e git
                            !onboardingDone || !userExist -> {
                                findNavController().navigate(
                                    FragmentSplashDirections.actionSplashFragmentToFragmentOnboarding()
                                )
                            }
                            // Her ikisi de tamsa Home'a git
                            else -> {
                                findNavController().navigate(
                                    FragmentSplashDirections.actionSplashFragmentToFragmentHome()
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}
