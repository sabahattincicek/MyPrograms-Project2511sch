package com.saboon.project_2511sch.presentation.course

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentCourseListBinding
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.tag.DialogFragmentManageTag
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class FragmentCourseList : Fragment() {

    private var _binding: FragmentCourseListBinding?=null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()
    private lateinit var recyclerAdapterCourse: RecyclerAdapterCourse
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupObservers()

        binding.fabAdd.setOnClickListener {
            val dialog = DialogFragmentCourse.newInstanceForCreate(currentUser)
            dialog.show(childFragmentManager, "dialog fragment course")
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_manage -> {
                    val dialog = DialogFragmentManageTag.newInstanceForManage()
                    dialog.show(childFragmentManager, "DialogFragmentManageTag")
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
        recyclerAdapterCourse = RecyclerAdapterCourse()
        recyclerAdapterCourse.onItemClickListener = { course ->
            val action = FragmentCourseListDirections.actionFragmentCourseListToCourseDetailsFragment(course.id)
            findNavController().navigate(action)
        }
        binding.rvCourses.apply {
            adapter = recyclerAdapterCourse
            layoutManager = LinearLayoutManager(context)
        }
    }
    private fun setupObservers(){
        //USER STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelUser.currentUser.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            currentUser = resource.data!!
                        }
                    }
                }
            }
        }
        //COURSES STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.coursesState.collect { resource ->
                    when(resource) {
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading -> {}
                        is Resource.Success -> {
                            val courseDisplayItemList = resource.data
                            if (courseDisplayItemList.isNullOrEmpty()){
                                binding.llEmptyList.visibility = View.VISIBLE
                                binding.rvCourses.visibility = View.GONE
                            }else{
                                binding.llEmptyList.visibility = View.GONE
                                binding.rvCourses.visibility = View.VISIBLE

                                recyclerAdapterCourse.submitList(courseDisplayItemList)
                            }
                        }
                    }
                }
            }
        }
    }
}