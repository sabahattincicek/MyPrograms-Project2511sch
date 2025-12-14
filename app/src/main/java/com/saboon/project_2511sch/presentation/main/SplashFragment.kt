package com.saboon.project_2511sch.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.nfc.Tag
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
import androidx.navigation.fragment.findNavController
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentSplashBinding
import com.saboon.project_2511sch.presentation.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val tag = "SplashFragment"

    // İzin isteme ve sonucunu dinleme mekanizması
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            // Kullanıcı izin verdi ya da vermedi, kararını verdi.
            // Her iki durumda da artık kullanıcı durumunu kontrol edip yönlendirme yapabiliriz.
            Log.d(tag, "Permission result received: isGranted = $isGranted")
            val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
            findNavController().navigate(action)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
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
                    // İzin zaten verilmiş, direkt kullanıcı durumunu kontrol etmeye geç.
                    Log.d(tag, "Notification permission already granted.")
                    val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
                    findNavController().navigate(action)
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
                    // İlk defa izin isteniyor.
                    Log.d(tag, "Requesting notification permission for the first time.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        else {
            // Android 13'ten eski cihazlarda bu izne gerek yok.
            Log.d(tag, "OS version is below Tiramisu, no permission needed.")
            val action = SplashFragmentDirections.actionSplashFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}