package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.data.entities.Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeaderboardAdapter : ListAdapter<Player, LeaderboardAdapter.ViewHolder>(PlayerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.rankText)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(player: Player) {
            rankText.text = (adapterPosition + 1).toString()
            nameText.text = player.name
            scoreText.text = player.score.toString()
            dateText.text = dateFormat.format(Date(player.timestamp))
        }
    }

    private class PlayerDiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }
}
