package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.multiplayer.Player

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {
    private val players = mutableListOf<Player>()
    private var sortByScore = true

    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankText: TextView = view.findViewById(R.id.rankText)
        val playerNameText: TextView = view.findViewById(R.id.playerNameText)
        val statsText: TextView = view.findViewById(R.id.statsText)
        val scoreText: TextView = view.findViewById(R.id.scoreText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val player = players[position]
        holder.rankText.text = (position + 1).toString()
        holder.playerNameText.text = player.name
        
        if (sortByScore) {
            holder.statsText.text = "Toplam Oyun: ${player.totalGames}"
            holder.scoreText.text = player.highScore.toString()
        } else {
            holder.statsText.text = "Kazanma Oranı: %${(player.wins.toFloat() / player.totalGames * 100).toInt()}"
            holder.scoreText.text = "${player.wins} Zafer"
        }
    }

    override fun getItemCount() = players.size

    fun updatePlayers(newPlayers: List<Player>, sortByScore: Boolean) {
        this.sortByScore = sortByScore
        players.clear()
        players.addAll(if (sortByScore) {
            newPlayers.sortedByDescending { it.highScore }
        } else {
            newPlayers.sortedByDescending { it.wins }
        })
        notifyDataSetChanged()
    }
}
