package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.databinding.ItemHintSquaresBinding
import com.brainfocus.numberdetective.model.Hint

class HintAdapter(private val onHintClick: (Hint) -> Unit) : ListAdapter<Hint, HintAdapter.HintViewHolder>(HintDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintViewHolder {
        val binding = ItemHintSquaresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HintViewHolder(binding, onHintClick)
    }
    
    override fun onBindViewHolder(holder: HintViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class HintViewHolder(
        private val binding: ItemHintSquaresBinding,
        private val onHintClick: (Hint) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hint: Hint) {
            binding.apply {
                square1.text = hint.guess[0].toString()
                square2.text = hint.guess[1].toString()
                square3.text = hint.guess[2].toString()
                square4.visibility = if (hint.guess.length == 4) {
                    square4.text = hint.guess[3].toString()
                    View.VISIBLE
                } else {
                    View.GONE
                }
                hintDescription.text = hint.description

                root.setOnClickListener {
                    onHintClick(hint)
                }
            }
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
