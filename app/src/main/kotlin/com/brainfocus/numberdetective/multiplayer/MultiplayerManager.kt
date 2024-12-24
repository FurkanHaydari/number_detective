package com.brainfocus.numberdetective.multiplayer

data class Challenge(
    val challengerId: String,
    val challengerName: String,
    val challengerScore: Int,
    val gameNumber: String,
    val timestamp: Long
)

data class Player(
    val id: String,
    val name: String,
    var highScore: Int = 0,
    var totalGames: Int = 0,
    var wins: Int = 0
)

class MultiplayerManager {
    private val activePlayers = mutableMapOf<String, Player>()
    private val activeChallenges = mutableListOf<Challenge>()
    private val leaderboard = mutableListOf<Player>()
    
    fun createChallenge(challenger: Player, gameNumber: String): Challenge {
        val challenge = Challenge(
            challengerId = challenger.id,
            challengerName = challenger.name,
            challengerScore = challenger.highScore,
            gameNumber = gameNumber,
            timestamp = System.currentTimeMillis()
        )
        activeChallenges.add(challenge)
        return challenge
    }
    
    fun acceptChallenge(challengeId: String, player: Player) {
        val challenge = activeChallenges.find { it.challengerId == challengeId }
            ?: throw IllegalArgumentException("Challenge not found")
            
        // Meydan okumayı kabul eden oyuncuyu kaydet
        activePlayers[player.id] = player
    }
    
    fun updateLeaderboard(player: Player) {
        val existingPlayer = leaderboard.find { it.id == player.id }
        if (existingPlayer != null) {
            existingPlayer.highScore = maxOf(existingPlayer.highScore, player.highScore)
            existingPlayer.totalGames++
            if (player.wins > 0) existingPlayer.wins++
        } else {
            leaderboard.add(player)
        }
        
        // Liderlik tablosunu yüksek skora göre sırala
        leaderboard.sortByDescending { it.highScore }
    }
    
    fun getTopPlayers(limit: Int = 10): List<Player> {
        return leaderboard.take(limit)
    }
    
    fun getActiveChallenges(): List<Challenge> = activeChallenges.toList()
    
    fun removeChallenge(challengeId: String) {
        activeChallenges.removeIf { it.challengerId == challengeId }
    }
}
