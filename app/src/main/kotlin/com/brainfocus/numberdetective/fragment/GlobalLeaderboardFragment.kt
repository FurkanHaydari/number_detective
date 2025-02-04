package com.brainfocus.numberdetective.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainfocus.numberdetective.adapter.LeaderboardAdapter
import com.brainfocus.numberdetective.databinding.FragmentLeaderboardListBinding
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GlobalLeaderboardFragment : Fragment() {
    private var _binding: FragmentLeaderboardListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LeaderboardViewModel by activityViewModels()
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        loadLeaderboard()
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        binding.leaderboardRecyclerView.apply {
            adapter = leaderboardAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scores.collect { scores ->
                        leaderboardAdapter.submitList(scores)
                    }
                }

                launch {
                    viewModel.loading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            showError(it)
                        }
                    }
                }
            }
        }
    }

    private fun loadLeaderboard() {
        viewLifecycleOwner.lifecycleScope.launch {
            requireActivity().let { activity ->
                viewModel.loadGlobalLeaderboard(activity)
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
