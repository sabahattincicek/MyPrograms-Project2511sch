package com.saboon.project_2511sch.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.DialogFragmentCharacterBinding
import com.saboon.project_2511sch.presentation.settings.ViewModelSettings
import com.saboon.project_2511sch.util.Character
import com.saboon.project_2511sch.util.CharacterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DialogFragmentCharacter: DialogFragment() {

    private var _binding: DialogFragmentCharacterBinding?=null
    private val binding get() = _binding!!

    private val viewModelSettings: ViewModelSettings by viewModels()
    private lateinit var recyclerAdapterCharacter: RecyclerAdapterCharacter
    private lateinit var characterManager: CharacterManager
    private lateinit var selectedCharacter: Character

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentCharacterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogAnimation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        characterManager = CharacterManager(requireContext())
        selectedCharacter = characterManager.getCharacter("av1")!!
        setupAdapters()
        setupObservers()

        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            viewModelSettings.onCharacterSelected(selectedCharacter.id)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun updateCharacterUI(character: Character) {
        selectedCharacter = character
        binding.tvCharName.text = character.name
        binding.tvPersonality.text = character.personality["tr"]
    }


    private fun setupAdapters(){
        recyclerAdapterCharacter = RecyclerAdapterCharacter()
        binding.rvCharacters.apply {
            adapter = recyclerAdapterCharacter
            layoutManager =  LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)

            post {
                val screenWidth = resources.displayMetrics.widthPixels
                // Item genişliğini alıyoruz (XML'de item'a verdiğin genişlik, örn: 120dp'nin px karşılığı)
                // Eğer ViewHolder henüz oluşmadıysa güvenli bir varsayılan değer (örn: 120dp px) kullanabilirsin
//                val itemWidth = if (childCount > 0) getChildAt(0).width else (96 * resources.displayMetrics.density).toInt()
                val itemWidth = (96 * resources.displayMetrics.density).toInt()
                val padding = (screenWidth / 2) - (itemWidth / 2)
                setPadding(padding, 0, padding, 0)
                // Padding set edildikten sonra SnapHelper'ın düzgün çalışması için invalidate ediyoruz
                invalidateItemDecorations()
            }
            // 2. Scroll Dinleyici: Durduğunda ortadaki öğeyi yakala
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val centerView = snapHelper.findSnapView(layoutManager)
                        centerView?.let { view ->
                            val pos = getChildAdapterPosition(view)
                            if (pos != RecyclerView.NO_POSITION) {
                                val character = recyclerAdapterCharacter.getCharacterAt(pos)
                                updateCharacterUI(character)
                            }
                        }
                    }
                }
            })
        }
        recyclerAdapterCharacter.submitList(characterManager.allCharacters)
    }

    private fun setupObservers(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModelSettings.selectedCharacterState.collect { characterId ->
                    val char = characterManager.getCharacter(characterId) ?: return@collect
                    updateCharacterUI(char)

                    // Listede bu karakterin yerini bul ve oraya kaydır
                    val allChars = characterManager.allCharacters
                    val index = allChars.indexOfFirst { it.id == characterId }

                    if (index != -1) {
                        // Veriler yüklendikten sonra kaydırabilmek için post kullanıyoruz
                        binding.rvCharacters.post {
                            binding.rvCharacters.scrollToPosition(index)
                        }
                    }
                }
            }
        }
    }
}