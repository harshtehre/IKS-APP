package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KnowledgeNode
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGradientButton
import com.example.ui.components.SacredGeometryBackground
import com.example.ui.theme.*

@Composable
fun KnowledgeMapScreen(
    nodes: List<KnowledgeNode>,
    selectedNode: KnowledgeNode?,
    onSelectNode: (KnowledgeNode?) -> Unit,
    onAskAiAboutNode: (String) -> Unit
) {
    var filterCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Domain", "Scholar", "Text", "Concept", "Innovation")

    val filteredNodes = remember(filterCategory, nodes) {
        if (filterCategory == "All") nodes else nodes.filter { it.category == filterCategory }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "graph_ambient")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "line_pulse"
    )

    SacredGeometryBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("knowledge_map_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- 1. Header ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Interactive Knowledge Map",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Discover organic interconnections between ancient sciences, scholars, primary manuscripts, and discoveries.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            // --- 2. Category Filter Chips ---
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == filterCategory
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) SaffronPrimary else SurfaceCardNavy,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) RadiantGold else SurfaceCardBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { filterCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) DeepestVoid else TextPrimaryDark
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // --- 3. Interactive Graphical Grid Canvas ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(GlassSurface)
                    .border(
                        BorderStroke(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    SurfaceCardBorder,
                                    SaffronPrimary.copy(alpha = 0.35f),
                                    SurfaceCardBorder
                                )
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                // Background Canvas drawing connecting lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    nodes.forEach { node ->
                        val startOffset = Offset(node.xOffset * w, node.yOffset * h)
                        node.connectedNodeIds.forEach { targetId ->
                            val targetNode = nodes.find { it.id == targetId }
                            if (targetNode != null) {
                                val endOffset = Offset(targetNode.xOffset * w, targetNode.yOffset * h)
                                val isConnectedToSelected = selectedNode?.id == node.id || selectedNode?.id == targetId

                                drawLine(
                                    color = if (isConnectedToSelected) SaffronPrimary.copy(alpha = 0.85f) else SurfaceCardBorder.copy(alpha = pulseAlpha),
                                    start = startOffset,
                                    end = endOffset,
                                    strokeWidth = if (isConnectedToSelected) 3f else 1.5f
                                )
                            }
                        }
                    }
                }

                // Interactive Nodes Layer
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val boxWidth = maxWidth
                    val boxHeight = maxHeight

                    filteredNodes.forEach { node ->
                        val isSelected = selectedNode?.id == node.id
                        val nodeColor = when (node.category) {
                            "Domain" -> SaffronPrimary
                            "Scholar" -> RadiantGold
                            "Text" -> EmeraldAccent
                            "Concept" -> TealAccent
                            else -> SaffronLight
                        }

                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (boxWidth * node.xOffset - 48.dp).coerceAtLeast(8.dp),
                                    y = (boxHeight * node.yOffset - 20.dp).coerceAtLeast(8.dp)
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.graphicsLayer {
                                            scaleX = 1.1f
                                            scaleY = 1.1f
                                        }
                                    } else Modifier
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectNode(if (isSelected) null else node) }
                                .background(if (isSelected) nodeColor else GlassSurfaceElevated)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) RadiantGold else nodeColor.copy(alpha = 0.6f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("node_${node.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) DeepestVoid else nodeColor)
                                )
                                Text(
                                    text = node.label.split(" ").take(2).joinToString(" "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) DeepestVoid else TextPrimaryDark
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. Node Detail Sheet (Animated Reveal) ---
            AnimatedVisibility(
                visible = selectedNode != null,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut()
            ) {
                selectedNode?.let { node ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderGlow = true
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = node.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SaffronLight
                                    )
                                )
                                Text(
                                    text = "${node.labelHi} • ${node.category.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RadiantGold,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = { onSelectNode(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = node.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimaryDark,
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connected to ${node.connectedNodeIds.size} related concepts",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )

                            Button(
                                onClick = { onAskAiAboutNode("Explain the role of ${node.label} in Indian Knowledge Systems and how it connects to related fields.") },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = DeepestVoid, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask AI Tutor", color = DeepestVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
