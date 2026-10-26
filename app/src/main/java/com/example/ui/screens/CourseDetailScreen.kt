package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Course
import com.example.model.Lesson
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun CourseDetailScreen(
    course: Course,
    lessons: List<Lesson>,
    completedLessonIds: Set<String>,
    onLessonClick: (String) -> Unit,
    onStartCourseQuiz: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .testTag("course_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Course Banner Header ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCardNavy)
                    .border(1.dp, SaffronPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iks_hero_banner_1790663442394),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    alpha = 0.35f
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SaffronPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${course.difficulty.uppercase()} • ${course.durationMinutes} MIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronLight
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onBookmarkClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceNavy)
                                .testTag("btn_course_bookmark")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = RadiantGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Text(
                        text = "${course.titleHi} • ${course.titleMr}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = RadiantGold,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = course.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // --- 2. Learning Objectives ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Learning Objectives",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaffronLight
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                course.objectives.forEach { objective ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldAccent,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = objective,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryDark)
                        )
                    }
                }
            }
        }

        // --- 3. Academic Source & Trust Reference ---
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Curated & Verified Source",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = course.sourceTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Institution: ${course.sourceInstitution}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
                Text(
                    text = "Prerequisites: ${course.prerequisites}",
                    style = MaterialTheme.typography.labelSmall.copy(color = SaffronLight.copy(alpha = 0.8f))
                )
            }
        }

        // --- 4. Curriculum & Lessons ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Curriculum (${lessons.size} Lessons)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                Button(
                    onClick = onStartCourseQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceNavy),
                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Quiz, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Course Quiz", color = SaffronLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        itemsIndexed(lessons) { index, lesson ->
            val isDone = completedLessonIds.contains(lesson.id)
            LessonListItem(
                index = index + 1,
                lesson = lesson,
                isCompleted = isDone,
                onClick = { onLessonClick(lesson.id) }
            )
        }
    }
}

@Composable
fun LessonListItem(
    index: Int,
    lesson: Lesson,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("lesson_item_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceCardNavy,
        border = BorderStroke(
            1.dp,
            if (isCompleted) EmeraldAccent.copy(alpha = 0.4f) else SurfaceCardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) EmeraldAccent else SurfaceNavy),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = MidnightNavy,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "$index",
                        color = SaffronPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "⏱ ${lesson.readingTimeMinutes} min read • ${lesson.keyFigures.firstOrNull() ?: "Classical"} tradition",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
            }

            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = "Start Lesson",
                tint = if (isCompleted) EmeraldAccent else SaffronPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
