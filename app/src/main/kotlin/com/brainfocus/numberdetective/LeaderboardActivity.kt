package com.brainfocus.numberdetective

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.LeaderboardAdapter
import com.brainfocus.numberdetective.data.entities.Player
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LeaderboardActivity : AppCompatActivity() {
    private val viewModel: LeaderboardViewModel by viewModel()
    private lateinit var adapter: LeaderboardAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViews()
        setupRecyclerView()
        observeLeaderboard()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        emptyText = findViewById(R.id.emptyText)
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
        }
    }

    private fun observeLeaderboard() {
        lifecycleScope.launch {
            viewModel.leaderboardEntries.collectLatest { entries ->
                if (entries.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyText.visibility = View.GONE
                    adapter.submitList(entries)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
