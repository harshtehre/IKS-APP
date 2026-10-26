package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.IksRepository
import com.example.data.local.IksDatabase
import com.example.data.local.UserProgressEntity
import com.example.data.remote.FirebaseAuthService
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class Screen {
    DASHBOARD,
    EXPLORE,
    LEARNING_PATHS,
    COURSE_DETAIL,
    LESSON_READER,
    AI_TUTOR,
    QUIZ_CENTER,
    FLASHCARDS,
    NOTES_BOOKMARKS,
    TIMELINE,
    KNOWLEDGE_MAP,
    STUDY_PLAN,
    ANALYTICS,
    ACHIEVEMENTS,
    SEARCH,
    PROFILE_SETTINGS,
    ADMIN
}

data class IksUiState(
    val currentScreen: Screen = Screen.DASHBOARD,
    val screenStack: List<Screen> = listOf(Screen.DASHBOARD),
    val userProfile: UserProfile = UserProfile(),
    val completedLessonIds: Set<String> = emptySet(),
    val selectedCourse: Course? = null,
    val selectedLesson: Lesson? = null,
    val searchQuery: String = "",
    val activeDomainFilter: String? = null,
    // Quiz State
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val quizScore: Int = 0,
    val isQuizFinished: Boolean = false,
    // AI Chat State
    val isAiLoading: Boolean = false,
    val currentAiMode: String = "Teach Me",
    val chatInputText: String = "",
    // Modals
    val showLanguageDialog: Boolean = false,
    val showTrustDialog: Boolean = false,
    val showAddNoteDialog: Boolean = false,
    val showVoiceDialog: Boolean = false,
    val activeKnowledgeNode: KnowledgeNode? = null,
    val isTtsSpeaking: Boolean = false
)

class IksViewModel(application: Application) : AndroidViewModel(application) {

    private val db = IksDatabase.getDatabase(application)
    val firebaseAuthService = FirebaseAuthService(application)
    private val repository = IksRepository(db.iksDao(), firebaseAuthService = firebaseAuthService)

    private val _uiState = MutableStateFlow(IksUiState())
    val uiState: StateFlow<IksUiState> = _uiState.asStateFlow()

    val domains: List<KnowledgeDomain> = repository.getDomains()
    val courses: List<Course> = repository.getCourses()
    val timelineEvents: List<TimelineEvent> = repository.getTimelineEvents()
    val knowledgeNodes: List<KnowledgeNode> = repository.getKnowledgeNodes()
    val achievements: List<Achievement> = repository.getAchievements()
    val dailyFact: DailyIksFact = repository.getDailyFact()

    val allNotes: StateFlow<List<NoteItem>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookmarks: StateFlow<List<BookmarkItem>> = repository.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<Flashcard>> = repository.flashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyPlan: StateFlow<List<StudyPlanItem>> = repository.studyPlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var textToSpeech: TextToSpeech? = null

    init {
        // Observe user profile
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }

