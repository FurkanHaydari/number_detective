package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.game.NumberDetectiveGame

class GuessHistoryAdapter : RecyclerView.Adapter<GuessHistoryAdapter.GuessViewHolder>() {
    private val guesses = mutableListOf<GuessHistoryItem>()

    data class GuessHistoryItem(
        val number: String,
        val correct: Int,
        val misplaced: Int
    )

    class GuessViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val guessNumberText: TextView = view.findViewById(R.id.guessNumberText)
        val correctText: TextView = view.findViewById(R.id.correctText)
        val misplacedText: TextView = view.findViewById(R.id.misplacedText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guess_history, parent, false)
        return GuessViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuessViewHolder, position: Int) {
        val guess = guesses[position]
        holder.guessNumberText.text = guess.number
        holder.correctText.text = "Doğru yer: ${guess.correct}"
        holder.misplacedText.text = "Yanlış yer: ${guess.misplaced}"
    }

    override fun getItemCount() = guesses.size

    fun addGuess(number: String, result: NumberDetectiveGame.Guess) {
        guesses.add(0, GuessHistoryItem(number, result.correct, result.misplaced))
        notifyItemInserted(0)
    }

    fun clear() {
        guesses.clear()
        notifyDataSetChanged()
    }
}
