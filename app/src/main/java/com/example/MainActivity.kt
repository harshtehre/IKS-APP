package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: IksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val notes by viewModel.allNotes.collectAsStateWithLifecycle()
                val bookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
                val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
                val studyPlan by viewModel.studyPlan.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

                // Intercept back button to navigate backwards in the screen stack
                BackHandler(enabled = uiState.currentScreen != Screen.DASHBOARD) {
                    viewModel.navigateBack()
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpandedScreen = maxWidth >= 600.dp

                    val showNavigation = uiState.currentScreen in listOf(
                        Screen.DASHBOARD,
                        Screen.EXPLORE,
                        Screen.LEARNING_PATHS,
                        Screen.AI_TUTOR,
                        Screen.QUIZ_CENTER,
                        Screen.FLASHCARDS,
                        Screen.NOTES_BOOKMARKS,
                        Screen.TIMELINE,
                        Screen.KNOWLEDGE_MAP,
                        Screen.STUDY_PLAN,
                        Screen.ANALYTICS,
                        Screen.ACHIEVEMENTS
                    )

                    if (isExpandedScreen) {
                        // --- Expanded Tablet / Desktop Layout with Side Navigation Rail ---
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MidnightNavy)
                        ) {
                            if (showNavigation) {
                                IksNavigationRail(
                                    currentScreen = uiState.currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onProfileClick = { viewModel.navigateTo(Screen.PROFILE_SETTINGS) }
                                )
                            }

                            Scaffold(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                topBar = {
                                    AppHeader(
                                        currentScreen = uiState.currentScreen,
                                        streakDays = uiState.userProfile.streakDays,
                                        currentLanguage = uiState.userProfile.preferredLanguage,
                                        onBackClick = { viewModel.navigateBack() },
                                        onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                                        onLanguageClick = { viewModel.toggleLanguageDialog(true) },
                                        onTrustClick = { viewModel.toggleTrustDialog(true) },
                                        onProfileClick = { viewModel.navigateTo(Screen.PROFILE_SETTINGS) }
                                    )
                                }
                            ) { innerPadding ->
                                ScreenContentAnimated(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    notes = notes,
                                    bookmarks = bookmarks,
                                    flashcards = flashcards,
                                    studyPlan = studyPlan,
                                    chatMessages = chatMessages
                                )
                            }
                        }
                    } else {
                        // --- Mobile Layout with Bottom Navigation ---
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                AppHeader(
                                    currentScreen = uiState.currentScreen,
                                    streakDays = uiState.userProfile.streakDays,
                                    currentLanguage = uiState.userProfile.preferredLanguage,
                                    onBackClick = { viewModel.navigateBack() },
                                    onSearchClick = { viewModel.navigateTo(Screen.SEARCH) },
                                    onLanguageClick = { viewModel.toggleLanguageDialog(true) },
                                    onTrustClick = { viewModel.toggleTrustDialog(true) },
                                    onProfileClick = { viewModel.navigateTo(Screen.PROFILE_SETTINGS) }
                                )
                            },
                            bottomBar = {
                                if (showNavigation) {
                                    IksBottomNavigationBar(
                                        currentScreen = uiState.currentScreen,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            ScreenContentAnimated(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                uiState = uiState,
                                viewModel = viewModel,
                                notes = notes,
                                bookmarks = bookmarks,
                                flashcards = flashcards,
                                studyPlan = studyPlan,
                                chatMessages = chatMessages
                            )
                        }
                    }
                }

                // --- Modal Dialogs ---
                if (uiState.showLanguageDialog) {
                    LanguageDialog(
                        currentLanguage = uiState.userProfile.preferredLanguage,
                        onSelectLanguage = { viewModel.setLanguage(it) },
                        onDismiss = { viewModel.toggleLanguageDialog(false) }
                    )
                }

                if (uiState.showTrustDialog) {
                    TrustSourceDialog(
                        onDismiss = { viewModel.toggleTrustDialog(false) }
                    )
                }

                if (uiState.showAddNoteDialog) {
                    AddNoteDialog(
                        lessonTitle = uiState.selectedLesson?.title ?: "General Note",
                        onSave = { content, tags ->
                            viewModel.addNote(content, tags)
                        },
                        onDismiss = { viewModel.toggleAddNoteDialog(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenContentAnimated(
    modifier: Modifier = Modifier,
    uiState: IksUiState,
    viewModel: IksViewModel,
    notes: List<com.example.model.NoteItem>,
    bookmarks: List<com.example.model.BookmarkItem>,
    flashcards: List<com.example.model.Flashcard>,
    studyPlan: List<com.example.model.StudyPlanItem>,
    chatMessages: List<com.example.model.ChatMessage>
) {
    Crossfade(
        targetState = uiState.currentScreen,
        animationSpec = tween(140),
        modifier = modifier,
        label = "page_transition"
    ) { screen ->
        when (screen) {
            Screen.DASHBOARD -> {
                DashboardScreen(
                    userProfile = uiState.userProfile,
                    courses = viewModel.courses,
                    dailyFact = viewModel.dailyFact,
                    onCourseClick = { courseId -> viewModel.openCourse(courseId) },
                    onNavigate = { dest -> viewModel.navigateTo(dest) },
                    onAskAi = { prompt ->
                        viewModel.updateChatInput(prompt)
                        viewModel.navigateTo(Screen.AI_TUTOR)
                        viewModel.sendAiPrompt(prompt)
                    },
                    onStartDailyQuiz = {
                        viewModel.startQuizForLesson("lesson_math_101")
                    }
                )
            }

            Screen.EXPLORE -> {
                ExploreScreen(
                    domains = viewModel.domains,
                    courses = viewModel.courses,
                    onCourseClick = { courseId -> viewModel.openCourse(courseId) },
                    onDomainFilterChange = { domainId -> viewModel.setDomainFilter(domainId) }
                )
            }

            Screen.LEARNING_PATHS -> {
                LearningPathsScreen(
                    courses = viewModel.courses,
                    onCourseClick = { courseId -> viewModel.openCourse(courseId) }
                )
            }

            Screen.COURSE_DETAIL -> {
                val course = uiState.selectedCourse ?: viewModel.courses.first()
                val lessons = com.example.data.sample.IksSampleData.lessons.filter { it.courseId == course.id }
                CourseDetailScreen(
                    course = course,
                    lessons = lessons,
                    completedLessonIds = uiState.completedLessonIds,
                    onLessonClick = { lessonId -> viewModel.openLesson(lessonId) },
                    onStartCourseQuiz = { viewModel.startQuizForLesson(lessons.firstOrNull()?.id ?: "lesson_math_101") },
                    onBookmarkClick = { viewModel.toggleBookmarkCurrentLesson() }
                )
            }

            Screen.LESSON_READER -> {
                val lesson = uiState.selectedLesson ?: com.example.data.sample.IksSampleData.lessons.first()
                val course = uiState.selectedCourse ?: viewModel.courses.find { it.id == lesson.courseId }
                LessonReaderScreen(
                    lesson = lesson,
                    course = course,
                    currentLanguage = uiState.userProfile.preferredLanguage,
                    isCompleted = uiState.completedLessonIds.contains(lesson.id),
                    isTtsSpeaking = uiState.isTtsSpeaking,
                    onCompleteLesson = { viewModel.completeCurrentLesson() },
                    onTakeQuiz = { viewModel.startQuizForLesson(lesson.id) },
                    onAskAi = { prompt ->
                        viewModel.updateChatInput(prompt)
                        viewModel.navigateTo(Screen.AI_TUTOR)
                        viewModel.sendAiPrompt(prompt, lesson)
                    },
                    onAddNote = { viewModel.toggleAddNoteDialog(true) },
                    onBookmark = { viewModel.toggleBookmarkCurrentLesson() },
                    onSpeak = { text -> viewModel.speakText(text) },
                    onStopSpeak = { viewModel.stopSpeaking() },
                    onNavigateBack = { viewModel.navigateBack() }
                )
            }

            Screen.AI_TUTOR -> {
                AiMentorScreen(
                    messages = chatMessages,
                    isThinking = uiState.isAiLoading,
                    currentMode = uiState.currentAiMode,
                    inputText = uiState.chatInputText,
                    currentLanguage = uiState.userProfile.preferredLanguage,
                    onModeChange = { viewModel.setAiMode(it) },
                    onInputChange = { viewModel.updateChatInput(it) },
                    onSendMessage = { prompt -> viewModel.sendAiPrompt(prompt) },
                    onVoiceInputClick = {
                        viewModel.sendAiPrompt("What are the key mathematical contributions of the Sulba Sutras?")
                    }
                )
            }

            Screen.QUIZ_CENTER -> {
                QuizCenterScreen(
                    questions = uiState.quizQuestions.ifEmpty { listOf(viewModel.dailyFact.dailyQuestion) },
                    currentIndex = uiState.currentQuestionIndex,
                    selectedOptionIndex = uiState.selectedOptionIndex,
                    isAnswerSubmitted = uiState.isAnswerSubmitted,
                    score = uiState.quizScore,
                    isQuizFinished = uiState.isQuizFinished,
                    onSelectOption = { viewModel.selectQuizOption(it) },
                    onSubmitAnswer = { viewModel.submitQuizAnswer() },
                    onNextQuestion = { viewModel.nextQuizQuestion() },
                    onRestartQuiz = { viewModel.restartQuiz() },
                    onExploreMore = { viewModel.navigateTo(Screen.EXPLORE) }
                )
            }

            Screen.FLASHCARDS -> {
                FlashcardsScreen(
                    flashcards = flashcards,
                    onMasteryChange = { cardId, level ->
                        viewModel.updateFlashcardMastery(cardId, level)
                    }
                )
            }

            Screen.NOTES_BOOKMARKS -> {
                NotesBookmarksScreen(
                    notes = notes,
                    bookmarks = bookmarks,
                    onDeleteNote = { viewModel.deleteNote(it) },
                    onOpenLesson = { viewModel.openLesson(it) },
                    onOpenCourse = { viewModel.openCourse(it) },
                    onAddNoteClick = { viewModel.toggleAddNoteDialog(true) },
                    onAiSummarizeNote = { noteContent ->
                        viewModel.updateChatInput("Summarize and structure this study note: $noteContent")
                        viewModel.navigateTo(Screen.AI_TUTOR)
                        viewModel.sendAiPrompt("Summarize and structure this study note: $noteContent")
                    }
                )
            }

            Screen.TIMELINE -> {
                TimelineScreen(
                    events = viewModel.timelineEvents,
                    onAskAiAboutEvent = { prompt ->
                        viewModel.updateChatInput(prompt)
                        viewModel.navigateTo(Screen.AI_TUTOR)
                        viewModel.sendAiPrompt(prompt)
                    }
                )
            }

            Screen.KNOWLEDGE_MAP -> {
                KnowledgeMapScreen(
                    nodes = viewModel.knowledgeNodes,
                    selectedNode = uiState.activeKnowledgeNode,
                    onSelectNode = { viewModel.setActiveKnowledgeNode(it) },
                    onAskAiAboutNode = { prompt ->
                        viewModel.updateChatInput(prompt)
                        viewModel.navigateTo(Screen.AI_TUTOR)
                        viewModel.sendAiPrompt(prompt)
                    }
                )
            }

            Screen.STUDY_PLAN -> {
                StudyPlanScreen(
                    studyPlan = studyPlan,
                    dailyTargetMinutes = uiState.userProfile.dailyTargetMinutes,
                    onToggleItem = { viewModel.toggleStudyPlanItem(it) },
                    onRegeneratePlan = {
                        viewModel.updateChatInput("Generate a balanced 7-day Indian Knowledge Systems study plan for college exams")
                        viewModel.navigateTo(Screen.AI_TUTOR)
                        viewModel.sendAiPrompt()
                    }
                )
            }

            Screen.ANALYTICS -> {
                AnalyticsScreen(userProfile = uiState.userProfile)
            }

            Screen.ACHIEVEMENTS -> {
                AchievementsScreen(achievements = viewModel.achievements)
            }

            Screen.SEARCH -> {
                SearchScreen(
                    courses = viewModel.courses,
                    lessons = com.example.data.sample.IksSampleData.lessons,
                    onCourseClick = { viewModel.openCourse(it) },
                    onLessonClick = { viewModel.openLesson(it) }
                )
            }

            Screen.PROFILE_SETTINGS -> {
                ProfileSettingsScreen(
                    userProfile = uiState.userProfile,
                    onLanguageChangeClick = { viewModel.toggleLanguageDialog(true) },
                    onOpenTrustDialog = { viewModel.toggleTrustDialog(true) },
                    onOpenAdmin = { viewModel.navigateTo(Screen.ADMIN) },
                    onSyncFirestore = { viewModel.syncWithFirestore() }
                )
            }

            Screen.ADMIN -> {
                AdminScreen(
                    courses = viewModel.courses,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
