package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Course
import com.example.model.KnowledgeDomain
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    domains: List<KnowledgeDomain>,
    courses: List<Course>,
    onCourseClick: (String) -> Unit,
    onDomainFilterChange: (String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "Exact Sciences", "Life Sciences", "Philosophy & Logic", "Art & Engineering", "Applied Engineering")

    val filteredDomains = remember(searchQuery, selectedCategoryFilter) {
        domains.filter { domain ->
            val matchesCategory = if (selectedCategoryFilter == "All") true else domain.categoryGroup == selectedCategoryFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                domain.title.contains(searchQuery, ignoreCase = true) ||
                domain.description.contains(searchQuery, ignoreCase = true) ||
                domain.titleHi.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .testTag("explore_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Explore Knowledge Domains",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Discover 18 distinct scientific, philosophical, and technological traditions of ancient and classical India.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }
        }

        // --- Search Bar ---
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_domains_input"),
                placeholder = { Text("Search by domain, scholar, or concept...", color = TextSecondaryDark) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary)
                },
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
        }

        // --- Category Filter Chips ---
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = category == selectedCategoryFilter
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) SaffronPrimary else SurfaceCardNavy,
                        border = BorderStroke(1.dp, if (isSelected) SaffronPrimary else SurfaceCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { selectedCategoryFilter = category }
                            .testTag("filter_$category")
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MidnightNavy else TextPrimaryDark
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // --- Domain Cards List ---
        items(filteredDomains) { domain ->
            val domainCourses = courses.filter { it.domainId == domain.id }
            DomainCard(
                domain = domain,
                courseCount = domainCourses.size,
                onClick = {
                    // Open the first course in this domain if available
                    domainCourses.firstOrNull()?.let { course ->
                        onCourseClick(course.id)
                    }
                }
            )
        }

        if (filteredDomains.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No matching knowledge domains found", color = TextSecondaryDark, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun DomainCard(
    domain: KnowledgeDomain,
    courseCount: Int,
    onClick: () -> Unit
) {
    val iconVector: ImageVector = when (domain.iconName) {
        "Calculate" -> Icons.Default.Calculate
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "Healing" -> Icons.Default.Healing
        "SelfImprovement" -> Icons.Default.SelfImprovement
        "Psychology" -> Icons.Default.Psychology
        "Translate" -> Icons.Default.Translate
        "Apartment" -> Icons.Default.Apartment
        "Build" -> Icons.Default.Build
        "WaterDrop" -> Icons.Default.WaterDrop
        "Spa" -> Icons.Default.Spa
        "MusicNote" -> Icons.Default.MusicNote
        "AccountBalance" -> Icons.Default.AccountBalance
        else -> Icons.Default.MenuBook
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, SaffronPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = domain.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )

                    Surface(
                        color = SurfaceNavy,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = domain.difficulty,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${domain.titleHi} • ${domain.titleMr}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = RadiantGold.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = domain.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "📚 ${domain.lessonCount} Lessons",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (courseCount > 0) {
                            Text(
                                text = "• 🎓 $courseCount Courses",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TealAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Explore",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SaffronPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
