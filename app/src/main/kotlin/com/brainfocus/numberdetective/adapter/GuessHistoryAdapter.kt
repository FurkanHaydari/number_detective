package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R

data class GuessHistoryItem(
    val guess: Int,
    val isCorrect: Boolean,
    val hint: String
)

class GuessHistoryAdapter : ListAdapter<GuessHistoryItem, GuessHistoryAdapter.ViewHolder>(GuessHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guess_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val guessText: TextView = itemView.findViewById(R.id.guessText)
        private val resultText: TextView = itemView.findViewById(R.id.resultText)

        fun bind(item: GuessHistoryItem) {
            guessText.text = item.guess.toString()
            resultText.text = if (item.isCorrect) "Correct!" else item.hint
        }
    }

    private class GuessHistoryDiffCallback : DiffUtil.ItemCallback<GuessHistoryItem>() {
        override fun areItemsTheSame(oldItem: GuessHistoryItem, newItem: GuessHistoryItem): Boolean {
            return oldItem.guess == newItem.guess
        }

        override fun areContentsTheSame(oldItem: GuessHistoryItem, newItem: GuessHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
