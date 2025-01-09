package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
        
        // Animasyon ekle
        holder.itemView.startAnimation(
            AnimationUtils.loadAnimation(
                holder.itemView.context,
                R.anim.item_animation_fall_down
            )
        )
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.rankText)
        private val playerNameText: TextView = itemView.findViewById(R.id.playerNameText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)

        fun bind(player: PlayerProfile, rank: Int) {
            rankText.text = rank.toString()
            playerNameText.text = player.displayName
            locationText.text = player.location
            scoreText.text = player.highScore.toString()

            // İlk 3 sıra için özel stil
            when (rank) {
                1 -> {
                    rankText.setBackgroundResource(R.drawable.circle_gold)
                    scoreText.setTextColor(itemView.context.getColor(R.color.gold))
                }
                2 -> {
                    rankText.setBackgroundResource(R.drawable.circle_silver)
                    scoreText.setTextColor(itemView.context.getColor(R.color.silver))
                }
                3 -> {
                    rankText.setBackgroundResource(R.drawable.circle_bronze)
                    scoreText.setTextColor(itemView.context.getColor(R.color.bronze))
                }
                else -> {
                    rankText.setBackgroundResource(R.drawable.circle_background)
                    scoreText.setTextColor(itemView.context.getColor(R.color.score_color))
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlayerProfile>() {
        override fun areItemsTheSame(oldItem: PlayerProfile, newItem: PlayerProfile): Boolean {
            return oldItem.playerId == newItem.playerId
        }

        override fun areContentsTheSame(oldItem: PlayerProfile, newItem: PlayerProfile): Boolean {
            return oldItem == newItem
        }
    }
}
