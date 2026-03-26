package com.saboon.project_2511sch.presentation.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentOnboardingBinding
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.AppConstants
import com.saboon.project_2511sch.util.PermissionManager
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class FragmentOnboarding : Fragment() {

    private var _binding: FragmentOnboardingBinding?=null
    private val binding get() = _binding!!
    private val viewModelSetting: ViewModelSettings by viewModels()

    private lateinit var recyclerAdapter: RecyclerAdapterOnboarding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted){
            finishOnboarding()
        } else {
            Toast.makeText(context,
                getString(R.string.reminders_don_t_work_when_notifications_are_turned_off), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pages = listOf(
            OnboardingPage(
                title = "Derslerini Planla",
                description = "Hangi gün hangi dersin olduğunu kolayca takip et.",
                imageRes = R.drawable.ic_launcher_foreground
            ),
            OnboardingPage(
                title = "Sınavları Kaçırma",
                description = "Sınav tarihlerini ve hedeflerini belirle, biz sana hatırlatalım.",
                imageRes = R.drawable.ic_launcher_background
            ),
            OnboardingPage(
                title = "Widget Desteği",
                description = "Uygulamayı açmadan ana ekranından programına göz at.",
                imageRes = R.drawable.empty_list
            ),
            OnboardingPage(
                title ="Bildirimleri Aç",
                description = "Ders ve sınav hatırlatıcılarını alabilmek için bildirim izni vermen gerekiyor.",
                imageRes = R.drawable.baseline_home_24, // Uygun bir ikon koy
                isPermission = true
            ),
        )

        recyclerAdapter = RecyclerAdapterOnboarding(pages)
        binding.viewPager.adapter = recyclerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager){p0, p1 -> }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                if (pages[position].isPermission){
                    // notification permission page
                    binding.btnNext.text = getString(R.string.grant_permission)
                    binding.btnSkip.text = getString(R.string.keep_going_without_grant_permission)
                }else{
                    binding.btnNext.text = getString(R.string.next)
                    binding.btnSkip.text = getString(R.string.skip)
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.btnNext.text.equals(getString(R.string.next))){
                binding.viewPager.currentItem += 1
            }else{
                //grant permission and start
                checkAndRequestNotificationPermission()
            }
        }
        binding.btnSkip.setOnClickListener {
            if (binding.btnSkip.text.equals(getString(R.string.skip))){
                binding.viewPager.currentItem = pages.size - 1 // go to last (permission) page
            }else{
                //start without grant permission
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.legal_agreement_title))
        builder.setMessage(getString(R.string.by_continuing_you_agree_to_our_terms))
        builder.setPositiveButton(getString(R.string.accept_and_continue)){dialog, which ->
            viewModelSetting.isOnoardingCompleted(true)
            val action = FragmentOnboardingDirections.actionFragmentOnboardingToFragmentAboutYourself()
            findNavController().navigate(action)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.view_terms)){dialog, which ->
            val intent = Intent(Intent.ACTION_VIEW, AppConstants.SUPPORT_URL.toUri())
            startActivity(intent)
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAndRequestNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }else{
            // Android 13 altı: Sistem izni istenemez (zaten izin var kabul edilir)
            // Ama biz kullanıcıya "Bildirimleriniz açık" mesajı verip
            // bir sonraki sayfaya kaydırabiliriz.
            Toast.makeText(context, getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            binding.viewPager.currentItem += 1
        }
    }
}