package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StudyPlanItem
import com.example.ui.components.GlassCard
import com.example.ui.components.IksMarkdownText
import com.example.ui.theme.*

@Composable
fun StudyPlanScreen(
    studyPlan: List<StudyPlanItem>,
    dailyTargetMinutes: Int,
    onToggleItem: (String) -> Unit,
    onRegeneratePlan: () -> Unit
) {
    val completedCount = studyPlan.count { it.isCompleted }
    val progress = if (studyPlan.isNotEmpty()) completedCount.toFloat() / studyPlan.size.toFloat() else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("study_plan_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header & Summary ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Personalized IKS Study Plan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "AI-generated structured schedule designed around your daily $dailyTargetMinutes-minute goal.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }
        }

        // --- 2. Progress Overview Card ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly Schedule Progress",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                        Text(
                            text = "$completedCount of ${studyPlan.size} Sessions Completed",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )
                    }

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SaffronPrimary,
                    trackColor = SurfaceNavy
                )
            }
        }

        // --- 3. Daily Schedule Checklist ---
        items(studyPlan) { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onToggleItem(item.id) },
                shape = RoundedCornerShape(14.dp),
                color = SurfaceCardNavy,
                border = BorderStroke(
                    1.dp,
                    if (item.isCompleted) EmeraldAccent.copy(alpha = 0.5f) else SurfaceCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { onToggleItem(item.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldAccent,
                            uncheckedColor = TextSecondaryDark,
                            checkmarkColor = MidnightNavy
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.dayOfWeek.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronLight,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = "• ${item.timeMinutes} MIN",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        IksMarkdownText(
                            markdown = item.subjectName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (item.isCompleted) TextSecondaryDark else TextPrimaryDark
                            )
                        )

                        Text(
                            text = item.activityType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (item.isCompleted) EmeraldAccent else RadiantGold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // --- 4. Regenerate Button ---
        item {
            Button(
                onClick = onRegeneratePlan,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavy),
                border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SaffronPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Regenerate AI Study Plan", color = SaffronLight, fontWeight = FontWeight.Bold)
            }
        }
    }
}
