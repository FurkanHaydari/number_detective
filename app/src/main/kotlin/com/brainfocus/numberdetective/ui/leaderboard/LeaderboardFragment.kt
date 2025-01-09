package com.brainfocus.numberdetective.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.databinding.FragmentLeaderboardBinding
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import kotlinx.coroutines.launch

class LeaderboardFragment : Fragment() {
    private val viewModel: LeaderboardViewModel by viewModels()
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var leaderboardAdapter: LeaderboardAdapter

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
        
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        setupViews()
        setupRecyclerView()
        observeViewModel()
        
        viewModel.loadLeaderboard(requireContext())
    }

    private fun setupViews() {
        leaderboardAdapter = LeaderboardAdapter()
    }

    private fun setupRecyclerView() {
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaderboardAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.leaderboardState.collect { state ->
                when (state) {
                    is LeaderboardViewModel.LeaderboardState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.leaderboardRecyclerView.visibility = View.GONE
                        binding.errorCard.visibility = View.GONE
                    }
                    is LeaderboardViewModel.LeaderboardState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.leaderboardRecyclerView.visibility = View.VISIBLE
                        binding.errorCard.visibility = View.GONE
                        binding.titleText.text = getString(R.string.district_leaderboard, state.location.district)
                        leaderboardAdapter.submitList(state.players)
                    }
                    is LeaderboardViewModel.LeaderboardState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.leaderboardRecyclerView.visibility = View.GONE
                        binding.errorCard.visibility = View.VISIBLE
                        binding.errorText.text = state.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
