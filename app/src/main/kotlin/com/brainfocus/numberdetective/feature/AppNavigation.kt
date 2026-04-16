package com.brainfocus.numberdetective.feature

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.feature.home.HomeScreen
import com.brainfocus.numberdetective.feature.game.GameScreen
import com.brainfocus.numberdetective.feature.result.ResultScreen
import com.brainfocus.numberdetective.feature.game.GameViewModel

@Composable
fun AppNavigation(
    onPlayClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: String
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onPlayClick = {
                    onPlayClick()
                    navController.navigate("game")
                },
                onLanguageChange = onLanguageChange,
                currentLanguage = currentLanguage
            )
        }
        composable("game") {
            val gameViewModel: GameViewModel = hiltViewModel()
            GameScreen(
                viewModel = gameViewModel,
                onNavigateToResult = { isWin, score, correctAnswer, attempts, timeInSeconds, guesses -> 
                    // Using URI encoding for a quick string representation without complex JSON logic
                    val guessesStr = guesses.joinToString(",")
                    navController.navigate("result/$isWin/$score/$correctAnswer/$attempts/$timeInSeconds?guesses=$guessesStr") {
                        popUpTo("home")
                    }
                }
            )
        }
        composable("result/{isWin}/{score}/{correctAnswer}/{attempts}/{timeInSeconds}?guesses={guesses}") { backStackEntry ->
            val isWin = backStackEntry.arguments?.getString("isWin")?.toBoolean() ?: false
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val correctAnswer = backStackEntry.arguments?.getString("correctAnswer") ?: ""
            val attempts = backStackEntry.arguments?.getString("attempts")?.toIntOrNull() ?: 0
            val timeInSeconds = backStackEntry.arguments?.getString("timeInSeconds")?.toIntOrNull() ?: 0
            val guessesStr = backStackEntry.arguments?.getString("guesses") ?: ""
            val guesses = if (guessesStr.isNotEmpty()) guessesStr.split(",") else emptyList()

            ResultScreen(
                isWin = isWin,
                score = score,
                correctAnswer = correctAnswer,
                attempts = attempts,
                timeInSeconds = timeInSeconds,
                guesses = guesses,
                onPlayAgain = {
                    navController.navigate("game") { popUpTo("home") }
                },
                onGoHome = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}
