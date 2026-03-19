package com.saboon.project_2511sch.presentation.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentManageTagBinding
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.presentation.tag.DialogFragmentTag.Companion.ARG_USER
import com.saboon.project_2511sch.presentation.user.ViewModelUser
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentManageTag: DialogFragment() {

    private var _binding : DialogFragmentManageTagBinding? = null
    private val binding get() = _binding!!

    private val viewModelUser: ViewModelUser by activityViewModels()
    private val viewModelTag: ViewModelTag by viewModels()
    private lateinit var currentUser: User
    private lateinit var recyclerAdapterTag: RecyclerAdapterTag
    private var isSelectModeForCourse = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentManageTagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sistemin çubuklarının (StatusBar ve NavBar) yüksekliğini al ve layout'a padding olarak ekle
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // v (yani senin root view'ın) sistem çubukları kadar içeri itilir
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,    // Üstteki bildirim çubuğunu kurtarır
                right = systemBars.right,
                bottom = systemBars.bottom // Alttaki navigasyon çubuğunu kurtarır
            )

            insets
        }

        arguments?.let {
            isSelectModeForCourse = arguments?.getBoolean(ARG_IS_SELECT_MODE_FOR_COURSE, false) == true
        }

        setupAdapters()
        setupObservers()

        binding.fabAdd.setOnClickListener {
            val dialog = DialogFragmentTag.newInstanceForCreate()
            dialog.show(childFragmentManager, "CreateTagDialog")
        }
        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
        recyclerAdapterTag = RecyclerAdapterTag()
        recyclerAdapterTag.onItemClickListener = { tag ->
            if (isSelectModeForCourse){
                setFragmentResult(REQUEST_TAG, bundleOf(RESULT_TAG to tag))
                dismiss()
            }
            else{
                val dialog = DialogFragmentTag.newInstanceForUpdate(tag)
                dialog.show(childFragmentManager, "CreateTagDialog")
            }
        }
        binding.rvTags.apply {
            adapter = recyclerAdapterTag
            layoutManager = LinearLayoutManager(requireContext())
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
        //TAGS STATE
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelTag.tagsState.collect { resource ->
                    when(resource){
                        is Resource.Error -> {}
                        is Resource.Idle -> {}
                        is Resource.Loading ->{}
                        is Resource.Success -> {
                            val displayItemList = resource.data
                            if (displayItemList.isNullOrEmpty()){
                                binding.llEmptyList.visibility = View.VISIBLE
                                binding.rvTags.visibility = View.GONE
                            }else{
                                binding.llEmptyList.visibility = View.GONE
                                binding.rvTags.visibility = View.VISIBLE

                                recyclerAdapterTag.submitList(resource.data)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object{
        const val ARG_IS_SELECT_MODE_FOR_COURSE = "dialog_fragment_manage_tag_arg_is_select_mode_for_course"
        const val REQUEST_TAG = "dialog_fragment_manage_tag_request_tag"
        const val RESULT_TAG = "dialog_fragment_manage_tag_result_tag"

        fun newInstanceForManage(): DialogFragmentManageTag{
            val fragment = DialogFragmentManageTag()
            fragment.arguments = bundleOf(
                ARG_IS_SELECT_MODE_FOR_COURSE to false
            )
            return fragment
        }
        fun newInstanceForSelect(): DialogFragmentManageTag{
            val fragment = DialogFragmentManageTag()
            fragment.arguments = bundleOf(
                ARG_IS_SELECT_MODE_FOR_COURSE to true
            )
            return fragment
        }
    }
}