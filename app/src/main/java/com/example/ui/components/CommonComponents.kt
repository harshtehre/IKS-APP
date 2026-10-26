package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.Screen
import com.example.ui.theme.*

/**
 * Dedicated OM Logo mark wrapper.
 * Guarantees a perfectly circular container, mathematically centered glyph,
 * no clipping, no distortion, crisp lines at all screen densities.
 */
@Composable
fun IksLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 32.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    listOf(
                        RadiantGold,
                        SaffronPrimary,
                        SaffronDark,
                        RadiantGold
                    )
                )
            )
            .border(
                BorderStroke(1.dp, RadiantGold.copy(alpha = 0.85f)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner depth circle
        Box(
            modifier = Modifier
                .size(size - 2.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            RadiantGold,
                            SaffronPrimary,
                            SaffronDark
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ॐ",
                color = DeepestVoid,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.54f).sp,
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both
                    )
                ),
                modifier = Modifier.wrapContentSize(Alignment.Center)
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderGlow: Boolean = false,
    borderColor: Color = if (borderGlow) SaffronPrimary else SurfaceCardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "glass_card_scale"
    )

    Surface(
        modifier = modifier
            .then(
                if (isPressed || scale != 1f) {
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                } else Modifier
            )
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(22.dp),
        color = GlassSurface,
        border = BorderStroke(
            if (borderGlow) 1.5.dp else 1.dp,
            if (borderGlow) {
                Brush.sweepGradient(
                    listOf(
                        SaffronPrimary.copy(alpha = 0.8f),
                        TealAccent.copy(alpha = 0.6f),
                        RadiantGold.copy(alpha = 0.9f),
                        SaffronPrimary.copy(alpha = 0.8f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        borderColor.copy(alpha = 0.45f),
                        SaffronPrimary.copy(alpha = 0.2f),
                        borderColor.copy(alpha = 0.35f)
                    )
                )
            }
        ),
        tonalElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Top specular shine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                SaffronLight.copy(alpha = if (borderGlow) 0.5f else 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(18.dp),
                content = content
            )
        }
    }
}

@Composable
fun GlowingGradientButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerGradient: List<Color> = SaffronGradient
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "btn_press"
    )

    Surface(
        onClick = {
            android.util.Log.d("IKSPHERE_CLICK", "[CLICK] $text")
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, RadiantGold.copy(alpha = 0.6f)),
        interactionSource = interactionSource,
        modifier = modifier
            .then(
                if (isPressed || scale != 1f) {
                    Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                } else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(containerGradient))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DeepestVoid,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = DeepestVoid,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}

/**
 * Global responsive AppHeader used across all pages of IKSphere.
 * Fully adapts between 320px mobile screens and large desktop screens without clipping,
 * text wrapping, or overflowing elements.
 */
@Composable
fun AppHeader(
    currentScreen: Screen,
    streakDays: Int,
    currentLanguage: AppLanguage,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onTrustClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = DeepNavy,
        border = BorderStroke(1.dp, SurfaceCardBorder.copy(alpha = 0.5f)),
        tonalElevation = 6.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 10.dp)
        ) {
            val width = maxWidth
            val isSmallScreen = width < 360.dp
            val isExpanded = width >= 600.dp

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Brand Block
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (currentScreen != Screen.DASHBOARD) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("nav_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IksLogo(size = if (isExpanded) 38.dp else 32.dp)

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "IKSphere",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.3.sp,
                                    fontSize = if (isSmallScreen) 15.sp else 17.sp,
                                    color = RadiantGold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (width >= 370.dp) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldAccent)
                                )
                            }
                        }
                        if (width >= 340.dp) {
                            Text(
                                text = if (width < 380.dp) "Indian Knowledge" else "Indian Knowledge Systems",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right Actions Block
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 6.dp)
                ) {
                    // 1. Streak Badge
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SurfaceNavy,
                        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.clip(RoundedCornerShape(100.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (isSmallScreen) 6.dp else 8.dp,
                                vertical = 4.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = "🔥", fontSize = 11.sp)
                            Text(
                                text = if (isExpanded) "$streakDays d" else "$streakDays",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // 2. Language Selector
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SurfaceNavy,
                        border = BorderStroke(1.dp, SurfaceCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { onLanguageClick() }
                            .testTag("btn_select_language")
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (isSmallScreen) 6.dp else 8.dp,
                                vertical = 4.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = currentLanguage.code.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RadiantGold,
                                    fontSize = 10.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Language",
                                tint = RadiantGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // 3. Trust Icon (screens >= 400dp)
                    if (width >= 400.dp) {
                        IconButton(
                            onClick = onTrustClick,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_trust_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = "Trust & Source Citations",
                                tint = EmeraldAccent,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // 4. Search Icon (screens >= 360dp)
                    if (width >= 360.dp) {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_top_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextPrimaryDark,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // 5. Profile Icon (always visible)
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_top_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile & Settings",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IksTopAppBar(
    currentScreen: Screen,
    streakDays: Int,
    currentLanguage: AppLanguage,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onTrustClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    AppHeader(
        currentScreen = currentScreen,
        streakDays = streakDays,
        currentLanguage = currentLanguage,
        onBackClick = onBackClick,
        onSearchClick = onSearchClick,
        onLanguageClick = onLanguageClick,
        onTrustClick = onTrustClick,
        onProfileClick = onProfileClick
    )
}

@Composable
fun IksBottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = DeepNavy.copy(alpha = 0.98f),
        contentColor = TextSecondaryDark,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(Screen.DASHBOARD, Icons.Filled.Home, "Home"),
            Triple(Screen.EXPLORE, Icons.Filled.Explore, "Explore"),
            Triple(Screen.LEARNING_PATHS, Icons.Filled.Timeline, "Paths"),
            Triple(Screen.AI_TUTOR, Icons.Filled.AutoAwesome, "AI Mentor"),
            Triple(Screen.QUIZ_CENTER, Icons.Filled.Quiz, "Quizzes")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen
            val isAi = screen == Screen.AI_TUTOR

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    android.util.Log.d("IKSPHERE_NAV", "[NAV] $label clicked -> $screen")
                    onNavigate(screen)
                },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        if (isAi && isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(SaffronPrimary.copy(alpha = 0.45f), Color.Transparent)
                                        )
                                    )
                            )
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) SaffronPrimary else TextSecondaryDark
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SaffronPrimary else TextSecondaryDark
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SurfaceNavy,
                    selectedIconColor = SaffronPrimary,
                    unselectedIconColor = TextSecondaryDark,
                    selectedTextColor = SaffronPrimary,
                    unselectedTextColor = TextSecondaryDark
                ),
                modifier = Modifier.testTag("nav_item_${label.lowercase().replace(" ", "_")}")
            )
        }
    }
}

