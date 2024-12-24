package com.brainfocus.numberdetective

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialButton>(R.id.singlePlayerButton).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.challengeButton).setOnClickListener {
            // TODO: Implement multiplayer challenge screen
        }

        findViewById<MaterialButton>(R.id.leaderboardButton).setOnClickListener {
            // TODO: Implement leaderboard screen
        }
    }
}
