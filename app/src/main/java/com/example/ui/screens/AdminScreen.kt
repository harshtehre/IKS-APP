package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun AdminScreen(
    courses: List<Course>,
    onBack: () -> Unit
) {
    var showAddCourseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("admin_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Curriculum & Admin Panel",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                    Text(
                        text = "Role: IKS Academic Administrator (Full Access)",
                        style = MaterialTheme.typography.labelSmall.copy(color = RadiantGold)
                    )
                }

                Button(
                    onClick = { showAddCourseDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Course", color = MidnightNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- System Health & Platform Analytics ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Total Courses", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                    Text(
                        text = "${courses.size}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronLight
                        )
                    )
                    Text("All peer-verified", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent, fontSize = 9.sp))
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Active Students", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                    Text(
                        text = "1,842",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RadiantGold
                        )
                    )
                    Text("+14% this week", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldAccent, fontSize = 9.sp))
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Reported Content", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                    )
                    Text("100% verified citations", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp))
                }
            }
        }

        // --- Course Content Management List ---
        item {
            Text(
                text = "Course Content Management",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
        }

        items(courses) { course ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = SurfaceCardNavy,
                border = BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "Domain: ${course.domainId} • ${course.lessonCount} Lessons • ${course.durationMinutes} min",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                        )
                    }

                    var isCourseActive by remember { mutableStateOf(true) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { showAddCourseDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Course", tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { isCourseActive = !isCourseActive }) {
                            Icon(
                                if (isCourseActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isCourseActive) "Active" else "Inactive",
                                tint = if (isCourseActive) EmeraldAccent else TextSecondaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCourseDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newDomain by remember { mutableStateOf("Indian Mathematics") }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            containerColor = SurfaceNavy,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Add New IKS Course",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Course Title", color = TextSecondaryDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                    OutlinedTextField(
                        value = newDomain,
                        onValueChange = { newDomain = it },
                        label = { Text("Knowledge Domain", color = TextSecondaryDark) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAddCourseDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Save Course", color = MidnightNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}