/**
 * NavigationRail for Expanded & Tablet screens (Canonical Material 3 layout)
 */
@Composable
fun IksNavigationRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = DeepNavy,
        header = {
            IksLogo(
                size = 40.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    ) {
        val items = listOf(
            Triple(Screen.DASHBOARD, Icons.Filled.Home, "Home"),
            Triple(Screen.EXPLORE, Icons.Filled.Explore, "Explore"),
            Triple(Screen.LEARNING_PATHS, Icons.Filled.Timeline, "Paths"),
            Triple(Screen.AI_TUTOR, Icons.Filled.AutoAwesome, "AI Mentor"),
            Triple(Screen.QUIZ_CENTER, Icons.Filled.Quiz, "Quizzes"),
            Triple(Screen.FLASHCARDS, Icons.Filled.Style, "Cards"),
            Triple(Screen.TIMELINE, Icons.Filled.HistoryEdu, "Timeline"),
            Triple(Screen.KNOWLEDGE_MAP, Icons.Filled.Share, "Map"),
            Triple(Screen.ANALYTICS, Icons.Filled.BarChart, "Analytics")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) SaffronPrimary else TextSecondaryDark
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = SurfaceNavy,
                    selectedIconColor = SaffronPrimary,
                    unselectedIconColor = TextSecondaryDark,
                    selectedTextColor = SaffronPrimary,
                    unselectedTextColor = TextSecondaryDark
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = TextPrimaryDark
            )
        }
    }
}

@Composable
fun LanguageDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceNavy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Translate, contentDescription = null, tint = SaffronPrimary)
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Content, AI Mentor responses, and quizzes will adapt to your preferred language:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )

                AppLanguage.values().forEach { lang ->
                    val isSelected = lang == currentLanguage
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectLanguage(lang) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SurfaceCardNavy else SurfaceNavy,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) SaffronPrimary else SurfaceCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SaffronPrimary else TextPrimaryDark
                                    )
                                )
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryDark
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SaffronPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun TrustSourceDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceNavy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Verified, contentDescription = null, tint = EmeraldAccent)
                Text(
                    text = "IKS Trust & Academic Rigor",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "“Learning content should be verified against cited sources.”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = SaffronLight,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "IKSphere strictly adheres to peer-reviewed academic rigor and classical textual fidelity. We maintain explicit separation between:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )

                listOf(
                    "🏛 Primary Archaeological Evidence (ASI, Inscriptions, Artifacts)",
                    "📜 Classical Sanskrit & Regional Treatises (Critical Editions)",
                    "🔬 Peer-Reviewed Academic Indology (INSA, NIAS, Oxford, Harvard)",
                    "⚖ Clear notation of historically debated dates and interpretations"
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimaryDark,
                            fontSize = 12.sp
                        )
                    )
                }

                Text(
                    text = "Never do we fabricate historical claims or present myth as empirical physics. Every lesson contains explicit source citations.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Understood", color = MidnightNavy, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AddNoteDialog(
    lessonTitle: String,
    onSave: (content: String, tags: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    var tagText by remember { mutableStateOf("IKS, Study, Revision") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceNavy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Add Note for $lessonTitle",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Your Note / Reflections", color = TextSecondaryDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                OutlinedTextField(
                    value = tagText,
                    onValueChange = { tagText = it },
                    label = { Text("Tags (comma separated)", color = TextSecondaryDark) },
                    singleLine = true,
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
                onClick = {
                    if (noteText.isNotBlank()) {
                        val tags = tagText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(noteText, tags)
                    }
                },
                enabled = noteText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Save Note", color = MidnightNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
