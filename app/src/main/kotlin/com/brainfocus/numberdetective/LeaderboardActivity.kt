package com.brainfocus.numberdetective

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.LeaderboardAdapter
import com.brainfocus.numberdetective.base.BaseActivity
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class LeaderboardActivity : BaseActivity() {
    override val viewModel: LeaderboardViewModel by viewModel()
    
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        
        initializeViews()
        setupRecyclerView()
        setupTabLayout()
        observeLeaderboardState()
    }

    private fun initializeViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
        }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setSortType(LeaderboardViewModel.SortType.SCORE)
                    1 -> viewModel.setSortType(LeaderboardViewModel.SortType.WINS)
                    2 -> viewModel.setSortType(LeaderboardViewModel.SortType.TIME)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeLeaderboardState() {
        collectFlow(viewModel.leaderboardState) { state ->
            when (state) {
                is LeaderboardViewModel.LeaderboardState.Loading -> {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.GONE
                }
                is LeaderboardViewModel.LeaderboardState.Success -> {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = if (state.results.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(state.results)
                }
                is LeaderboardViewModel.LeaderboardState.Error -> {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    showMessage(state.message)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_leaderboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_refresh -> {
                viewModel.loadLeaderboard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
