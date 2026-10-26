package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BookmarkItem
import com.example.model.NoteItem
import com.example.ui.components.GlassCard
import com.example.ui.components.IksMarkdownText
import com.example.ui.theme.*

@Composable
fun NotesBookmarksScreen(
    notes: List<NoteItem>,
    bookmarks: List<BookmarkItem>,
    onDeleteNote: (Long) -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    onAddNoteClick: () -> Unit,
    onAiSummarizeNote: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = Bookmarks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(16.dp)
            .testTag("notes_bookmarks_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Tab Selector ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceNavy,
            contentColor = SaffronPrimary,
            indicator = {},
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "My Notes (${notes.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) SaffronPrimary else TextSecondaryDark
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Bookmarks (${bookmarks.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) SaffronPrimary else TextSecondaryDark
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // --- Notes Tab ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Study Notes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )

                Button(
                    onClick = onAddNoteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MidnightNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Note", color = MidnightNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.NoteAlt, contentDescription = null, tint = TextSecondaryDark, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No notes yet. Add thoughts while reading any lesson!", color = TextSecondaryDark)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes) { note ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = note.lessonTitle,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronLight
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { onDeleteNote(note.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            IksMarkdownText(
                                markdown = note.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimaryDark,
                                    lineHeight = 20.sp
                                )
                            )

                            if (note.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    note.tags.forEach { tag ->
                                        Surface(
                                            color = SurfaceNavy,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = EmeraldAccent,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { onAiSummarizeNote(note.content) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask AI to polish / expand this note", color = SaffronLight, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // --- Bookmarks Tab ---
            if (bookmarks.isEmpty()) {
                // Default pre-populated showcase bookmarks if database is empty
                val sampleBookmarks = listOf(
                    BookmarkItem(1, "lesson_math_101", "lesson", "The Sulba Sutras: Ancient Vedic Geometry", "Indian Mathematics"),
                    BookmarkItem(2, "lesson_ayur_102", "lesson", "Sushruta: Ancient Surgery & Rhinoplasty", "Ayurveda & Health"),
                    BookmarkItem(3, "course_math_1", "course", "Indian Mathematics: Sulba to Calculus", "Indian Mathematics")
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sampleBookmarks) { item ->
                        BookmarkCard(
                            bookmark = item,
                            onClick = {
                                if (item.itemType == "course") onOpenCourse(item.itemId) else onOpenLesson(item.itemId)
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(bookmarks) { item ->
                        BookmarkCard(
                            bookmark = item,
                            onClick = {
                                if (item.itemType == "course") onOpenCourse(item.itemId) else onOpenLesson(item.itemId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkCard(
    bookmark: BookmarkItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCardNavy,
        border = BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (bookmark.itemType == "course") Icons.Filled.School else Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "${bookmark.domainTitle} • ${bookmark.itemType.uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = SaffronPrimary
            )
        }
    }
}
