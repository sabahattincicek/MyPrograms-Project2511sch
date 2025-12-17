package com.saboon.project_2511sch.presentation.file

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentFileBinding
import com.saboon.project_2511sch.domain.model.Course

class FileFragment : Fragment() {

    private var _binding: FragmentFileBinding?=null
    private val binding get() = _binding!!

    private val args : FileFragmentArgs by navArgs()

    private lateinit var course: Course

    private val viewModelFile : ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        course = args.course
        binding.toolbar.subtitle = course.title

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.fabAddNewFile.setOnClickListener { anchorView ->
            val popup = PopupMenu(requireContext(), anchorView)
            popup.menuInflater.inflate(R.menu.add_file_menu, popup.menu)
            popup.setOnMenuItemClickListener { item -> 
                when(item.itemId){
                    R.id.action_add_file -> {
                        // TODO: add necessary code for "add file" option.
                        true
                    }
                    R.id.action_add_note -> {
                        // TODO: add necessary code for "add note" option
                        true
                    }
                    R.id.action_add_link -> {
                        // TODO: add necessary code for "add link" option
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}