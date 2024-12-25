package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.missions.DailyMission

class MissionsAdapter(
    private val onClaimReward: (DailyMission) -> Unit
) : ListAdapter<DailyMission, MissionsAdapter.ViewHolder>(MissionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return ViewHolder(view, onClaimReward)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onClaimReward: (DailyMission) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val rewardText: TextView = itemView.findViewById(R.id.rewardText)
        private val claimButton: Button = itemView.findViewById(R.id.claimButton)

        fun bind(mission: DailyMission) {
            titleText.text = mission.title
            descriptionText.text = mission.description
            progressBar.max = mission.targetProgress
            progressBar.progress = mission.currentProgress
            progressText.text = "${mission.currentProgress}/${mission.targetProgress}"
            rewardText.text = "${mission.reward} points"

            claimButton.isEnabled = mission.isCompleted && !mission.isRewardClaimed
            claimButton.text = when {
                mission.isRewardClaimed -> "Claimed"
                mission.isCompleted -> "Claim"
                else -> "In Progress"
            }

            claimButton.setOnClickListener {
                if (mission.isCompleted && !mission.isRewardClaimed) {
                    onClaimReward(mission)
                }
            }
        }
    }

    private class MissionDiffCallback : DiffUtil.ItemCallback<DailyMission>() {
        override fun areItemsTheSame(oldItem: DailyMission, newItem: DailyMission): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DailyMission, newItem: DailyMission): Boolean {
            return oldItem == newItem
        }
    }
}
