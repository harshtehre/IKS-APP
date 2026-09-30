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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Course
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

data class LearningPathStep(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val isLocked: Boolean = false
)

data class LearningPath(
    val id: String,
    val title: String,
    val description: String,
    val domainId: String,
    val primaryCourseId: String,
    val totalSteps: Int,
    val completedSteps: Int,
    val steps: List<LearningPathStep>
)

@Composable
fun LearningPathsScreen(
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    val paths = listOf(
        LearningPath(
            id = "path_math",
            title = "Indian Mathematics: Foundations to Calculus",
            description = "A structured 5-step voyage through Vedic geometry, zero arithmetic, and Madhava's infinite series.",
            domainId = "math",
            primaryCourseId = "course_math_1",
            totalSteps = 5,
            completedSteps = 2,
            steps = listOf(
                LearningPathStep("1. Sulba Sutras & Altar Geometry", "Baudhayana diagonal theorem & √2 calculation", true),
                LearningPathStep("2. The Decimal Place-Value & Zero", "Brahmagupta's Brahmasphutasiddhanta rules", true),
                LearningPathStep("3. Aryabhata: Pi & The Pulverizer", "Approximation of Pi as 3.1416 & Kuttaka algorithm", false),
                LearningPathStep("4. Medieval Trigonometry & Bhaskara II", "Lilavati, Bijaganita, and cyclic algebra", false, isLocked = false),
                LearningPathStep("5. Madhava's Infinite Power Series", "Sine, cosine, and arctangent calculus centuries before Newton", false, isLocked = false)
            )
        ),
        LearningPath(
            id = "path_astro",
            title = "Indian Astronomy: Mapping the Celestial Sphere",
            description = "Master sidereal coordinates, the 27 Nakshatras, and planetary motion models.",
            domainId = "astronomy",
            primaryCourseId = "course_astro_1",
            totalSteps = 4,
            completedSteps = 1,
            steps = listOf(
                LearningPathStep("1. The 27 Nakshatras & Panchanga", "5 coordinates of the Indian lunisolar calendar", true),
                LearningPathStep("2. Vedanga Jyotisha & Solar Regimes", "Lagadha's earliest mathematical astronomy manual", false),
                LearningPathStep("3. Aryabhata's Axial Rotation Model", "Heliocentric intuitions and spherical geometry", false),
                LearningPathStep("4. Jantar Mantar Astronomical Yantras", "Maharaja Jai Singh's colossal stone observatories", false)
            )
        ),
        LearningPath(
            id = "path_ayur",
            title = "Ayurveda: The Holistic Healing Continuum",
            description = "From fundamental Tridosha physiology to surgical instruments and circadian regimens.",
            domainId = "ayurveda",
            primaryCourseId = "course_ayur_1",
            totalSteps = 4,
            completedSteps = 3,
            steps = listOf(
                LearningPathStep("1. Pancha Mahabhuta & Tridosha", "Vata, Pitta, and Kapha homeostatic principles", true),
                LearningPathStep("2. Acharya Charaka: Internal Medicine", "Etiology, pharmacology, and clinical diagnosis", true),
                LearningPathStep("3. Acharya Sushruta: Surgery & Rhinoplasty", "121 surgical instruments and living tissue reconstruction", true),
                LearningPathStep("4. Dinacharya & Preventive Health", "Circadian harmony and seasonal adaptation", false)
            )
        ),
        LearningPath(
            id = "path_phil",
            title = "The Six Classical Darshanas of Indian Philosophy",
            description = "Explore Nyaya logic, Vaisheshika atomism, Samkhya cosmology, Yoga, and Vedanta.",
            domainId = "philosophy",
            primaryCourseId = "course_phil_1",
            totalSteps = 6,
            completedSteps = 1,
            steps = listOf(
                LearningPathStep("1. Nyaya: Epistemology & 4 Pramanas", "Perception, inference, comparison, and testimony", true),
                LearningPathStep("2. Vaisheshika: Atomic Pluralism", "Kanada's categories of reality and Paramanu theory", false),
                LearningPathStep("3. Samkhya: Purusha & Prakriti", "Evolution of the 24 material principles", false),
                LearningPathStep("4. Yoga: Mind & Meditative Stillness", "Patanjali's 8 limbs for Chitta Vritti Nirodha", false),
                LearningPathStep("5. Mimamsa: Language & Hermeneutics", "Vedic interpretation and ethical action", false),
                LearningPathStep("6. Vedanta: The Non-Dual Ultimate", "Inquiry into Brahman and cosmic consciousness", false)
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .testTag("learning_paths_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Structured Learning Paths",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Text(
                    text = "Follow curated pedagogical journeys with step-by-step conceptual mastery and assessments.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }
        }

        items(paths) { path ->
            LearningPathCard(
                path = path,
                onStartPath = { onCourseClick(path.primaryCourseId) }
            )
        }
    }
}

@Composable
fun LearningPathCard(
    path: LearningPath,
    onStartPath: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = path.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronLight
                        )
                    )
                    Text(
                        text = "${path.completedSteps} of ${path.totalSteps} Modules Completed",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                    )
                }

                Surface(
                    color = SurfaceNavy,
                    shape = RoundedCornerShape(100.dp),
                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${(path.completedSteps * 100 / path.totalSteps)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = path.description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
            )

            LinearProgressIndicator(
                progress = { path.completedSteps.toFloat() / path.totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SaffronPrimary,
                trackColor = SurfaceNavy
            )

            // Step progression tree
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                path.steps.forEachIndexed { index, step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Node circle indicator
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        step.isCompleted -> EmeraldAccent
                                        step.isLocked -> SurfaceNavy
                                        else -> SaffronPrimary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                step.isCompleted -> Icon(Icons.Default.Check, contentDescription = "Done", tint = MidnightNavy, modifier = Modifier.size(16.dp))
                                step.isLocked -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextSecondaryDark, modifier = Modifier.size(14.dp))
                                else -> Text(text = "${index + 1}", color = MidnightNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (step.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (step.isLocked) TextSecondaryDark else TextPrimaryDark
                                )
                            )
                            Text(
                                text = step.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onStartPath,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MidnightNavy)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (path.completedSteps > 0) "Continue Learning Path" else "Start Learning Path",
                    color = MidnightNavy,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
