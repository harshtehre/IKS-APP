# VidyaDhara Mobile 5.0 — Indian Knowledge Systems Study App

VidyaDhara — **Learn the wisdom of Bharat** — is a **mobile-first IKS learning PWA and institution SaaS foundation** built with **React + Vite**, **Node.js + Express**, **Python + FastAPI**, and **Supabase PostgreSQL in hosted deployments** with **SQLite for local development**. Its original IKS catalog and learner workflows remain available alongside tenant-aware institution administration.

## Stack

- **Mobile UI:** React + Vite, responsive PWA, portrait-first layout, safe-area spacing, touch targets, bottom navigation
- **Node.js:** Express production server and API proxy
- **Python:** FastAPI application, Supabase PostgreSQL / SQLite persistence, certificate PDF generation, Groq AI orchestration
- **Authentication:** Firebase Web Authentication — Google, email/password, and anonymous guest sign-in
- **AI:** Groq only. Default model: `openai/gpt-oss-20b`. The Groq key stays on the FastAPI server.
- **Learning data:** PostgreSQL on Supabase when `SUPABASE_DB_URL` is configured; SQLite fallback for local development
- **Offline shell:** service worker + built-in Samvada knowledge fallback

## IKS learning features

### Home / Daily Wisdom
- Animated Sri Yantra-inspired sacred geometry canvas
- Indian-heritage visual hero
- Daily Sanskrit subhāṣita/shloka with Devanagari, IAST, English meaning and word hints
- Copy/share actions and speech-synthesis pronunciation
- 7-day study streak, XP and rank
- Scholar of the Day
- Daily knowledge challenge

### Nine IKS streams
1. Ganita — Mathematics
2. Khagola Shastra — Astronomy
3. Ayurveda & Swasthya — Life and health traditions
4. Dhatu Shastra — Metallurgy and materials
5. Sthapatya Veda — Architecture and spatial planning
6. Darshana & Tarka — Philosophy and logic
7. Vyakarana — Paninian linguistics
8. Krishi & Vrikshayurveda — Agriculture and plant traditions
9. Jala Prabandhana — Water engineering and management

### Courses and deep lessons
- Course overview, era, texts, scholars and progress
- Devanagari + IAST + plain-English learning explanation
- Historical context separated from modern comparison
- At least 10 explained study-outline sections per lesson
- Key takeaways and practical learning applications
- In-lesson knowledge checkpoint
- Notes and bookmarks
- Lesson completion, XP, ranks and certificates

### Learning paths
- Vedic Mathematics & Algorithmic Arithmetic
- Celestial Indian Astronomy & Siddhantas
- Ayurveda & Preventive Longevity
- Ancient Indian Metallurgy & Chemical Sciences
- Sacred Architecture & Civil Engineering
- Atomic Philosophy, Linguistics & Botany

### Quiz and memory lab
- Multiple-choice quizzes with explanations
- 50-question practice banks for each seeded course and lesson, with randomized question and answer-option order
- Custom practice settings for question count and timer, plus quiz exit, elapsed-time tracking and a scored result review
- Live scoring and review
- 3D flip flashcards
- Shuffle / next / previous controls
- Spaced-repetition queue with Again / Hard / Good / Easy ratings and per-account schedules
- Teacher-managed classes with student join codes and course / quiz assignments
- Quiz XP and achievement tracking

### Samvada — Groq AI Guru
- Open-ended chat for IKS and related academic questions
- Groq model responses when `GROQ_API_KEY` is configured
- Curated offline fallback when no key is present
- Retrieved lesson context when a user asks about known IKS material
- Clear distinction between historical evidence, traditional interpretation and modern analogy
- Avoids presenting invented quotations or citations as historical facts
- Sanskrit explanations with simple transliteration/help when relevant
- Assistant headings, emphasis, code and lists render as formatted chat content; long Groq completions continue when the model reports a length limit

### Sanskrit support
- Vowel and consonant pronunciation guide
- IAST transliteration
- Common IKS terms with simple English meanings
- Lesson word-by-word hints
- Devanagari speech synthesis for pronunciation practice; available voice quality depends on the browser and installed Sanskrit/Hindi voices

## Interface languages

