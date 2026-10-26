package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    MARATHI("mr", "Marathi", "मराठी")
}

data class UserProfile(
    val name: String = "Aryavart Sharma",
    val email: String = "aryavart@mitacsc.edu.in",
    val educationLevel: String = "Undergraduate (B.Tech / B.Sc)",
    val college: String = "MIT ACSC / Pune University",
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH,
    val interests: List<String> = listOf("Indian Mathematics", "Indian Astronomy", "Ayurveda", "Indian Philosophy"),
    val learningGoal: String = "Master Vedic & Classical Indian Sciences for Academic Excellence",
    val dailyTargetMinutes: Int = 20,
    val dailyLearnedMinutes: Int = 14,
    val streakDays: Int = 7,
    val xpPoints: Int = 1420,
    val currentLevel: Int = 4,
    val levelTitle: String = "Jijnasu (Inquirer)",
    val coursesInProgressCount: Int = 3,
    val lessonsCompletedCount: Int = 18,
    val quizzesTakenCount: Int = 12,
    val averageQuizScore: Int = 88
)

data class KnowledgeDomain(
    val id: String,
    val title: String,
    val titleHi: String,
    val titleMr: String,
    val description: String,
    val descriptionHi: String,
    val descriptionMr: String,
    val iconName: String,
    val lessonCount: Int,
    val courseCount: Int,
    val difficulty: String,
    val categoryGroup: String = "Science & Philosophy"
)

data class Course(
    val id: String,
    val domainId: String,
    val title: String,
    val titleHi: String,
    val titleMr: String,
    val description: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val durationMinutes: Int,
    val lessonCount: Int,
    val coverImageRes: String,
    val objectives: List<String>,
    val prerequisites: String,
    val sourceTitle: String,
    val sourceInstitution: String,
    val sourceUrl: String = "",
    val progressPercent: Int = 0
)

data class Lesson(
    val id: String,
    val courseId: String,
    val title: String,
    val titleHi: String,
    val titleMr: String,
    val readingTimeMinutes: Int,
    val order: Int,
    val overview: String,
    val keyConcepts: List<String>,
    val contentMarkdown: String,
    val contentHindi: String,
    val contentMarathi: String,
    val historicalContext: String,
    val keyFigures: List<String>,
    val keyTexts: List<String>,
    val sources: List<SourceCitation>,
    val relatedTopicIds: List<String>
)

data class SourceCitation(
    val title: String,
    val authorOrEditor: String,
    val institutionOrPublisher: String,
    val publicationYear: String,
    val referenceText: String,
    val sourceType: String = "Academic Journal / Classical Text" // Primary Text, Academic Publication, Govt Repository
)

enum class QuestionType {
    MCQ,
    TRUE_FALSE,
    MATCH,
    FILL_BLANK
}

data class QuizQuestion(
    val id: String,
    val lessonId: String,
    val courseId: String,
    val domainId: String,
    val questionText: String,
    val questionTextHi: String,
    val questionTextMr: String,
    val type: QuestionType,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val explanationHi: String,
    val explanationMr: String,
    val sourceRef: String,
    val difficulty: String = "Medium"
)

data class Flashcard(
    val id: String,
    val lessonId: String,
    val domainId: String,
    val frontText: String,
    val frontTextHi: String,
    val frontTextMr: String,
    val backText: String,
    val backTextHi: String,
    val backTextMr: String,
    val category: String,
    val sourceRef: String,
    val masteryLevel: Int = 0 // 0 = New, 1 = Review, 2 = Mastered
)

data class TimelineEvent(
    val id: String,
    val period: String, // e.g. "c. 800–500 BCE", "c. 499 CE"
    val eraName: String, // e.g. "Vedic & Sulba Period", "Classical Golden Era"
    val title: String,
    val titleHi: String,
    val description: String,
    val domain: String,
    val scholars: List<String>,
    val primaryTexts: List<String>,
    val sourceCitation: String
)

