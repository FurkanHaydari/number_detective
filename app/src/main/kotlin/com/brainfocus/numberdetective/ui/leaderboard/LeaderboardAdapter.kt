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
import com.google.firebase.auth.FirebaseAuth

class LeaderboardAdapter : ListAdapter<PlayerProfile, LeaderboardAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = getItem(position)
        holder.bind(player, position + 1)
        
        if (player.userId == currentUserId) {
            holder.itemView.setBackgroundResource(R.drawable.current_user_background)
            holder.currentUserIndicator.visibility = View.VISIBLE
        } else {
            holder.itemView.background = null
            holder.currentUserIndicator.visibility = View.GONE
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.rankText)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        val currentUserIndicator: View = itemView.findViewById(R.id.currentUserIndicator)

        fun bind(player: PlayerProfile, rank: Int) {
            rankText.text = rank.toString()
            nameText.text = player.displayName
            scoreText.text = itemView.context.getString(R.string.score_format, player.score)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlayerProfile>() {
            override fun areItemsTheSame(oldItem: PlayerProfile, newItem: PlayerProfile): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: PlayerProfile, newItem: PlayerProfile): Boolean {
                return oldItem == newItem
            }
        }
    }
}
