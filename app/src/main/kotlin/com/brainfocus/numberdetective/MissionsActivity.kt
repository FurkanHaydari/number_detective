package com.brainfocus.numberdetective

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.MissionsAdapter
import com.brainfocus.numberdetective.missions.DailyMission
import com.brainfocus.numberdetective.viewmodel.MissionsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MissionsActivity : AppCompatActivity() {
    private val viewModel: MissionsViewModel by viewModel()
    private lateinit var adapter: MissionsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViews()
        setupRecyclerView()
        observeMissions()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.missionsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)
    }

    private fun setupRecyclerView() {
        adapter = MissionsAdapter { mission ->
            viewModel.claimReward(mission.id)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MissionsActivity)
            adapter = this@MissionsActivity.adapter
        }
    }

    private fun observeMissions() {
        lifecycleScope.launch {
            viewModel.missions.collectLatest { missions ->
                if (missions.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyText.visibility = View.GONE
                    adapter.submitList(missions)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
