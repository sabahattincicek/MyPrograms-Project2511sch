package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.FragmentHomeBinding
import com.saboon.project_2511sch.domain.model.BaseModel
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.common.DialogFragmentFilter
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.presentation.common.FilterTask
import com.saboon.project_2511sch.presentation.course.ViewModelCourse
import com.saboon.project_2511sch.presentation.programtable.ViewModelProgramTable
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModelHome: ViewModelHome by viewModels()
    private val viewModelProgramTable: ViewModelProgramTable by viewModels()
    private val viewModelCourse: ViewModelCourse by viewModels()

    private var filteredProgramTable: ProgramTable? = null
    private var filteredCourse: Course? = null

    private lateinit var recyclerAdapterHome: RecyclerAdapterHome

    private val tag = "HomeFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate: Fragment is being created.")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(tag, "onCreateView: Layout is being inflated.")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated: View has been created. Setting up UI and observers.")

        setupRecyclerAdapter()
        setupListeners()
        observeHomeDisplayItemsState()

        viewModelHome.loadCurrentWeek()

        ////////////////////////////////////////////////
        binding.cpProgramTable.setOnClickListener {
            binding.cpProgramTable.isChecked = !binding.cpProgramTable.isChecked
            val dialog = DialogFragmentFilter.newInstanceFilterProgramTable()
            dialog.show(childFragmentManager, "DialogFragmentFileFilter")
        }
        binding.cpProgramTable.setOnCloseIconClickListener {
            filteredProgramTable = null
           viewModelHome.updateFilterProgramTable(filteredProgramTable)

            binding.cpProgramTable.isChecked = false
            binding.cpProgramTable.isCloseIconVisible = false
            binding.cpProgramTable.text = getString(R.string.program_table)
        }
        binding.cpCourse.setOnClickListener {
            binding.cpCourse.isChecked = !binding.cpCourse.isChecked
            if(filteredProgramTable != null){
                val dialog = DialogFragmentFilter.newInstanceFilterCourse(filteredProgramTable!!)
                dialog.show(childFragmentManager, "DialogFragmentFileFilter")
            }else{
//                val shake = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
//                binding.cpProgramTable.startAnimation(shake)
            }
        }
        binding.cpCourse.setOnCloseIconClickListener {
            filteredCourse = null
            viewModelHome.updateFilterCourse(filteredCourse)

            binding.cpCourse.isChecked = false
            binding.cpCourse.isCloseIconVisible = false
            binding.cpCourse.text = getString(R.string.course)
        }
        ////////////////////////////////////////////////
        binding.cpLesson.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpLesson checked state changed: $isChecked")
            if (!binding.cpLesson.isChecked && !binding.cpExam.isChecked && !binding.cpHomework.isChecked){
                viewModelHome.updateFilterTask(FilterTask())
            }else{
                val newFilter = FilterTask(
                    lesson = isChecked,
                    exam = binding.cpExam.isChecked,
                    homework = binding.cpHomework.isChecked
                )
                viewModelHome.updateFilterTask(newFilter)
            }
        }
        binding.cpExam.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpExam checked state changed: $isChecked")
            if (!binding.cpLesson.isChecked && !binding.cpExam.isChecked && !binding.cpHomework.isChecked){
                viewModelHome.updateFilterTask(FilterTask())
            }else{
                val newFilter = FilterTask(
                    lesson = binding.cpLesson.isChecked,
                    exam = isChecked,
                    homework = binding.cpHomework.isChecked
                )
                viewModelHome.updateFilterTask(newFilter)
            }
        }
        binding.cpHomework.setOnCheckedChangeListener { _, isChecked ->
            Log.d(tag, "cpHomework checked state changed: $isChecked")
            if (!binding.cpLesson.isChecked && !binding.cpExam.isChecked && !binding.cpHomework.isChecked){
                viewModelHome.updateFilterTask(FilterTask())
            }else{
                val newFilter = FilterTask(
                    lesson = binding.cpLesson.isChecked,
                    exam = binding.cpExam.isChecked,
                    homework = isChecked
                )
                viewModelHome.updateFilterTask(newFilter)
            }
        }
        binding.osaOverScroll.onActionTriggered = {isTop ->
            if (isTop) {
                Log.d(tag, "overscroll triggered: Top")
                viewModelHome.loadPrevious()
            } else {
                Log.d(tag, "overscroll triggered: Bottom")
                viewModelHome.loadNext()
            }
        }
        binding.rvHome.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisible = layoutManager.findFirstVisibleItemPosition()
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                val todayPosition = recyclerAdapterHome.getTodayPosition()

                if (todayPosition != -1 && (todayPosition !in firstVisible..lastVisible)){
                    binding.btnScrollToday.visibility = View.VISIBLE
                }else{
                    binding.btnScrollToday.visibility = View.GONE
                }
            }
        })
        binding.btnScrollToday.setOnClickListener {
            val todayPosition = recyclerAdapterHome.getTodayPosition()
            if (todayPosition != -1){
                val layoutManager = binding.rvHome.layoutManager as LinearLayoutManager
                val offset = binding.rvHome.height / 3
                layoutManager.scrollToPositionWithOffset(todayPosition, offset)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tag, "onDestroyView: View is being destroyed, nullifying binding to prevent memory leaks.")
        _binding = null
    }
    private fun observeHomeDisplayItemsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(tag, "Subscribing to displayItemsState flow.")
                viewModelHome.displayItemsState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Log.e(tag, "displayItemsState: Error - ${result.message}")
                        }
                        is Resource.Idle<*> -> {
                            Log.d(tag, "displayItemsState: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Log.d(tag, "displayItemsState: Loading.")
                        }
                        is Resource.Success<*> -> {
                            val homeDisplayItemList = result.data
                            recyclerAdapterHome.submitList(homeDisplayItemList)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerAdapter() {
        Log.d(tag, "setupRecyclerAdapter: Initializing RecyclerAdapterHome.")
        recyclerAdapterHome = RecyclerAdapterHome()
        binding.rvHome.apply {
            adapter = recyclerAdapterHome
            layoutManager = LinearLayoutManager(context)
        }
        recyclerAdapterHome.onItemClickListener = { programTable, course ->
            Log.d(tag, "Recycler item clicked. Course: ${course.title}")
            val action = HomeFragmentDirections.actionHomeFragmentToCourseDetailsFragment(programTable, course)
            findNavController().navigate(action)
        }
    }

    private fun setupListeners(){
        childFragmentManager.setFragmentResultListener(DialogFragmentFilter.REQUEST_KEY_BASE_MODEL, viewLifecycleOwner){ requestKey, result ->
            val baseModel = BundleCompat.getParcelable(result, DialogFragmentFilter.RESULT_KEY_BASE_MODEL,BaseModel::class.java)
            Log.d(tag, "FragmentResultListener: Filter result received - Type: ${baseModel?.javaClass?.simpleName}")
            when(baseModel){
                is ProgramTable -> {
                    filteredProgramTable = baseModel
                    viewModelHome.updateFilterProgramTable(filteredProgramTable)

                    binding.cpProgramTable.isChecked = true
                    binding.cpProgramTable.isCloseIconVisible = true
                    binding.cpProgramTable.text = filteredProgramTable!!.title

                    binding.cpCourse.isChecked = false
                    binding.cpCourse.isCloseIconVisible = false
                    binding.cpCourse.text = getString(R.string.course)
                }
                is Course -> {
                    filteredCourse = baseModel
                    viewModelHome.updateFilterCourse(filteredCourse)

                    binding.cpCourse.isChecked = true
                    binding.cpCourse.isCloseIconVisible = true
                    binding.cpCourse.text = filteredCourse!!.title
                }
            }
        }
    }
}
