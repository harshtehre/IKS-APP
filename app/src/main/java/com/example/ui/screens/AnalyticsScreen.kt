package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.components.SacredGeometryBackground
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    userProfile: UserProfile
) {
    val weeklyData = listOf(
        "Mon" to 25,
        "Tue" to 30,
        "Wed" to 15,
        "Thu" to 42,
        "Fri" to 20,
        "Sat" to 35,
        "Sun" to 14
    )

    val animatedChartProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatedChartProgress.animateTo(1f, animationSpec = tween(1200, easing = EaseOutCubic))
    }

    SacredGeometryBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("analytics_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Header ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Progress & Learning Analytics",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Track your learning velocity, domain accuracy, and AI study insights.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }

            // --- 2. Key Metrics Grid ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("Total XP", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${userProfile.xpPoints}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = RadiantGold
                            )
                        )
                        Text("Top 5% in class", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent, fontSize = 9.sp))
                    }

                    GlassCard(modifier = Modifier.weight(1f), borderGlow = true) {
                        Text("Study Streak", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${userProfile.streakDays} Days",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SaffronPrimary
                            )
                        )
                        Text("Active habit 🔥", style = MaterialTheme.typography.labelSmall.copy(color = SaffronLight, fontSize = 9.sp))
                    }

                    GlassCard(modifier = Modifier.weight(1f)) {
                        Text("Avg Quiz Score", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${userProfile.averageQuizScore}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldAccent
                            )
                        )
                        Text("12 Quizzes taken", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp))
                    }
                }
            }

            // --- 3. Custom Canvas Chart: Weekly Study Time ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Learning Time (Minutes)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                        Text(
                            text = "Total: 176 mins",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = RadiantGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Canvas Bar Chart with glowing gradients and rounded tops
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24f
                        val maxMinutes = 50f
                        val barWidth = 32f
                        val step = canvasWidth / weeklyData.size

                        // Draw horizontal subtle grid lines
                        for (level in listOf(0.25f, 0.5f, 0.75f, 1f)) {
                            val y = canvasHeight * (1f - level)
                            drawLine(
                                color = SurfaceCardBorder.copy(alpha = 0.35f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        weeklyData.forEachIndexed { index, (day, minutes) ->
                            val normalizedHeight = (minutes / maxMinutes) * canvasHeight * animatedChartProgress.value
                            val x = index * step + (step - barWidth) / 2f
                            val y = canvasHeight - normalizedHeight

                            val barBrush = if (day == "Thu") {
                                Brush.verticalGradient(
                                    listOf(RadiantGold, SaffronPrimary, SaffronDark),
                                    startY = y,
                                    endY = canvasHeight
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(TealAccent, EmeraldAccent.copy(alpha = 0.8f), SurfaceNavy),
                                    startY = y,
                                    endY = canvasHeight
                                )
                            }

                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, normalizedHeight),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }

                    // Day labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weeklyData.forEach { (day, minutes) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (day == "Thu") FontWeight.Bold else FontWeight.Normal,
                                        color = if (day == "Thu") RadiantGold else TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "${minutes}m",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. Topic Accuracy Breakdown ---
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Domain Mastery Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val domainsAccuracy = listOf(
                        "Indian Mathematics (Ganita)" to 92,
                        "Ayurveda & Health Sciences" to 88,
                        "Indian Philosophy & Nyaya Logic" to 82,
                        "Metallurgy & Material Science" to 78,
                        "Indian Astronomy (Jyotisha)" to 68
                    )

                    domainsAccuracy.forEach { (domain, accuracy) ->
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = domain,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "$accuracy%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (accuracy >= 80) EmeraldAccent else if (accuracy >= 70) RadiantGold else CoralRed
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { accuracy / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (accuracy >= 80) EmeraldAccent else if (accuracy >= 70) RadiantGold else CoralRed,
                                trackColor = SurfaceNavy
                            )
                        }
                    }
                }
            }

            // --- 5. AI Learning Insight ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SaffronPrimary,
                    borderGlow = true
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.sweepGradient(SaffronGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = DeepestVoid, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "IKS AI Diagnostic Insight",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "“You demonstrate exemplary retention in Indian Mathematics (92%) and Ayurveda (88%), while your accuracy in Astronomy quizzes is 68%. We recommend revisiting the 27 Nakshatras and Panchanga calendrical cycles module to achieve uniform scholarly distinction.”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryDark,
                            lineHeight = 22.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }
            }
        }
    }
}
