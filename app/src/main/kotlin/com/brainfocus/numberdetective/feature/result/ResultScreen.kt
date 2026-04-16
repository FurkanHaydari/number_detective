package com.brainfocus.numberdetective.feature.result

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*
import com.brainfocus.numberdetective.feature.home.RowDefaults

@Composable
fun ResultScreen(
    isWin: Boolean,
    score: Int,
    correctAnswer: String,
    attempts: Int,
    timeInSeconds: Int,
    guesses: List<String>,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Layer 1: Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isWin) {
                        Brush.radialGradient(
                            colors = listOf(PrimaryCyan.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.9f)),
                            radius = 1500f
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), ErrorRed.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.95f))
                        )
                    }
                )
        )

        // --- Layer 2: Debriefing Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cinematic Header
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { -it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isWin) "MISSION ACCOMPLISHED" else "MISSION FAILED",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = Montserrat,
                            fontSize = 28.sp,
                            letterSpacing = 2.sp
                        ),
                        color = if (isWin) PrimaryCyan else ErrorRed,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (if (isWin) stringResource(R.string.win_motivation) else stringResource(R.string.lose_motivation)).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Debrief Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000, 400)) + scaleIn(initialScale = 0.95f)
            ) {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(28.dp),
                    border = RowDefaults.CardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FINAL SCORE",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PrimaryCyan
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DebriefStat(stringResource(R.string.correct_answer), correctAnswer)
                            DebriefStat(stringResource(R.string.attempts), attempts.toString())
                            DebriefStat(stringResource(R.string.time), formattedTime)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guess History List
            Text(
                text = "INTERROGATION LOG",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.Start),
                letterSpacing = 1.sp
            )
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(guesses.size) { index ->
                    Text(
                        text = "${index + 1}. ANALYZED: ${guesses[index]}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Poppins),
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share
                ResultActionButton(
                    text = stringResource(R.string.share_button).uppercase(),
                    isPrimary = false,
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_score_message, score, attempts, formattedTime))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_score_title)))
                    }
                )

                // Play Again
                ResultActionButton(
                    text = stringResource(R.string.play_again_button).uppercase(),
                    isPrimary = true,
                    onClick = onPlayAgain
                )

                TextButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.back_to_menu).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 1.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun DebriefStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
fun ResultActionButton(text: String, isPrimary: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                1.dp,
                if (isPrimary) PrimaryCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isPrimary) PlayButtonGradient else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.02f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Montserrat,
                    letterSpacing = 1.sp
                ),
                color = Color.White
            )
        }
    }
}