        // Observe completed lessons
        viewModelScope.launch {
            repository.getAllProgress().collect { progressList ->
                val completedIds = progressList.filter { it.isCompleted }.map { it.lessonId }.toSet()
                _uiState.update { it.copy(completedLessonIds = completedIds) }
            }
        }
    }

    private fun initTtsIfNeeded() {
        if (textToSpeech != null) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                textToSpeech = TextToSpeech(getApplication()) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        try {
                            textToSpeech?.language = Locale.ENGLISH
                        } catch (e: Exception) {
                            android.util.Log.w("IksViewModel", "TTS language init exception", e)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("IksViewModel", "TextToSpeech init failed gracefully", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            // Ignore on shutdown
        }
    }

    // --- Navigation ---
    fun navigateTo(screen: Screen) {
        if (_uiState.value.currentScreen == screen) return
        android.util.Log.d("IksViewModel", "[NAV] Navigating from ${_uiState.value.currentScreen} to $screen")
        _uiState.update {
            it.copy(
                currentScreen = screen,
                screenStack = it.screenStack + screen
            )
        }
    }

    fun navigateBack(): Boolean {
        val stack = _uiState.value.screenStack
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            android.util.Log.d("IksViewModel", "[NAV] Popping stack back to ${newStack.last()}")
            _uiState.update {
                it.copy(
                    currentScreen = newStack.last(),
                    screenStack = newStack
                )
            }
            return true
        } else if (_uiState.value.currentScreen != Screen.DASHBOARD) {
            android.util.Log.d("IksViewModel", "[NAV] Back to DASHBOARD")
            _uiState.update {
                it.copy(
                    currentScreen = Screen.DASHBOARD,
                    screenStack = listOf(Screen.DASHBOARD)
                )
            }
            return true
        }
        return false
    }

    fun openCourse(courseId: String) {
        val course = repository.getCourseById(courseId)
        _uiState.update { it.copy(selectedCourse = course) }
        navigateTo(Screen.COURSE_DETAIL)
    }

    fun openLesson(lessonId: String) {
        val lesson = repository.getLessonById(lessonId)
        _uiState.update { it.copy(selectedLesson = lesson) }
        navigateTo(Screen.LESSON_READER)
    }

    fun startQuizForLesson(lessonId: String) {
        val questions = repository.getQuizQuestionsForLesson(lessonId)
        _uiState.update {
            it.copy(
                quizQuestions = questions,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizScore = 0,
                isQuizFinished = false
            )
        }
        navigateTo(Screen.QUIZ_CENTER)
    }

    fun startFullPracticeQuiz() {
        val allQuestions = repository.getAllQuizQuestions().shuffled()
        _uiState.update {
            it.copy(
                quizQuestions = allQuestions,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizScore = 0,
                isQuizFinished = false
            )
        }
        navigateTo(Screen.QUIZ_CENTER)
    }

    fun completeCurrentLesson() {
        val lesson = _uiState.value.selectedLesson ?: return
        viewModelScope.launch {
            repository.recordLessonCompletion(lesson.id, lesson.courseId)
        }
    }

    // --- Quiz Actions ---
    fun selectQuizOption(index: Int) {
        if (_uiState.value.isAnswerSubmitted) return
        _uiState.update { it.copy(selectedOptionIndex = index) }
    }

    fun submitQuizAnswer() {
        val state = _uiState.value
        val selected = state.selectedOptionIndex ?: return
        val currentQuestion = state.quizQuestions.getOrNull(state.currentQuestionIndex) ?: return

        val isCorrect = selected == currentQuestion.correctOptionIndex
        val newScore = if (isCorrect) state.quizScore + 1 else state.quizScore

        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                quizScore = newScore
            )
        }
    }

    fun nextQuizQuestion() {
        val state = _uiState.value
        if (state.currentQuestionIndex + 1 < state.quizQuestions.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false
                )
            }
        } else {
            // Finish quiz and record score
            _uiState.update { it.copy(isQuizFinished = true) }
            val courseId = state.selectedCourse?.id ?: "general_quiz"
            val lessonId = state.selectedLesson?.id ?: "general"
            viewModelScope.launch {
                repository.recordQuizResult(lessonId, courseId, state.quizScore, state.quizQuestions.size)
            }
        }
    }

    fun restartQuiz() {
        _uiState.update {
            it.copy(
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    // --- AI Chat Actions ---
    fun updateChatInput(text: String) {
        _uiState.update { it.copy(chatInputText = text) }
    }

    fun setAiMode(mode: String) {
        _uiState.update { it.copy(currentAiMode = mode) }
    }

    fun sendAiPrompt(prompt: String? = null, contextLesson: Lesson? = null) {
        val query = (prompt ?: _uiState.value.chatInputText).trim()
        if (query.isBlank()) return

        _uiState.update { it.copy(isAiLoading = true, chatInputText = "") }
        val lessonContext = contextLesson?.let {
            "Lesson: ${it.title}\nOverview: ${it.overview}\nConcepts: ${it.keyConcepts.joinToString()}"
        }

        viewModelScope.launch {
            repository.sendChatMessage(
                userText = query,
                lessonContext = lessonContext,
                mode = _uiState.value.currentAiMode
            )
            _uiState.update { it.copy(isAiLoading = false) }
        }
    }

    // --- Flashcards ---
    fun updateFlashcardMastery(cardId: String, newLevel: Int) {
        repository.updateFlashcardMastery(cardId, newLevel)
    }

    // --- Notes & Bookmarks ---
    fun addNote(content: String, tags: List<String>) {
        val lesson = _uiState.value.selectedLesson
        val lessonId = lesson?.id ?: "general"
        val lessonTitle = lesson?.title ?: "General Note"
        viewModelScope.launch {
            repository.addNote(lessonId, lessonTitle, content, tags)
            _uiState.update { it.copy(showAddNoteDialog = false) }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun toggleBookmarkCurrentLesson() {
        val lesson = _uiState.value.selectedLesson ?: return
        val course = _uiState.value.selectedCourse
        val isCurrentlyBookmarked = _uiState.value.completedLessonIds.contains(lesson.id) // check bookmark
        viewModelScope.launch {
            repository.toggleBookmark(
                itemId = lesson.id,
                itemType = "lesson",
                title = lesson.title,
                domainTitle = course?.title ?: "Indian Knowledge Systems",
                currentlyBookmarked = false
            )
        }
    }

    fun toggleStudyPlanItem(id: String) {
        repository.toggleStudyPlanItem(id)
    }

    // --- Audio / TTS ---
    fun speakText(text: String) {
        initTtsIfNeeded()
        textToSpeech?.stop()
        textToSpeech?.speak(text.take(800), TextToSpeech.QUEUE_FLUSH, null, "IKS_TTS")
        _uiState.update { it.copy(isTtsSpeaking = true) }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _uiState.update { it.copy(isTtsSpeaking = false) }
    }

    // --- Language & Dialogs ---
    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun toggleLanguageDialog(show: Boolean) {
        _uiState.update { it.copy(showLanguageDialog = show) }
    }

    fun toggleTrustDialog(show: Boolean) {
        _uiState.update { it.copy(showTrustDialog = show) }
    }

    fun toggleAddNoteDialog(show: Boolean) {
        _uiState.update { it.copy(showAddNoteDialog = show) }
    }

    fun toggleVoiceDialog(show: Boolean) {
        _uiState.update { it.copy(showVoiceDialog = show) }
    }

    fun setActiveKnowledgeNode(node: KnowledgeNode?) {
        _uiState.update { it.copy(activeKnowledgeNode = node) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setDomainFilter(domainId: String?) {
        _uiState.update { it.copy(activeDomainFilter = domainId) }
    }

    // --- Firebase Auth & Firestore Sync ---
    fun syncWithFirestore() {
        viewModelScope.launch {
            try {
                repository.syncWithFirestore()
            } catch (e: Exception) {
                android.util.Log.w("IksViewModel", "Firestore sync exception", e)
            }
        }
    }
}
