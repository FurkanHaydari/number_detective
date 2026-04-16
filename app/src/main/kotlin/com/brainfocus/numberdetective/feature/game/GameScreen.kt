package com.brainfocus.numberdetective.feature.game

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.data.model.GameState
import com.brainfocus.numberdetective.data.model.GuessResult
import com.brainfocus.numberdetective.data.model.Hint
import com.brainfocus.numberdetective.core.sound.SoundManager
import com.brainfocus.numberdetective.feature.game.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateToResult: (Boolean, Int, String, Int, Int, List<String>) -> Unit
) {
    val context = LocalContext.current
    val currentLevel by viewModel.currentLevel.collectAsState()
    val remainingAttempts by viewModel.remainingAttempts.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val score by viewModel.score.collectAsState()
    val hints by viewModel.hints.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val guesses by viewModel.guesses.collectAsState()
    val correctAnswer by viewModel.correctAnswer.collectAsState()
    val attempts = viewModel.attempts

    // In a real approach, SoundManager should be injected or requested via ViewModel
    // We'll trust that SoundManager is initialized correctly. 
    // To simplify we might not use SoundManager directly here unless we resolve it from Di.
    // However, keeping the sound logic simple is better.

    val expectedLength = if (currentLevel == 3) 4 else 3
    var pickerValues by remember(currentLevel) {
        mutableStateOf(List(expectedLength) { 0 })
    }

    LaunchedEffect(gameState) {
        when (gameState) {
            is GameState.Win -> {
                onNavigateToResult(true, score, correctAnswer, attempts, viewModel.getTimeInSeconds(), guesses.toList())
            }
            is GameState.GameOver -> {
                onNavigateToResult(false, score, correctAnswer, attempts, viewModel.getTimeInSeconds(), guesses.toList())
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level $currentLevel", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "❤️ $remainingAttempts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = String.format("%d:%02d", remainingTime / 60, remainingTime % 60),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "⭐ $score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hints List
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(hints) { hint ->
                        HintCard(hint = hint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickerValues.forEachIndexed { index, value ->
                    NumberPickerWheel(
                        value = value,
                        onValueChange = { newValue ->
                            val newList = pickerValues.toMutableList()
                            newList[index] = newValue
                            pickerValues = newList
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    val guess = pickerValues.joinToString("")
                    if (guess.length != expectedLength) return@Button
                    
                    if (guess.toSet().size != expectedLength) {
                        Toast.makeText(context, context.getString(R.string.toast_invalid_guess), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (guesses.contains(guess)) {
                        Toast.makeText(context, context.getString(R.string.toast_duplicate_guess), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    val result = viewModel.makeGuess(guess)
                    when (result) {
                        is GuessResult.Correct -> {
                            if (currentLevel < GameViewModel.MAX_LEVELS) {
                                viewModel.nextLevel()
                                Toast.makeText(context, "Level Up!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is GuessResult.Wrong -> {
                            Toast.makeText(context, context.getString(R.string.toast_remaining_attempts, remainingAttempts), Toast.LENGTH_SHORT).show()
                        }
                        is GuessResult.Partial -> {
                            // Hint logic is generated in ViewModel and appears in hints list
                        }
                        is GuessResult.Invalid -> {
                            Toast.makeText(context, context.getString(R.string.toast_invalid_guess), Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Reset to 000
                    pickerValues = List(expectedLength) { 0 }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.submit_button),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HintCard(hint: Hint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Guess Digits
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                hint.guess.forEach { char ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Description
            Text(
                text = hint.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NumberPickerWheel(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var scaled by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (scaled) 1.1f else 1f, tween(100))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.3f), RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        IconButton(
            onClick = { 
                scaled = !scaled
                onValueChange((value + 1) % 10) 
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = MaterialTheme.colorScheme.primary)
        }
        
        Text(
            text = value.toString(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(60.dp)
                .scale(scale)
        )
        
        IconButton(
            onClick = { 
                scaled = !scaled
                onValueChange((value - 1 + 10) % 10) 
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
