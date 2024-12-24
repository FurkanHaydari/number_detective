package com.brainfocus.numberdetective

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.LeaderboardAdapter
import com.brainfocus.numberdetective.multiplayer.MultiplayerManager
import com.google.android.material.tabs.TabLayout

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var multiplayerManager: MultiplayerManager
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        multiplayerManager = MultiplayerManager()
        initializeViews()
        setupTabLayout()
        updateLeaderboard(true)
    }

    private fun initializeViews() {
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        
        leaderboardAdapter = LeaderboardAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
        }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateLeaderboard(tab?.position == 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateLeaderboard(sortByScore: Boolean) {
        val players = multiplayerManager.getTopPlayers()
        leaderboardAdapter.updatePlayers(players, sortByScore)
    }

    override fun onResume() {
        super.onResume()
        updateLeaderboard(tabLayout.selectedTabPosition == 0)
    }
}
