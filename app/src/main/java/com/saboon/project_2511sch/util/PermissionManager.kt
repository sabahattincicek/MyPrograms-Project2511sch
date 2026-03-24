package com.saboon.project_2511sch.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.saboon.project_2511sch.R

object PermissionManager {

    object NotificationPermission {
        fun isPermissionGranted(context: Context): Boolean{
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }else {
                true
            }
        }
        fun showPermissionRationale(
            fragment: Fragment,
            onNegativeClick: (() -> Unit)? = null
        ){
            val context = fragment.requireContext()
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.notification_permission_required))
            builder.setMessage(context.getString(R.string.should_grant_notification_permission_for_reminder))
            builder.setPositiveButton(context.getString(R.string.go_to_settings)){ dialog, which ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                fragment.startActivity(intent)
            }
            builder.setNegativeButton(context.getString(R.string.cancel)){ dialog, which ->
                onNegativeClick?.invoke()
                dialog.dismiss()
            }
            builder.show()
        }
    }
}