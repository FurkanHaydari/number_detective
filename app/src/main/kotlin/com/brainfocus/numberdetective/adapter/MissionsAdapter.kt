package com.brainfocus.numberdetective.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.missions.DailyMission
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class MissionsAdapter(
    private val onClaimReward: (DailyMission) -> Unit
) : RecyclerView.Adapter<MissionsAdapter.MissionViewHolder>() {

    private val missions = mutableListOf<DailyMission>()

    class MissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.missionTitleText)
        val descriptionText: TextView = view.findViewById(R.id.missionDescriptionText)
        val rewardText: TextView = view.findViewById(R.id.rewardText)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.progressBar)
        val claimButton: MaterialButton = view.findViewById(R.id.claimButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val mission = missions[position]
        
        holder.titleText.text = mission.title
        holder.descriptionText.text = mission.description
        holder.rewardText.text = mission.reward.toString()
        
        // Progress bar ayarları
        holder.progressBar.max = mission.target * 100
        holder.progressBar.progress = mission.progress * 100
        
        // Ödül butonu görünürlüğü
        holder.claimButton.visibility = when {
            mission.isClaimed -> View.GONE
            mission.isCompleted -> View.VISIBLE
            else -> View.GONE
        }
        
        holder.claimButton.setOnClickListener {
            onClaimReward(mission)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = missions.size

    fun updateMissions(newMissions: List<DailyMission>) {
        missions.clear()
        missions.addAll(newMissions)
        notifyDataSetChanged()
    }
}
