package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Flashcard
import com.example.ui.components.GlassCard
import com.example.ui.components.IksMarkdownText
import com.example.ui.theme.*

@Composable
fun FlashcardsScreen(
    flashcards: List<Flashcard>,
    onMasteryChange: (cardId: String, newLevel: Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var currentCardIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    val categories = listOf("All", "Geometry", "Arithmetic", "Calculus", "Physiology", "Surgery", "Logic", "Metallurgy")

    val filteredCards = remember(selectedCategory, flashcards) {
        if (selectedCategory == "All") flashcards else flashcards.filter { it.category == selectedCategory }
    }

    val currentCard = filteredCards.getOrNull(currentCardIndex)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400),
        label = "card_flip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("flashcards_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Header ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Interactive Flashcards",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
            Text(
                text = "Tap to flip. Spaced repetition for rapid recall of IKS treatises, scholars, and proofs.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
            )
        }

        // --- 2. Category Filter Row ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isSelected) SaffronPrimary else SurfaceCardNavy,
                    border = BorderStroke(1.dp, if (isSelected) SaffronPrimary else SurfaceCardBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .clickable {
                            selectedCategory = category
                            currentCardIndex = 0
                            isFlipped = false
                        }
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MidnightNavy else TextPrimaryDark
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filteredCards.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No flashcards found in this category", color = TextSecondaryDark)
            }
        } else if (currentCard != null) {
            // --- 3. Card Counter & Mastery Badge ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Card ${currentCardIndex + 1} of ${filteredCards.size}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SaffronLight,
                        fontWeight = FontWeight.Bold
                    )
                )

                Surface(
                    color = SurfaceNavy,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    val (statusText, statusColor) = when (currentCard.masteryLevel) {
                        2 -> "Mastered" to EmeraldAccent
                        1 -> "Review" to RadiantGold
                        else -> "New" to SaffronPrimary
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // --- 4. The 3D Flipping Flashcard ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { isFlipped = !isFlipped }
                    .background(SurfaceCardNavy)
                    .border(
                        2.dp,
                        if (isFlipped) EmeraldAccent.copy(alpha = 0.6f) else SaffronPrimary.copy(alpha = 0.6f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
                    .testTag("flashcard_flip_box"),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // FRONT SIDE
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            color = SurfaceNavy,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentCard.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SaffronLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = currentCard.frontText,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )
                        )

                        if (currentCard.frontTextHi.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentCard.frontTextHi,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = RadiantGold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Tap to reveal insight",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }
                } else {
                    // BACK SIDE (Mirrored so text reads correctly)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        IksMarkdownText(
                            markdown = currentCard.backText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Source: ${currentCard.sourceRef}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // --- 5. Action Buttons (Review Again / Know It) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onMasteryChange(currentCard.id, 1)
                        isFlipped = false
                        if (currentCardIndex + 1 < filteredCards.size) currentCardIndex++
                    },
                    border = BorderStroke(1.dp, CoralRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_review_again")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = CoralRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Review Again", color = CoralRed, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onMasteryChange(currentCard.id, 2)
                        isFlipped = false
                        if (currentCardIndex + 1 < filteredCards.size) currentCardIndex++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_know_it")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Know It!", color = MidnightNavy, fontWeight = FontWeight.Bold)
                }
            }

            // Next / Prev small controllers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        if (currentCardIndex > 0) {
                            currentCardIndex--
                            isFlipped = false
                        }
                    },
                    enabled = currentCardIndex > 0
                ) {
                    Text("← Previous", color = SaffronLight)
                }

                TextButton(
                    onClick = {
                        if (currentCardIndex + 1 < filteredCards.size) {
                            currentCardIndex++
                            isFlipped = false
                        }
                    },
                    enabled = currentCardIndex + 1 < filteredCards.size
                ) {
                    Text("Next →", color = SaffronLight)
                }
            }
        }
    }
}
