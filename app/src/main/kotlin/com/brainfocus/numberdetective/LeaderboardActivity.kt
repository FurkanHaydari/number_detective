package com.brainfocus.numberdetective

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.LeaderboardAdapter
import com.brainfocus.numberdetective.auth.GameSignInManager
import com.brainfocus.numberdetective.viewmodel.LeaderboardViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {

    private val viewModel: LeaderboardViewModel by viewModels()
    private lateinit var adapter: LeaderboardAdapter
    private lateinit var signInManager: GameSignInManager

    private lateinit var loadingProgress: View
    private lateinit var emptyStateText: TextView
    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var playerNameText: TextView
    private lateinit var playerLocationText: TextView
    private lateinit var playerHighScoreText: TextView
    private lateinit var playerTotalGamesText: TextView

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadLeaderboard()
        } else {
            showSnackbar(
                getString(R.string.location_permission_required),
                getString(R.string.grant_permission)
            ) {
                requestLocationPermission()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        setupViews()
        setupToolbar()
        setupRecyclerView()
        setupSignIn()
        observeViewModel()
    }

    private fun setupViews() {
        loadingProgress = findViewById(R.id.loadingProgress)
        emptyStateText = findViewById(R.id.emptyStateText)
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView)
        playerNameText = findViewById(R.id.playerNameText)
        playerLocationText = findViewById(R.id.playerLocationText)
        playerHighScoreText = findViewById(R.id.playerHighScoreText)
        playerTotalGamesText = findViewById(R.id.playerTotalGamesText)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter()
        leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSignIn() {
        signInManager = GameSignInManager(this)
        signInManager.initializeSignIn()
        signInManager.signIn(
            onSuccess = { account ->
                viewModel.loadPlayerStats(account)
                checkLocationPermission()
            },
            onFailed = {
                showSnackbar(getString(R.string.error_loading_leaderboard))
            }
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.leaderboardState.collect { state ->
                when (state) {
                    is LeaderboardViewModel.LeaderboardState.Loading -> {
                        loadingProgress.visibility = View.VISIBLE
                        emptyStateText.visibility = View.GONE
                        leaderboardRecyclerView.visibility = View.GONE
                    }
                    is LeaderboardViewModel.LeaderboardState.Success -> {
                        loadingProgress.visibility = View.GONE
                        emptyStateText.visibility = View.GONE
                        leaderboardRecyclerView.visibility = View.VISIBLE
                        adapter.submitList(state.players)
                    }
                    is LeaderboardViewModel.LeaderboardState.Empty -> {
                        loadingProgress.visibility = View.GONE
                        emptyStateText.visibility = View.VISIBLE
                        leaderboardRecyclerView.visibility = View.GONE
                    }
                    is LeaderboardViewModel.LeaderboardState.Error -> {
                        loadingProgress.visibility = View.GONE
                        showSnackbar(
                            state.message,
                            getString(R.string.retry)
                        ) {
                            checkLocationPermission()
                        }
                    }
                    is LeaderboardViewModel.LeaderboardState.LocationPermissionRequired -> {
                        loadingProgress.visibility = View.GONE
                        requestLocationPermission()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.playerStats.collect { stats ->
                stats?.let {
                    playerNameText.text = it.displayName
                    playerLocationText.text = it.location
                    playerHighScoreText.text = it.highScore.toString()
                    playerTotalGamesText.text = it.totalGames.toString()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.loadLeaderboard()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showSnackbar(
                    getString(R.string.location_permission_required),
                    getString(R.string.grant_permission)
                ) {
                    requestLocationPermission()
                }
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showSnackbar(
        message: String,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
        }
        
        snackbar.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
