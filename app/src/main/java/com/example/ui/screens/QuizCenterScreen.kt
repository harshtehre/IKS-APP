package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuestionType
import com.example.model.QuizQuestion
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGradientButton
import com.example.ui.components.IksMarkdownText
import com.example.ui.components.SacredGeometryBackground
import com.example.ui.theme.*

@Composable
fun QuizCenterScreen(
    questions: List<QuizQuestion>,
    currentIndex: Int,
    selectedOptionIndex: Int?,
    isAnswerSubmitted: Boolean,
    score: Int,
    isQuizFinished: Boolean,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestartQuiz: () -> Unit,
    onExploreMore: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightNavy),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading quiz questions...", color = TextSecondaryDark)
        }
        return
    }

    if (isQuizFinished) {
        QuizResultView(
            totalQuestions = questions.size,
            score = score,
            onRestart = onRestartQuiz,
            onExploreMore = onExploreMore
        )
        return
    }

    val currentQuestion = questions.getOrNull(currentIndex) ?: return

    val animatedProgress by animateFloatAsState(
        targetValue = (currentIndex + 1).toFloat() / questions.size.toFloat(),
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "quiz_progress"
    )

    SacredGeometryBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("quiz_center_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Progress & Score Header ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SurfaceNavy,
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronLight,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        color = SurfaceNavy,
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, RadiantGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Filled.Stars, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Score: $score",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RadiantGold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SaffronPrimary,
                    trackColor = SurfaceNavy
                )
            }

            // --- 2. Question Card ---
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
                        Surface(
                            color = SurfaceNavy,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentQuestion.difficulty.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = when (currentQuestion.difficulty) {
                                        "Hard" -> CoralRed
                                        "Medium" -> RadiantGold
                                        else -> EmeraldAccent
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = when (currentQuestion.type) {
                                QuestionType.TRUE_FALSE -> "TRUE / FALSE"
                                QuestionType.MATCH -> "MATCH THE FOLLOWING"
                                else -> "MULTIPLE CHOICE"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            lineHeight = 24.sp
                        )
                    )

                    if (currentQuestion.questionTextHi.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQuestion.questionTextHi,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RadiantGold.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // --- 3. Options List ---
            items(currentQuestion.options.size) { optIndex ->
                val option = currentQuestion.options[optIndex]
                val isSelected = selectedOptionIndex == optIndex
                val isCorrect = optIndex == currentQuestion.correctOptionIndex

                val optionBg = when {
                    !isAnswerSubmitted -> if (isSelected) GlassSurfaceElevated else GlassSurface
                    isCorrect -> EmeraldAccent.copy(alpha = 0.22f)
                    isSelected && !isCorrect -> CoralRed.copy(alpha = 0.22f)
                    else -> GlassSurface
                }

                val optionBorder = when {
                    !isAnswerSubmitted -> if (isSelected) SaffronPrimary else SurfaceCardBorder
                    isCorrect -> EmeraldAccent
                    isSelected && !isCorrect -> CoralRed
                    else -> SurfaceCardBorder
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    label = "option_scale"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isPressed || scale != 1f) {
                                Modifier.graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                            } else Modifier
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            enabled = !isAnswerSubmitted,
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectOption(optIndex)
                        }
                        .testTag("quiz_option_$optIndex"),
                    shape = RoundedCornerShape(16.dp),
                    color = optionBg,
                    border = BorderStroke(1.2.dp, optionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isAnswerSubmitted && isCorrect -> EmeraldAccent
                                        isAnswerSubmitted && isSelected && !isCorrect -> CoralRed
                                        isSelected -> SaffronPrimary
                                        else -> SurfaceCardNavy
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${('A'.code + optIndex).toChar()}",
                                color = if (isSelected || (isAnswerSubmitted && isCorrect)) DeepestVoid else TextSecondaryDark,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimaryDark,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (isAnswerSubmitted) {
                            if (isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = EmeraldAccent, modifier = Modifier.size(22.dp))
                            } else if (isSelected) {
                                Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = CoralRed, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            // --- 4. Answer Explanation (Pulsing feedback) ---
            if (isAnswerSubmitted) {
                item {
                    val isUserCorrect = selectedOptionIndex == currentQuestion.correctOptionIndex

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isUserCorrect) EmeraldAccent else CoralRed,
                        borderGlow = true
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isUserCorrect) EmeraldAccent else CoralRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (isUserCorrect) "Correct! +30 XP" else "Incorrect",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserCorrect) EmeraldAccent else CoralRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        IksMarkdownText(
                            markdown = currentQuestion.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimaryDark,
                                lineHeight = 20.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Source: ${currentQuestion.sourceRef}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = EmeraldAccent,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // --- 5. Submit or Next Button ---
            item {
                if (!isAnswerSubmitted) {
                    GlowingGradientButton(
                        text = "Submit Answer",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSubmitAnswer()
                        },
                        enabled = selectedOptionIndex != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_quiz_answer")
                    )
                } else {
                    GlowingGradientButton(
                        text = if (currentIndex + 1 < questions.size) "Next Question →" else "Finish & View Results 🎉",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNextQuestion()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_next_quiz_question")
                    )
                }
            }
        }
    }
}

@Composable
fun QuizResultView(
    totalQuestions: Int,
    score: Int,
    onRestart: () -> Unit,
    onExploreMore: () -> Unit
) {
    val accuracy = if (totalQuestions > 0) (score * 100 / totalQuestions) else 0

    val animatedAccuracy by animateFloatAsState(
        targetValue = accuracy / 100f,
        animationSpec = tween(1400, easing = EaseOutCubic),
        label = "result_acc"
    )

    SacredGeometryBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .testTag("quiz_result_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(SaffronGradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (accuracy >= 70) Icons.Default.EmojiEvents else Icons.Default.School,
                        contentDescription = null,
                        tint = DeepestVoid,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            item {
                Text(
                    text = if (accuracy >= 80) "Outstanding Mastery! 🏆" else if (accuracy >= 50) "Commendable Effort! 🌟" else "Keep Practicing! 📚",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )
                )

                Text(
                    text = "You answered $score of $totalQuestions questions correctly ($accuracy%)",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderGlow = true
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$score", style = MaterialTheme.typography.displayMedium.copy(color = EmeraldAccent, fontWeight = FontWeight.ExtraBold))
                            Text(text = "Correct", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${totalQuestions - score}", style = MaterialTheme.typography.displayMedium.copy(color = CoralRed, fontWeight = FontWeight.ExtraBold))
                            Text(text = "Incorrect", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "+${score * 30}", style = MaterialTheme.typography.displayMedium.copy(color = RadiantGold, fontWeight = FontWeight.ExtraBold))
                            Text(text = "XP Gained", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { animatedAccuracy },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (accuracy >= 70) EmeraldAccent else SaffronPrimary,
                        trackColor = SurfaceNavy
                    )
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "IKS AI Diagnostic Analysis",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IksMarkdownText(
                        markdown = if (accuracy < 70) {
                            "**Recommendation:** Review the *Baudhayana Sulba Sutras* and decimal zero modules. Your conceptual recall of geometric transformation proofs will benefit from quick flashcard revision."
                        } else {
                            "**Exemplary recall!** You demonstrate thorough mastery of classical Indian sciences and historical textual citations. Ready to advance to calculus series."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryDark,
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlowingGradientButton(
                        text = "Retry Quiz",
                        icon = Icons.Default.Refresh,
                        onClick = onRestart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    OutlinedButton(
                        onClick = onExploreMore,
                        border = BorderStroke(1.2.dp, SaffronPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Continue Learning Journey", color = SaffronLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
