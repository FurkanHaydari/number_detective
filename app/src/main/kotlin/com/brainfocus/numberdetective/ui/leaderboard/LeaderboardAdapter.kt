package com.brainfocus.numberdetective.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.model.PlayerProfile

class LeaderboardAdapter : ListAdapter<PlayerProfile, LeaderboardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = getItem(position)
        holder.bind(player, position + 1)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.rankText)
        private val playerNameText: TextView = itemView.findViewById(R.id.playerNameText)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)

        fun bind(player: PlayerProfile, rank: Int) {
            rankText.text = rank.toString()
            playerNameText.text = player.name
            scoreText.text = player.score.toString()
            locationText.text = player.location?.district ?: player.location?.city ?: player.location?.country ?: "Global"
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlayerProfile>() {
        override fun areItemsTheSame(oldItem: PlayerProfile, newItem: PlayerProfile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlayerProfile, newItem: PlayerProfile): Boolean {
            return oldItem == newItem
        }
    }
}
