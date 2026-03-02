package com.saboon.project_2511sch.presentation.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.saboon.project_2511sch.databinding.RowCircularImageViewBinding
import com.saboon.project_2511sch.util.Character

class RecyclerAdapterCharacter: ListAdapter<Character, RecyclerAdapterCharacter.CharacterViewHolder>(CharacterDiffCallback()) {

    var onItemClickListener: ((Character) -> Unit)?=null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CharacterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowCircularImageViewBinding.inflate(inflater, parent, false)
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CharacterViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
    }

    // Bu fonksiyonu Fragment içinden çağıracağız (SnapHelper ile birlikte)
    fun getCharacterAt(position: Int): Character {
        return getItem(position)
    }


    inner class CharacterViewHolder(private val binding: RowCircularImageViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Character){
            binding.ivPortrait.load(item.portrait)
        }
    }

    class CharacterDiffCallback: DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(
            oldItem: Character,
            newItem: Character
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Character,
            newItem: Character
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
}