data class KnowledgeNode(
    val id: String,
    val label: String,
    val labelHi: String,
    val category: String, // Domain, Text, Scholar, Concept
    val description: String,
    val connectedNodeIds: List<String>,
    val xOffset: Float = 0f,
    val yOffset: Float = 0f
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val xpReward: Int,
    val isUnlocked: Boolean,
    val unlockedAt: String = "2026-09-20"
)

data class DailyIksFact(
    val date: String,
    val factText: String,
    val factSource: String,
    val conceptTitle: String,
    val conceptSummary: String,
    val figureName: String,
    val figureRole: String,
    val figureEra: String,
    val dailyQuestion: QuizQuestion
)

data class StudyPlanItem(
    val id: String,
    val dayOfWeek: String, // "Monday", "Tuesday"...
    val timeMinutes: Int,
    val subjectName: String,
    val activityType: String, // "Lesson Reading", "Flashcards", "Practice Quiz", "Revision"
    val isCompleted: Boolean = false
)

data class NoteItem(
    val id: Long = 0,
    val lessonId: String,
    val lessonTitle: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookmarkItem(
    val id: Long = 0,
    val itemId: String,
    val itemType: String, // "course", "lesson"
    val title: String,
    val domainTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AgentAction(val actionKey: String, val label: String) {
    TEACH_ME("teach_me", "Teach me"),
    EXPLAIN_SIMPLY("explain_simply", "Explain simply"),
    EXPLAIN_WITH_EXAMPLE("explain_example", "Explain with example"),
    SUMMARIZE_LESSON("summarize_lesson", "Summarize this lesson"),
    QUIZ_ME("quiz_me", "Quiz me"),
    GENERATE_FLASHCARDS("generate_flashcards", "Generate flashcards"),
    CREATE_EXAM_QUESTIONS("create_exam_questions", "Create exam questions"),
    MAKE_STUDY_PLAN("make_study_plan", "Make a study plan"),
    WHAT_SHOULD_I_STUDY_NEXT("study_next", "What should I study next?"),
    FIND_MY_WEAK_TOPICS("weak_topics", "Find my weak topics"),
    TRANSLATE_THIS("translate_this", "Translate this"),
    COMPARE_CONCEPTS("compare_concepts", "Compare these concepts"),
    REVISE_TOPIC("revise_topic", "Revise this topic"),
    GIVE_IMPORTANT_POINTS("important_points", "Give me important points")
}

data class StudentAgentContext(
    val studentName: String,
    val educationLevel: String,
    val college: String,
    val currentLevel: Int,
    val levelTitle: String,
    val streakDays: Int,
    val xpPoints: Int,
    val preferredLanguage: AppLanguage,
    val currentCourseTitle: String? = null,
    val currentLessonTitle: String? = null,
    val currentLessonOverview: String? = null,
    val currentLessonConcepts: List<String> = emptyList(),
    val completedLessons: List<String> = emptyList(),
    val weakTopics: List<String> = emptyList(),
    val strongTopics: List<String> = emptyList(),
    val savedNotes: List<String> = emptyList(),
    val bookmarkedItems: List<String> = emptyList(),
    val studyPlanSummary: String? = null
)

data class AgentExecutionResult(
    val replyText: String,
    val recommendedNextActivity: String,
    val stateUpdates: AgentStateUpdate? = null,
    val generatedFlashcards: List<Flashcard> = emptyList(),
    val generatedStudyPlanItems: List<StudyPlanItem> = emptyList(),
    val generatedQuizQuestion: QuizQuestion? = null,
    val suggestedFollowUps: List<String> = emptyList()
)

data class AgentStateUpdate(
    val xpEarned: Int = 0,
    val flashcardsAdded: Int = 0,
    val noteCreated: NoteItem? = null,
    val studyPlanUpdated: Boolean = false
)

data class ChatMessage(
    val id: String,
    val role: String, // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "Teach Me",
    val actionType: String? = null,
    val recommendedNextActivity: String? = null,
    val quickSuggestions: List<String> = emptyList()
)

