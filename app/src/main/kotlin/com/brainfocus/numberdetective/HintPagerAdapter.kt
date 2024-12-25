package com.brainfocus.numberdetective

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class HintPagerAdapter : RecyclerView.Adapter<HintPagerAdapter.HintViewHolder>() {
    private val hints = mutableListOf<HintItem>()

    data class HintItem(
        var description: String,
        val number: String // Three-digit number to be split into squares
    )

    class HintViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val square1: TextView = view.findViewById(R.id.square1)
        private val square2: TextView = view.findViewById(R.id.square2)
        private val square3: TextView = view.findViewById(R.id.square3)
        private val description: TextView = view.findViewById(R.id.hintDescription)

        fun bind(hint: HintItem) {
            // Set description with custom styling
            description.apply {
                text = hint.description
                setTypeface(typeface, Typeface.BOLD)
                textSize = 16f
            }

            // Split the three-digit number into individual digits
            val digits = hint.number.toCharArray()
            val squares = listOf(square1, square2, square3)
            
            squares.forEachIndexed { index, square ->
                if (index < digits.size) {
                    square.text = digits[index].toString()
                } else {
                    square.text = ""
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hint_squares, parent, false)
        return HintViewHolder(view)
    }

    override fun onBindViewHolder(holder: HintViewHolder, position: Int) {
        holder.bind(hints[position])
    }

    override fun getItemCount() = hints.size

    fun updateHints(newHints: List<HintItem>) {
        hints.clear()
        hints.addAll(newHints)
        notifyDataSetChanged()
    }

    fun updateHintDescription(position: Int, newDescription: String) {
        if (position < hints.size) {
            hints[position] = hints[position].copy(description = newDescription)
            notifyItemChanged(position)
        }
    }
}