Choose English, Hindi, or Marathi in Settings. The selected language updates navigation, common interface labels, learning controls, account settings, the Sanskrit guide, and classroom flows. Course and lesson content, learning paths, quizzes, assignments, and flashcards are translated on demand with Groq and cached in the backend database; Sanskrit, IAST, identifiers, and quiz answer order are preserved. Set `GROQ_API_KEY` in `backend/.env` to enable these translations. English works without a key.

## Teacher and student classrooms

Teachers sign in with Firebase using an email listed in the backend `TEACHER_EMAILS` allow-list. They can create IKS classes, share each class's join code, create or edit IKS courses and topic-specific quizzes, and assign a course or quiz to the whole class. Classroom assignments accept teacher attachments and student submissions in any file format up to 20 MB per file, alongside existing text responses; students can download files and teachers can review, download, and grade submissions. The class report provides searchable student activity rows with quiz score, course progress, elapsed time, completion time, and status. Students sign in as learners, join with the shared code, and open the class assignments. The API checks teacher ownership and classroom membership before returning assignments or reports. Local demo logins are only available with local SQLite and no hosted-auth configuration.

In local development, the sign-in screen also offers a teacher demo preview for exploring teacher features. This local-only option is not available when hosted authentication or storage is configured.

### Teacher quiz authoring

Teacher Studio supports saving quizzes as private drafts, editing them before the first student attempt, and publishing them to learners. Published quizzes can set a time limit, marks and passing threshold, negative marking, maximum attempts, availability dates, question/option randomization, retake behavior, and whether results and explanations are released immediately. Learner reviews identify selected and correct answers when results are released. Drafts are visible only to their author. The current question editor supports single-answer multiple-choice questions; other question formats are not implemented yet. The retired legacy quiz titles are hidden from all learner and classroom views without deleting attempt history.

### Scholars and achievements
- Scholar timeline with era, domain, work and contribution
- Jijnasu → Vidyarthi → Upadhyaya → Acharya progression
- Milestone badges
- Subject/course analytics
- Downloadable course-completion certificates

## Mobile UX

The interface is designed **phone-first**, not desktop-first:

- Full-screen lesson reader on small screens
- Bottom navigation for thumb reach
- Sticky top controls only where useful
- 44px+ touch targets for primary controls
- No fixed-width cards that can overflow a phone
- CSS `dvh` and safe-area support for modern mobile browsers
- Breakpoints for narrow phones and tablets
- Reduced-motion setting
- Dark and light themes
- Accent-color customization
- PWA install support

The app can be opened from a phone browser and added to the home screen. Firebase's web authentication flow is configured to use redirect sign-in on smaller viewports and popup sign-in on wider screens.

## Authentication setup

Create a Firebase project and a **Web app** in the Firebase Console. Enable these Authentication providers:

1. Google
2. Email/Password
3. Anonymous

Copy the Web app configuration into `frontend/.env.local` using the template in `frontend/.env.example`.

Set the same Firebase project ID for FastAPI in `backend/.env`:

```text
FIREBASE_PROJECT_ID=your-project-id
```

Optional teacher access is controlled by an explicit allow-list:

```text
TEACHER_EMAILS=teacher@example.com,another-teacher@example.com
```

In Firebase Authentication, add your local and production domains to the project's authorized domains.

## Institution SaaS administration

The app supports server-authorized **Super Admin**, **Institution Admin**, **Teacher**, and **Student** roles (legacy learner accounts continue to work). Configure the platform administrator allow-list only in the FastAPI environment:

```text
SUPER_ADMIN_EMAILS=platform-admin@example.com
```

After signing in with a verified Firebase account on the allow-list, the Super Admin can create trial institutions, select a plan, and change institution status or plan. Institution Admins can invite teachers and students, manage departments and programs, inspect tenant usage, and update institution branding. Invited users must sign in with the invited email; invitations are currently recorded in the database but **are not emailed automatically**. Plan limits are enforced by the API; payment processing is not configured.

Courses, quizzes, paths, classrooms, file and text assignments with manual grading, class schedules, attendance, discussions, announcements, and supplemental course resources are tenant-scoped by the API. Seeded IKS learning content remains shared. Institution Admins can paste CSV rows (`name,email,role`) to create pending invitations in bulk. Scheduled classes can include a manually supplied meeting URL; the app does not host video calls. For a linked meeting, a participant can explicitly start browser screen/tab recording and download the resulting WebM locally after obtaining everyone’s consent; this does not silently record or integrate with the external meeting provider. Automatic email delivery and payment processing are not configured.

