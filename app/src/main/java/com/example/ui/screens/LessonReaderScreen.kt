package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.Course
import com.example.model.Lesson
import com.example.ui.components.GlassCard
import com.example.ui.components.IksMarkdownText
import com.example.ui.theme.*

@Composable
fun LessonReaderScreen(
    lesson: Lesson,
    course: Course?,
    currentLanguage: AppLanguage,
    isCompleted: Boolean,
    isTtsSpeaking: Boolean,
    onCompleteLesson: () -> Unit,
    onTakeQuiz: () -> Unit,
    onAskAi: (prompt: String) -> Unit,
    onAddNote: () -> Unit,
    onBookmark: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeak: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedLanguageTab by remember { mutableStateOf(currentLanguage) }

    val activeContent = remember(selectedLanguageTab, lesson) {
        when (selectedLanguageTab) {
            AppLanguage.HINDI -> if (lesson.contentHindi.isNotBlank()) lesson.contentHindi else lesson.contentMarkdown
            AppLanguage.MARATHI -> if (lesson.contentMarathi.isNotBlank()) lesson.contentMarathi else lesson.contentMarkdown
            AppLanguage.ENGLISH -> lesson.contentMarkdown
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .testTag("lesson_reader_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Breadcrumbs & Top Meta ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 ${course?.title ?: "Indian Knowledge Systems"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SaffronLight,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )

                Surface(
                    color = SurfaceNavy,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Text(
                        text = "⏱ ${lesson.readingTimeMinutes} MIN READ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // --- 2. Lesson Title & Overview ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                Text(
                    text = "${lesson.titleHi} • ${lesson.titleMr}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RadiantGold,
                        fontWeight = FontWeight.Medium
                    )
                )

                Surface(
                    color = SurfaceCardNavy,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SurfaceCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(14.dp)) {
                        IksMarkdownText(
                            markdown = lesson.overview,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimaryDark,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }
        }

        // --- 3. Language Selector & Audio TTS Bar ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = selectedLanguageTab == lang
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SaffronPrimary else SurfaceNavy,
                            border = BorderStroke(1.dp, if (isSelected) SaffronPrimary else SurfaceCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedLanguageTab = lang }
                        ) {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MidnightNavy else TextSecondaryDark
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // TTS Listen Button
                Button(
                    onClick = {
                        if (isTtsSpeaking) onStopSpeak() else onSpeak(lesson.overview + ". " + lesson.contentMarkdown.take(600))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTtsSpeaking) CoralRed else SurfaceNavy
                    ),
                    border = BorderStroke(1.dp, if (isTtsSpeaking) CoralRed else SaffronPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_lesson_tts")
                ) {
                    Icon(
                        imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                        contentDescription = "Audio Listen",
                        tint = if (isTtsSpeaking) Color.White else SaffronPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTtsSpeaking) "Stop" else "Listen",
                        color = if (isTtsSpeaking) Color.White else SaffronLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 4. Floating / Quick AI Tools Bar ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderGlow = true
            ) {
                Text(
                    text = "AI Study Assistant & Tools",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaffronLight
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val actions = listOf(
                        Triple("Explain Simply", Icons.Filled.Lightbulb) {
                            onAskAi("Explain the lesson '${lesson.title}' in very simple language for a beginner with real-life analogies.")
                        },
                        Triple("Summarize", Icons.Filled.Summarize) {
                            onAskAi("Provide a 60-second high-yield summary of '${lesson.title}' with 4 bullet points.")
                        },
                        Triple("Take Quiz", Icons.Filled.Quiz) {
                            onTakeQuiz()
                        },
                        Triple("Add Note", Icons.Filled.NoteAdd) {
                            onAddNote()
                        },
                        Triple("Bookmark", Icons.Outlined.BookmarkBorder) {
                            onBookmark()
                        }
                    )

                    items(actions) { (label, icon, onClick) ->
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = SurfaceNavy,
                            border = BorderStroke(1.dp, SurfaceCardBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { onClick() }
                                .testTag("btn_tool_${label.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Key Concepts Highlight ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Key Concepts to Master",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RadiantGold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                lesson.keyConcepts.forEach { concept ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary)
                        )
                        Text(
                            text = concept,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // --- 6. Main Detailed Lesson Content ---
        item {
            Surface(
                color = SurfaceCardNavy.copy(alpha = 0.9f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    IksMarkdownText(
                        markdown = activeContent,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryDark,
                            lineHeight = 24.sp
                        )
                    )
                }
            }
        }

        // --- 7. Historical Context & Primary Texts ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Historical Context & Transmission",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaffronLight
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                IksMarkdownText(
                    markdown = lesson.historicalContext,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Key Scholars:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        lesson.keyFigures.forEach { scholar ->
                            Text(
                                text = "• $scholar",
                                style = MaterialTheme.typography.labelSmall.copy(color = SaffronPrimary)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Primary Treatises:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        lesson.keyTexts.forEach { textTitle ->
                            Text(
                                text = "• $textTitle",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent)
                            )
                        }
                    }
                }
            }
        }

        // --- 8. Source Citations (Trust Policy) ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Verified Scholarly References",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                lesson.sources.forEach { citation ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "${citation.title} (${citation.publicationYear})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "By ${citation.authorOrEditor} • Publisher: ${citation.institutionOrPublisher}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                        )
                    }
                }
            }
        }

        // --- 9. Bottom Actions: Mark Complete & Quiz ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onCompleteLesson,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) EmeraldAccent else SaffronPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_complete_lesson")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Done,
                        contentDescription = null,
                        tint = MidnightNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCompleted) "Lesson Completed (+50 XP)" else "Mark as Completed (+50 XP)",
                        color = MidnightNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onTakeQuiz,
                    border = BorderStroke(1.dp, SaffronPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_take_quiz_from_lesson")
                ) {
                    Icon(Icons.Filled.Quiz, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Practice Quiz for this Lesson", color = SaffronLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
