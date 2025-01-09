package com.brainfocus.numberdetective.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainfocus.numberdetective.databinding.FragmentLeaderboardBinding
import com.brainfocus.numberdetective.database.LeaderboardDatabase
import com.brainfocus.numberdetective.location.LocationManager
import com.brainfocus.numberdetective.model.GameLocation
import com.brainfocus.numberdetective.model.PlayerProfile
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var leaderboardDatabase: LeaderboardDatabase
    private lateinit var locationManager: LocationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDependencies()
        setupRecyclerView()
        loadLeaderboard()
    }

    private fun setupDependencies() {
        leaderboardDatabase = LeaderboardDatabase()
        locationManager = LocationManager(requireContext())
        leaderboardAdapter = LeaderboardAdapter()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = leaderboardAdapter
        }
    }

    private fun loadLeaderboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            val location = locationManager.getCurrentLocation()
            leaderboardDatabase.getLeaderboard(location ?: GameLocation())
                .collectLatest { players ->
                    updateLeaderboardUI(players, location)
                }
        }
    }

    private fun updateLeaderboardUI(players: List<PlayerProfile>, location: GameLocation?) {
        // Update header text based on location
        binding.headerText.text = when {
            !location?.district.isNullOrEmpty() -> "İlçe Sıralaması: ${location?.district}"
            !location?.city.isNullOrEmpty() -> "Şehir Sıralaması: ${location?.city}"
            !location?.country.isNullOrEmpty() -> "Ülke Sıralaması: ${location?.country}"
            else -> "Global Sıralama"
        }

        // Update player list
        leaderboardAdapter.submitList(players)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
