package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.viewmodel.Hint

class HintAdapter : RecyclerView.Adapter<HintAdapter.HintViewHolder>() {
    private var hints = mutableListOf<Hint>()

    fun updateHints(newHints: List<Hint>) {
        hints.clear()
        hints.addAll(newHints)
        notifyDataSetChanged()
    }

    fun getCurrentHints(): List<Hint> = hints.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HintViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hint_item, parent, false)
        return HintViewHolder(view)
    }

    override fun onBindViewHolder(holder: HintViewHolder, position: Int) {
        val hint = hints[position]
        holder.bind(hint)
    }

    override fun getItemCount(): Int = hints.size

    class HintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val guessNumberText: TextView = itemView.findViewById(R.id.text_guess_number)
        private val hintText: TextView = itemView.findViewById(R.id.text_hint)

        fun bind(hint: Hint) {
            guessNumberText.text = hint.numbers.joinToString("  ")
            hintText.text = hint.description
        }
    }
}
