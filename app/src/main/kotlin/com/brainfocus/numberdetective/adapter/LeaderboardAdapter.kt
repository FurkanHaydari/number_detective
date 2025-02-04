package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.databinding.ItemLeaderboardBinding
import com.brainfocus.numberdetective.model.PlayerScore

class LeaderboardAdapter : ListAdapter<PlayerScore, LeaderboardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playerScore: PlayerScore) {
            binding.apply {
                rankText.text = "#${playerScore.rank}"
                nameText.text = playerScore.name
                scoreText.text = playerScore.score.toString()
                playerScore.playerIcon?.let { iconUri ->
                    playerIcon.setImageURI(iconUri)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlayerScore>() {
        override fun areItemsTheSame(oldItem: PlayerScore, newItem: PlayerScore): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlayerScore, newItem: PlayerScore): Boolean {
            return oldItem == newItem
        }
    }
}
