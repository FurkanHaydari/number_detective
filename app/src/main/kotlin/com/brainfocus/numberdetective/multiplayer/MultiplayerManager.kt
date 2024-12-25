package com.brainfocus.numberdetective.multiplayer

enum class ChallengeStatus {
    PENDING,
    ACCEPTED,
    COMPLETED,
    CANCELLED
}

data class Challenge(
    val challengerId: String,
    val challengerName: String,
    val challengerScore: Int,
    val gameNumber: String,
    val timestamp: Long,
    var status: ChallengeStatus = ChallengeStatus.PENDING,
    var opponent: Player? = null
)

data class Player(
    val id: String,
    val name: String,
    var score: Int = 0,
    var attempts: Int = 0,
    var highScore: Int = 0,
    var totalGames: Int = 0,
    var wins: Int = 0
)

class MultiplayerManager {
    private val activePlayers = mutableMapOf<String, Player>()
    private val activeChallenges = mutableListOf<Challenge>()
    private val leaderboard = mutableListOf<Player>()
    private val activeGameSessions = mutableMapOf<String, GameSession>()
    private val gameSessionListeners = mutableListOf<GameSessionListener>()

    fun addGameSessionListener(listener: GameSessionListener) {
        gameSessionListeners.add(listener)
    }

    fun removeGameSessionListener(listener: GameSessionListener) {
        gameSessionListeners.remove(listener)
    }
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
        val foundChallenge = activeChallenges.find { it.challengerId == challengeId }
        ?: throw IllegalArgumentException("Challenge not found")
    
        // Meydan okumayı kabul eden oyuncuyu kaydet
        activePlayers[player.id] = player
        
        // Meydan okumayı güncelle
        foundChallenge.apply {
            status = ChallengeStatus.ACCEPTED
            opponent = player
        }
        
        // Oyunu başlat
        startGame(foundChallenge, player)
}

    private fun startGame(challenge: Challenge, opponent: Player) {
        // Oyun oturumu oluştur
        val gameSession = GameSession(
            sessionId = "${challenge.challengerId}_${opponent.id}",
            challenger = Player(
                id = challenge.challengerId,
                name = challenge.challengerName,
                score = challenge.challengerScore
            ),
            opponent = opponent,
            gameNumber = challenge.gameNumber,
            startTime = System.currentTimeMillis()
        )
        
        // Aktif oyun oturumlarını takip et
        activeGameSessions[gameSession.sessionId] = gameSession
        
        // Oyun başladığında dinleyicileri bilgilendir
        gameSessionListeners.forEach { listener ->
            listener.onGameSessionStarted(gameSession)
        }
    }

    // GameSession data class'ını ekleyelim
    data class GameSession(
        val sessionId: String,
        val challenger: Player,
        val opponent: Player,
        val gameNumber: String,
        val startTime: Long,
        var endTime: Long? = null,
        var winner: Player? = null
    )

    // Oyun oturumu dinleyici interface'i
    interface GameSessionListener {
        fun onGameSessionStarted(session: GameSession)
        fun onGameSessionEnded(session: GameSession)
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
