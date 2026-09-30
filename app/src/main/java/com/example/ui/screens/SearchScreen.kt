package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Course
import com.example.model.Lesson
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    courses: List<Course>,
    lessons: List<Lesson>,
    onCourseClick: (String) -> Unit,
    onLessonClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCourses = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList() else courses.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.titleHi.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredLessons = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList() else lessons.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.overview.contains(searchQuery, ignoreCase = true) ||
            it.keyFigures.any { f -> f.contains(searchQuery, ignoreCase = true) } ||
            it.keyTexts.any { t -> t.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("search_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Smart Search",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_all_input"),
            placeholder = { Text("Search Aryabhata, zero, Nyaya, Charaka...", color = TextSecondaryDark) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaffronPrimary,
                unfocusedBorderColor = SurfaceCardBorder,
                focusedContainerColor = SurfaceCardNavy,
                unfocusedContainerColor = SurfaceCardNavy,
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark
            )
        )

        if (searchQuery.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Popular Searches:", style = MaterialTheme.typography.titleSmall.copy(color = SaffronLight))
                val suggestions = listOf("Aryabhata", "Baudhayana Theorem", "Zero", "Tridosha", "Nyaya Pramanas", "Delhi Iron Pillar", "Nakshatras")
                suggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { searchQuery = suggestion },
                        color = SurfaceNavy,
                        border = BorderStroke(1.dp, SurfaceCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = RadiantGold, modifier = Modifier.size(16.dp))
                            Text(suggestion, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimaryDark))
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredLessons.isNotEmpty()) {
                    item {
                        Text(
                            text = "Lessons (${filteredLessons.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                    }
                    items(filteredLessons) { lesson ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onLessonClick(lesson.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceCardNavy,
                            border = BorderStroke(1.dp, SurfaceCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                )
                                Text(
                                    text = lesson.overview,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                if (filteredCourses.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Courses (${filteredCourses.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronLight
                            )
                        )
                    }
                    items(filteredCourses) { course ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onCourseClick(course.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceCardNavy,
                            border = BorderStroke(1.dp, SurfaceCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = course.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RadiantGold
                                    )
                                )
                                Text(
                                    text = course.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                if (filteredLessons.isEmpty() && filteredCourses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No results for \"$searchQuery\"", color = TextSecondaryDark)
                        }
                    }
                }
            }
        }
    }
}
