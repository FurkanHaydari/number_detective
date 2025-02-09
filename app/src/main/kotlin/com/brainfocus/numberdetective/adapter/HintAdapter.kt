package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.databinding.ItemHintSquaresBinding
import com.brainfocus.numberdetective.model.Hint

class HintAdapter(
    private val onHintClick: (Hint) -> Unit,
    private val isTablet: Boolean = false
) : ListAdapter<Hint, HintAdapter.HintViewHolder>(HintDiffCallback()) {
    init {
        // Enable view holder recycling
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // Use the hint's guess as a stable ID
        return getItem(position).guess.hashCode().toLong()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintViewHolder {
        val binding = ItemHintSquaresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Enable hardware acceleration for better performance
        binding.root.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        return HintViewHolder(binding, onHintClick, isTablet)
    }
    
    override fun onBindViewHolder(holder: HintViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class HintViewHolder(
        private val binding: ItemHintSquaresBinding,
        private val onHintClick: (Hint) -> Unit,
        private val isTablet: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        // Cache views for better performance
        private val squares = listOf(binding.square1, binding.square2, binding.square3, binding.square4)
        private var currentHint: Hint? = null

        init {
            // Set click listener once during initialization
            binding.root.setOnClickListener { 
                // Scale animation
                binding.root.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(100)
                    .withEndAction {
                        binding.root.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                        currentHint?.let(onHintClick)
                    }
                    .start()
            }
        }

        fun bind(hint: Hint) {
            currentHint = hint
            
            // Update squares efficiently
            hint.guess.forEachIndexed { index, digit ->
                squares[index].text = digit.toString()
            }
            
            // Only update visibility if needed
            if (binding.square4.visibility != if (hint.guess.length == 4) View.VISIBLE else View.GONE) {
                binding.square4.visibility = if (hint.guess.length == 4) View.VISIBLE else View.GONE
            }

            // Update description
            binding.hintDescription.text = hint.description

            // Set text sizes and dimensions for tablet
            if (isTablet) {
                squares.forEach { square ->
                    square.textSize = 36f
                    val size = (60 * square.resources.displayMetrics.density).toInt()
                    square.layoutParams.width = size
                    square.layoutParams.height = size
                }
                binding.hintDescription.textSize = 24f
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
