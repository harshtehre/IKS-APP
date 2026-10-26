package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Course
import com.example.model.DailyIksFact
import com.example.model.UserProfile
import com.example.ui.Screen
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGradientButton
import com.example.ui.components.IksMarkdownText
import com.example.ui.components.SacredGeometryBackground
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    userProfile: UserProfile,
    courses: List<Course>,
    dailyFact: DailyIksFact,
    onCourseClick: (String) -> Unit,
    onNavigate: (Screen) -> Unit,
    onAskAi: (prompt: String) -> Unit,
    onStartDailyQuiz: () -> Unit
) {
    SacredGeometryBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- 1. Hero Greeting Banner (Futuristic Indian Observatory Aesthetic) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF14244E),
                                    Color(0xFF0C1630),
                                    Color(0xFF070D1E)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        RadiantGold.copy(alpha = 0.8f),
                                        TealAccent.copy(alpha = 0.5f),
                                        SaffronPrimary.copy(alpha = 0.7f),
                                        RadiantGold.copy(alpha = 0.8f)
                                    )
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iks_hero_banner_1790663442394),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(26.dp)),
                        alpha = 0.32f
                    )

                    // Subtle specular overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0x99060B18),
                                        Color(0xEE060B18)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = SaffronPrimary.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, RadiantGold.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(RadiantGold)
                                    )
                                    Text(
                                        text = "LEVEL ${userProfile.currentLevel} • ${userProfile.levelTitle.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = RadiantGold,
                                            letterSpacing = 0.6.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                color = SurfaceNavy.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, SurfaceCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.Stars, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${userProfile.xpPoints} XP",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RadiantGold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Namaste, ${userProfile.name.split(" ").firstOrNull() ?: "Scholar"} 🙏",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimaryDark
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "“Learn India’s Knowledge. Understand Its Legacy. Build the Future.”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RadiantGold.copy(alpha = 0.95f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlowingGradientButton(
                                text = "Explore IKS",
                                icon = Icons.Filled.Explore,
                                onClick = { onNavigate(Screen.EXPLORE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_dashboard_explore")
                            )

                            OutlinedButton(
                                onClick = { onNavigate(Screen.AI_TUTOR) },
                                border = BorderStroke(1.2.dp, SaffronPrimary),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = GlassSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("btn_dashboard_ai_mentor")
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask AI Tutor", color = SaffronLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // --- 2. Quick Stats Row with Soft Halo Icons ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Streak Card
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        borderGlow = true
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SaffronPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔥", fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    text = "${userProfile.streakDays} Days",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SaffronPrimary
                                    )
                                )
                                Text("Learning Streak", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp))
                            }
                        }
                    }

                    // Lessons Done
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TealAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "${userProfile.lessonsCompletedCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimaryDark
                                    )
                                )
                                Text("Lessons Done", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp))
                            }
                        }
                    }

                    // Accuracy
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = "${userProfile.averageQuizScore}%",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RadiantGold
                                    )
                                )
                                Text("Quiz Accuracy", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp))
                            }
                        }
                    }
                }
            }

            // --- 3. Daily Learning Goal with Smooth Progress ---
            item {
                val animatedProgress by animateFloatAsState(
                    targetValue = (userProfile.dailyLearnedMinutes.toFloat() / userProfile.dailyTargetMinutes.toFloat()).coerceIn(0f, 1f),
                    animationSpec = tween(1200, easing = EaseOutCubic),
                    label = "daily_goal_anim"
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Timer, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Daily Study Target",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${userProfile.dailyLearnedMinutes} of ${userProfile.dailyTargetMinutes} minutes completed",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(56.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxSize(),
                                color = SaffronPrimary,
                                trackColor = SurfaceNavy,
                                strokeWidth = 5.5.dp
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SaffronPrimary,
                        trackColor = SurfaceNavy
                    )
                }
            }

            // --- 4. Daily IKS Fact Card (With Shimmer Border) ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(RadiantGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "Daily Verified IKS Fact",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RadiantGold
                                )
                            )
                        }

                        Surface(
                            color = SurfaceNavy,
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, SurfaceCardBorder)
                        ) {
                            Text(
                                text = dailyFact.date,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    IksMarkdownText(
                        markdown = dailyFact.factText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryDark,
                            lineHeight = 21.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Source: ${dailyFact.factSource}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldAccent,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onStartDailyQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavy),
                        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_daily_quiz")
                    ) {
                        Icon(Icons.Filled.Quiz, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Answer Today's Question (+30 XP)",
                            color = SaffronLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- 5. Interactive Learning Tools Grid ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Interactive Learning Tools",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnimatedToolButton(
                            title = "Knowledge Map",
                            icon = Icons.Filled.Share,
                            color = TealAccent,
                            onClick = { onNavigate(Screen.KNOWLEDGE_MAP) },
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedToolButton(
                            title = "Timeline",
                            icon = Icons.Filled.HistoryEdu,
                            color = SaffronPrimary,
                            onClick = { onNavigate(Screen.TIMELINE) },
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedToolButton(
                            title = "Flashcards",
                            icon = Icons.Filled.Style,
                            color = EmeraldAccent,
                            onClick = { onNavigate(Screen.FLASHCARDS) },
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedToolButton(
                            title = "Study Plan",
                            icon = Icons.Filled.EventNote,
                            color = RadiantGold,
                            onClick = { onNavigate(Screen.STUDY_PLAN) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- 6. Continue Learning ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continue Learning",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        TextButton(onClick = { onNavigate(Screen.EXPLORE) }) {
                            Text("See All", color = SaffronPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    courses.take(3).forEach { course ->
                        CourseProgressCard(
                            course = course,
                            onClick = { onCourseClick(course.id) }
                        )
                    }
                }
            }

            // --- 7. AI Study Assistant Widget ---
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = SaffronPrimary,
                    borderGlow = true
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(SaffronGradient)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = DeepestVoid, modifier = Modifier.size(22.dp))
                        }

                        Column {
                            Text(
                                text = "IKS AI Study Companion",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            )
                            Text(
                                text = "Powered by classical treatises & generative synthesis",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val promptChips = listOf(
                            "Baudhayana Theorem" to "Explain Baudhayana's theorem in Sulba Sutras and its geometric altar application.",
                            "Aryabhata's Pi formula" to "Explain Aryabhata's Pi formula (3.1416) simply.",
                            "Tridosha in Ayurveda" to "Explain Tridosha physiology with biological analogies.",
                            "5-Step Nyaya Logic" to "Explain the 5-step syllogism of Nyaya epistemology with examples."
                        )

                        items(promptChips) { (label, fullPrompt) ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = SurfaceNavy,
                                border = BorderStroke(1.dp, SurfaceCardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { onAskAi(fullPrompt) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(13.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RadiantGold,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedToolButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "tool_btn_scale"
    )

    Surface(
        modifier = modifier
            .then(
                if (isPressed || scale != 1f) {
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                } else Modifier
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = GlassSurface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextPrimaryDark
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CourseProgressCard(
    course: Course,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceNavy)
                    .border(1.2.dp, SaffronPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = SurfaceNavy,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = course.difficulty.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${course.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { course.progressPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SaffronPrimary,
                        trackColor = SurfaceNavy
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${course.progressPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaffronLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Course",
                tint = SaffronPrimary
            )
        }
    }
}
