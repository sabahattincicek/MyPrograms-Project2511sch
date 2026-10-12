package com.saboon.project_2511sch.presentation.home

import android.os.Bundle
import timber.log.Timber
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
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.FragmentHomeBinding
import com.saboon.project_2511sch.presentation.task.AbsenceDialogs
import com.saboon.project_2511sch.presentation.task.FilterTask
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.presentation.task.ViewModelTask
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home screen (presentation layer): the timeline of upcoming and past tasks.
 *
 * Shows the tasks of all active courses grouped by day, for a date range that grows when the
 * user overscrolls at the top or bottom. Chips filter the list by task type. Tapping a row
 * opens the course details; swiping a lesson row reveals the absence buttons, where "+" asks
 * for the date of the absence and "-" asks which recorded absence to remove.
 *
 * Data comes from the activity-scoped [ViewModelHome]; absence changes go through
 * [ViewModelTask] and display settings come from [ViewModelSettings].
 */
@AndroidEntryPoint
class FragmentHome : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModelHome: ViewModelHome by activityViewModels()
    private val viewModelTask: ViewModelTask by viewModels()
    private val viewModelSettings: ViewModelSettings by viewModels()
    private lateinit var recyclerAdapterHome: RecyclerAdapterHome
    private var overscrollDaysCount = SettingsConstants.OverscrollDaysCount.DEFAULT


    /**
     * Logs fragment creation; no state is restored here.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate: Fragment is being created.")
    }

    /**
     * Inflates the layout through view binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.d("onCreateView: Layout is being inflated.")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up the list and observers, loads the timeline and wires the filter chips,
     * overscroll paging and the "scroll to today" button.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated: View has been created. Setting up UI and observers.")

        setupRecyclerAdapter()
        setupObservers()

        viewModelHome.loadData()

        binding.cpLesson.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("cpLesson checked state changed: $isChecked")
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
            Timber.d("cpExam checked state changed: $isChecked")
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
            Timber.d("cpHomework checked state changed: $isChecked")
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
                Timber.d("overscroll triggered: Top")
                viewModelHome.loadPrevious(overscrollDaysCount)
            } else {
                Timber.d("overscroll triggered: Bottom")
                viewModelHome.loadNext(overscrollDaysCount)
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

    /**
     * Releases the view binding.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("onDestroyView: View is being destroyed, nullifying binding to prevent memory leaks.")
        _binding = null
    }

    /**
     * Collects the timeline items and the display settings while the view is at least STARTED.
     */
    private fun setupObservers(){
        // DISPLAY ITEMS STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Timber.d("Subscribing to displayItemsState flow.")
                viewModelHome.displayItemsState.collect { result ->
                    when (result) {
                        is Resource.Error<*> -> {
                            Timber.e("displayItemsState: Error - ${result.message}")
                            binding.shimmerViewContainer.visibility = View.GONE
                            binding.shimmerViewContainer.stopShimmer()
                        }
                        is Resource.Idle<*> -> {
                            Timber.d("displayItemsState: Idle.")
                        }
                        is Resource.Loading<*> -> {
                            Timber.d("displayItemsState: Loading.")
                            binding.shimmerViewContainer.visibility = View.VISIBLE
                            binding.shimmerViewContainer.startShimmer()
                            binding.llEmptyList.visibility = View.GONE
                            binding.osaOverScroll.visibility = View.GONE

                        }
                        is Resource.Success<*> -> {
                            binding.shimmerViewContainer.visibility = View.GONE
                            binding.shimmerViewContainer.stopShimmer()

                            val homeDisplayItemList = result.data
                            if (homeDisplayItemList.isNullOrEmpty()){
                                binding.llEmptyList.visibility = View.VISIBLE
                                binding.shimmerViewContainer.visibility = View.GONE
                                binding.osaOverScroll.visibility = View.GONE
                            }else{
                                binding.llEmptyList.visibility = View.GONE
                                binding.osaOverScroll.visibility = View.VISIBLE
                                recyclerAdapterHome.submitList(homeDisplayItemList)
                            }
                        }
                    }
                }
            }
        }
        // TASK LESSON UPDATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTask.operationEvent.collect { resource ->
                    when(resource) {
                        is Resource.Error<*> -> {}
                        is Resource.Idle<*> -> {}
                        is Resource.Loading<*> -> {}
                        is Resource.Success<*> -> {}
                    }
                }
            }
        }
        //OVERSCROLL DAYS COUNT
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelSettings.overScrollDaysCountState.collect { daysCount ->
                    overscrollDaysCount = daysCount
                }
            }
        }
        //HOME LIST ITEM COLOR ENABLED
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelSettings.homeListItemColorEnabledState.collect { isEnabled ->
                    recyclerAdapterHome.isColorEnabled = isEnabled
                }
            }
        }
        //HOME LIST ITEM COLOR SOURCE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelSettings.homeListItemColorSourceState.collect { source ->
                    recyclerAdapterHome.colorSource = source
                }
            }
        }
    }

    /**
     * Creates the timeline adapter and connects row clicks and the absence buttons.
     */
    private fun setupRecyclerAdapter() {
        Timber.d("setupRecyclerAdapter: Initializing RecyclerAdapterHome.")
        recyclerAdapterHome = RecyclerAdapterHome()
        binding.rvHome.apply {
            adapter = recyclerAdapterHome
            layoutManager = LinearLayoutManager(context)
        }
        recyclerAdapterHome.onContentItemClickListener = { course ->
            Timber.d("Recycler item clicked. Course: ${course.title}")
            val action = FragmentHomeDirections.actionHomeFragmentToCourseDetailsFragment(course.id)
            findNavController().navigate(action)
        }
        // A home row is one session of a lesson, so its date is the natural default to offer.
        recyclerAdapterHome.onAbsenceIncreaseClickListener = { lesson ->
            AbsenceDialogs.showAddAbsenceDatePicker(
                requireContext(), childFragmentManager, lesson.date
            ) { date ->
                viewModelTask.addAbsence(lesson, date)
            }
        }
        recyclerAdapterHome.onAbsenceDecreaseClickListener = { lesson ->
            AbsenceDialogs.showRemoveAbsenceDialog(requireContext(), lesson) { date ->
                viewModelTask.removeAbsence(lesson, date)
            }
        }
    }
}
