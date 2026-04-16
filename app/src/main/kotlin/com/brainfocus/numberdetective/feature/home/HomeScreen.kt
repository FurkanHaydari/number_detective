package com.brainfocus.numberdetective.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.brainfocus.numberdetective.R
import com.brainfocus.numberdetective.core.designsystem.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onPlayClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    currentLanguage: String
) {
    val highScore by viewModel.highScore.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Layer 1: Atmospheric Background ---
        Image(
            painter = painterResource(id = R.drawable.detective_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // --- Layer 2: Deep Gradient Overlay ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundOverlayGradient)
        )

        // --- Layer 3: UI Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- Section 1: Top (Language & Title) ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    LanguageButton(
                        text = "TR",
                        isSelected = currentLanguage == "tr",
                        onClick = { onLanguageChange("tr") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LanguageButton(
                        text = "EN",
                        isSelected = currentLanguage == "en",
                        onClick = { onLanguageChange("en") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedVisibility(
                    visible = highScore > 0,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -it }
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = RowDefaults.CardBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.score_text, highScore),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.app_title_1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = stringResource(R.string.app_title_2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = PrimaryCyan
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center: Glass HUD Briefing
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 400))
            ) {
                MissionBriefingPanel()
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom: Magnetic Play Button
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 800)) + 
                        slideInVertically(animationSpec = tween(1200, delayMillis = 800)) { it / 3 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayButton(onClick = onPlayClick)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Reserve space for the ad to prevent layout jumps/shifts
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp), // Common banner height
                        contentAlignment = Alignment.Center
                    ) {
                        com.brainfocus.numberdetective.core.designsystem.BannerAd()
                    }
                }
            }
        }
    }
}

@Composable
fun MissionBriefingPanel() {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        border = RowDefaults.CardBorder
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_mission_title).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryCyan,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            BriefingRow(
                icon = "🔍",
                title = stringResource(R.string.home_step_1_title),
                description = stringResource(R.string.home_step_1_desc)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BriefingRow(
                icon = "🧠",
                title = stringResource(R.string.home_step_2_title),
                description = stringResource(R.string.home_step_2_desc)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BriefingRow(
                icon = "🎯",
                title = stringResource(R.string.home_step_3_title),
                description = stringResource(R.string.home_step_3_desc)
            )
        }
    }
}

@Composable
fun BriefingRow(icon: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PlayButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .border(2.dp, PrimaryCyan.copy(alpha = glowAlpha), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
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
                stringResource(R.string.start_button),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun LanguageButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) PrimaryCyan.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) RowDefaults.CardBorder else null
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isSelected) PrimaryCyan else Color.White.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

object RowDefaults {
    val CardBorder @Composable get() = androidx.compose.foundation.BorderStroke(
        width = 1.dp,
        brush = CardBorderGradient
    )
}