## Supabase database setup

Create a Supabase project, then copy its PostgreSQL connection string from **Project Settings → Database → Connection string**. Set `SUPABASE_DB_URL` in the backend environment (or the root `.env` used by Docker Compose). Use a server-side connection string and keep it private; do not add it to any `VITE_*` variable. The API creates/migrates its tables and seeds the IKS catalog when it starts. When the variable is empty, local development uses SQLite as before. Existing SQLite records are not automatically copied into Supabase.

The API connects directly to Supabase PostgreSQL; no Supabase service-role key is exposed to the browser. Configure trusted teacher emails through `TEACHER_EMAILS`; other registered accounts remain learners.

Backend API tests use a temporary SQLite file through `IKS_DB_PATH`; running the test suite does not delete or reset the local application database.

## Groq AI setup

Create a Groq API key and keep it **server-side**. Do not put the Groq key in React/Vite variables.

In `backend/.env`:

```text
GROQ_API_KEY=your-groq-key
GROQ_MODEL=openai/gpt-oss-20b
```

The frontend only asks the FastAPI `/api/ai/ask` endpoint; the browser never receives the Groq secret.

## Run in VS Code on Windows

### FastAPI terminal

```powershell
cd backend
python -m venv .venv
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m uvicorn app.main:app --reload --port 8000
```

FastAPI docs:

`http://127.0.0.1:8000/docs`

### React terminal

Open a second VS Code terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open:

`http://localhost:5173`

Vite already proxies `/api/*` to FastAPI on port 8000.

### Node.js production mode

Build React:

```powershell
cd frontend
npm install
npm run build
```

Then:

```powershell
cd ..\server
npm install
node server.js
```

Open:

`http://localhost:3000`

### One-click development on Windows

Run:

```text
run-dev.bat
```

It creates the Python environment when needed and starts FastAPI and Vite in separate windows.

## Docker

From the project root:

```powershell
docker compose up --build
```

The React Firebase values are passed as build arguments and the Groq/Firebase server values are passed to FastAPI.

## Firebase and Groq configuration

Create `frontend/.env.local` from `frontend/.env.example` and enter your Firebase Web app values. Enable Google, Email/Password and Anonymous sign-in in Firebase Authentication. Then create `backend/.env` from `backend/.env.example` and set `GROQ_API_KEY`, `GROQ_MODEL`, `FIREBASE_PROJECT_ID`, and (optionally) `TEACHER_EMAILS`. FastAPI loads `backend/.env` automatically. See `docs/FIREBASE_GROQ_SETUP.md` for the exact checklist.

## Local demo mode

When Firebase is not configured, the app keeps a local guest/demo path so the learning UI can still be explored. Production deployments should configure Firebase and FastAPI token verification.

Demo local accounts:

- Learner: `student@iksphere.local`
- Teacher: `teacher@iksphere.local`

## Project structure

```text
VidyaDhara-mobile-final/
├── backend/
│   ├── app/
│   │   ├── main.py
│   │   └── seed.json
│   └── tests/
├── frontend/
│   ├── public/
│   └── src/
│       ├── api.js
│       ├── firebase.js
│       ├── i18n.js
│       ├── main.jsx
│       └── styles.css
├── server/
│   ├── server.js
│   └── Dockerfile
├── docker-compose.yml
├── run-dev.bat
└── README.md
```

## Important source-quality rule

VidyaDhara intentionally avoids presenting a made-up line as an ancient quotation. Where the historical record is uncertain, the UI should describe the uncertainty. Modern scientific comparisons are presented as comparisons, not as proof that an ancient text anticipated a modern theory.

## Verification

Run the backend checks from the project root:

```powershell
pytest -q
```

Or from `backend`:

```powershell
pytest -q
```

Also check JavaScript server syntax:

```powershell
node --check server/server.js
```

The learner Home page links directly to the Computing in Ancient India syllabus; Learn shows all five chapters and their topic breakdowns, and Explore search also finds it with “syllabus.” The course catalog includes the requested content as five topic-led lessons. Institution names and subject codes are intentionally excluded from the course content. No Kotlin, Android, Jetpack Compose or native mobile runtime is part of this release.
