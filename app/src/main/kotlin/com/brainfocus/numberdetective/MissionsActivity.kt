package com.brainfocus.numberdetective

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brainfocus.numberdetective.adapter.MissionsAdapter
import com.brainfocus.numberdetective.missions.MissionManager
import com.brainfocus.numberdetective.utils.PreferencesManager
import com.brainfocus.numberdetective.utils.SoundManager
import com.brainfocus.numberdetective.utils.SoundType
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MissionsActivity : AppCompatActivity() {
    private lateinit var missionManager: MissionManager
    private lateinit var prefsManager: PreferencesManager
    private lateinit var soundManager: SoundManager
    private lateinit var missionsAdapter: MissionsAdapter
    private lateinit var resetTimeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)

        missionManager = MissionManager.getInstance(this)
        prefsManager = PreferencesManager.getInstance(this)
        soundManager = SoundManager.getInstance(this)

        initializeViews()
        setupRecyclerView()
        updateResetTime()
    }

    private fun initializeViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        resetTimeText = findViewById(R.id.resetTimeText)
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.missionsRecyclerView)
        
        missionsAdapter = MissionsAdapter { mission ->
            soundManager.playSound(SoundType.BUTTON_CLICK)
            
            missionManager.claimReward(mission.id)?.let { reward ->
                // Ödülü kullanıcının puanına ekle
                val currentScore = prefsManager.getHighScore()
                prefsManager.updateHighScore(currentScore + reward)
                
                showMessage("Tebrikler! $reward puan kazandınız!")
            }
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MissionsActivity)
            adapter = missionsAdapter
        }
        
        updateMissions()
    }

    private fun updateMissions() {
        val missions = missionManager.getDailyMissions()
        missionsAdapter.updateMissions(missions)
    }

    private fun updateResetTime() {
        val calendar = Calendar.getInstance()
        val nextDay = calendar.apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        val currentTime = System.currentTimeMillis()
        val timeUntilReset = nextDay.timeInMillis - currentTime
        
        val hoursLeft = timeUntilReset / (1000 * 60 * 60)
        val minutesLeft = (timeUntilReset % (1000 * 60 * 60)) / (1000 * 60)
        
        resetTimeText.text = "Yeni görevlere kalan süre: ${hoursLeft}s ${minutesLeft}d"
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
