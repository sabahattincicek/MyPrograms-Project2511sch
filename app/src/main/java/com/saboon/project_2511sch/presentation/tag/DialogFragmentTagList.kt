package com.saboon.project_2511sch.presentation.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentTagListBinding
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentTagList: DialogFragment() {

    private var _binding : DialogFragmentTagListBinding? = null
    private val binding get() = _binding!!

    private val viewModelTag: ViewModelTag by viewModels()

    private lateinit var recyclerAdapterTag: RecyclerAdapterTag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentTagListBinding.inflate(inflater, container, false)
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

        setupAdapters()
        setupObservers()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters(){
        recyclerAdapterTag = RecyclerAdapterTag()
        recyclerAdapterTag.onItemClickListener = { tag ->
            setFragmentResult(REQUEST_TAG, bundleOf(RESULT_TAG to tag))
            dismiss()
        }
        binding.rvTags.apply {
            adapter = recyclerAdapterTag
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers(){
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
                            recyclerAdapterTag.submitList(displayItemList)
                        }
                    }
                }
            }
        }
    }

    companion object{
        const val REQUEST_TAG = "dialog_fragment_tag_list_request_tag"
        const val RESULT_TAG = "dialog_fragment_tag_list_result_tag"
    }
}