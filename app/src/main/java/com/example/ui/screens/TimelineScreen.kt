package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimelineEvent
import com.example.ui.components.GlassCard
import com.example.ui.components.SacredGeometryBackground
import com.example.ui.theme.*

@Composable
fun TimelineScreen(
    events: List<TimelineEvent>,
    onAskAiAboutEvent: (String) -> Unit
) {
    var expandedEventId by remember { mutableStateOf<String?>(null) }

    SacredGeometryBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("timeline_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Historical Knowledge Timeline",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Trace 4,500 years of intellectual heritage from Harappan engineering to Classical astronomy and Kerala calculus.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }

            items(events) { event ->
                val isExpanded = expandedEventId == event.id

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Vertical Timeline Spine with Glowing Indicator Node
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isExpanded) SaffronPrimary else SurfaceNavy)
                                .border(
                                    1.5.dp,
                                    if (isExpanded) RadiantGold else SaffronPrimary.copy(alpha = 0.6f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = if (isExpanded) DeepestVoid else SaffronPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(if (isExpanded) 220.dp else 130.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            SaffronPrimary.copy(alpha = 0.5f),
                                            SurfaceCardBorder.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                    }

                    // Event Card
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                expandedEventId = if (isExpanded) null else event.id
                            },
                        borderGlow = isExpanded
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SurfaceNavy,
                                shape = RoundedCornerShape(100.dp),
                                border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = event.period,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RadiantGold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = event.eraName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                lineHeight = 19.sp
                            )
                        )

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Divider(color = SurfaceCardBorder)

                                if (event.scholars.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Key Figures:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SaffronLight))
                                        Text(event.scholars.joinToString(), style = MaterialTheme.typography.labelSmall.copy(color = TextPrimaryDark))
                                    }
                                }

                                if (event.primaryTexts.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Primary Texts:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldAccent))
                                        Text(event.primaryTexts.joinToString(), style = MaterialTheme.typography.labelSmall.copy(color = TextPrimaryDark))
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(13.dp))
                                    Text("Citation: ${event.sourceCitation}", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent, fontSize = 10.sp))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { onAskAiAboutEvent("Tell me more about the historical significance of ${event.title} in Indian Knowledge Systems.") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavy),
                                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ask IKS Mentor About This Era", color = SaffronLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
