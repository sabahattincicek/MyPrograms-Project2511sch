package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentFileBinding
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.launch


class FileFragment : Fragment() {

    private var _binding : FragmentFileBinding?=null
    private val binding get() = _binding!!

    private val viewModelFile: ViewModelFile by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun observeFilesState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelFile.filesState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {
                            // TODO: set file list to recycler adapter
                        }
                    }
                }
            }
        }
    }
}