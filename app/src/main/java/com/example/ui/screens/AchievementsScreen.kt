package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>
) {
    val unlockedCount = achievements.count { it.isUnlocked }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("achievements_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Achievements & Vidya Badges",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Earn scholarly milestones and XP by completing lessons, retaining knowledge, and acing quizzes.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }
        }

        // Summary Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Milestones Unlocked",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                        Text(
                            text = "$unlockedCount of ${achievements.size} badges earned",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(SurfaceNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$unlockedCount/${achievements.size}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RadiantGold
                            )
                        )
                    }
                }
            }
        }

        items(achievements) { ach ->
            val iconVector: ImageVector = when (ach.iconName) {
                "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
                "Calculate" -> Icons.Default.Calculate
                "Explore" -> Icons.Default.Explore
                "Healing" -> Icons.Default.Healing
                "Psychology" -> Icons.Default.Psychology
                "Style" -> Icons.Default.Style
                "EmojiEvents" -> Icons.Default.EmojiEvents
                else -> Icons.Default.School
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = if (ach.isUnlocked) SurfaceCardNavy else SurfaceNavy.copy(alpha = 0.6f),
                border = BorderStroke(
                    1.dp,
                    if (ach.isUnlocked) SaffronPrimary.copy(alpha = 0.5f) else SurfaceCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (ach.isUnlocked) SaffronPrimary.copy(alpha = 0.2f) else SurfaceNavy
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ach.isUnlocked) iconVector else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) RadiantGold else TextSecondaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = ach.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (ach.isUnlocked) TextPrimaryDark else TextSecondaryDark
                                )
                            )

                            if (ach.isUnlocked) {
                                Surface(
                                    color = EmeraldAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "UNLOCKED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldAccent,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = ach.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "+${ach.xpReward} XP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RadiantGold
                                )
                            )
                            if (ach.isUnlocked) {
                                Text(
                                    text = "• Earned on ${ach.unlockedAt}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 10.sp
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
