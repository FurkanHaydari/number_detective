package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.multiplayer.MultiplayerManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MultiplayerActivity : AppCompatActivity() {
    private lateinit var multiplayerManager: MultiplayerManager
    private lateinit var challengesRecyclerView: RecyclerView
    private lateinit var createChallengeButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        multiplayerManager = MultiplayerManager()
        initializeViews()
        setupChallengeButton()
    }

    private fun initializeViews() {
        challengesRecyclerView = findViewById(R.id.challengesRecyclerView)
        createChallengeButton = findViewById(R.id.createChallengeButton)

        challengesRecyclerView.layoutManager = LinearLayoutManager(this)
        // TODO: Implement ChallengesAdapter
    }

    private fun setupChallengeButton() {
        createChallengeButton.setOnClickListener {
            showCreateChallengeDialog()
        }
    }

    private fun showCreateChallengeDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Yeni Meydan Okuma")
            .setMessage("Rakibinize meydan okumak için hazır mısınız?")
            .setPositiveButton("Evet") { _, _ ->
                startNewChallenge()
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun startNewChallenge() {
        // TODO: Implement player name input and challenge creation
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("MODE", "CHALLENGE")
        }
        startActivity(intent)
    }
}
