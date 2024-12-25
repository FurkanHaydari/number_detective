package com.brainfocus.numberdetective

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.MissionsAdapter
import com.brainfocus.numberdetective.base.BaseActivity
import com.brainfocus.numberdetective.utils.SoundManager
import com.brainfocus.numberdetective.utils.SoundType
import com.brainfocus.numberdetective.viewmodel.MissionsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MissionsActivity : BaseActivity() {
    override val viewModel: MissionsViewModel by viewModel()
    
    private lateinit var resetTimeText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MissionsAdapter
    private lateinit var emptyView: View
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)
        
        initializeViews()
        setupRecyclerView()
        observeMissionsState()
    }

    private fun initializeViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        resetTimeText = findViewById(R.id.resetTimeText)
        recyclerView = findViewById(R.id.missionsRecyclerView)
        emptyView = findViewById(R.id.emptyView)
        soundManager = SoundManager.getInstance(this)
    }

    private fun setupRecyclerView() {
        adapter = MissionsAdapter { mission ->
            soundManager.playSound(SoundType.BUTTON_CLICK)
            viewModel.claimReward(mission)
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MissionsActivity)
            adapter = this@MissionsActivity.adapter
        }
    }

    private fun observeMissionsState() {
        collectFlow(viewModel.missionsState) { state ->
            when (state) {
                is MissionsViewModel.MissionsState.Loading -> {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.GONE
                }
                is MissionsViewModel.MissionsState.Success -> {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = if (state.missions.isEmpty()) View.VISIBLE else View.GONE
                    adapter.updateMissions(state.missions)
                }
                is MissionsViewModel.MissionsState.Error -> {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    showMessage(state.message)
                }
            }
        }

        collectFlow(viewModel.resetTimeState) { timeText ->
            resetTimeText.text = timeText
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
