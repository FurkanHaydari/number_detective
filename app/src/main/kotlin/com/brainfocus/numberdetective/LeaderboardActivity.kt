package com.brainfocus.numberdetective

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.brainfocus.numberdetective.adapter.LeaderboardPagerAdapter
import com.brainfocus.numberdetective.databinding.ActivityLeaderboardBinding
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private val viewModel: LeaderboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupViewPager() {
        val pagerAdapter = LeaderboardPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.global_leaderboard)
                1 -> getString(R.string.nearby_leaderboard)
                else -> throw IllegalStateException("Invalid position $position")
            }
        }.attach()

        // Set default tab to Global Scores
        binding.viewPager.setCurrentItem(0, false)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
