package com.example.data

import com.example.data.local.*
import com.example.data.remote.FirebaseAuthService
import com.example.data.remote.GeminiService
import com.example.data.sample.IksSampleData
import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class IksRepository(
    private val iksDao: IksDao,
    private val geminiService: GeminiService = GeminiService(),
    private val firebaseAuthService: FirebaseAuthService? = null
) {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: Flow<UserProfile> = _userProfile.asStateFlow()

    private val _studyPlan = MutableStateFlow(IksSampleData.defaultStudyPlan)
    val studyPlan: Flow<List<StudyPlanItem>> = _studyPlan.asStateFlow()

    private val _flashcards = MutableStateFlow(IksSampleData.flashcards)
    val flashcards: Flow<List<Flashcard>> = _flashcards.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome_msg",
                role = "assistant",
                content = "Namaste! I am your **IKS AI Mentor**. How can I help you explore Indian Knowledge Systems today? You can ask me to explain concepts, generate quizzes, compare philosophical schools, or summarize any lesson.",
                mode = "Teach Me",
                quickSuggestions = listOf("Explain Baudhayana Theorem", "Who was Aryabhata?", "What is Tridosha in Ayurveda?", "Explain Nyaya Logic")
            )
        )
    )
    val chatMessages: Flow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun getDomains(): List<KnowledgeDomain> = IksSampleData.domains

    fun getDomainById(id: String): KnowledgeDomain? = IksSampleData.domains.find { it.id == id }

    fun getCourses(): List<Course> = IksSampleData.courses

    fun getCourseById(id: String): Course? = IksSampleData.courses.find { it.id == id }

    fun getLessonsForCourse(courseId: String): List<Lesson> =
        IksSampleData.lessons.filter { it.courseId == courseId }

    fun getLessonById(id: String): Lesson? = IksSampleData.lessons.find { it.id == id }

    fun getQuizQuestionsForLesson(lessonId: String): List<QuizQuestion> =
        IksSampleData.quizQuestions.filter { it.lessonId == lessonId }.ifEmpty {
            IksSampleData.quizQuestions.take(5)
        }

    fun getAllQuizQuestions(): List<QuizQuestion> = IksSampleData.quizQuestions

    fun getTimelineEvents(): List<TimelineEvent> = IksSampleData.timelineEvents

    fun getKnowledgeNodes(): List<KnowledgeNode> = IksSampleData.knowledgeNodes

    fun getAchievements(): List<Achievement> = IksSampleData.achievements

    fun getDailyFact(): DailyIksFact = IksSampleData.dailyFact

    // --- Progress & Tracking ---
    fun getAllProgress(): Flow<List<UserProgressEntity>> = iksDao.getAllProgress()

    suspend fun recordLessonCompletion(lessonId: String, courseId: String) {
        iksDao.recordProgress(
            UserProgressEntity(
                lessonId = lessonId,
                courseId = courseId,
                isCompleted = true,
                score = 100
            )
        )
        // Increment user XP
        _userProfile.value = _userProfile.value.copy(
            xpPoints = _userProfile.value.xpPoints + 50,
            lessonsCompletedCount = _userProfile.value.lessonsCompletedCount + 1,
            dailyLearnedMinutes = (_userProfile.value.dailyLearnedMinutes + 8).coerceAtMost(60)
        )
    }

    suspend fun recordQuizResult(lessonId: String, courseId: String, score: Int, total: Int) {
        iksDao.insertQuizAttempt(
            QuizAttemptEntity(
                courseId = courseId,
                lessonId = lessonId,
                score = score,
                totalQuestions = total
            )
        )
        val gainedXp = score * 30
        _userProfile.value = _userProfile.value.copy(
            xpPoints = _userProfile.value.xpPoints + gainedXp,
            quizzesTakenCount = _userProfile.value.quizzesTakenCount + 1,
            averageQuizScore = ((_userProfile.value.averageQuizScore * 5 + (score * 100 / total)) / 6)
        )
        try {
            firebaseAuthService?.syncProfileToFirestore(_userProfile.value)
        } catch (e: Exception) {
            // Ignore offline
        }
    }

    suspend fun syncWithFirestore() {
        firebaseAuthService?.ensureAuthenticated()
        firebaseAuthService?.syncProfileToFirestore(_userProfile.value)
        val remoteProfile = firebaseAuthService?.fetchProfileFromFirestore()
        if (remoteProfile != null) {
            _userProfile.value = remoteProfile
        }
    }

    // --- Notes ---
    fun getAllNotes(): Flow<List<NoteItem>> = iksDao.getAllNotes().map { list ->
        list.map {
            NoteItem(
                id = it.id,
                lessonId = it.lessonId,
                lessonTitle = it.lessonTitle,
                content = it.content,
                tags = if (it.tags.isNotBlank()) it.tags.split(",") else emptyList(),
                createdAt = it.timestamp
            )
        }
    }

    suspend fun addNote(lessonId: String, lessonTitle: String, content: String, tags: List<String>) {
        iksDao.insertNote(
            NoteEntity(
                lessonId = lessonId,
                lessonTitle = lessonTitle,
                content = content,
                tags = tags.joinToString(",")
            )
        )
    }

    suspend fun deleteNote(id: Long) {
        iksDao.deleteNote(id)
    }

    // --- Bookmarks ---
    fun getAllBookmarks(): Flow<List<BookmarkItem>> = iksDao.getAllBookmarks().map { list ->
        list.map {
            BookmarkItem(
                id = it.id,
                itemId = it.itemId,
                itemType = it.itemType,
                title = it.title,
                domainTitle = it.domainTitle,
                timestamp = it.timestamp
            )
        }
    }

    fun isBookmarked(itemId: String): Flow<Boolean> = iksDao.isBookmarked(itemId)

    suspend fun toggleBookmark(itemId: String, itemType: String, title: String, domainTitle: String, currentlyBookmarked: Boolean) {
        if (currentlyBookmarked) {
            iksDao.deleteBookmarkByItemId(itemId)
        } else {
            iksDao.insertBookmark(
                BookmarkEntity(
                    itemId = itemId,
                    itemType = itemType,
                    title = title,
                    domainTitle = domainTitle
                )
            )
        }
    }

    // --- Study Plan ---
    fun toggleStudyPlanItem(id: String) {
        _studyPlan.value = _studyPlan.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun updateFlashcardMastery(cardId: String, newLevel: Int) {
        _flashcards.value = _flashcards.value.map {
            if (it.id == cardId) it.copy(masteryLevel = newLevel.coerceIn(0, 2)) else it
        }
    }

    fun setLanguage(language: AppLanguage) {
        _userProfile.value = _userProfile.value.copy(preferredLanguage = language)
    }

    // --- AI Chat ---
    suspend fun sendChatMessage(userText: String, lessonContext: String? = null, mode: String = "Teach Me"): String {
        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            role = "user",
            content = userText,
            mode = mode
        )
        _chatMessages.value = _chatMessages.value + userMsg

        val profile = _userProfile.value
        val agentContext = StudentAgentContext(
            studentName = profile.name,
            educationLevel = profile.educationLevel,
            college = profile.college,
            currentLevel = profile.currentLevel,
            levelTitle = profile.levelTitle,
            streakDays = profile.streakDays,
            xpPoints = profile.xpPoints,
            preferredLanguage = profile.preferredLanguage,
            currentCourseTitle = lessonContext ?: "Indian Knowledge Systems",
            currentLessonTitle = lessonContext,
            weakTopics = listOf("Indian Astronomy (27 Nakshatras & Solar Motion)", "Ayurvedic Rasa-Panchaka"),
            strongTopics = listOf("Baudhayana Sulba Sutras", "Brahmagupta Arithmetic"),
            completedLessons = listOf("Vedic Mathematics Fundamentals", "Aryabhata Diophantine Equations"),
            bookmarkedItems = listOf("Sulba Sutra 1.48", "Kerala School Infinite Series"),
            savedNotes = listOf("Baudhayana diagonal cord rules", "Brahmagupta zero division")
        )

        val result = geminiService.processAgentWorkflow(
            userPrompt = userText,
            action = null,
            context = agentContext,
            conversationHistory = _chatMessages.value.dropLast(1)
        )

        // Apply state updates from the agent workflow
        result.stateUpdates?.let { update ->
            if (update.xpEarned > 0) {
                _userProfile.value = _userProfile.value.copy(
                    xpPoints = _userProfile.value.xpPoints + update.xpEarned
                )
            }
        }

        if (result.generatedFlashcards.isNotEmpty()) {
            _flashcards.value = result.generatedFlashcards + _flashcards.value
        }

        if (result.generatedStudyPlanItems.isNotEmpty()) {
            _studyPlan.value = result.generatedStudyPlanItems + _studyPlan.value
        }

        val assistantMsg = ChatMessage(
            id = (System.currentTimeMillis() + 1).toString(),
            role = "assistant",
            content = result.replyText,
            mode = mode,
            recommendedNextActivity = result.recommendedNextActivity,
            quickSuggestions = result.suggestedFollowUps.ifEmpty {
                listOf("Summarize this", "Give an example", "Quiz me", "Explain simply")
            }
        )
        _chatMessages.value = _chatMessages.value + assistantMsg
        return result.replyText
    }
}
