package com.saboon.project_2511sch.presentation.course

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentCourseBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CourseFragment : Fragment() {

    private val TAG = "CourseFragment"

    private var _binding: FragmentCourseBinding? = null
    private val binding get() = _binding!!

    private val args : CourseFragmentArgs by navArgs()

    private val viewModelCourse: ViewModelCourse by viewModels()
    private lateinit var recyclerAdapterCourse: RecyclerAdapterCourse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment created.")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: View created.")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View is created and UI is being set up.")

        (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)

        setupRecyclerAdapter()
        observeCoursesState()
        observeInsertNewCourseEvent()

        viewModelCourse.getCoursesWithProgramTableId(args.programTable.id)
        Log.d(TAG, "onViewCreated: Requesting courses for program table id: ${args.programTable.id}")

        childFragmentManager.setFragmentResultListener(DialogFragmentCourse.REQUEST_KEY_CREATE, this){ requestKey, result ->
            val newCourse = BundleCompat.getParcelable(result,DialogFragmentCourse.RESULT_KEY_COURSE, Course::class.java)
            Log.d(TAG, "onViewCreated: Fragment result received for request key: $requestKey")
            if(newCourse != null){
                Log.d(TAG, "onViewCreated: New course received from dialog: $newCourse")
                viewModelCourse.insertNewCourse(newCourse)
            }else{
                Log.w(TAG, "onViewCreated: Received null course from dialog.")
            }
        }


        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {
                menuInflater.inflate(R.menu.menu_action_add, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.action_add -> {
                        Log.d(TAG, "onMenuItemSelected: Add action clicked.")
                        val dialog = DialogFragmentCourse.newInstance(args.programTable, null)
                        dialog.show(childFragmentManager, "CreateCourseFragmentDialog")
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.topAppBar.setNavigationOnClickListener {
            Log.d(TAG, "onViewCreated: Navigation back button clicked.")
            findNavController().popBackStack()
        }

        binding.topAppBar.subtitle = args.programTable.title
    }

    private fun observeInsertNewCourseEvent(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.insertNewCourseEvent.collect { event ->
                    when(event) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "observeInsertNewCourseEvent: Error inserting new course: ${event.message}")
                            Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> { Log.d(TAG, "observeInsertNewCourseEvent: Idle state.")}
                        is Resource.Loading<*> -> {Log.d(TAG, "observeInsertNewCourseEvent: Loading...")}
                        is Resource.Success -> {
                            Log.d(TAG, "observeInsertNewCourseEvent: Course added successfully.")
                            Toast.makeText(context, getString(R.string.course_added_successfully), Toast.LENGTH_SHORT).show()
                            val action = CourseFragmentDirections.actionCourseFragmentToCourseDetailsFragment(args.programTable, event.data!!)
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }

    private fun observeCoursesState(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelCourse.coursesState.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {
                            Log.e(TAG, "observeCoursesState: Error fetching courses: ${resource.message}")
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Idle<*> -> {Log.d(TAG, "observeCoursesState: Idle state.")}
                        is Resource.Loading<*> -> {Log.d(TAG, "observeCoursesState: Loading courses...")}
                        is Resource.Success<*> -> {
                            Log.d(TAG, "observeCoursesState: Successfully fetched courses. Count: ${resource.data?.size}")
                            recyclerAdapterCourse.submitList(resource.data)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerAdapter(){
        Log.d(TAG, "setupRecyclerAdapter: Setting up RecyclerView adapter.")
        recyclerAdapterCourse = RecyclerAdapterCourse()
        recyclerAdapterCourse.onItemClickListener = { course ->
            Log.d(TAG, "setupRecyclerAdapter: Course item clicked: ${course.title}")
            val action = CourseFragmentDirections.actionCourseFragmentToCourseDetailsFragment(args.programTable, course)
            findNavController().navigate(action)
        }
        binding.programRecyclerView.apply {
            adapter = recyclerAdapterCourse
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.d(TAG, "onDestroy: Fragment destroyed.")
    }
}