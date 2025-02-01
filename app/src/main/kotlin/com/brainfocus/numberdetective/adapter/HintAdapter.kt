package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.databinding.ItemHintSquaresBinding
import com.brainfocus.numberdetective.model.Hint

class HintAdapter : ListAdapter<Hint, HintAdapter.HintViewHolder>(HintDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintViewHolder {
        val binding = ItemHintSquaresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HintViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HintViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class HintViewHolder(private val binding: ItemHintSquaresBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hint: Hint) {
            binding.square1.text = hint.guess[0].toString()
            binding.square2.text = hint.guess[1].toString()
            binding.square3.text = hint.guess[2].toString()
            binding.hintDescription.text = hint.description
        }
    }
}

private class HintDiffCallback : DiffUtil.ItemCallback<Hint>() {
    override fun areItemsTheSame(oldItem: Hint, newItem: Hint): Boolean {
        return oldItem.guess == newItem.guess
    }
    
    override fun areContentsTheSame(oldItem: Hint, newItem: Hint): Boolean {
        return oldItem == newItem
    }
}
