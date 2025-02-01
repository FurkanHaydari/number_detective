package com.brainfocus.numberdetective.ui.leaderboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.databinding.ItemLeaderboardBinding
import com.brainfocus.numberdetective.model.PlayerScore
import com.bumptech.glide.Glide

class LeaderboardAdapter : ListAdapter<PlayerScore, LeaderboardAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlayerScore>() {
            override fun areItemsTheSame(oldItem: PlayerScore, newItem: PlayerScore): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PlayerScore, newItem: PlayerScore): Boolean {
                return oldItem == newItem
            }
        }
    }

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

    inner class ViewHolder(
        private val binding: ItemLeaderboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlayerScore) {
            binding.apply {
                rankText.text = "#${item.rank}"
                nameText.text = item.name
                scoreText.text = item.score.toString()

                // Oyuncu ikonunu yükle
                item.playerIcon?.let { uri ->
                    Glide.with(playerIcon)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.ic_player_placeholder)
                        .error(R.drawable.ic_player_placeholder)
                        .into(playerIcon)
                } ?: run {
                    Glide.with(playerIcon)
                        .load(R.drawable.ic_player_placeholder)
                        .circleCrop()
                        .into(playerIcon)
                }

                // İlk 3 sıradaki oyuncular için özel arka plan
                val backgroundRes = when (item.rank) {
                    1L -> R.drawable.bg_rank_gold
                    2L -> R.drawable.bg_rank_silver
                    3L -> R.drawable.bg_rank_bronze
                    else -> R.drawable.bg_rank_default
                }
                root.setBackgroundResource(backgroundRes)
            }
        }
    }
}
