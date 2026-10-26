package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.ChatMessage
import com.example.ui.components.AIMessageRenderer
import com.example.ui.components.IksLogo
import com.example.ui.components.IksMarkdownText
import com.example.ui.components.SacredGeometryBackground
import com.example.ui.theme.*

@Composable
fun AiMentorScreen(
    messages: List<ChatMessage>,
    isThinking: Boolean,
    currentMode: String,
    inputText: String,
    currentLanguage: AppLanguage,
    onModeChange: (String) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onVoiceInputClick: () -> Unit
) {
    val modes = listOf("Teach Me", "Exam Mode", "Beginner Mode", "Deep Dive", "Quick Revision", "Quiz Me")
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    SacredGeometryBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_mentor_screen")
        ) {
            // --- 1. Mode Selector Banner ---
            Surface(
                color = SurfaceNavy,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SurfaceCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🧠", fontSize = 12.sp)
                            Text(
                                text = "Learning Mode:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronLight,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            color = SurfaceCardNavy,
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Gemini AI • Verified Sources",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = RadiantGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(modes) { mode ->
                            val isSelected = mode == currentMode
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (isSelected) SaffronPrimary else SurfaceCardNavy,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) RadiantGold else SurfaceCardBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .clickable { onModeChange(mode) }
                                    .testTag("ai_mode_$mode")
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) DeepestVoid else TextPrimaryDark
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. Chat Messages Stream ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        onSuggestionClick = { onSendMessage(it) },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("IKS AI Answer", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                if (isThinking) {
                    item {
                        ThinkingAnimation()
                    }
                }
            }

            // --- 3. Quick Chips Bar ---
            Surface(
                color = GlassSurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickChips = listOf(
                        "Explain Baudhayana Theorem",
                        "Who was Aryabhata?",
                        "What is Tridosha?",
                        "5-Step Nyaya Logic",
                        "Zero in Brahmasphutasiddhanta",
                        "Madhava's Infinite Series"
                    )

                    items(quickChips) { chip ->
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = SurfaceNavy,
                            border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { onSendMessage(chip) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("✦", color = RadiantGold, fontSize = 10.sp)
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = RadiantGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. Input Row ---
            Surface(
                color = DeepNavy.copy(alpha = 0.98f),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onVoiceInputClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceNavy)
                            .border(1.dp, SaffronPrimary.copy(alpha = 0.4f), CircleShape)
                            .testTag("btn_voice_input")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = SaffronPrimary
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        placeholder = {
                            Text(
                                text = "Ask anything about Indian Knowledge...",
                                color = TextSecondaryDark,
                                fontSize = 13.sp
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = SurfaceCardNavy,
                            unfocusedContainerColor = SurfaceCardNavy,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isThinking) {
                                onSendMessage(inputText)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && !isThinking) {
                                    Brush.sweepGradient(SaffronGradient)
                                } else {
                                    Brush.linearGradient(listOf(SurfaceNavy, SurfaceNavy))
                                }
                            )
                            .testTag("btn_ai_send")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isThinking) DeepestVoid else TextSecondaryDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse),
        label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse),
        label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse),
        label = "d3"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = GlassSurfaceElevated,
        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Consulting classical manuscripts",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SaffronLight,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(5.dp).graphicsLayer { alpha = dot1 }.clip(CircleShape).background(SaffronPrimary))
                Box(modifier = Modifier.size(5.dp).graphicsLayer { alpha = dot2 }.clip(CircleShape).background(SaffronPrimary))
                Box(modifier = Modifier.size(5.dp).graphicsLayer { alpha = dot3 }.clip(CircleShape).background(SaffronPrimary))
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onSuggestionClick: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            ) {
                IksLogo(size = 18.dp)
                Text(
                    text = "IKS AI Mentor (${message.mode})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadiantGold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) SaffronPrimary else GlassSurfaceElevated,
            border = if (!isUser) {
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            SurfaceCardBorder,
                            SaffronPrimary.copy(alpha = 0.35f),
                            SurfaceCardBorder
                        )
                    )
                )
            } else null,
            tonalElevation = 4.dp,
            modifier = if (isUser) {
                Modifier.widthIn(max = 300.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isUser) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DeepestVoid,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp
                        )
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onCopy(message.content) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy text",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    AIMessageRenderer(
                        content = message.content,
                        isAiResponse = true,
                        onActionClick = onSuggestionClick
                    )

                    if (!message.recommendedNextActivity.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = SurfaceNavy,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🎯", fontSize = 14.sp)
                                Column {
                                    Text(
                                        text = "Recommended Next Learning Step",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = EmeraldAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    IksMarkdownText(
                                        markdown = message.recommendedNextActivity,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimaryDark,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (message.quickSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Follow up with AI:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SaffronLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        message.quickSuggestions.forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceNavy,
                                border = BorderStroke(1.dp, SurfaceCardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSuggestionClick(suggestion) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = RadiantGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
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
