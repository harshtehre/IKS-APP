package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.UserProfile
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfile,
    onLanguageChangeClick: () -> Unit,
    onOpenTrustDialog: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSyncFirestore: () -> Unit = {}
) {
    var dailyGoal by remember { mutableStateOf(userProfile.dailyTargetMinutes.toFloat()) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("profile_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Student Identity Card ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SaffronPrimary,
                borderGlow = true
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(SaffronPrimary, SaffronDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.take(2).uppercase(),
                            color = MidnightNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = userProfile.email,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                        )
                        Text(
                            text = "${userProfile.educationLevel} • ${userProfile.college}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronLight,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Scholar Level", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Text(
                            text = "Level ${userProfile.currentLevel} (${userProfile.levelTitle})",
                            style = MaterialTheme.typography.titleSmall.copy(color = RadiantGold, fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total XP", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Text(
                            text = "${userProfile.xpPoints} XP",
                            style = MaterialTheme.typography.titleSmall.copy(color = RadiantGold, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // --- 2. Learning Preferences & Goal Slider ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daily Study Target",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                Text(
                    text = "Target: ${dailyGoal.toInt()} minutes per day",
                    style = MaterialTheme.typography.bodySmall.copy(color = SaffronLight)
                )

                Slider(
                    value = dailyGoal,
                    onValueChange = { dailyGoal = it },
                    valueRange = 10f..60f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = SaffronPrimary,
                        activeTrackColor = SaffronPrimary,
                        inactiveTrackColor = SurfaceNavy
                    )
                )
            }
        }

        // --- 3. App Settings Options ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Preferences & Trust",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                // Language Option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLanguageChangeClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCardNavy,
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = SaffronPrimary)
                            Text("Language", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark))
                        }
                        Text(userProfile.preferredLanguage.displayName, style = MaterialTheme.typography.labelMedium.copy(color = RadiantGold, fontWeight = FontWeight.Bold))
                    }
                }

                // Trust & Academic Rigor Option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenTrustDialog() },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCardNavy,
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent)
                            Text("Academic Citations & Rigor Statement", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryDark)
                    }
                }

                // Admin / Content Management Option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenAdmin() },
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCardNavy,
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RadiantGold)
                            Text("Admin & Curriculum Panel", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondaryDark)
                    }
                }
            }
        }

        // --- 4. Firebase Auth & Firestore Sync Card ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = TealAccent,
                borderGlow = true
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = TealAccent, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Sync & Firebase Auth",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                        Text(
                            text = "Securely persist your student profile, quiz scores, and spaced-repetition cards to Firebase Firestore.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isSyncing = true
                            onSyncFirestore()
                            syncSuccess = true
                            isSyncing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavy),
                        border = BorderStroke(1.dp, TealAccent.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (syncSuccess) Icons.Default.CheckCircle else Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (syncSuccess) EmeraldAccent else TealAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (syncSuccess) "Synced to Firestore" else "Sync with Cloud",
                            color = TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 5. About IKSphere ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "About IKSphere",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaffronLight
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“Learn India’s Knowledge. Understand Its Legacy. Build the Future.”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TextPrimaryDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "IKSphere is a production educational platform bringing millennia of Indian scientific, philosophical, mathematical, and medical traditions to college and school students through modern pedagogy and Gemini AI assistance.",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, lineHeight = 16.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Version 1.0.0 • Verified IKS Academic Standards",
                    style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent, fontSize = 10.sp)
                )
            }
        }
    }
}
