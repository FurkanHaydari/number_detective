package com.brainfocus.numberdetective.feature.game

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.data.model.GameState
import com.brainfocus.numberdetective.data.model.GuessResult
import com.brainfocus.numberdetective.data.model.Hint
import com.brainfocus.numberdetective.feature.home.RowDefaults

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
    val currentReport by viewModel.currentReport.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val attempts = viewModel.attempts

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

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Layer 1: Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(if (currentReport != null) 8.dp else 0.dp), // Dynamic blur
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (currentReport != null) 0.9f else 0.85f))
        )

        // --- Layer 2: UI Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameTopBar(level = currentLevel)
            Spacer(modifier = Modifier.height(16.dp))
            StatsDashboard(attempts = remainingAttempts, time = remainingTime, score = score)
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (hints.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("NO EVIDENCE YET...", color = TextSecondary.copy(alpha = 0.3f), letterSpacing = 2.sp)
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(hints.reversed()) { hint -> HintCard(hint = hint) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickerValues.forEachIndexed { index, value ->
                    NumberVaultPicker(
                        value = value,
                        onValueChange = { newValue ->
                            if (!isPaused) {
                                val newList = pickerValues.toMutableList()
                                newList[index] = newValue
                                pickerValues = newList
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            GuessButton(
                enabled = !isPaused,
                onClick = {
                    val guess = pickerValues.joinToString("")
                    if (guess.length != expectedLength) return@GuessButton
                    
                    // Trigger analysis & validation
                    val result = viewModel.makeGuess(guess)
                    
                    // Only reset picker if the guess was actually analyzed (not a validation error)
                    if (result != GuessResult.Invalid) {
                        pickerValues = List(expectedLength) { 0 }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Layer 3: Field Report Overlay ---
        AnimatedVisibility(
            visible = currentReport != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            currentReport?.let { report ->
                FieldReportOverlay(
                    report = report,
                    onDismiss = { viewModel.dismissReport() }
                )
            }
        }
    }
}

@Composable
fun FieldReportOverlay(report: FieldReport, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            color = SurfaceCard,
            shape = RoundedCornerShape(28.dp),
            border = RowDefaults.CardBorder
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (report.isPositive) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .border(1.dp, if (report.isPositive) SuccessGreen else ErrorRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (report.isPositive) "🎖️" else "⚠️", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = report.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = Montserrat),
                    color = if (report.isPositive) PrimaryCyan else ErrorRed,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = report.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, PrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PlayButtonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CONTINUE MISSION",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Montserrat,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameTopBar(level: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CASE FILE: LEVEL $level",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = Montserrat,
                letterSpacing = 2.sp,
                fontSize = 16.sp
            ),
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatsDashboard(attempts: Int, time: Int, score: Int) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(20.dp),
        border = RowDefaults.CardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "LIVES", value = "x$attempts", color = ErrorRed)
            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            StatItem(label = "TIME", value = String.format("%02d:%02d", time / 60, time % 60), color = PrimaryCyan)
            VerticalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.White.copy(alpha = 0.1f))
            StatItem(label = "SCORE", value = score.toString(), color = SuccessGreen)
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 10.sp)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HintCard(hint: Hint) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = RowDefaults.CardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                hint.guess.forEach { char ->
                    Box(
                        modifier = Modifier.size(34.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = char.toString(), style = MaterialTheme.typography.titleMedium, color = PrimaryCyan)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = hint.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f), lineHeight = 18.sp)
        }
    }
}

@Composable
fun NumberVaultPicker(value: Int, onValueChange: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp).background(SurfaceCard, RoundedCornerShape(16.dp)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(vertical = 8.dp)
    ) {
        IconButton(onClick = { onValueChange((value + 1) % 10) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
        }
        Text(text = value.toString(), style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
        IconButton(onClick = { onValueChange((value - 1 + 10) % 10) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun GuessButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, PrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent)
    ) {
        Box(
            modifier = if (enabled) {
                Modifier.fillMaxSize().background(PlayButtonGradient)
            } else {
                Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f))
            },
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(R.string.submit_button).uppercase(), style = MaterialTheme.typography.titleMedium.copy(fontFamily = Montserrat, letterSpacing = 2.sp), color = if (enabled) Color.White else Color.Gray)
        }
    }
}
