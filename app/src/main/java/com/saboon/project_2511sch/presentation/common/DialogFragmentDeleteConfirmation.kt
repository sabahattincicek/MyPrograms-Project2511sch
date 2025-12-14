package com.saboon.project_2511sch.presentation.common

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saboon.project_2511sch.R

class DialogFragmentDeleteConfirmation: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARG_TITLE)?: ""
        val message = arguments?.getString(ARG_MESSAGE)?: ""

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(getString(R.string.no)){ _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to false))
            }
            .setPositiveButton(getString(R.string.yes)){ _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(RESULT_KEY to true))
            }
            .create()
    }

    companion object{
        const val REQUEST_KEY = "delete_confirmation_request"
        const val RESULT_KEY = "delete_confirmation_result"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(title: String, message: String): DialogFragmentDeleteConfirmation{
            val fragment = DialogFragmentDeleteConfirmation()
            fragment.arguments = bundleOf(
                ARG_TITLE to title,
                ARG_MESSAGE to message
            )
            return fragment
        }
    }
}