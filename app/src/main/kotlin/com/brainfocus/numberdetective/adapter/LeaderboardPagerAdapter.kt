package com.brainfocus.numberdetective.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.brainfocus.numberdetective.fragment.GlobalLeaderboardFragment

class LeaderboardPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GlobalLeaderboardFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}
