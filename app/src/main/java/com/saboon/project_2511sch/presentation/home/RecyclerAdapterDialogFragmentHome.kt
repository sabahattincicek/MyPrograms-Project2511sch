package com.saboon.project_2511sch.presentation.home

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.domain.model.ProgramTable

class RecyclerAdapterDialogFragmentHome : ListAdapter<ProgramTable, RecyclerAdapterDialogFragmentHome.DialogFragmentHomeViewHolder>(
    DialogFragmentHomeDiffCallback()
) {

    class DialogFragmentHomeViewHolder(view: View): RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DialogFragmentHomeViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: DialogFragmentHomeViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    open class DialogFragmentHomeDiffCallback {

    }
}