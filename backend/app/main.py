from __future__ import annotations

import base64
import hashlib
import json
import os
import random
import re
import secrets
import sqlite3
from contextlib import asynccontextmanager
from datetime import date, datetime, timedelta, timezone
from io import BytesIO
from pathlib import Path
from typing import Literal, Optional

import httpx
from dotenv import load_dotenv
from fastapi import FastAPI, File, Form, Header, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, Response
from pydantic import BaseModel, Field, field_validator, model_validator
from reportlab.lib.pagesizes import A4, landscape
from reportlab.pdfgen import canvas

BASE = Path(__file__).resolve().parent
load_dotenv(BASE / ".env")
load_dotenv(BASE.parent.parent / ".env", override=False)
DB_PATH = Path(os.getenv("IKS_DB_PATH", str(BASE / "iksphere.db"))).expanduser()
SEED_PATH = BASE / "seed.json"
CURRICULUM_PATH = BASE / "curriculum_computing.json"
ASSIGNMENT_UPLOAD_DIR = Path(os.getenv("ASSIGNMENT_UPLOAD_DIR", str(BASE / "assignment_uploads"))).expanduser()
MAX_ASSIGNMENT_FILE_BYTES = 20 * 1024 * 1024

GROQ_API_KEY = os.getenv("GROQ_API_KEY", "").strip()
GROQ_MODEL = os.getenv("GROQ_MODEL", "openai/gpt-oss-20b").strip()
SUPABASE_DB_URL = os.getenv("SUPABASE_DB_URL", "").strip()
FIREBASE_PROJECT_ID = os.getenv("FIREBASE_PROJECT_ID", "").strip()
TEACHER_EMAILS = {x.strip().lower() for x in os.getenv("TEACHER_EMAILS", "").split(",") if x.strip()}
SUPER_ADMIN_EMAILS = {x.strip().lower() for x in os.getenv("SUPER_ADMIN_EMAILS", "").split(",") if x.strip()}
CORS_ORIGINS = [x.strip() for x in os.getenv("CORS_ORIGINS", "http://localhost:5173,http://localhost:3000") .split(",") if x.strip()]
APP_VERSION = "5.0.0-mobile"

COURSE_SCHOLARS = {
    "course_ganita": ["sch_baudhayana", "sch_brahmagupta", "sch_madhava"],
    "course_khagola": ["sch_aryabhata", "sch_brahmagupta"],
    "course_ayurveda": ["sch_sushruta", "sch_charaka"],
    "course_darshana": ["sch_kanada"],
    "course_vyakarana": ["sch_panini"],
    "course_rasayana": [],
    "course_sthapatya": [],
    "course_krishi": [],
    "course_jala": [],
}

SANSKRIT_GUIDE = {
    "vowels": [
        ["अ", "a", "short a"], ["आ", "ā", "long aa"], ["इ", "i", "short i"], ["ई", "ī", "long ee"],
        ["उ", "u", "short u"], ["ऊ", "ū", "long oo"], ["ऋ", "ṛ", "vocalic r"], ["ए", "e", "long e"],
        ["ऐ", "ai", "as in aisle"], ["ओ", "o", "long o"], ["औ", "au", "as in loud"], ["अं", "aṃ", "anusvāra"],
        ["अः", "aḥ", "visarga"],
    ],
    "consonants": [
        ["क", "ka", "ka"], ["ख", "kha", "aspirated kha"], ["ग", "ga", "ga"], ["घ", "gha", "aspirated gha"],
        ["च", "ca", "cha-like c"], ["ज", "ja", "ja"], ["ट", "ṭa", "retroflex t"], ["ड", "ḍa", "retroflex d"],
        ["त", "ta", "dental t"], ["द", "da", "dental d"], ["प", "pa", "pa"], ["ब", "ba", "ba"],
        ["म", "ma", "ma"], ["य", "ya", "ya"], ["र", "ra", "ra"], ["ल", "la", "la"],
        ["व", "va", "va/wa"], ["श", "śa", "palatal sh"], ["ष", "ṣa", "retroflex sh"], ["स", "sa", "sa"], ["ह", "ha", "ha"],
    ],
    "terms": [
        ["ज्ञान", "jñāna", "knowledge"], ["विद्या", "vidyā", "learning / knowledge"], ["गणित", "gaṇita", "mathematics"],
        ["खगोल", "khagola", "astronomy / celestial sphere"], ["शून्य", "śūnya", "zero / void"], ["सूत्र", "sūtra", "compact rule / aphorism"],
        ["आचार्य", "ācārya", "teacher / preceptor"], ["श्लोक", "śloka", "verse"], ["तर्क", "tarka", "reasoning"], ["विज्ञान", "vijñāna", "discernment / specialized knowledge"],
    ],
}


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def today_key() -> str:
    return date.today().isoformat()


def uid(prefix: str = "id") -> str:
    return f"{prefix}_{secrets.token_hex(6)}"


def postgres_sql(sql: str) -> Optional[str]:
    sql = re.sub(
        r"ALTER TABLE (\w+) ADD COLUMN (?!IF NOT EXISTS)",
        r"ALTER TABLE \1 ADD COLUMN IF NOT EXISTS ",
        sql,
        flags=re.IGNORECASE,
    )
    if re.match(r"\s*PRAGMA\b", sql, flags=re.IGNORECASE):
        return None
    ignored_insert = re.match(r"\s*INSERT\s+OR\s+IGNORE\s+INTO\b", sql, flags=re.IGNORECASE)
    if ignored_insert:
        sql = re.sub(r"INSERT\s+OR\s+IGNORE\s+INTO", "INSERT INTO", sql, count=1, flags=re.IGNORECASE)
        sql = sql.rstrip().removesuffix(";") + " ON CONFLICT DO NOTHING"
    return sql.replace("?", "%s")


class PostgresConnection:
    def __init__(self, connection):
        self.connection = connection

    def execute(self, sql: str, params=()):
        sql = postgres_sql(sql)
        if sql is None:
            return None
        return self.connection.execute(sql, params)

    def executescript(self, script: str):
        for statement in script.split(";"):
            if statement.strip():
                self.execute(statement)

    def __getattr__(self, name):
        return getattr(self.connection, name)


def db():
    if SUPABASE_DB_URL:
        try:
            import psycopg
            from psycopg.rows import dict_row
        except ImportError as exc:
            raise RuntimeError("SUPABASE_DB_URL is configured but the psycopg package is missing") from exc
        return PostgresConnection(psycopg.connect(SUPABASE_DB_URL, row_factory=dict_row))
    conn = sqlite3.connect(DB_PATH, timeout=15)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys=ON")
    conn.execute("PRAGMA journal_mode=WAL")
    return conn


def rows(sql: str, params=()):
    conn = db()
    try:
        return [dict(r) for r in conn.execute(sql, params).fetchall()]
    finally:
        conn.close()


def row(sql: str, params=()):
    conn = db()
    try:
        r = conn.execute(sql, params).fetchone()
        return dict(r) if r else None
    finally:
        conn.close()


def translation_shape_matches(source, translated) -> bool:
    if isinstance(source, dict):
        return isinstance(translated, dict) and source.keys() == translated.keys() and all(
            (source[key] == translated[key] if key == "id" or key.endswith("_id") or key in {"created_by", "email", "join_code"}
             else translation_shape_matches(source[key], translated[key]))
            for key in source
        )
    if isinstance(source, list):
        return isinstance(translated, list) and len(source) == len(translated) and all(
            translation_shape_matches(left, right) for left, right in zip(source, translated)
        )
    if isinstance(source, str):
        return isinstance(translated, str)
    return type(source) is type(translated) and source == translated


def translate_content(cache_key: str, language: str, source):
    if language == "en":
        return source
    language_names = {"hi": "Hindi", "mr": "Marathi"}
    language_name = language_names.get(language)
    if not language_name:
        raise HTTPException(422, "Choose English, Hindi, or Marathi.")
    if not GROQ_API_KEY:
        raise HTTPException(503, "Course translations require GROQ_API_KEY in the backend environment.")

    source_json = json.dumps(source, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    source_hash = hashlib.sha256(source_json.encode("utf-8")).hexdigest()
    cached = row(
        "SELECT source_hash,content_json FROM content_translations WHERE cache_key=? AND language=?",
        (cache_key, language),
    )
    if cached and cached["source_hash"] == source_hash:
        try:
            translated = json.loads(cached["content_json"])
            if translation_shape_matches(source, translated):
                return translated
        except json.JSONDecodeError:
            pass

    payload = {
        "model": GROQ_MODEL,
        "messages": [
            {
                "role": "system",
                "content": (
                    f"Translate every human-readable string in the provided JSON into natural, clear {language_name}. "
                    "Preserve every JSON key, object/list structure, identifier, number, and option order exactly. "
                    "Keep Sanskrit and Devanagari text, IAST transliteration, formulas, dates, and URLs unchanged. "
                    "Translate lesson explanations faithfully; do not summarize or omit text. "
                    "Return only valid JSON, with no Markdown fences or commentary."
                ),
            },
            {"role": "user", "content": source_json},
        ],
        "temperature": 0.1,
        "max_completion_tokens": 12000,
        "response_format": {"type": "json_object"},
    }
    try:
        response = httpx.post(
            "https://api.groq.com/openai/v1/chat/completions",
            headers={"Authorization": f"Bearer {GROQ_API_KEY}", "Content-Type": "application/json"},
            json=payload,
            timeout=120,
        )
        response.raise_for_status()
        body = response.json()
        choices = body.get("choices") or []
        message = choices[0].get("message") if choices else None
        content = (message or {}).get("content", "").strip()
        if choices and choices[0].get("finish_reason") == "length":
            raise HTTPException(502, "The translation response was cut short. Try again.")
        translated = json.loads(content)
    except HTTPException:
        raise
    except httpx.HTTPStatusError as exc:
        raise HTTPException(502, f"Translation service returned HTTP {exc.response.status_code}.") from exc
    except (httpx.HTTPError, json.JSONDecodeError, IndexError, TypeError) as exc:
        raise HTTPException(502, "The translation service could not return valid translated content.") from exc
    if not translation_shape_matches(source, translated):
        raise HTTPException(502, "The translation response changed the content structure. Try again.")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO content_translations(cache_key,language,source_hash,content_json,updated_at)
            VALUES (?,?,?,?,?) ON CONFLICT(cache_key,language) DO UPDATE SET
            source_hash=excluded.source_hash,content_json=excluded.content_json,updated_at=excluded.updated_at""",
            (cache_key, language, source_hash, json.dumps(translated, ensure_ascii=False), now_iso()),
        )
        conn.commit()
    finally:
        conn.close()
    return translated


def course_translation_source(course_data: dict) -> dict:
    translatable_course = {
        field: course_data.get(field) for field in
        ("title", "category", "level", "duration", "description", "era", "highlights", "texts")
    }
    translatable_course["lessons"] = []
    for lesson in course_data.get("lessons", []):
        translatable_course["lessons"].append({
            field: lesson.get(field) for field in
            ("title", "overview", "content", "english_meaning", "meter", "historical_context",
             "modern_parallel", "source", "takeaways", "concepts", "practical", "difficulty",
             "checkpoint", "study_outline")
        })
    return translatable_course


def translated_course(course_data: dict, language: str) -> dict:
    if language == "en":
        return course_data
    translation = translate_content(f"course:{course_data['id']}", language, course_translation_source(course_data))
    result = {**course_data}
    for field in ("title", "category", "level", "duration", "description", "era", "highlights", "texts"):
        result[field] = translation[field]
    result["lessons"] = [
        {**lesson, **lesson_translation}
        for lesson, lesson_translation in zip(course_data.get("lessons", []), translation["lessons"])
    ]
    return result


def lesson_study_outline(lesson: dict) -> list[dict]:
    concepts = lesson.get("concepts", [])
    explicit = []
    for concept in concepts:
        if " :: " in concept:
            title, detail = concept.split(" :: ", 1)
            explicit.append({"title": title.strip(), "detail": detail.strip()})
    if len(explicit) >= 10:
        return explicit[:15]

    focus = lesson.get("overview") or lesson.get("content") or lesson.get("title", "the lesson topic")
    content = lesson.get("content") or focus
    takeaways = [x for x in lesson.get("takeaways", []) if x.strip()]
    terms = lesson.get("word_by_word", [])
    sections = [
        {"title": "Lesson focus", "detail": focus},
        {"title": "Historical context", "detail": lesson.get("historical_context") or content},
        {"title": "Core explanation", "detail": content},
        {"title": "Text and evidence", "detail": lesson.get("source") or "Use the lesson's historical context and named sources to evaluate claims."},
    ]
    sections.extend({"title": f"Key idea {i + 1}", "detail": text} for i, text in enumerate(takeaways[:4]))
    sections.extend(explicit[: max(0, 12 - len(sections))])
    if terms:
        sections.append({"title": "Key terms", "detail": "; ".join(terms)})
    sections.append({"title": "Modern comparison", "detail": lesson.get("modern_parallel") or "Compare the historical method with a modern idea without treating them as identical."})
    sections.append({"title": "Apply the idea", "detail": lesson.get("practical") or "Summarize the method and identify what evidence would support your explanation."})
    checkpoint = lesson.get("checkpoint", {})
    if checkpoint.get("question"):
        sections.append({"title": "Review question", "detail": checkpoint["question"] + (f" {checkpoint.get('explanation', '')}" if checkpoint.get("explanation") else "")})
    sections.append({"title": "Summary", "detail": "Explain the lesson's main idea, its evidence and one careful comparison in your own words."})
    while len(sections) < 10:
        sections.append({"title": f"Study prompt {len(sections) + 1}", "detail": focus})
    return sections[:15]


def ensure_practice_quizzes(conn, course_seeds: list[dict]) -> None:
    for course in course_seeds:
        lessons = conn.execute("SELECT * FROM lessons WHERE course_id=? ORDER BY position", (course["id"],)).fetchall()
        lessons_outlines = []
        for raw in lessons:
            lesson = dict(raw)
            hydrate_json(lesson, ["takeaways_json", "concepts_json", "checkpoint_json", "word_by_word_json"])
            outline = lesson_study_outline(lesson)
            quiz_id = f"quiz_practice_{lesson['id']}"
            write_practice_quiz(
                conn, quiz_id, course["id"], f"{lesson['title']} — 50-question practice",
                f"Randomized practice for {lesson['title']}. Choose a custom question count and time limit before starting.",
                outline[:10], questions_per_section=5,
            )
            lessons_outlines.append([
                {"title": lesson["title"], "detail": lesson.get("overview") or lesson.get("content") or lesson["title"]},
                *outline[:9],
            ])
        course_outline = []
        for position in range(max((len(items) for items in lessons_outlines), default=0)):
            for items in lessons_outlines:
                if position < len(items):
                    course_outline.append(items[position])
        course_outline = select_course_outline(course_outline, min(20, len(course_outline)))
        base_outline = list(course_outline)
        while course_outline and len(course_outline) < 20:
            source = base_outline[(len(course_outline) - len(base_outline)) % len(base_outline)]
            suffix = "evidence and context" if (len(course_outline) - len(base_outline)) % 2 == 0 else "application and comparison"
            course_outline.append({
                "title": f"{source['title']} — {suffix}",
                "detail": f"Review the evidence, context, application and limits of the lesson's explanation of {source['title']}: {source['detail']}",
            })
        if course_outline:
            write_practice_quiz(
                conn, f"quiz_practice_course_{course['id']}", course["id"],
                f"{course['title']} — 200-question course review",
                f"Randomized review across the topics in {course['title']}.",
                course_outline, questions_per_section=10,
            )


def select_course_outline(outline: list[dict], limit: int) -> list[dict]:
    selected = []
    seen = set()
    for section in outline:
        key = section["title"].casefold()
        if key not in seen:
            selected.append(section)
            seen.add(key)
    return selected[:limit]


def write_practice_quiz(
    conn, quiz_id: str, course_id: str, title: str, description: str, outline: list[dict],
    questions_per_section: int,
) -> None:
    conn.execute(
        "INSERT INTO quizzes(id,course_id,title,description,created_by,created_at) VALUES (?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET title=excluded.title,description=excluded.description",
        (quiz_id, course_id, title, description, "seed_practice", now_iso()),
    )
    expected_count = len(outline) * questions_per_section
    existing_ids = [
        item["id"] for item in conn.execute("SELECT id FROM quiz_questions WHERE quiz_id=?", (quiz_id,)).fetchall()
    ]
    if len(existing_ids) == expected_count and expected_count >= 50 and all("_v2_" in item for item in existing_ids):
        return
    conn.execute("DELETE FROM quiz_questions WHERE quiz_id=?", (quiz_id,))
    question_templates = [
        ("Which heading most directly explains this idea: “{detail}”?", "title"),
        ("What is the central point of “{title}”?", "detail"),
        ("Which topic should a student revisit to understand “{detail}”?", "title"),
        ("Which explanation belongs under “{title}”?", "detail"),
        ("A learner is reviewing “{detail}”. Which lesson section should they open?", "title"),
        ("Which lesson section provides the key context for “{title}”?", "title"),
        ("What should a student be able to explain after studying “{title}”?", "detail"),
        ("Which section is most useful when checking the claim “{detail}”?", "title"),
        ("Which statement best summarizes the lesson's treatment of “{title}”?", "detail"),
        ("Where should a learner look for a careful explanation of “{detail}”?", "title"),
    ]
    seen_questions = set()
    for section_index, section in enumerate(outline):
        others = [item for i, item in enumerate(outline) if i != section_index]
        for variant, (template, answer_field) in enumerate(question_templates[:questions_per_section]):
            correct_title = section["title"]
            correct_detail = section["detail"]
            question = template.format(title=correct_title, detail=correct_detail)
            if question.casefold() in seen_questions:
                question = f"{question} Use the “{correct_title}” section to answer."
            if question.casefold() in seen_questions:
                question = f"{question} (Study item {section_index + 1}.{variant + 1})"
            seen_questions.add(question.casefold())
            answer = correct_title if answer_field == "title" else correct_detail
            options = [answer]
            for distractor in others:
                value = distractor["title"] if answer_field == "title" else distractor["detail"]
                if value != answer and value not in options:
                    options.append(value)
                if len(options) == 4:
                    break
            while len(options) < 4:
                options.append(f"A separate topic from {title}")
            correct_index = random.Random(f"{quiz_id}:{section_index}:{variant}").randrange(len(options))
            options[0], options[correct_index] = options[correct_index], options[0]
            question_id = f"{quiz_id}_v2_{section_index:02d}_{variant}"
            conn.execute(
                "INSERT INTO quiz_questions VALUES (?,?,?,?,?,?)",
                (question_id, quiz_id, question, json.dumps(options, ensure_ascii=False), correct_index, f"{correct_title}: {correct_detail}"),
            )


def hydrate_json(obj: dict, fields: list[str]) -> dict:
    for field in fields:
        raw = obj.pop(field, None)
        if raw is not None:
            try:
                obj[field.removesuffix("_json")] = json.loads(raw or "[]")
            except json.JSONDecodeError:
                obj[field.removesuffix("_json")] = []
    return obj


def ensure_db() -> None:
    seed = json.loads(SEED_PATH.read_text(encoding="utf-8"))
    extra = json.loads(CURRICULUM_PATH.read_text(encoding="utf-8"))
    course_seeds = [*seed["courses"], extra["course"]]
    conn = db()
    conn.executescript(
        """
        CREATE TABLE IF NOT EXISTS users(
            id TEXT PRIMARY KEY, name TEXT NOT NULL, email TEXT, role TEXT NOT NULL,
            firebase_uid TEXT UNIQUE, provider TEXT DEFAULT 'local', is_guest INTEGER DEFAULT 0, created_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS courses(
            id TEXT PRIMARY KEY, title TEXT NOT NULL, sanskrit TEXT, category TEXT, icon TEXT,
            level TEXT, duration TEXT, description TEXT, era TEXT, highlights_json TEXT DEFAULT '[]',
            texts_json TEXT DEFAULT '[]', created_by TEXT, created_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS lessons(
            id TEXT PRIMARY KEY, course_id TEXT NOT NULL, title TEXT, sanskrit TEXT, overview TEXT,
            content TEXT, takeaways_json TEXT DEFAULT '[]', practical TEXT, minutes INTEGER, difficulty TEXT,
            position INTEGER, iast TEXT DEFAULT '', english_meaning TEXT DEFAULT '', meter TEXT DEFAULT '',
            historical_context TEXT DEFAULT '', modern_parallel TEXT DEFAULT '', source TEXT DEFAULT '',
            concepts_json TEXT DEFAULT '[]', checkpoint_json TEXT DEFAULT '{}', word_by_word_json TEXT DEFAULT '[]',
            FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS paths(
            id TEXT PRIMARY KEY, title TEXT NOT NULL, level TEXT, weeks INTEGER, description TEXT,
            created_by TEXT, created_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS path_courses(
            path_id TEXT, course_id TEXT, position INTEGER, PRIMARY KEY(path_id, course_id),
            FOREIGN KEY(path_id) REFERENCES paths(id) ON DELETE CASCADE, FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS scholars(
            id TEXT PRIMARY KEY, name TEXT, sanskrit TEXT, era TEXT, domain TEXT, work TEXT,
            contribution TEXT, location TEXT DEFAULT 'Indian subcontinent', quote TEXT DEFAULT '', quote_note TEXT DEFAULT ''
        );
        CREATE TABLE IF NOT EXISTS progress(
            user_id TEXT, lesson_id TEXT, completed INTEGER DEFAULT 0, updated_at TEXT NOT NULL,
            PRIMARY KEY(user_id, lesson_id), FOREIGN KEY(lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS quizzes(
            id TEXT PRIMARY KEY, course_id TEXT, title TEXT NOT NULL, description TEXT, lesson_id TEXT,
            created_by TEXT, created_at TEXT NOT NULL, settings_json TEXT DEFAULT '{}', published INTEGER DEFAULT 1,
            FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE SET NULL
        );
        CREATE TABLE IF NOT EXISTS quiz_questions(
            id TEXT PRIMARY KEY, quiz_id TEXT, question TEXT, options_json TEXT, answer_index INTEGER, explanation TEXT,
            FOREIGN KEY(quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS attempts(
            id TEXT PRIMARY KEY, user_id TEXT, quiz_id TEXT, score INTEGER, total INTEGER, percent REAL,
            elapsed_seconds INTEGER, started_at TEXT, classroom_assignment_id TEXT, created_at TEXT NOT NULL,
            FOREIGN KEY(quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS attempt_questions(
            attempt_id TEXT NOT NULL, user_id TEXT NOT NULL, quiz_id TEXT NOT NULL, question_id TEXT NOT NULL,
            PRIMARY KEY(attempt_id,question_id)
        );
        CREATE TABLE IF NOT EXISTS content_translations(
            cache_key TEXT NOT NULL, language TEXT NOT NULL, source_hash TEXT NOT NULL,
            content_json TEXT NOT NULL, updated_at TEXT NOT NULL, PRIMARY KEY(cache_key,language)
        );
        CREATE TABLE IF NOT EXISTS bookmarks(
            user_id TEXT, lesson_id TEXT, created_at TEXT NOT NULL, PRIMARY KEY(user_id, lesson_id),
            FOREIGN KEY(lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS notes(
            id TEXT PRIMARY KEY, user_id TEXT, lesson_id TEXT, content TEXT, created_at TEXT NOT NULL, updated_at TEXT NOT NULL,
            FOREIGN KEY(lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS certificates(
            id TEXT PRIMARY KEY, user_id TEXT, course_id TEXT, certificate_no TEXT UNIQUE, issued_at TEXT NOT NULL,
            FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS badges(id TEXT PRIMARY KEY, name TEXT, icon TEXT, description TEXT, condition_text TEXT);
        CREATE TABLE IF NOT EXISTS user_badges(user_id TEXT, badge_id TEXT, awarded_at TEXT NOT NULL, PRIMARY KEY(user_id, badge_id));
        CREATE TABLE IF NOT EXISTS study_days(user_id TEXT, day_key TEXT, created_at TEXT NOT NULL, PRIMARY KEY(user_id, day_key));
        CREATE TABLE IF NOT EXISTS flashcards(
            id TEXT PRIMARY KEY, course_id TEXT, category TEXT, front TEXT, back TEXT, parallel TEXT,
            position INTEGER DEFAULT 0, FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS flashcard_reviews(
            user_id TEXT NOT NULL, card_id TEXT NOT NULL, repetitions INTEGER NOT NULL DEFAULT 0,
            interval_days REAL NOT NULL DEFAULT 0, due_at TEXT NOT NULL, last_reviewed_at TEXT NOT NULL,
            PRIMARY KEY(user_id, card_id),
            FOREIGN KEY(card_id) REFERENCES flashcards(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classrooms(
            id TEXT PRIMARY KEY, teacher_id TEXT NOT NULL, name TEXT NOT NULL,
            join_code TEXT NOT NULL UNIQUE, created_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS classroom_members(
            classroom_id TEXT NOT NULL, user_id TEXT NOT NULL, joined_at TEXT NOT NULL,
            PRIMARY KEY(classroom_id,user_id),
            FOREIGN KEY(classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_assignments(
            id TEXT PRIMARY KEY, classroom_id TEXT NOT NULL, course_id TEXT, quiz_id TEXT,
            created_at TEXT NOT NULL,
            CHECK ((course_id IS NOT NULL AND quiz_id IS NULL) OR (course_id IS NULL AND quiz_id IS NOT NULL)),
            FOREIGN KEY(classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
            FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE,
            FOREIGN KEY(quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_events(
            id TEXT PRIMARY KEY, classroom_id TEXT NOT NULL, title TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '', starts_at TEXT NOT NULL, ends_at TEXT,
            meeting_url TEXT NOT NULL DEFAULT '', created_by TEXT NOT NULL, created_at TEXT NOT NULL,
            FOREIGN KEY(classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_attendance(
            event_id TEXT NOT NULL, user_id TEXT NOT NULL, status TEXT NOT NULL,
            marked_by TEXT NOT NULL, marked_at TEXT NOT NULL,
            PRIMARY KEY(event_id,user_id),
            FOREIGN KEY(event_id) REFERENCES classroom_events(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS announcements(
            id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, classroom_id TEXT,
            author_id TEXT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, created_at TEXT NOT NULL,
            FOREIGN KEY(classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS course_resources(
            id TEXT PRIMARY KEY, course_id TEXT NOT NULL, institution_id TEXT,
            created_by TEXT NOT NULL, title TEXT NOT NULL, url TEXT NOT NULL,
            description TEXT NOT NULL DEFAULT '', kind TEXT NOT NULL DEFAULT 'link', created_at TEXT NOT NULL,
            FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_discussions(
            id TEXT PRIMARY KEY, classroom_id TEXT NOT NULL, user_id TEXT NOT NULL,
            parent_id TEXT, body TEXT NOT NULL, created_at TEXT NOT NULL,
            FOREIGN KEY(classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY(parent_id) REFERENCES classroom_discussions(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_tasks(
            id TEXT PRIMARY KEY, classroom_id TEXT NOT NULL, created_by TEXT NOT NULL,
            title TEXT NOT NULL, instructions TEXT NOT NULL, due_at TEXT, points INTEGER NOT NULL DEFAULT 100,
            created_at TEXT NOT NULL,
            FOREIGN KEY(classroom_id) REFERENCES classrooms(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_submissions(
            id TEXT PRIMARY KEY, task_id TEXT NOT NULL, student_id TEXT NOT NULL,
            body TEXT NOT NULL, submitted_at TEXT NOT NULL, grade INTEGER, feedback TEXT NOT NULL DEFAULT '',
            graded_at TEXT, UNIQUE(task_id,student_id),
            FOREIGN KEY(task_id) REFERENCES classroom_tasks(id) ON DELETE CASCADE,
            FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_task_files(
            id TEXT PRIMARY KEY, task_id TEXT NOT NULL, stored_name TEXT NOT NULL UNIQUE,
            original_name TEXT NOT NULL, content_type TEXT NOT NULL, size_bytes INTEGER NOT NULL,
            uploaded_by TEXT NOT NULL, created_at TEXT NOT NULL,
            FOREIGN KEY(task_id) REFERENCES classroom_tasks(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS classroom_submission_files(
            id TEXT PRIMARY KEY, submission_id TEXT NOT NULL, stored_name TEXT NOT NULL UNIQUE,
            original_name TEXT NOT NULL, content_type TEXT NOT NULL, size_bytes INTEGER NOT NULL,
            uploaded_at TEXT NOT NULL,
            FOREIGN KEY(submission_id) REFERENCES classroom_submissions(id) ON DELETE CASCADE
        );
        CREATE TABLE IF NOT EXISTS daily_wisdom(
            id TEXT PRIMARY KEY, sanskrit TEXT, iast TEXT, translation TEXT, source TEXT, url TEXT, word_by_word_json TEXT
        );
        CREATE TABLE IF NOT EXISTS chat_messages(
            id TEXT PRIMARY KEY, user_id TEXT NOT NULL, role TEXT NOT NULL, content TEXT NOT NULL, created_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS plans(
            id TEXT PRIMARY KEY, name TEXT NOT NULL, student_limit INTEGER NOT NULL,
            teacher_limit INTEGER NOT NULL, course_limit INTEGER NOT NULL, active INTEGER NOT NULL DEFAULT 1
        );
        CREATE TABLE IF NOT EXISTS institutions(
            id TEXT PRIMARY KEY, name TEXT NOT NULL, short_name TEXT NOT NULL UNIQUE,
            logo TEXT DEFAULT '', favicon TEXT DEFAULT '', primary_color TEXT DEFAULT '#c75d0c',
            secondary_color TEXT DEFAULT '#1a237e', welcome_message TEXT DEFAULT '',
            contact_email TEXT DEFAULT '', phone TEXT DEFAULT '', address TEXT DEFAULT '',
            website TEXT DEFAULT '', status TEXT NOT NULL DEFAULT 'trial', plan_id TEXT NOT NULL,
            subscription_status TEXT NOT NULL DEFAULT 'trial', trial_ends_at TEXT,
            onboarding_step INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL, updated_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS subscriptions(
            id TEXT PRIMARY KEY, institution_id TEXT NOT NULL UNIQUE, plan_id TEXT NOT NULL,
            status TEXT NOT NULL, started_at TEXT NOT NULL, trial_ends_at TEXT
        );
        CREATE TABLE IF NOT EXISTS institution_memberships(
            user_id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'active',
            department_id TEXT, program_id TEXT, academic_year TEXT, semester TEXT,
            division TEXT, batch TEXT, student_number TEXT, created_at TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS institution_invites(
            id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, email TEXT NOT NULL,
            name TEXT NOT NULL, role TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'pending',
            department_id TEXT, program_id TEXT, academic_year TEXT, semester TEXT,
            division TEXT, batch TEXT, student_number TEXT,
            created_at TEXT NOT NULL, accepted_at TEXT, UNIQUE(institution_id,email)
        );
        CREATE TABLE IF NOT EXISTS tenant_resources(
            resource_type TEXT NOT NULL, resource_id TEXT NOT NULL, institution_id TEXT NOT NULL,
            PRIMARY KEY(resource_type,resource_id)
        );
        CREATE TABLE IF NOT EXISTS departments(
            id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, name TEXT NOT NULL,
            created_at TEXT NOT NULL, UNIQUE(institution_id,name)
        );
        CREATE TABLE IF NOT EXISTS programs(
            id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, department_id TEXT NOT NULL,
            name TEXT NOT NULL, created_at TEXT NOT NULL, UNIQUE(institution_id,name)
        );
        CREATE TABLE IF NOT EXISTS batches(
            id TEXT PRIMARY KEY, institution_id TEXT NOT NULL, program_id TEXT,
            academic_year TEXT NOT NULL, semester TEXT NOT NULL, division TEXT NOT NULL,
            batch_name TEXT DEFAULT '', created_at TEXT NOT NULL
        );
        """
    )
    # Migrate older databases while keeping the computing syllabus in the authored IKS course catalog.
    for table, col, ddl in [
        ("users", "firebase_uid", "TEXT"), ("users", "provider", "TEXT DEFAULT 'local'"), ("users", "is_guest", "INTEGER DEFAULT 0"),
        ("courses", "texts_json", "TEXT DEFAULT '[]'"), ("lessons", "iast", "TEXT DEFAULT ''"),
        ("lessons", "english_meaning", "TEXT DEFAULT ''"), ("lessons", "meter", "TEXT DEFAULT ''"),
        ("lessons", "historical_context", "TEXT DEFAULT ''"), ("lessons", "modern_parallel", "TEXT DEFAULT ''"),
        ("lessons", "source", "TEXT DEFAULT ''"), ("lessons", "concepts_json", "TEXT DEFAULT '[]'"),
        ("lessons", "checkpoint_json", "TEXT DEFAULT '{}'"), ("lessons", "word_by_word_json", "TEXT DEFAULT '[]'"),
        ("scholars", "location", "TEXT DEFAULT 'Indian subcontinent'"), ("scholars", "quote", "TEXT DEFAULT ''"),
        ("scholars", "quote_note", "TEXT DEFAULT ''"),
        ("attempts", "elapsed_seconds", "INTEGER"), ("attempts", "started_at", "TEXT"),
        ("attempts", "classroom_assignment_id", "TEXT"), ("quizzes", "lesson_id", "TEXT"),
        ("quizzes", "settings_json", "TEXT DEFAULT '{}'"), ("quizzes", "published", "INTEGER DEFAULT 1"),
    ]:
        try:
            conn.execute(f"ALTER TABLE {table} ADD COLUMN {col} {ddl}")
        except sqlite3.OperationalError:
            pass

    for plan in [
        ("starter", "Starter", 100, 10, 50),
        ("professional", "Professional", 5000, 200, 1000),
        ("enterprise", "Enterprise", 1000000, 100000, 100000),
    ]:
        conn.execute("INSERT OR IGNORE INTO plans(id,name,student_limit,teacher_limit,course_limit) VALUES (?,?,?,?,?)", plan)

    now = now_iso()
    for c in course_seeds:
        conn.execute(
            "INSERT INTO courses VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO NOTHING",
            (
                c["id"], c["title"], c.get("sanskrit", ""), c.get("category", "General"), c.get("icon", "📘"),
                c.get("level", "Beginner"), c.get("duration", "2 weeks"), c.get("description", ""), c.get("era", ""),
                json.dumps(c.get("highlights", []), ensure_ascii=False), json.dumps(c.get("texts", []), ensure_ascii=False), "seed", now,
            ),
        )
        # Keep authored seed content stable; teacher-created courses are untouched.
        conn.execute(
            "UPDATE courses SET title=?,sanskrit=?,category=?,icon=?,level=?,duration=?,description=?,era=?,highlights_json=?,texts_json=? WHERE id=? AND created_by='seed'",
            (c["title"], c.get("sanskrit", ""), c.get("category", "General"), c.get("icon", "📘"), c.get("level", "Beginner"), c.get("duration", "2 weeks"), c.get("description", ""), c.get("era", ""), json.dumps(c.get("highlights", []), ensure_ascii=False), json.dumps(c.get("texts", []), ensure_ascii=False), c["id"]),
        )
        for i, l in enumerate(c.get("lessons", [])):
            conn.execute(
                """INSERT INTO lessons(id,course_id,title,sanskrit,overview,content,takeaways_json,practical,minutes,difficulty,position,iast,english_meaning,meter,historical_context,modern_parallel,source,concepts_json,checkpoint_json,word_by_word_json)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET
                course_id=excluded.course_id,title=excluded.title,sanskrit=excluded.sanskrit,overview=excluded.overview,
                content=excluded.content,takeaways_json=excluded.takeaways_json,practical=excluded.practical,
                minutes=excluded.minutes,difficulty=excluded.difficulty,position=excluded.position,iast=excluded.iast,
                english_meaning=excluded.english_meaning,meter=excluded.meter,historical_context=excluded.historical_context,
                modern_parallel=excluded.modern_parallel,source=excluded.source,concepts_json=excluded.concepts_json,
                checkpoint_json=excluded.checkpoint_json,word_by_word_json=excluded.word_by_word_json""",
                (
                    l["id"], c["id"], l["title"], l.get("sanskrit", ""), l.get("overview", ""), l.get("content", ""),
                    json.dumps(l.get("takeaways", []), ensure_ascii=False), l.get("practical", ""), int(l.get("minutes", 10)),
                    l.get("difficulty", "Beginner"), i, l.get("iast", l.get("sanskrit", "")), l.get("english_meaning", l.get("overview", "")),
                    l.get("meter", "Textual / instructional passage"), l.get("historical_context", ""), l.get("modern_parallel", ""),
                    l.get("source", ""), json.dumps(l.get("concepts", []), ensure_ascii=False), json.dumps(l.get("checkpoint", {}), ensure_ascii=False),
                    json.dumps(l.get("word_by_word", []), ensure_ascii=False),
                ),
            )

    for s in seed.get("scholars", []):
        conn.execute("""INSERT INTO scholars VALUES (?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET
            name=excluded.name,sanskrit=excluded.sanskrit,era=excluded.era,domain=excluded.domain,work=excluded.work,
            contribution=excluded.contribution,location=excluded.location,quote=excluded.quote,quote_note=excluded.quote_note""", (
            s["id"], s["name"], s.get("sanskrit", ""), s.get("era", ""), s.get("domain", ""), s.get("work", ""),
            s.get("contribution", ""), s.get("location", "Indian subcontinent"), s.get("quote", ""), s.get("quote_note", ""),
        ))
    for p in [*seed.get("paths", []), extra["path"]]:
        conn.execute("INSERT INTO paths VALUES (?,?,?,?,?,?,?) ON CONFLICT(id) DO NOTHING", (p["id"], p["title"], p.get("level", "Beginner"), int(p.get("weeks", 4)), f"{p['title']} guided curriculum", "seed", now))
        conn.execute("UPDATE paths SET title=?,level=?,weeks=?,description=? WHERE id=? AND created_by='seed'", (p["title"], p.get("level", "Beginner"), int(p.get("weeks", 4)), f"{p['title']} guided curriculum", p["id"]))
        conn.execute("DELETE FROM path_courses WHERE path_id=?", (p["id"],))
        for i, cid in enumerate(p.get("course_ids", [])):
            conn.execute("INSERT INTO path_courses VALUES (?,?,?) ON CONFLICT(path_id,course_id) DO NOTHING", (p["id"], cid, i))
    for b in seed.get("badges", []):
        conn.execute("""INSERT INTO badges VALUES (?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET
            name=excluded.name,icon=excluded.icon,description=excluded.description,condition_text=excluded.condition_text""", (b["id"], b["name"], b["icon"], b["description"], b["condition"]))
    for w in seed.get("daily_wisdom", []):
        conn.execute("""INSERT INTO daily_wisdom VALUES (?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET
            sanskrit=excluded.sanskrit,iast=excluded.iast,translation=excluded.translation,source=excluded.source,
            url=excluded.url,word_by_word_json=excluded.word_by_word_json""", (w["id"], w["sanskrit"], w["iast"], w["translation"], w["source"], w.get("url", ""), json.dumps(w.get("word_by_word", []), ensure_ascii=False)))
    for i, f in enumerate([*seed.get("flashcards", []), extra["flashcard"]]):
        conn.execute("""INSERT INTO flashcards VALUES (?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET
            course_id=excluded.course_id,category=excluded.category,front=excluded.front,back=excluded.back,
            parallel=excluded.parallel,position=excluded.position""", (f["id"], f["course_id"], f.get("category", "General"), f["front"], f["back"], f.get("parallel", ""), i))

    # Demo local accounts are useful when Firebase is not configured.
    conn.execute("INSERT OR IGNORE INTO users(id,name,email,role,provider,is_guest,created_at) VALUES (?,?,?,?,?,?,?)", ("demo-student", "Aarav Sharma", "student@iksphere.local", "learner", "local", 0, now))
    conn.execute("INSERT OR IGNORE INTO users(id,name,email,role,provider,is_guest,created_at) VALUES (?,?,?,?,?,?,?)", ("demo-teacher", "Ananya Deshmukh", "teacher@iksphere.local", "teacher", "local", 0, now))

    if not conn.execute("SELECT 1 FROM quizzes LIMIT 1").fetchone():
        qid = "quiz_ganita_foundations"
        conn.execute(
            "INSERT INTO quizzes(id,course_id,title,description,created_by,created_at) VALUES (?,?,?,?,?,?)",
            (qid, "course_ganita", "Ganita Foundations", "Geometry and number concepts checkpoint.", "seed", now),
        )
        questions = [
            ("q1", "Which text is associated with the geometric construction known as the Baudhayana Sulba Sutra?", ["Baudhayana Sulba Sutra", "Charaka Samhita", "Surya Siddhanta", "Ashtadhyayi"], 0, "The Baudhayana Sulba Sutra is a Śulbasūtra text concerned with geometric constructions for ritual spaces."),
            ("q2", "Which mathematician is associated with rules for arithmetic involving zero in the Brahmasphutasiddhanta?", ["Brahmagupta", "Panini", "Sushruta", "Kanada"], 0, "Brahmagupta's Brahmasphutasiddhanta includes arithmetic rules for zero and signed quantities."),
            ("q3", "What is the best historical way to describe the Kerala school's infinite-series work?", ["A mathematical approximation tradition", "A modern computer language", "A metal alloy", "A surgical manual"], 0, "The Kerala mathematical-astronomical tradition developed series methods for trigonometric quantities and π."),
        ]
        for q in questions:
            conn.execute("INSERT INTO quiz_questions VALUES (?,?,?,?,?,?)", (q[0], qid, q[1], json.dumps(q[2], ensure_ascii=False), q[3], q[4]))
    ensure_practice_quizzes(conn, course_seeds)
    conn.commit()
    conn.close()


@asynccontextmanager
async def lifespan(_app):
    ensure_db()
    yield


app = FastAPI(title="VidyaDhara API", version=APP_VERSION, description="Mobile-first Indian Knowledge Systems learning API", lifespan=lifespan)
app.add_middleware(CORSMiddleware, allow_origins=CORS_ORIGINS or ["*"], allow_credentials=False, allow_methods=["*"], allow_headers=["*"])


class LoginIn(BaseModel):
    name: str = Field(min_length=2, max_length=100)
    email: str = Field(min_length=5, max_length=200)
    role: str = "learner"

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value: str):
        value = value.strip().lower()
        if "@" not in value:
            raise ValueError("Enter a valid email address")
        return value


class GuestIn(BaseModel):
    name: str = Field(default="Guest Learner", min_length=2, max_length=100)


class FirebaseAuthIn(BaseModel):
    id_token: str = Field(min_length=20)
    name: str = Field(min_length=2, max_length=100)
    email: str = Field(default="", max_length=200)
    provider: str = Field(default="firebase")


class ProgressIn(BaseModel):
    completed: bool = True


class BookmarkIn(BaseModel):
    lesson_id: str = Field(min_length=1)


class NoteIn(BaseModel):
    lesson_id: str = Field(min_length=1)
    content: str = Field(min_length=1, max_length=5000)


class QuizAnswerIn(BaseModel):
    answers: list[int] = Field(min_length=1, max_length=200)
    question_ids: Optional[list[str]] = Field(default=None, min_length=1, max_length=200)
    elapsed_seconds: Optional[int] = Field(default=None, ge=0, le=10800)
    classroom_assignment_id: Optional[str] = None


class FlashcardReviewIn(BaseModel):
    rating: Literal["again", "hard", "good", "easy"]


class AskIn(BaseModel):
    message: str = Field(min_length=1, max_length=4000)
    context: str = Field(default="", max_length=7000)
    history: list[dict] = Field(default_factory=list, max_length=20)


class LessonIn(BaseModel):
    id: Optional[str] = None
    title: str = Field(min_length=1, max_length=200)
    sanskrit: str = ""
    iast: str = ""
    overview: str = ""
    content: str = ""
    english_meaning: str = ""
    meter: str = ""
    historical_context: str = ""
    modern_parallel: str = ""
    source: str = ""
    takeaways: list[str] = Field(default_factory=list)
    concepts: list[str] = Field(default_factory=list)
    word_by_word: list[str] = Field(default_factory=list)
    practical: str = ""
    minutes: int = Field(default=10, ge=1, le=600)
    difficulty: str = "Beginner"
    checkpoint: dict = Field(default_factory=dict)


class CourseIn(BaseModel):
    title: str = Field(min_length=3, max_length=200)
    texts: list[str] = Field(default_factory=list)
    sanskrit: str = ""
    category: str = "General"
    icon: str = "📘"
    level: str = "Beginner"
    duration: str = "2 weeks"
    description: str = ""
    era: str = "Classical / historical study"
    highlights: list[str] = Field(default_factory=list)
    lessons: list[LessonIn] = Field(default_factory=list, max_length=40)


class QuizQuestionIn(BaseModel):
    question: str = Field(min_length=1, max_length=500)
    options: list[str] = Field(min_length=2, max_length=6)
    answer_index: int = 0
    explanation: str = ""

    @field_validator("options")
    @classmethod
    def valid_options(cls, value):
        cleaned = [str(x).strip() for x in value]
        if any(not x for x in cleaned):
            raise ValueError("Quiz options cannot be empty")
        return cleaned


class QuizSettingsIn(BaseModel):
    time_limit_minutes: int = Field(default=0, ge=0, le=600)
    marks_per_question: int = Field(default=1, ge=1, le=100)
    passing_marks: int = Field(default=0, ge=0, le=20000)
    negative_marking: int = Field(default=0, ge=0, le=100)
    max_attempts: int = Field(default=0, ge=0, le=100)
    randomize_questions: bool = True
    randomize_options: bool = True
    show_results_immediately: bool = True
    show_correct_answers: bool = True
    show_explanations: bool = True
    allow_retake: bool = True
    start_at: Optional[datetime] = None
    end_at: Optional[datetime] = None

    @model_validator(mode="after")
    def validate_settings(self):
        if self.negative_marking > self.marks_per_question:
            raise ValueError("Negative marking cannot exceed marks per question")
        if self.start_at and self.end_at:
            start = self.start_at.replace(tzinfo=timezone.utc) if self.start_at.tzinfo is None else self.start_at
            end = self.end_at.replace(tzinfo=timezone.utc) if self.end_at.tzinfo is None else self.end_at
            if start >= end:
                raise ValueError("Quiz end time must be after its start time")
        return self


class QuizIn(BaseModel):
    course_id: str = Field(min_length=1)
    lesson_id: Optional[str] = None
    title: str = Field(min_length=3, max_length=200)
    description: str = ""
    questions: list[QuizQuestionIn] = Field(min_length=1, max_length=200)
    settings: QuizSettingsIn = Field(default_factory=QuizSettingsIn)
    published: bool = True

    @model_validator(mode="after")
    def validate_passing_marks(self):
        if self.settings.passing_marks > len(self.questions) * self.settings.marks_per_question:
            raise ValueError("Passing marks cannot exceed the quiz total")
        return self


class PathIn(BaseModel):
    title: str = Field(min_length=3, max_length=200)
    level: str = "Beginner"
    weeks: int = Field(default=4, ge=1, le=52)
    course_ids: list[str] = Field(min_length=1, max_length=20)


class ClassroomIn(BaseModel):
    name: str = Field(min_length=2, max_length=100)


class ClassroomJoinIn(BaseModel):
    join_code: str = Field(min_length=6, max_length=20)


class ClassroomAssignmentIn(BaseModel):
    course_id: Optional[str] = None
    quiz_id: Optional[str] = None

    @model_validator(mode="after")
    def require_one_assignment_target(self):
        if (self.course_id is None) == (self.quiz_id is None):
            raise ValueError("Choose exactly one course or quiz to assign")
        return self


class ClassroomEventIn(BaseModel):
    title: str = Field(min_length=2, max_length=160)
    description: str = Field(default="", max_length=2000)
    starts_at: datetime
    ends_at: Optional[datetime] = None
    meeting_url: str = Field(default="", max_length=2000)

    @field_validator("meeting_url")
    @classmethod
    def valid_meeting_url(cls, value: str):
        value = value.strip()
        if value and not value.lower().startswith(("https://", "http://")):
            raise ValueError("Meeting link must begin with http:// or https://")
        return value

    @model_validator(mode="after")
    def valid_event_time(self):
        start = self.starts_at.replace(tzinfo=timezone.utc) if self.starts_at.tzinfo is None else self.starts_at
        end = self.ends_at.replace(tzinfo=timezone.utc) if self.ends_at and self.ends_at.tzinfo is None else self.ends_at
        if end and start >= end:
            raise ValueError("Class end time must be after its start time")
        return self


class AttendanceMarkIn(BaseModel):
    user_id: str = Field(min_length=1)
    status: Literal["present", "absent", "late", "excused"]


class AttendanceBatchIn(BaseModel):
    entries: list[AttendanceMarkIn] = Field(min_length=1, max_length=500)

    @model_validator(mode="after")
    def unique_students(self):
        if len({item.user_id for item in self.entries}) != len(self.entries):
            raise ValueError("Attendance contains duplicate students")
        return self


class AnnouncementIn(BaseModel):
    title: str = Field(min_length=2, max_length=160)
    body: str = Field(min_length=1, max_length=8000)
    classroom_id: Optional[str] = None


class CourseResourceIn(BaseModel):
    title: str = Field(min_length=2, max_length=160)
    url: str = Field(min_length=8, max_length=2000)
    description: str = Field(default="", max_length=1000)
    kind: Literal["link", "video", "document"] = "link"

    @field_validator("url")
    @classmethod
    def valid_resource_url(cls, value: str):
        value = value.strip()
        if not value.lower().startswith(("https://", "http://")):
            raise ValueError("Resource URL must begin with http:// or https://")
        return value


class ClassroomDiscussionIn(BaseModel):
    body: str = Field(min_length=1, max_length=4000)
    parent_id: Optional[str] = None


class ClassroomTaskIn(BaseModel):
    title: str = Field(min_length=2, max_length=160)
    instructions: str = Field(min_length=1, max_length=8000)
    due_at: Optional[datetime] = None
    points: int = Field(default=100, ge=1, le=10000)


class ClassroomSubmissionIn(BaseModel):
    body: str = Field(default="", max_length=20000)


class ClassroomGradeIn(BaseModel):
    grade: int = Field(ge=0, le=10000)
    feedback: str = Field(default="", max_length=4000)


class InstitutionCreateIn(BaseModel):
    name: str = Field(min_length=2, max_length=200)
    short_name: str = Field(min_length=2, max_length=24, pattern=r"^[A-Za-z0-9-]+$")
    admin_name: str = Field(min_length=2, max_length=100)
    admin_email: str = Field(min_length=5, max_length=200)
    plan_id: str = "starter"
    trial_days: int = Field(default=30, ge=0, le=365)

    @field_validator("admin_email")
    @classmethod
    def normalized_admin_email(cls, value: str):
        value = value.strip().lower()
        if "@" not in value:
            raise ValueError("Enter a valid institution admin email")
        return value


class InstitutionBrandingIn(BaseModel):
    name: str = Field(min_length=2, max_length=200)
    logo: str = Field(default="", max_length=2000)
    favicon: str = Field(default="", max_length=2000)
    primary_color: str = Field(default="#c75d0c", pattern=r"^#[0-9A-Fa-f]{6}$")
    secondary_color: str = Field(default="#1a237e", pattern=r"^#[0-9A-Fa-f]{6}$")
    welcome_message: str = Field(default="", max_length=500)
    contact_email: str = Field(default="", max_length=200)
    phone: str = Field(default="", max_length=40)
    address: str = Field(default="", max_length=500)
    website: str = Field(default="", max_length=500)


class InstitutionMemberInviteIn(BaseModel):
    name: str = Field(min_length=2, max_length=100)
    email: str = Field(min_length=5, max_length=200)
    role: Literal["teacher", "student"]
    department_id: Optional[str] = None
    program_id: Optional[str] = None
    academic_year: Optional[str] = Field(default=None, max_length=40)
    semester: Optional[str] = Field(default=None, max_length=40)
    division: Optional[str] = Field(default=None, max_length=40)
    batch: Optional[str] = Field(default=None, max_length=80)
    student_number: Optional[str] = Field(default=None, max_length=80)

    @field_validator("email")
    @classmethod
    def normalized_member_email(cls, value: str):
        value = value.strip().lower()
        if "@" not in value:
            raise ValueError("Enter a valid member email")
        return value


class InstitutionBulkInviteIn(BaseModel):
    members: list[InstitutionMemberInviteIn] = Field(min_length=1, max_length=500)


class InstitutionStatusIn(BaseModel):
    status: Literal["trial", "active", "suspended", "cancelled"]


class DepartmentIn(BaseModel):
    name: str = Field(min_length=2, max_length=120)


class ProgramIn(BaseModel):
    name: str = Field(min_length=2, max_length=120)
    department_id: str = Field(min_length=1)


def decode_jwt_payload(token: str) -> dict:
    try:
        parts = token.split(".")
        if len(parts) != 3:
            raise ValueError
        padded = parts[1] + "=" * (-len(parts[1]) % 4)
        return json.loads(base64.urlsafe_b64decode(padded.encode("ascii")).decode("utf-8"))
    except Exception as exc:
        raise HTTPException(401, "Invalid Firebase token") from exc


def verify_firebase_token(token: str) -> dict:
    if not FIREBASE_PROJECT_ID:
        if SUPABASE_DB_URL:
            raise HTTPException(503, "Set FIREBASE_PROJECT_ID before using the hosted database")
        payload = decode_jwt_payload(token)
        uid_claim = payload.get("user_id") or payload.get("sub")
        if not uid_claim:
            raise HTTPException(401, "Firebase token does not contain a user id")
        # Local configuration intentionally supports development without server credentials.
        payload["uid"] = uid_claim
        payload["_verified"] = False
        return payload
    try:
        from google.auth.transport import requests as google_requests
        from google.oauth2 import id_token as google_id_token
        payload = google_id_token.verify_firebase_token(token, google_requests.Request(), audience=FIREBASE_PROJECT_ID)
        payload["uid"] = payload.get("uid") or payload.get("user_id") or payload.get("sub")
        payload["_verified"] = True
        return payload
    except Exception as exc:
        raise HTTPException(401, f"Firebase token verification failed: {exc}") from exc


def current_user(user_id: Optional[str] = None) -> dict:
    user = row("SELECT * FROM users WHERE id=?", (user_id or "demo-student",))
    if not user:
        raise HTTPException(404, "User not found")
    membership = row(
        """SELECT m.institution_id,m.status membership_status,i.status institution_status
        FROM institution_memberships m JOIN institutions i ON i.id=m.institution_id
        WHERE m.user_id=?""",
        (user["id"],),
    )
    user["institution_id"] = membership["institution_id"] if membership else None
    user["membership_status"] = membership["membership_status"] if membership else None
    user["institution_status"] = membership["institution_status"] if membership else None
    return user


def require_super_admin(user_id: str) -> dict:
    user = current_user(user_id)
    if user["role"] != "super_admin":
        raise HTTPException(403, "Super Admin role required")
    return user


def require_institution_admin(user_id: str, institution_id: Optional[str] = None) -> dict:
    user = current_user(user_id)
    if user["role"] not in {"institution_admin", "super_admin"}:
        raise HTTPException(403, "Institution Admin role required")
    if user["role"] == "super_admin":
        if institution_id:
            institution = row("SELECT id FROM institutions WHERE id=?", (institution_id,))
            if not institution:
                raise HTTPException(404, "Institution not found")
        return user
    if not user["institution_id"] or (institution_id and user["institution_id"] != institution_id):
        raise HTTPException(404, "Institution not found")
    if user["membership_status"] != "active" or not institution_is_active(user["institution_id"]):
        raise HTTPException(403, "Institution membership is inactive")
    return user


def institution_is_active(institution_id: Optional[str]) -> bool:
    if not institution_id:
        return True
    institution = row("SELECT status,trial_ends_at FROM institutions WHERE id=?", (institution_id,))
    if not institution:
        return False
    if institution["status"] == "active":
        return True
    if institution["status"] != "trial" or not institution.get("trial_ends_at"):
        return False
    try:
        trial_ends_at = datetime.fromisoformat(institution["trial_ends_at"])
    except ValueError:
        return False
    if trial_ends_at.tzinfo is None:
        trial_ends_at = trial_ends_at.replace(tzinfo=timezone.utc)
    return datetime.now(timezone.utc) <= trial_ends_at


def tenant_resource(institution_id: str, resource_type: str, resource_id: str) -> None:
    conn = db()
    try:
        conn.execute(
            "INSERT INTO tenant_resources(resource_type,resource_id,institution_id) VALUES (?,?,?) "
            "ON CONFLICT(resource_type,resource_id) DO NOTHING",
            (resource_type, resource_id, institution_id),
        )
        existing = conn.execute(
            "SELECT institution_id FROM tenant_resources WHERE resource_type=? AND resource_id=?",
            (resource_type, resource_id),
        ).fetchone()
        if existing and existing["institution_id"] != institution_id:
            raise HTTPException(409, "Content already belongs to another institution")
        conn.commit()
    finally:
        conn.close()


def course_accessible(course_data: Optional[dict], user: dict) -> bool:
    if not course_data:
        return False
    if course_data.get("created_by") == "seed":
        return True
    owner = current_user(course_data["created_by"]) if course_data.get("created_by") else None
    owner_institution = owner["institution_id"] if owner else None
    resource = row(
        "SELECT institution_id FROM tenant_resources WHERE resource_type='course' AND resource_id=?",
        (course_data["id"],),
    )
    course_institution = resource["institution_id"] if resource else owner_institution
    if not user.get("institution_id"):
        return (
            (not course_institution and not SUPABASE_DB_URL)
            or course_data.get("created_by") == user["id"]
        )
    return bool(
        institution_is_active(user["institution_id"])
        and (
            course_institution == user["institution_id"]
            or course_data.get("created_by") == user["id"]
        )
    )


def require_course_access(course_id: str, user: dict) -> dict:
    course_data = row("SELECT * FROM courses WHERE id=?", (course_id,))
    if not course_accessible(course_data, user):
        raise HTTPException(404, "Course not found")
    return course_data


def accessible_course_ids(user: dict) -> set[str]:
    return {
        course_data["id"]
        for course_data in rows("SELECT * FROM courses")
        if course_accessible(course_data, user)
    }


def accessible_quiz_ids(user: dict) -> set[str]:
    return {
        quiz_data["id"]
        for quiz_data in rows("SELECT * FROM quizzes")
        if quiz_accessible(quiz_data, user)
    }


def quiz_accessible(quiz_data: Optional[dict], user: dict) -> bool:
    if not quiz_data or hidden_quiz_title(quiz_data.get("title", "")):
        return False
    if quiz_data.get("created_by") == "seed":
        return True
    resource = row(
        "SELECT institution_id FROM tenant_resources WHERE resource_type='quiz' AND resource_id=?",
        (quiz_data["id"],),
    )
    if resource:
        return bool(
            user.get("institution_id") == resource["institution_id"]
            and institution_is_active(resource["institution_id"])
        )
    linked_course = row("SELECT * FROM courses WHERE id=?", (quiz_data.get("course_id"),))
    if linked_course and course_accessible(linked_course, user):
        if linked_course.get("created_by") == "seed":
            return True
        return quiz_data.get("created_by") == user["id"] or not user.get("institution_id")
    return quiz_data.get("created_by") == user["id"]


def hidden_quiz_title(title: str) -> bool:
    normalized = re.sub(r"[^a-z0-9]+", "", str(title).casefold())
    return normalized in {
        "iksteacherquiz",
        "configurableiksquiz",
        "delayedresultsquiz",
    }


def classroom_accessible(classroom: Optional[dict], user: dict) -> bool:
    if not classroom:
        return False
    if classroom.get("teacher_id") == user["id"]:
        return True
    resource = row(
        "SELECT institution_id FROM tenant_resources WHERE resource_type='classroom' AND resource_id=?",
        (classroom["id"],),
    )
    teacher = current_user(classroom["teacher_id"])
    classroom_institution = resource["institution_id"] if resource else teacher.get("institution_id")
    if user["role"] == "institution_admin":
        return bool(
            user.get("institution_id") and classroom_institution == user["institution_id"]
            and institution_is_active(user["institution_id"])
        )
    if user.get("institution_id") and classroom_institution != user["institution_id"]:
        return False
    return bool(row(
        "SELECT 1 FROM classroom_members WHERE classroom_id=? AND user_id=?",
        (classroom["id"], user["id"]),
    ))


def require_teacher(user_id: str) -> dict:
    user = current_user(user_id)
    if user["role"] != "teacher":
        raise HTTPException(403, "Teacher role required")
    if user["institution_id"] and (
        user["membership_status"] != "active" or not institution_is_active(user["institution_id"])
    ):
        raise HTTPException(403, "Institution membership is inactive")
    return user


def auth_row_for_user(user_id: str, authorization: Optional[str]) -> None:
    # Demo mode is unauthenticated only with local SQLite and no Firebase project.
    if not authorization:
        if FIREBASE_PROJECT_ID or SUPABASE_DB_URL:
            raise HTTPException(401, "Firebase authentication is required")
        user = current_user(user_id)
        if user.get("institution_id") and (
            user["membership_status"] != "active" or not institution_is_active(user["institution_id"])
        ):
            raise HTTPException(403, "Institution membership is inactive")
        return
    if not authorization.lower().startswith("bearer "):
        raise HTTPException(401, "Use a Bearer Firebase ID token")
    token = authorization.split(" ", 1)[1].strip()
    payload = verify_firebase_token(token)
    firebase_uid = payload.get("uid") or payload.get("sub")
    if firebase_uid:
        existing = row("SELECT id FROM users WHERE firebase_uid=?", (firebase_uid,))
        if not existing:
            raise HTTPException(403, "Authenticated Firebase account is not registered with this API")
        if existing["id"] != user_id:
            raise HTTPException(403, "Authenticated user does not match requested account")
    user = current_user(user_id)
    if user.get("institution_id") and (
        user["membership_status"] != "active" or not institution_is_active(user["institution_id"])
    ):
        raise HTTPException(403, "Institution membership is inactive")


def mark_study_day(user_id: str) -> None:
    conn = db(); conn.execute("INSERT OR IGNORE INTO study_days VALUES (?,?,?)", (user_id, today_key(), now_iso())); conn.commit(); conn.close()


def streak_payload(user_id: str) -> dict:
    days = {r["day_key"] for r in rows("SELECT day_key FROM study_days WHERE user_id=? ORDER BY day_key DESC LIMIT 90", (user_id,))}
    current = 0; cursor = date.today()
    while cursor.isoformat() in days:
        current += 1; cursor -= timedelta(days=1)
    last7 = [(date.today() - timedelta(days=i)).isoformat() in days for i in range(6, -1, -1)]
    return {"current": current, "last7": last7, "studied_days": len(days)}


def learner_xp(user_id: str) -> int:
    lesson_xp = int(row("SELECT COUNT(*) c FROM progress WHERE user_id=? AND completed=1", (user_id,))["c"]) * 20
    quiz_xp = int(row("SELECT COALESCE(SUM(score),0) s FROM attempts WHERE user_id=?", (user_id,))["s"])
    return lesson_xp + quiz_xp


def rank_for_xp(xp: int) -> dict:
    if xp >= 500: return {"key": "acharya", "name": "Acharya", "subtitle": "Master", "min": 500}
    if xp >= 250: return {"key": "upadhyaya", "name": "Upadhyaya", "subtitle": "Scholar", "min": 250}
    if xp >= 100: return {"key": "vidyarthi", "name": "Vidyarthi", "subtitle": "Student", "min": 100}
    return {"key": "jijnasu", "name": "Jijnasu", "subtitle": "Seeker", "min": 0}


def award_badge(user_id: str, badge_id: str) -> None:
    if not row("SELECT id FROM badges WHERE id=?", (badge_id,)): return
    conn = db(); conn.execute("INSERT OR IGNORE INTO user_badges VALUES (?,?,?)", (user_id, badge_id, now_iso())); conn.commit(); conn.close()


def course_completion(user_id: str, course_id: str) -> tuple[int, int]:
    total = int(row("SELECT COUNT(*) c FROM lessons WHERE course_id=?", (course_id,))["c"])
    completed = int(row("SELECT COUNT(*) c FROM progress p JOIN lessons l ON l.id=p.lesson_id WHERE p.user_id=? AND p.completed=1 AND l.course_id=?", (user_id, course_id))["c"])
    return total, completed


def maybe_issue_certificate(user_id: str, course_id: str) -> None:
    total, completed = course_completion(user_id, course_id)
    if total and total == completed and not row("SELECT 1 FROM certificates WHERE user_id=? AND course_id=?", (user_id, course_id)):
        conn = db(); conn.execute("INSERT INTO certificates VALUES (?,?,?,?,?)", (uid("cert"), user_id, course_id, f"IKS-{datetime.now().year}-{secrets.token_hex(3).upper()}", now_iso())); conn.commit(); conn.close()


def refresh_achievement_badges(user_id: str) -> None:
    completed = int(row("SELECT COUNT(*) c FROM progress WHERE user_id=? AND completed=1", (user_id,))["c"])
    if completed >= 1: award_badge(user_id, "badge_first_step")
    if completed >= 5: award_badge(user_id, "badge_scholar")
    if streak_payload(user_id)["current"] >= 7: award_badge(user_id, "badge_consistent")
    attempts = [float(x["percent"]) for x in rows("SELECT percent FROM attempts WHERE user_id=?", (user_id,))]
    if sum(1 for x in attempts if x >= 90) >= 3: award_badge(user_id, "badge_quiz_champion")
    mapping = {"badge_math_pioneer": "course_ganita", "badge_stars": "course_khagola", "badge_ayurveda": "course_ayurveda", "badge_metallurgy": "course_rasayana"}
    for badge_id, course_id in mapping.items():
        total, done = course_completion(user_id, course_id)
        if total and total == done: award_badge(user_id, badge_id)


def hydrate_course(c: dict, user_id: str) -> dict:
    hydrate_json(c, ["highlights_json", "texts_json"])
    c["lessons"] = []
    for lesson in rows("SELECT * FROM lessons WHERE course_id=? ORDER BY position", (c["id"],)):
        hydrate_json(lesson, ["takeaways_json", "concepts_json", "checkpoint_json", "word_by_word_json"])
        lesson["study_outline"] = lesson_study_outline(lesson)
        progress_row = row("SELECT completed FROM progress WHERE user_id=? AND lesson_id=?", (user_id, lesson["id"]))
        lesson["completed"] = bool(progress_row["completed"]) if progress_row else False
        lesson["bookmarked"] = bool(row("SELECT 1 FROM bookmarks WHERE user_id=? AND lesson_id=?", (user_id, lesson["id"])) )
        c["lessons"].append(lesson)
    scholars = []
    for sid in COURSE_SCHOLARS.get(c["id"], []):
        s = row("SELECT id,name,sanskrit,era,domain,work FROM scholars WHERE id=?", (sid,))
        if s: scholars.append(s)
    c["scholars"] = scholars
    total = len(c["lessons"]); done = sum(1 for l in c["lessons"] if l["completed"])
    c["progress_pct"] = round(done / total * 100, 1) if total else 0
    return c


@app.get("/api/health")
def health():
    return {"status": "ok", "service": "VidyaDhara API", "version": APP_VERSION, "time": now_iso(), "groq_configured": bool(GROQ_API_KEY), "firebase_configured": bool(FIREBASE_PROJECT_ID), "supabase_configured": bool(SUPABASE_DB_URL)}


@app.get("/api/config")
def config():
    return {
        "version": APP_VERSION, "groq_configured": bool(GROQ_API_KEY), "groq_model": GROQ_MODEL,
        "firebase_server_verification": bool(FIREBASE_PROJECT_ID), "teacher_email_count": len(TEACHER_EMAILS),
        "super_admin_configured": bool(SUPER_ADMIN_EMAILS), "supabase_configured": bool(SUPABASE_DB_URL),
        "database": "supabase" if SUPABASE_DB_URL else "sqlite",
    }


def accept_institution_invite(user_id: str, email: str) -> None:
    invite = row(
        """SELECT * FROM institution_invites WHERE lower(email)=? AND status='pending'
        ORDER BY created_at LIMIT 1""",
        (email.lower(),),
    )
    if not invite:
        return
    if not institution_is_active(invite["institution_id"]):
        raise HTTPException(403, "This institution is not accepting members")
    membership = row("SELECT institution_id FROM institution_memberships WHERE user_id=?", (user_id,))
    if membership and membership["institution_id"] != invite["institution_id"]:
        raise HTTPException(409, "This account already belongs to another institution")
    conn = db()
    try:
        conn.execute("BEGIN")
        conn.execute("UPDATE users SET name=?,role=? WHERE id=?", (invite["name"], invite["role"], user_id))
        conn.execute(
            """INSERT INTO institution_memberships(
            user_id,institution_id,status,department_id,program_id,academic_year,semester,
            division,batch,student_number,created_at
            ) VALUES (?,?, 'active', ?,?,?,?,?,?,?,?)
            ON CONFLICT(user_id) DO UPDATE SET institution_id=excluded.institution_id,
            status='active',department_id=excluded.department_id,program_id=excluded.program_id,
            academic_year=excluded.academic_year,semester=excluded.semester,division=excluded.division,
            batch=excluded.batch,student_number=excluded.student_number""",
            (
                user_id, invite["institution_id"], invite["department_id"], invite["program_id"],
                invite["academic_year"], invite["semester"], invite["division"], invite["batch"],
                invite["student_number"], invite["created_at"],
            ),
        )
        conn.execute(
            "UPDATE institution_invites SET status='accepted',accepted_at=? WHERE id=?",
            (now_iso(), invite["id"]),
        )
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def institution_usage(institution_id: str) -> dict:
    members = rows(
        """SELECT u.role,COUNT(*) count FROM institution_memberships m
        JOIN users u ON u.id=m.user_id WHERE m.institution_id=? AND m.status='active' GROUP BY u.role""",
        (institution_id,),
    )
    counts = {item["role"]: int(item["count"]) for item in members}
    courses_count = int(row(
        "SELECT COUNT(*) count FROM tenant_resources WHERE resource_type='course' AND institution_id=?",
        (institution_id,),
    )["count"])
    return {
        "students": counts.get("student", 0) + counts.get("learner", 0),
        "teachers": counts.get("teacher", 0),
        "courses": courses_count,
    }


def institution_summary(institution: dict) -> dict:
    usage = institution_usage(institution["id"])
    plan = row("SELECT * FROM plans WHERE id=?", (institution["plan_id"],))
    return {
        **institution,
        "usage": usage,
        "plan": plan,
        "limits": {
            "students": int(plan["student_limit"]) if plan else 0,
            "teachers": int(plan["teacher_limit"]) if plan else 0,
            "courses": int(plan["course_limit"]) if plan else 0,
        },
    }


@app.get("/api/platform/dashboard")
def platform_dashboard(user_id: str = "demo-super-admin", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); require_super_admin(user_id)
    return {
        "institutions": int(row("SELECT COUNT(*) count FROM institutions")["count"]),
        "active_institutions": int(row("SELECT COUNT(*) count FROM institutions WHERE status='active'")["count"]),
        "trial_institutions": int(row("SELECT COUNT(*) count FROM institutions WHERE status='trial'")["count"]),
        "suspended_institutions": int(row("SELECT COUNT(*) count FROM institutions WHERE status='suspended'")["count"]),
        "students": int(row("SELECT COUNT(*) count FROM institution_memberships m JOIN users u ON u.id=m.user_id WHERE m.status='active' AND u.role IN ('student','learner')")["count"]),
        "teachers": int(row("SELECT COUNT(*) count FROM institution_memberships m JOIN users u ON u.id=m.user_id WHERE m.status='active' AND u.role='teacher'")["count"]),
        "courses": int(row("SELECT COUNT(*) count FROM tenant_resources WHERE resource_type='course'")["count"]),
    }


@app.get("/api/plans")
def plans(user_id: str = "demo-super-admin", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); require_super_admin(user_id)
    return rows("SELECT id,name,student_limit,teacher_limit,course_limit,active FROM plans ORDER BY student_limit")


@app.get("/api/institutions")
def institutions(user_id: str = "demo-super-admin", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); require_super_admin(user_id)
    return [institution_summary(item) for item in rows("SELECT * FROM institutions ORDER BY created_at DESC")]


@app.post("/api/institutions")
def create_institution(
    data: InstitutionCreateIn,
    user_id: str = "demo-super-admin",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); require_super_admin(user_id)
    plan = row("SELECT id FROM plans WHERE id=? AND active=1", (data.plan_id,))
    if not plan:
        raise HTTPException(404, "Plan not found")
    if row("SELECT id FROM institutions WHERE lower(short_name)=?", (data.short_name.lower(),)):
        raise HTTPException(409, "Institution short name is already in use")
    institution_id = uid("institution")
    invite_id = uid("invite")
    now = now_iso()
    trial_ends_at = (datetime.now(timezone.utc) + timedelta(days=data.trial_days)).isoformat()
    conn = db()
    try:
        conn.execute("BEGIN")
        conn.execute(
            """INSERT INTO institutions(id,name,short_name,status,plan_id,subscription_status,
            trial_ends_at,created_at,updated_at) VALUES (?,?,?,'trial',?,'trial',?,?,?)""",
            (institution_id, data.name.strip(), data.short_name.strip(), data.plan_id, trial_ends_at, now, now),
        )
        conn.execute(
            "INSERT INTO subscriptions(id,institution_id,plan_id,status,started_at,trial_ends_at) VALUES (?,?,?,'trial',?,?)",
            (uid("subscription"), institution_id, data.plan_id, now, trial_ends_at),
        )
        conn.execute(
            "INSERT INTO institution_invites(id,institution_id,email,name,role,status,created_at) VALUES (?,?,?,?,'institution_admin','pending',?)",
            (invite_id, institution_id, data.admin_email, data.admin_name.strip(), now),
        )
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
    return {"id": institution_id, "status": "trial", "admin_invite": data.admin_email, "trial_ends_at": trial_ends_at}


@app.get("/api/institutions/me")
def my_institution(user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] == "super_admin":
        return {"institution": None, "user": user}
    if not user.get("institution_id") or user["membership_status"] != "active":
        raise HTTPException(404, "Institution membership not found")
    institution = row("SELECT * FROM institutions WHERE id=?", (user["institution_id"],))
    if not institution:
        raise HTTPException(404, "Institution not found")
    if institution["status"] == "suspended":
        raise HTTPException(403, "This institution is suspended")
    return {"institution": institution_summary(institution), "user": user}


@app.put("/api/institutions/me")
def update_my_institution(
    data: InstitutionBrandingIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = require_institution_admin(user_id)
    if user["role"] == "super_admin":
        raise HTTPException(422, "Use the institution management endpoint to edit a tenant")
    conn = db()
    try:
        conn.execute(
            """UPDATE institutions SET name=?,logo=?,favicon=?,primary_color=?,secondary_color=?,
            welcome_message=?,contact_email=?,phone=?,address=?,website=?,updated_at=? WHERE id=?""",
            (
                data.name.strip(), data.logo, data.favicon, data.primary_color, data.secondary_color,
                data.welcome_message, data.contact_email.strip().lower(), data.phone, data.address,
                data.website, now_iso(), user["institution_id"],
            ),
        )
        conn.commit()
    finally:
        conn.close()
    return my_institution(user_id)


@app.put("/api/institutions/{institution_id}/status")
def update_institution_status(
    institution_id: str,
    data: InstitutionStatusIn,
    user_id: str = "demo-super-admin",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); require_super_admin(user_id)
    conn = db()
    try:
        updated = conn.execute(
            "UPDATE institutions SET status=?,subscription_status=?,updated_at=? WHERE id=?",
            (data.status, data.status, now_iso(), institution_id),
        ).rowcount
        conn.execute("UPDATE subscriptions SET status=? WHERE institution_id=?", (data.status, institution_id))
        conn.commit()
    finally:
        conn.close()
    if not updated:
        raise HTTPException(404, "Institution not found")
    return {"id": institution_id, "status": data.status}


@app.put("/api/institutions/{institution_id}/plan")
def update_institution_plan(
    institution_id: str,
    plan_id: str,
    user_id: str = "demo-super-admin",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); require_super_admin(user_id)
    plan = row("SELECT id FROM plans WHERE id=? AND active=1", (plan_id,))
    if not plan:
        raise HTTPException(404, "Plan not found")
    institution = row("SELECT id FROM institutions WHERE id=?", (institution_id,))
    if not institution:
        raise HTTPException(404, "Institution not found")
    plan = row("SELECT * FROM plans WHERE id=?", (plan_id,))
    usage = institution_usage(institution_id)
    pending = rows(
        """SELECT role,COUNT(*) count FROM institution_invites
        WHERE institution_id=? AND status='pending' GROUP BY role""",
        (institution_id,),
    )
    pending_counts = {item["role"]: int(item["count"]) for item in pending}
    if usage["students"] + pending_counts.get("student", 0) > int(plan["student_limit"]):
        raise HTTPException(409, "The selected plan does not support the institution's student usage")
    if usage["teachers"] + pending_counts.get("teacher", 0) > int(plan["teacher_limit"]):
        raise HTTPException(409, "The selected plan does not support the institution's teacher usage")
    if usage["courses"] > int(plan["course_limit"]):
        raise HTTPException(409, "The selected plan does not support the institution's course usage")
    conn = db()
    try:
        updated = conn.execute(
            "UPDATE institutions SET plan_id=?,updated_at=? WHERE id=?", (plan_id, now_iso(), institution_id)
        ).rowcount
        if updated:
            conn.execute("UPDATE subscriptions SET plan_id=? WHERE institution_id=?", (plan_id, institution_id))
        conn.commit()
    finally:
        conn.close()
    if not updated:
        raise HTTPException(404, "Institution not found")
    return institution_summary(row("SELECT * FROM institutions WHERE id=?", (institution_id,)))


@app.get("/api/institutions/{institution_id}/members")
def institution_members(
    institution_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); require_institution_admin(user_id, institution_id)
    return rows(
        """SELECT u.id,u.name,u.email,u.role,m.status,m.department_id,m.program_id,m.academic_year,
        m.semester,m.division,m.batch,m.student_number,m.created_at
        FROM institution_memberships m JOIN users u ON u.id=m.user_id
        WHERE m.institution_id=? ORDER BY u.role,u.name""",
        (institution_id,),
    )


@app.post("/api/institutions/{institution_id}/members")
def invite_institution_member(
    institution_id: str,
    data: InstitutionMemberInviteIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    admin = require_institution_admin(user_id, institution_id)
    if not institution_is_active(institution_id):
        raise HTTPException(403, "Institution is not accepting members")
    existing = row(
        """SELECT i.id FROM institution_invites i WHERE i.institution_id=? AND lower(i.email)=?
        AND i.status='pending'""",
        (institution_id, data.email),
    )
    if existing:
        raise HTTPException(409, "A pending invitation already exists for this email")
    linked_user = row(
        "SELECT institution_id FROM institution_memberships WHERE user_id=(SELECT id FROM users WHERE lower(email)=?)",
        (data.email,),
    )
    if linked_user:
        raise HTTPException(409, "This account already belongs to an institution")
    institution = row("SELECT plan_id FROM institutions WHERE id=?", (institution_id,))
    plan = row("SELECT * FROM plans WHERE id=?", (institution["plan_id"],))
    usage = institution_usage(institution_id)
    invited_counts = rows(
        "SELECT role,COUNT(*) count FROM institution_invites WHERE institution_id=? AND status='pending' GROUP BY role",
        (institution_id,),
    )
    pending = {item["role"]: int(item["count"]) for item in invited_counts}
    role_count = usage["teachers"] if data.role == "teacher" else usage["students"]
    role_count += pending.get(data.role, 0)
    limit_key = "teacher_limit" if data.role == "teacher" else "student_limit"
    if plan and role_count >= int(plan[limit_key]):
        raise HTTPException(409, f"The institution has reached its {data.role} plan limit")
    if data.department_id and not row(
        "SELECT id FROM departments WHERE id=? AND institution_id=?", (data.department_id, institution_id)
    ):
        raise HTTPException(404, "Department not found")
    if data.program_id and not row(
        "SELECT id FROM programs WHERE id=? AND institution_id=?", (data.program_id, institution_id)
    ):
        raise HTTPException(404, "Program not found")
    now = now_iso()
    conn = db()
    try:
        conn.execute(
            """INSERT INTO institution_invites(
            id,institution_id,email,name,role,status,department_id,program_id,academic_year,semester,
            division,batch,student_number,created_at
            ) VALUES (?,?,?,?,?,'pending',?,?,?,?,?,?,?,?)""",
            (
                uid("invite"), institution_id, data.email, data.name.strip(), data.role, data.department_id,
                data.program_id, data.academic_year, data.semester, data.division, data.batch,
                data.student_number, now,
            ),
        )
        conn.commit()
    finally:
        conn.close()
    return {"email": data.email, "role": data.role, "status": "invited"}


@app.post("/api/institutions/{institution_id}/members/bulk")
def bulk_invite_institution_members(
    institution_id: str,
    data: InstitutionBulkInviteIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    results = []
    for index, member in enumerate(data.members, start=1):
        try:
            result = invite_institution_member(institution_id, member, user_id, authorization)
            results.append({"row": index, **result})
        except HTTPException as error:
            results.append({
                "row": index,
                "email": member.email,
                "role": member.role,
                "status": "failed",
                "error": error.detail,
            })
    return {
        "created": sum(item["status"] == "invited" for item in results),
        "failed": sum(item["status"] == "failed" for item in results),
        "results": results,
    }


@app.get("/api/institutions/{institution_id}/usage")
def institution_usage_report(
    institution_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    institution = row("SELECT * FROM institutions WHERE id=?", (institution_id,))
    if not institution:
        raise HTTPException(404, "Institution not found")
    return institution_summary(institution)


@app.get("/api/institutions/{institution_id}/departments")
def list_departments(
    institution_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    return rows("SELECT id,name,created_at FROM departments WHERE institution_id=? ORDER BY name", (institution_id,))


@app.post("/api/institutions/{institution_id}/departments")
def create_department(
    institution_id: str,
    data: DepartmentIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    department_id = uid("department")
    conn = db()
    try:
        conn.execute(
            "INSERT INTO departments(id,institution_id,name,created_at) VALUES (?,?,?,?)",
            (department_id, institution_id, data.name.strip(), now_iso()),
        )
        conn.commit()
    except Exception as error:
        if "UNIQUE" in str(error).upper() or "unique" in str(error).lower():
            raise HTTPException(409, "Department already exists") from error
        raise
    finally:
        conn.close()
    return {"id": department_id, "name": data.name.strip()}


@app.get("/api/institutions/{institution_id}/programs")
def list_programs(
    institution_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    return rows(
        """SELECT p.id,p.name,p.department_id,d.name department_name,p.created_at
        FROM programs p JOIN departments d ON d.id=p.department_id
        WHERE p.institution_id=? ORDER BY d.name,p.name""",
        (institution_id,),
    )


@app.post("/api/institutions/{institution_id}/programs")
def create_program(
    institution_id: str,
    data: ProgramIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    if not row(
        "SELECT id FROM departments WHERE id=? AND institution_id=?",
        (data.department_id, institution_id),
    ):
        raise HTTPException(404, "Department not found")
    program_id = uid("program")
    conn = db()
    try:
        conn.execute(
            "INSERT INTO programs(id,institution_id,department_id,name,created_at) VALUES (?,?,?,?,?)",
            (program_id, institution_id, data.department_id, data.name.strip(), now_iso()),
        )
        conn.commit()
    except Exception as error:
        if "UNIQUE" in str(error).upper() or "unique" in str(error).lower():
            raise HTTPException(409, "Program already exists") from error
        raise
    finally:
        conn.close()
    return {"id": program_id, "name": data.name.strip()}


@app.get("/api/institutions/{institution_id}/dashboard")
def institution_dashboard(
    institution_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    require_institution_admin(user_id, institution_id)
    institution = row("SELECT * FROM institutions WHERE id=?", (institution_id,))
    if not institution:
        raise HTTPException(404, "Institution not found")
    summary = institution_summary(institution)
    return {
        "institution": summary,
        "departments": int(row("SELECT COUNT(*) count FROM departments WHERE institution_id=?", (institution_id,))["count"]),
        "programs": int(row("SELECT COUNT(*) count FROM programs WHERE institution_id=?", (institution_id,))["count"]),
        "pending_invites": int(row("SELECT COUNT(*) count FROM institution_invites WHERE institution_id=? AND status='pending'", (institution_id,))["count"]),
    }


@app.post("/api/auth/guest")
def guest_login(data: GuestIn):
    now = now_iso(); user_id = uid("guest"); email = f"{user_id}@guest.iksphere.local"
    conn = db(); conn.execute("INSERT INTO users(id,name,email,role,provider,is_guest,created_at) VALUES (?,?,?,?,?,?,?)", (user_id, data.name.strip(), email, "learner", "anonymous", 1, now)); conn.commit(); conn.close(); return current_user(user_id)


@app.post("/api/auth/firebase")
def firebase_login(data: FirebaseAuthIn):
    payload = verify_firebase_token(data.id_token)
    firebase_uid = payload.get("uid") or payload.get("sub")
    if not firebase_uid: raise HTTPException(401, "Firebase user id missing")
    token_email = (payload.get("email") or "").strip().lower()
    if FIREBASE_PROJECT_ID and token_email and not payload.get("email_verified"):
        raise HTTPException(403, "Verify your email before joining an institution")
    email = token_email or (data.email or "").strip().lower()
    name = (data.name or payload.get("name") or (email.split("@")[0] if email else "Learner")).strip()[:100]
    provider = data.provider or "firebase"
    is_guest = 1 if provider == "anonymous" else 0
    existing = row("SELECT * FROM users WHERE firebase_uid=?", (firebase_uid,))
    if existing:
        role = existing["role"]
        if email in SUPER_ADMIN_EMAILS:
            role = "super_admin"
        elif email in TEACHER_EMAILS and not SUPABASE_DB_URL and not existing.get("institution_id"):
            role = "teacher"
        conn = db(); conn.execute("UPDATE users SET name=?,email=?,role=?,provider=?,is_guest=? WHERE id=?", (name, email, role, provider, is_guest, existing["id"])); conn.commit(); conn.close()
        if role != "super_admin" and email:
            accept_institution_invite(existing["id"], email); return current_user(existing["id"])
    role = "super_admin" if email in SUPER_ADMIN_EMAILS else (
        "teacher" if email in TEACHER_EMAILS and not SUPABASE_DB_URL else "learner"
    )
    user = {"id": uid("user"), "name": name, "email": email, "role": role, "firebase_uid": firebase_uid, "provider": provider, "is_guest": is_guest, "created_at": now_iso()}
    conn = db()
    try:
        conn.execute("INSERT INTO users(id,name,email,role,firebase_uid,provider,is_guest,created_at) VALUES (?,?,?,?,?,?,?,?)", tuple(user.values()))
        conn.commit()
    finally:
        conn.close()
    if role != "super_admin" and email:
        accept_institution_invite(user["id"], email)
    return current_user(user["id"])


@app.post("/api/auth/login")
def local_login(data: LoginIn):
    if FIREBASE_PROJECT_ID or SUPABASE_DB_URL:
        raise HTTPException(403, "Local demo login is disabled when hosted authentication or storage is configured")
    email = data.email.strip().lower(); existing = row("SELECT * FROM users WHERE email=?", (email,))
    if existing:
        if data.email.strip().lower():
            accept_institution_invite(existing["id"], data.email.strip().lower())
        return current_user(existing["id"])
    role = data.role if data.role in ("learner", "teacher") else "learner"
    user = {"id": uid("user"), "name": data.name.strip(), "email": email, "role": role, "firebase_uid": None, "provider": "local", "is_guest": 0, "created_at": now_iso()}
    conn = db()
    try:
        conn.execute("INSERT INTO users(id,name,email,role,firebase_uid,provider,is_guest,created_at) VALUES (?,?,?,?,?,?,?,?)", tuple(user.values()))
        conn.commit()
    finally:
        conn.close()
    accept_institution_invite(user["id"], email)
    return current_user(user["id"])


@app.get("/api/users/{user_id}/dashboard")
def dashboard(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] in {"super_admin", "institution_admin"}:
        raise HTTPException(403, "This dashboard is for student accounts")
    course_ids = accessible_course_ids(user)
    quiz_ids = accessible_quiz_ids(user)
    if course_ids:
        course_marks = ",".join("?" for _ in course_ids)
        course_values = tuple(sorted(course_ids))
        total = int(row(f"SELECT COUNT(*) c FROM lessons WHERE course_id IN ({course_marks})", course_values)["c"])
        completed = int(row(
            f"""SELECT COUNT(*) c FROM progress p JOIN lessons l ON l.id=p.lesson_id
            WHERE p.user_id=? AND p.completed=1 AND l.course_id IN ({course_marks})""",
            (user_id, *course_values),
        )["c"])
        bookmarks = int(row(
            f"""SELECT COUNT(*) c FROM bookmarks b JOIN lessons l ON l.id=b.lesson_id
            WHERE b.user_id=? AND l.course_id IN ({course_marks})""",
            (user_id, *course_values),
        )["c"])
        notes = int(row(
            f"""SELECT COUNT(*) c FROM notes n JOIN lessons l ON l.id=n.lesson_id
            WHERE n.user_id=? AND l.course_id IN ({course_marks})""",
            (user_id, *course_values),
        )["c"])
        certs = int(row(
            f"""SELECT COUNT(*) c FROM certificates WHERE user_id=? AND course_id IN ({course_marks})""",
            (user_id, *course_values),
        )["c"])
        recent = rows(
            f"""SELECT l.id lesson_id,l.title,c.title course_title,p.updated_at
            FROM progress p JOIN lessons l ON l.id=p.lesson_id JOIN courses c ON c.id=l.course_id
            WHERE p.user_id=? AND p.completed=1 AND l.course_id IN ({course_marks})
            ORDER BY p.updated_at DESC LIMIT 5""",
            (user_id, *course_values),
        )
    else:
        total = completed = bookmarks = notes = certs = 0
        recent = []
    if quiz_ids:
        quiz_marks = ",".join("?" for _ in quiz_ids)
        quiz_values = tuple(sorted(quiz_ids))
        quiz_stats = row(
            f"""SELECT COALESCE(AVG(percent),0) a,COUNT(*) c FROM attempts
            WHERE user_id=? AND quiz_id IN ({quiz_marks})""",
            (user_id, *quiz_values),
        )
        quiz_attempts = int(quiz_stats["c"])
        avg = float(quiz_stats["a"] or 0)
    else:
        quiz_attempts = 0
        avg = 0.0
    badges = int(row("SELECT COUNT(*) c FROM user_badges WHERE user_id=?", (user_id,))["c"])
    xp = learner_xp(user_id)
    return {"user": user, "stats": {"total_lessons": total, "completed_lessons": completed, "completion_pct": round(completed / total * 100, 1) if total else 0, "avg_quiz": round(avg, 1), "bookmarks": bookmarks, "notes": notes, "certificates": certs, "badges": badges, "quiz_attempts": quiz_attempts, "xp": xp, "rank": rank_for_xp(xp)}, "recent": recent, "streak": streak_payload(user_id)}


@app.get("/api/home/{user_id}")
def home(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    wisdom = rows("SELECT * FROM daily_wisdom ORDER BY id")
    if not wisdom: raise HTTPException(503, "Daily wisdom data is unavailable")
    w = wisdom[date.today().toordinal() % len(wisdom)]; hydrate_json(w, ["word_by_word_json"])
    scholars = rows("SELECT * FROM scholars ORDER BY name"); scholar = scholars[date.today().toordinal() % len(scholars)] if scholars else None
    challenge = next(
        (
            {key: quiz_data.get(key) for key in ("id", "course_id", "title", "description")}
            for quiz_data in rows("SELECT * FROM quizzes WHERE published=1 ORDER BY created_at ASC")
            if quiz_accessible(quiz_data, user)
        ),
        None,
    )
    featured = [
        {key: course_data[key] for key in ("id", "title", "category", "icon")}
        for course_data in rows("SELECT * FROM courses ORDER BY title")
        if course_accessible(course_data, user)
    ][:4]
    return {"wisdom": w, "scholar_of_day": scholar, "daily_quiz": challenge, "featured_courses": featured}


@app.get("/api/search")
def search(q: str = "", user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id); query = q.strip().lower()
    if not query: return {"courses": [], "lessons": [], "scholars": []}
    like = f"%{query}%"
    available_courses = [
        item for item in rows(
            "SELECT * FROM courses WHERE lower(title||' '||category||' '||description||' '||era) LIKE ? ORDER BY title",
            (like,),
        ) if course_accessible(item, user)
    ]
    visible_course_ids = {item["id"] for item in available_courses}
    return {
        "courses": [{key: item[key] for key in ("id", "title", "category", "description", "era", "icon")} for item in available_courses],
        "lessons": [
            item for item in rows(
                """SELECT l.id,l.title,l.course_id,c.title course_title,l.overview FROM lessons l
                JOIN courses c ON c.id=l.course_id
                WHERE lower(l.title||' '||l.overview||' '||l.content||' '||l.concepts_json) LIKE ?
                ORDER BY l.position LIMIT 100""",
                (like,),
            ) if item["course_id"] in visible_course_ids
        ][:40],
        "scholars": rows("SELECT id,name,domain,work,contribution FROM scholars WHERE lower(name||' '||domain||' '||work||' '||contribution) LIKE ? ORDER BY name", (like,)),
    }


@app.get("/api/courses")
def courses(user_id: str = "demo-student", language: str = "en", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    return [
        translated_course(hydrate_course(c, user_id), language)
        for c in rows("SELECT * FROM courses ORDER BY title") if course_accessible(c, user)
    ]


@app.get("/api/courses/{course_id}")
def course(course_id: str, user_id: str = "demo-student", language: str = "en", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    c = require_course_access(course_id, user)
    return translated_course(hydrate_course(c, user_id), language)


@app.get("/api/translations/catalog")
def translation_catalog(language: str, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    visible_courses = {
        item["id"] for item in rows("SELECT * FROM courses") if course_accessible(item, user)
    }
    visible_quizzes = [
        item for item in rows("SELECT * FROM quizzes")
        if quiz_accessible(item, user) and bool(item.get("published", 1))
    ]
    catalog = {
        "courses": [],
        "paths": [],
        "quizzes": [{"id": item["id"], "title": item["title"], "description": item["description"]} for item in visible_quizzes],
    }
    for course_data in rows("SELECT id,title,category,level,duration,description FROM courses ORDER BY title"):
        if course_data["id"] not in visible_courses:
            continue
        course_data["lessons"] = rows(
            "SELECT id,title FROM lessons WHERE course_id=? ORDER BY position",
            (course_data["id"],),
        )
        catalog["courses"].append(course_data)
    for path_data in rows("SELECT id,title,description,level,weeks FROM paths ORDER BY title"):
        path_resource = row(
            "SELECT institution_id FROM tenant_resources WHERE resource_type='path' AND resource_id=?",
            (path_data["id"],),
        )
        if path_data["created_by"] != "seed" and (
            (user.get("institution_id") and (not path_resource or path_resource["institution_id"] != user["institution_id"]))
            or (not user.get("institution_id") and SUPABASE_DB_URL and path_data["created_by"] != user["id"])
        ):
            continue
        path_data["courses"] = rows(
            """SELECT c.id,c.title FROM path_courses pc
            JOIN courses c ON c.id=pc.course_id WHERE pc.path_id=? ORDER BY pc.position""",
            (path_data["id"],),
        )
        catalog["paths"].append(path_data)
    return translate_content("catalog:v1", language, catalog)


@app.post("/api/courses")
def create_course(data: CourseIn, user_id: str = "demo-teacher", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    if user["role"] == "institution_admin":
        require_institution_admin(user_id)
    if user.get("institution_id"):
        institution = row("SELECT plan_id FROM institutions WHERE id=?", (user["institution_id"],))
        usage = institution_usage(user["institution_id"])
        plan = row("SELECT course_limit FROM plans WHERE id=?", (institution["plan_id"],)) if institution else None
        if plan and usage["courses"] >= int(plan["course_limit"]):
            raise HTTPException(409, "The institution has reached its course plan limit")
    now = now_iso(); cid = uid("course"); conn = db()
    try:
        conn.execute("BEGIN")
        conn.execute("INSERT INTO courses VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", (cid, data.title.strip(), data.sanskrit, data.category, data.icon[:8], data.level, data.duration, data.description[:1200], data.era, json.dumps(data.highlights, ensure_ascii=False), json.dumps(data.texts, ensure_ascii=False), user_id, now))
        for i, l in enumerate(data.lessons):
            lid = l.id or uid("lesson")
            conn.execute("INSERT INTO lessons VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", (lid, cid, l.title.strip(), l.sanskrit, l.overview, l.content, json.dumps(l.takeaways, ensure_ascii=False), l.practical, l.minutes, l.difficulty, i, l.iast, l.english_meaning, l.meter, l.historical_context, l.modern_parallel, l.source, json.dumps(l.concepts, ensure_ascii=False), json.dumps(l.checkpoint, ensure_ascii=False), json.dumps(l.word_by_word, ensure_ascii=False)))
        conn.commit()
    except Exception:
        conn.rollback(); raise
    finally:
        conn.close()
    if user.get("institution_id"):
        tenant_resource(user["institution_id"], "course", cid)
    return {"id": cid, "message": "Course created"}


@app.put("/api/courses/{course_id}")
def update_course(course_id: str, data: CourseIn, user_id: str = "demo-teacher", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    existing = row("SELECT * FROM courses WHERE id=?", (course_id,))
    if not existing: raise HTTPException(404, "Course not found")
    resource = row("SELECT institution_id FROM tenant_resources WHERE resource_type='course' AND resource_id=?", (course_id,))
    same_institution = bool(user.get("institution_id") and resource and resource["institution_id"] == user["institution_id"])
    if existing["created_by"] == "seed":
        raise HTTPException(403, "Global IKS courses are read-only")
    if not same_institution and existing["created_by"] != user_id:
        raise HTTPException(404, "Course not found")
    if existing["created_by"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "You can only edit your own courses")
    conn = db()
    try:
        conn.execute("BEGIN")
        conn.execute("UPDATE courses SET title=?,sanskrit=?,category=?,icon=?,level=?,duration=?,description=?,era=?,highlights_json=?,texts_json=? WHERE id=?", (data.title.strip(), data.sanskrit, data.category, data.icon[:8], data.level, data.duration, data.description[:1200], data.era, json.dumps(data.highlights, ensure_ascii=False), json.dumps(data.texts, ensure_ascii=False), course_id))
        keep = set()
        for i, l in enumerate(data.lessons):
            lid = l.id or uid("lesson"); keep.add(lid)
            conn.execute("""INSERT INTO lessons VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET
                course_id=excluded.course_id,title=excluded.title,sanskrit=excluded.sanskrit,overview=excluded.overview,
                content=excluded.content,takeaways_json=excluded.takeaways_json,practical=excluded.practical,
                minutes=excluded.minutes,difficulty=excluded.difficulty,position=excluded.position,iast=excluded.iast,
                english_meaning=excluded.english_meaning,meter=excluded.meter,historical_context=excluded.historical_context,
                modern_parallel=excluded.modern_parallel,source=excluded.source,concepts_json=excluded.concepts_json,
                checkpoint_json=excluded.checkpoint_json,word_by_word_json=excluded.word_by_word_json""",
                (lid, course_id, l.title.strip(), l.sanskrit, l.overview, l.content, json.dumps(l.takeaways, ensure_ascii=False), l.practical, l.minutes, l.difficulty, i, l.iast, l.english_meaning, l.meter, l.historical_context, l.modern_parallel, l.source, json.dumps(l.concepts, ensure_ascii=False), json.dumps(l.checkpoint, ensure_ascii=False), json.dumps(l.word_by_word, ensure_ascii=False)))
        old = rows("SELECT id FROM lessons WHERE course_id=?", (course_id,))
        for l in old:
            if l["id"] not in keep:
                conn.execute("DELETE FROM lessons WHERE id=?", (l["id"],))
        conn.commit()
    except Exception:
        conn.rollback(); raise
    finally: conn.close()
    return {"id": course_id, "message": "Course updated"}


@app.get("/api/paths")
def paths(user_id: str = "demo-student", language: str = "en", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    result = []
    for p in rows("SELECT * FROM paths ORDER BY weeks,title"):
        path_resource = row(
            "SELECT institution_id FROM tenant_resources WHERE resource_type='path' AND resource_id=?",
            (p["id"],),
        )
        if p["created_by"] != "seed" and (
            (user.get("institution_id") and (not path_resource or path_resource["institution_id"] != user["institution_id"]))
            or (not user.get("institution_id") and SUPABASE_DB_URL and p["created_by"] != user_id)
        ):
            continue
        p["courses"] = []
        for pc in rows("SELECT c.id,c.title,c.icon,c.level,c.duration FROM path_courses pc JOIN courses c ON c.id=pc.course_id WHERE pc.path_id=? ORDER BY pc.position", (p["id"],)):
            if not course_accessible(row("SELECT * FROM courses WHERE id=?", (pc["id"],)), user):
                continue
            total, done = course_completion(user_id, pc["id"]); pc["lesson_count"] = total; pc["progress_pct"] = round(done / total * 100, 1) if total else 0; p["courses"].append(pc)
        lesson_total = sum(c["lesson_count"] for c in p["courses"]); done_total = sum(round(c["lesson_count"] * (c["progress_pct"] / 100)) for c in p["courses"])
        p["progress_pct"] = round(done_total / lesson_total * 100, 1) if lesson_total else 0
        result.append(p)
    return translate_content("paths", language, result)


@app.post("/api/paths")
def create_path(data: PathIn, user_id: str = "demo-teacher", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = require_teacher(user_id); now = now_iso(); pid = uid("path"); conn = db()
    try:
        conn.execute("BEGIN")
        for cid in data.course_ids:
            course_data = row("SELECT * FROM courses WHERE id=?", (cid,))
            if not course_accessible(course_data, user):
                raise HTTPException(404, f"Course not found: {cid}")
        conn.execute("INSERT INTO paths VALUES (?,?,?,?,?,?,?)", (pid, data.title.strip(), data.level, data.weeks, f"{data.title.strip()} guided curriculum", user_id, now))
        for i, cid in enumerate(dict.fromkeys(data.course_ids)):
            conn.execute("INSERT INTO path_courses VALUES (?,?,?)", (pid, cid, i))
        conn.commit()
    except Exception:
        conn.rollback(); raise
    finally: conn.close()
    if user.get("institution_id"):
        tenant_resource(user["institution_id"], "path", pid)
    return {"id": pid, "message": "Learning path created"}


@app.get("/api/classrooms")
def classrooms(user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] == "teacher":
        return rows(
            """SELECT c.*,COUNT(m.user_id) student_count FROM classrooms c
            LEFT JOIN classroom_members m ON m.classroom_id=c.id
            WHERE c.teacher_id=? GROUP BY c.id ORDER BY c.created_at DESC""",
            (user_id,),
        )
    if user["role"] == "institution_admin":
        if not user.get("institution_id"):
            return []
        return rows(
            """SELECT c.*,COUNT(m.user_id) student_count FROM classrooms c
            LEFT JOIN classroom_members m ON m.classroom_id=c.id
            JOIN tenant_resources t ON t.resource_type='classroom' AND t.resource_id=c.id
            WHERE t.institution_id=? GROUP BY c.id ORDER BY c.created_at DESC""",
            (user["institution_id"],),
        )
    return rows(
        """SELECT c.id,c.name,c.teacher_id,c.created_at,u.name teacher_name,COUNT(a.id) assignment_count
        FROM classroom_members m JOIN classrooms c ON c.id=m.classroom_id
        JOIN users u ON u.id=c.teacher_id LEFT JOIN classroom_assignments a ON a.classroom_id=c.id
        WHERE m.user_id=? GROUP BY c.id,u.name ORDER BY c.created_at DESC""",
        (user_id,),
    )


@app.post("/api/classrooms")
def create_classroom(data: ClassroomIn, user_id: str = "demo-teacher", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = require_teacher(user_id)
    classroom_id = uid("class")
    join_code = secrets.token_hex(4).upper()
    created_at = now_iso()
    conn = db()
    try:
        conn.execute(
            "INSERT INTO classrooms(id,teacher_id,name,join_code,created_at) VALUES (?,?,?,?,?)",
            (classroom_id, user_id, data.name.strip(), join_code, created_at),
        )
        conn.commit()
    finally:
        conn.close()
    if user.get("institution_id"):
        tenant_resource(user["institution_id"], "classroom", classroom_id)
    return {"id": classroom_id, "name": data.name.strip(), "join_code": join_code, "student_count": 0, "created_at": created_at}


@app.post("/api/classrooms/join")
def join_classroom(data: ClassroomJoinIn, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"learner", "student"}:
        raise HTTPException(403, "Only student accounts can join a classroom")
    classroom = row("SELECT id,name,teacher_id FROM classrooms WHERE join_code=?", (data.join_code.strip().upper(),))
    if not classroom:
        raise HTTPException(404, "Classroom join code not found")
    teacher = current_user(classroom["teacher_id"])
    if user.get("institution_id") != teacher.get("institution_id"):
        raise HTTPException(404, "Classroom join code not found")
    if user.get("institution_id") and (
        user["membership_status"] != "active" or not institution_is_active(user["institution_id"])
    ):
        raise HTTPException(403, "Institution membership is inactive")
    conn = db()
    try:
        conn.execute(
            "INSERT INTO classroom_members(classroom_id,user_id,joined_at) VALUES (?,?,?) ON CONFLICT(classroom_id,user_id) DO NOTHING",
            (classroom["id"], user_id, now_iso()),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": classroom["id"], "name": classroom["name"], "message": "Joined classroom"}


@app.get("/api/classrooms/{classroom_id}/assignments")
def classroom_assignments(classroom_id: str, user_id: str = "demo-student", language: str = "en", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom:
        raise HTTPException(404, "Classroom not found")
    if not classroom_accessible(classroom, user):
        classroom_teacher = current_user(classroom["teacher_id"])
        if user.get("institution_id") == classroom_teacher.get("institution_id"):
            raise HTTPException(403, "Join this classroom to view its assignments")
        raise HTTPException(404, "Classroom not found")
    assignments = rows(
        """SELECT a.id,a.created_at,a.course_id,c.title course_title,a.quiz_id,q.title quiz_title
        FROM classroom_assignments a
        LEFT JOIN courses c ON c.id=a.course_id
        LEFT JOIN quizzes q ON q.id=a.quiz_id
        WHERE a.classroom_id=? ORDER BY a.created_at DESC""",
        (classroom_id,),
    )
    assignments = [
        item for item in assignments
        if (not item.get("course_id") or course_accessible(row("SELECT * FROM courses WHERE id=?", (item["course_id"],)), user))
        and (not item.get("quiz_id") or quiz_accessible(row("SELECT * FROM quizzes WHERE id=?", (item["quiz_id"],)), user))
    ]
    return translate_content(f"classroom:{classroom_id}:assignments", language, assignments)


@app.get("/api/classrooms/{classroom_id}/report")
def classroom_report(
    classroom_id: str,
    search: str = "",
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    search_text = search.strip().lower()
    search_filter = f"%{search_text}%"
    students = rows(
        """SELECT u.id,u.name,u.email,m.joined_at FROM classroom_members m
        JOIN users u ON u.id=m.user_id
        WHERE m.classroom_id=? AND (?='' OR lower(u.name||' '||COALESCE(u.email,'')) LIKE ?)
        ORDER BY lower(u.name),u.id""",
        (classroom_id, search_text, search_filter),
    )
    activities = []
    assignments = rows(
        """SELECT a.id assignment_id,a.course_id,a.quiz_id,a.created_at assigned_at,
        COALESCE(q.title,c.title) activity_title,q.lesson_id
        FROM classroom_assignments a
        LEFT JOIN courses c ON c.id=a.course_id
        LEFT JOIN quizzes q ON q.id=a.quiz_id
        WHERE a.classroom_id=? ORDER BY a.created_at DESC""",
        (classroom_id,),
    )
    assignments = [
        assignment for assignment in assignments
        if not assignment.get("quiz_id")
        or quiz_accessible(row("SELECT * FROM quizzes WHERE id=?", (assignment["quiz_id"],)), user)
    ]
    for student in students:
        for assignment in assignments:
            if assignment["quiz_id"]:
                attempts = rows(
                    """SELECT id attempt_id,score,total,percent,elapsed_seconds,started_at,created_at completed_at
                    FROM attempts WHERE user_id=? AND quiz_id=? AND created_at>=?
                    AND (classroom_assignment_id=? OR classroom_assignment_id IS NULL)
                    ORDER BY created_at DESC""",
                    (student["id"], assignment["quiz_id"], assignment["assigned_at"], assignment["assignment_id"]),
                )
                if attempts:
                    activities.extend({
                        "student_id": student["id"], "student_name": student["name"],
                        "student_email": student["email"], "assignment_id": assignment["assignment_id"],
                        "activity_id": assignment["quiz_id"], "activity_title": assignment["activity_title"],
                        "activity_type": "quiz", "status": "completed", **attempt,
                    } for attempt in attempts)
                else:
                    activities.append({
                        "student_id": student["id"], "student_name": student["name"],
                        "student_email": student["email"], "assignment_id": assignment["assignment_id"],
                        "activity_id": assignment["quiz_id"], "activity_title": assignment["activity_title"],
                        "activity_type": "quiz", "status": "not_started", "score": None, "total": None,
                        "percent": None, "elapsed_seconds": None, "started_at": None, "completed_at": None,
                    })
            else:
                course_progress = row(
                    """SELECT COUNT(DISTINCT l.id) total,
                    COUNT(DISTINCT CASE WHEN p.completed=1 THEN l.id END) completed,
                    MAX(CASE WHEN p.completed=1 THEN p.updated_at END) completed_at
                    FROM lessons l LEFT JOIN progress p ON p.lesson_id=l.id AND p.user_id=?
                    WHERE l.course_id=?""",
                    (student["id"], assignment["course_id"]),
                )
                complete = bool(course_progress and course_progress["total"] and course_progress["total"] == course_progress["completed"])
                activities.append({
                    "student_id": student["id"], "student_name": student["name"],
                    "student_email": student["email"], "assignment_id": assignment["assignment_id"],
                    "activity_id": assignment["course_id"], "activity_title": assignment["activity_title"],
                    "activity_type": "course", "status": "completed" if complete else "in_progress",
                    "score": course_progress["completed"] if course_progress else 0,
                    "total": course_progress["total"] if course_progress else 0,
                    "percent": round(course_progress["completed"] / course_progress["total"] * 100, 1)
                    if course_progress and course_progress["total"] else 0,
                    "elapsed_seconds": None, "started_at": None,
                    "completed_at": course_progress["completed_at"] if complete else None,
                })
    return {"students": students, "activities": activities}


@app.post("/api/classrooms/{classroom_id}/assignments")
def assign_classroom_content(
    classroom_id: str,
    data: ClassroomAssignmentIn,
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can assign content")
    if data.course_id:
        require_course_access(data.course_id, user)
    if data.quiz_id and not quiz_accessible(row("SELECT * FROM quizzes WHERE id=?", (data.quiz_id,)), user):
        raise HTTPException(404, "Quiz not found")
    assignment_id = uid("assignment")
    created_at = now_iso()
    conn = db()
    try:
        conn.execute(
            "INSERT INTO classroom_assignments(id,classroom_id,course_id,quiz_id,created_at) VALUES (?,?,?,?,?)",
            (assignment_id, classroom_id, data.course_id, data.quiz_id, created_at),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": assignment_id, "message": "Content assigned to classroom"}


@app.get("/api/classrooms/{classroom_id}/events")
def classroom_events(
    classroom_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    return rows(
        """SELECT e.*,u.name creator_name FROM classroom_events e
        JOIN users u ON u.id=e.created_by WHERE e.classroom_id=?
        ORDER BY e.starts_at,e.created_at""",
        (classroom_id,),
    )


@app.post("/api/classrooms/{classroom_id}/events")
def create_classroom_event(
    classroom_id: str,
    data: ClassroomEventIn,
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can schedule classes")
    starts_at = data.starts_at.replace(tzinfo=timezone.utc) if data.starts_at.tzinfo is None else data.starts_at
    ends_at = data.ends_at.replace(tzinfo=timezone.utc) if data.ends_at and data.ends_at.tzinfo is None else data.ends_at
    event_id = uid("class_event")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO classroom_events(
            id,classroom_id,title,description,starts_at,ends_at,meeting_url,created_by,created_at
            ) VALUES (?,?,?,?,?,?,?,?,?)""",
            (
                event_id, classroom_id, data.title.strip(), data.description.strip(),
                starts_at.isoformat(), ends_at.isoformat() if ends_at else None,
                data.meeting_url, user_id, now_iso(),
            ),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": event_id, "message": "Class scheduled"}


@app.get("/api/classrooms/{classroom_id}/events/{event_id}/attendance")
def classroom_event_attendance(
    classroom_id: str,
    event_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    event = row(
        "SELECT id FROM classroom_events WHERE id=? AND classroom_id=?",
        (event_id, classroom_id),
    )
    if not classroom or not event or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Class session not found")
    if user["role"] in {"teacher", "institution_admin"}:
        if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
            raise HTTPException(403, "Only the classroom teacher or Institution Admin can view attendance")
        return rows(
            """SELECT a.user_id,u.name,u.email,a.status,a.marked_at
            FROM classroom_attendance a JOIN users u ON u.id=a.user_id
            WHERE a.event_id=? ORDER BY u.name""",
            (event_id,),
        )
    if user["role"] not in {"learner", "student"}:
        raise HTTPException(403, "Student account required")
    return rows(
        """SELECT a.user_id,u.name,a.status,a.marked_at FROM classroom_attendance a
        JOIN users u ON u.id=a.user_id WHERE a.event_id=? AND a.user_id=?""",
        (event_id, user_id),
    )


@app.put("/api/classrooms/{classroom_id}/events/{event_id}/attendance")
def record_classroom_event_attendance(
    classroom_id: str,
    event_id: str,
    data: AttendanceBatchIn,
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    event = row(
        "SELECT id FROM classroom_events WHERE id=? AND classroom_id=?",
        (event_id, classroom_id),
    )
    if not classroom or not event or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Class session not found")
    if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can mark attendance")
    for item in data.entries:
        if not row(
            "SELECT 1 FROM classroom_members WHERE classroom_id=? AND user_id=?",
            (classroom_id, item.user_id),
        ):
            raise HTTPException(404, f"Student {item.user_id} is not in this classroom")
    marked_at = now_iso()
    conn = db()
    try:
        conn.execute("BEGIN")
        for item in data.entries:
            conn.execute(
                """INSERT INTO classroom_attendance(event_id,user_id,status,marked_by,marked_at)
                VALUES (?,?,?,?,?) ON CONFLICT(event_id,user_id) DO UPDATE SET
                status=excluded.status,marked_by=excluded.marked_by,marked_at=excluded.marked_at""",
                (event_id, item.user_id, item.status, user_id, marked_at),
            )
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
    return {"updated": len(data.entries), "marked_at": marked_at}


@app.get("/api/announcements")
def announcements(
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if not user.get("institution_id"):
        return []
    visible_classrooms = {
        classroom["id"] for classroom in rows("SELECT id,teacher_id FROM classrooms")
        if classroom_accessible(classroom, user)
    }
    institution_announcements = rows(
        """SELECT a.id,a.institution_id,a.classroom_id,a.title,a.body,a.created_at,
        u.name author_name,c.name classroom_name FROM announcements a
        JOIN users u ON u.id=a.author_id LEFT JOIN classrooms c ON c.id=a.classroom_id
        WHERE a.institution_id=? ORDER BY a.created_at DESC""",
        (user["institution_id"],),
    )
    return [
        item for item in institution_announcements
        if not item["classroom_id"] or item["classroom_id"] in visible_classrooms
    ]


@app.post("/api/announcements")
def create_announcement(
    data: AnnouncementIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if not user.get("institution_id"):
        raise HTTPException(403, "Institution membership required")
    if user["role"] not in {"institution_admin", "teacher"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    if user["role"] == "institution_admin":
        require_institution_admin(user_id)
    elif not data.classroom_id:
        raise HTTPException(403, "Teachers can only post classroom announcements")
    if data.classroom_id:
        classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (data.classroom_id,))
        if not classroom or not classroom_accessible(classroom, user):
            raise HTTPException(404, "Classroom not found")
        if user["role"] == "teacher" and classroom["teacher_id"] != user_id:
            raise HTTPException(403, "Only the classroom teacher can post its announcements")
    announcement_id = uid("announcement")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO announcements(id,institution_id,classroom_id,author_id,title,body,created_at)
            VALUES (?,?,?,?,?,?,?)""",
            (
                announcement_id, user["institution_id"], data.classroom_id, user_id,
                data.title.strip(), data.body.strip(), now_iso(),
            ),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": announcement_id, "message": "Announcement published"}


@app.get("/api/classrooms/{classroom_id}/discussions")
def classroom_discussions(
    classroom_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    return rows(
        """SELECT d.id,d.classroom_id,d.user_id,d.parent_id,d.body,d.created_at,u.name author_name
        FROM classroom_discussions d JOIN users u ON u.id=d.user_id
        WHERE d.classroom_id=? ORDER BY d.created_at,d.id""",
        (classroom_id,),
    )


@app.post("/api/classrooms/{classroom_id}/discussions")
def create_classroom_discussion(
    classroom_id: str,
    data: ClassroomDiscussionIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    if user["role"] not in {"teacher", "institution_admin", "learner", "student"}:
        raise HTTPException(403, "Classroom member account required")
    if data.parent_id and not row(
        "SELECT id FROM classroom_discussions WHERE id=? AND classroom_id=?",
        (data.parent_id, classroom_id),
    ):
        raise HTTPException(404, "Discussion post not found")
    post_id = uid("discussion")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO classroom_discussions(id,classroom_id,user_id,parent_id,body,created_at)
            VALUES (?,?,?,?,?,?)""",
            (post_id, classroom_id, user_id, data.parent_id, data.body.strip(), now_iso()),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": post_id, "message": "Discussion post created"}


@app.get("/api/classrooms/{classroom_id}/tasks")
def classroom_tasks(
    classroom_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    if user["role"] not in {"teacher", "institution_admin", "learner", "student"}:
        raise HTTPException(403, "Classroom member account required")
    is_staff = user["role"] in {"teacher", "institution_admin"}
    if is_staff and classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can view tasks")
    tasks = rows(
        "SELECT * FROM classroom_tasks WHERE classroom_id=? ORDER BY due_at,created_at",
        (classroom_id,),
    )
    for task in tasks:
        task["files"] = rows(
            "SELECT id,original_name,size_bytes,created_at uploaded_at FROM classroom_task_files WHERE task_id=? ORDER BY created_at",
            (task["id"],),
        )
        if is_staff:
            task["submissions"] = rows(
                """SELECT s.id,s.student_id,u.name student_name,u.email student_email,s.body,
                s.submitted_at,s.grade,s.feedback,s.graded_at FROM classroom_submissions s
                JOIN users u ON u.id=s.student_id WHERE s.task_id=? ORDER BY u.name""",
                (task["id"],),
            )
            for submission in task["submissions"]:
                submission["files"] = rows(
                    "SELECT id,original_name,size_bytes,uploaded_at FROM classroom_submission_files WHERE submission_id=? ORDER BY uploaded_at",
                    (submission["id"],),
                )
            task["submission"] = None
        else:
            task["submission"] = row(
                """SELECT id,body,submitted_at,grade,feedback,graded_at
                FROM classroom_submissions WHERE task_id=? AND student_id=?""",
                (task["id"], user_id),
            )
            if task["submission"]:
                task["submission"]["files"] = rows(
                    "SELECT id,original_name,size_bytes,uploaded_at FROM classroom_submission_files WHERE submission_id=? ORDER BY uploaded_at",
                    (task["submission"]["id"],),
                )
            task["submissions"] = []
    return tasks


@app.post("/api/classrooms/{classroom_id}/tasks")
def create_classroom_task(
    classroom_id: str,
    data: ClassroomTaskIn,
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    if not classroom or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Classroom not found")
    if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can create tasks")
    due_at = data.due_at.replace(tzinfo=timezone.utc) if data.due_at and data.due_at.tzinfo is None else data.due_at
    task_id = uid("task")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO classroom_tasks(id,classroom_id,created_by,title,instructions,due_at,points,created_at)
            VALUES (?,?,?,?,?,?,?,?)""",
            (
                task_id, classroom_id, user_id, data.title.strip(), data.instructions.strip(),
                due_at.isoformat() if due_at else None, data.points, now_iso(),
            ),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": task_id, "message": "Assignment created"}


def safe_upload_name(filename: str) -> str:
    basename = str(filename or "assignment-file").replace("\\", "/").split("/")[-1]
    cleaned = re.sub(r"[\x00-\x1f\x7f]", "_", basename).strip().strip(".")
    return (cleaned or "assignment-file")[:200]


async def save_assignment_upload(upload: UploadFile) -> dict:
    original_name = safe_upload_name(upload.filename or "")
    stored_name = secrets.token_hex(24)
    destination = ASSIGNMENT_UPLOAD_DIR / stored_name
    size_bytes = 0
    try:
        ASSIGNMENT_UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
        with destination.open("xb") as target:
            while chunk := await upload.read(64 * 1024):
                size_bytes += len(chunk)
                if size_bytes > MAX_ASSIGNMENT_FILE_BYTES:
                    raise HTTPException(413, "Each assignment file must be 20 MB or smaller")
                target.write(chunk)
        if size_bytes == 0:
            raise HTTPException(422, "Empty files cannot be uploaded")
    except Exception:
        destination.unlink(missing_ok=True)
        raise
    finally:
        await upload.close()
    return {
        "id": uid("file"),
        "stored_name": stored_name,
        "original_name": original_name,
        "content_type": "application/octet-stream",
        "size_bytes": size_bytes,
    }


def remove_saved_uploads(uploads: list[dict]) -> None:
    for upload in uploads:
        (ASSIGNMENT_UPLOAD_DIR / upload["stored_name"]).unlink(missing_ok=True)


def assignment_download(stored_name: str, original_name: str) -> FileResponse:
    root = ASSIGNMENT_UPLOAD_DIR.resolve()
    path = (ASSIGNMENT_UPLOAD_DIR / stored_name).resolve()
    if path.parent != root or not path.is_file():
        raise HTTPException(404, "Assignment file not found")
    return FileResponse(
        path,
        media_type="application/octet-stream",
        filename=safe_upload_name(original_name),
        content_disposition_type="attachment",
        headers={"X-Content-Type-Options": "nosniff"},
    )


@app.post("/api/classrooms/{classroom_id}/tasks/{task_id}/files")
async def upload_classroom_task_file(
    classroom_id: str,
    task_id: str,
    upload: UploadFile = File(...),
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    task = row("SELECT id FROM classroom_tasks WHERE id=? AND classroom_id=?", (task_id, classroom_id))
    if not classroom or not task or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Assignment not found")
    if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can attach files")
    saved = await save_assignment_upload(upload)
    conn = db()
    try:
        conn.execute(
            """INSERT INTO classroom_task_files(
            id,task_id,stored_name,original_name,content_type,size_bytes,uploaded_by,created_at
            ) VALUES (?,?,?,?,?,?,?,?)""",
            (
                saved["id"], task_id, saved["stored_name"], saved["original_name"],
                saved["content_type"], saved["size_bytes"], user_id, now_iso(),
            ),
        )
        conn.commit()
    except Exception:
        conn.rollback()
        remove_saved_uploads([saved])
        raise
    finally:
        conn.close()
    return {"id": saved["id"], "original_name": saved["original_name"], "size_bytes": saved["size_bytes"]}


@app.get("/api/classrooms/{classroom_id}/tasks/{task_id}/files/{file_id}")
def download_classroom_task_file(
    classroom_id: str,
    task_id: str,
    file_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    item = row(
        """SELECT f.stored_name,f.original_name FROM classroom_task_files f
        JOIN classroom_tasks t ON t.id=f.task_id
        WHERE f.id=? AND f.task_id=? AND t.classroom_id=?""",
        (file_id, task_id, classroom_id),
    )
    if not classroom or not item or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Assignment file not found")
    if user["role"] in {"teacher", "institution_admin"} and classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can access this file")
    return assignment_download(item["stored_name"], item["original_name"])


@app.post("/api/classrooms/{classroom_id}/tasks/{task_id}/submissions")
def submit_classroom_task(
    classroom_id: str,
    task_id: str,
    data: ClassroomSubmissionIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"learner", "student"}:
        raise HTTPException(403, "Only student accounts can submit assignments")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    task = row(
        "SELECT id FROM classroom_tasks WHERE id=? AND classroom_id=?",
        (task_id, classroom_id),
    )
    if not classroom or not task or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Assignment not found")
    if not data.body.strip() and not row(
        """SELECT 1 FROM classroom_submission_files f
        JOIN classroom_submissions s ON s.id=f.submission_id
        WHERE s.task_id=? AND s.student_id=? LIMIT 1""",
        (task_id, user_id),
    ):
        raise HTTPException(422, "Write a response or attach at least one file")
    submitted_at = now_iso()
    submission_id = uid("submission")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO classroom_submissions(
            id,task_id,student_id,body,submitted_at,grade,feedback,graded_at
            ) VALUES (?,?,?,?,?,NULL,'',NULL)
            ON CONFLICT(task_id,student_id) DO UPDATE SET body=excluded.body,
            submitted_at=excluded.submitted_at,grade=NULL,feedback='',graded_at=NULL""",
            (submission_id, task_id, user_id, data.body.strip(), submitted_at),
        )
        submission_id = conn.execute(
            "SELECT id FROM classroom_submissions WHERE task_id=? AND student_id=?",
            (task_id, user_id),
        ).fetchone()["id"]
        conn.commit()
    finally:
        conn.close()
    return {"id": submission_id, "submitted_at": submitted_at, "message": "Assignment submitted"}


@app.post("/api/classrooms/{classroom_id}/tasks/{task_id}/submissions/files")
async def submit_classroom_task_files(
    classroom_id: str,
    task_id: str,
    files: list[UploadFile] = File(...),
    body: str = Form(default="", max_length=20000),
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"learner", "student"}:
        raise HTTPException(403, "Only student accounts can submit assignments")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    task = row("SELECT id FROM classroom_tasks WHERE id=? AND classroom_id=?", (task_id, classroom_id))
    if not classroom or not task or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Assignment not found")
    if not files or len(files) > 10:
        raise HTTPException(422, "Attach between 1 and 10 files per submission")
    saved = []
    try:
        for upload in files:
            saved.append(await save_assignment_upload(upload))
        submitted_at = now_iso()
        submission_id = uid("submission")
        conn = db()
        try:
            conn.execute(
                """INSERT INTO classroom_submissions(
                id,task_id,student_id,body,submitted_at,grade,feedback,graded_at
                ) VALUES (?,?,?,?,?,NULL,'',NULL)
                ON CONFLICT(task_id,student_id) DO UPDATE SET body=excluded.body,
                submitted_at=excluded.submitted_at,grade=NULL,feedback='',graded_at=NULL""",
                (submission_id, task_id, user_id, body.strip(), submitted_at),
            )
            submission_id = conn.execute(
                "SELECT id FROM classroom_submissions WHERE task_id=? AND student_id=?",
                (task_id, user_id),
            ).fetchone()["id"]
            for item in saved:
                conn.execute(
                    """INSERT INTO classroom_submission_files(
                    id,submission_id,stored_name,original_name,content_type,size_bytes,uploaded_at
                    ) VALUES (?,?,?,?,?,?,?)""",
                    (
                        item["id"], submission_id, item["stored_name"], item["original_name"],
                        item["content_type"], item["size_bytes"], submitted_at,
                    ),
                )
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()
    except Exception:
        remove_saved_uploads(saved)
        raise
    return {"id": submission_id, "submitted_at": submitted_at, "message": "Assignment files submitted"}


@app.get("/api/classrooms/{classroom_id}/tasks/{task_id}/submissions/files/{file_id}")
def download_classroom_submission_file(
    classroom_id: str,
    task_id: str,
    file_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    item = row(
        """SELECT f.stored_name,f.original_name,s.student_id FROM classroom_submission_files f
        JOIN classroom_submissions s ON s.id=f.submission_id
        JOIN classroom_tasks t ON t.id=s.task_id
        WHERE f.id=? AND t.id=? AND t.classroom_id=?""",
        (file_id, task_id, classroom_id),
    )
    if not classroom or not item or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Submission file not found")
    is_staff = user["role"] in {"teacher", "institution_admin"}
    if is_staff:
        if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
            raise HTTPException(403, "Only the classroom teacher or Institution Admin can access submissions")
    elif item["student_id"] != user_id:
        raise HTTPException(404, "Submission file not found")
    return assignment_download(item["stored_name"], item["original_name"])


@app.put("/api/classrooms/{classroom_id}/tasks/{task_id}/submissions/{student_id}")
def grade_classroom_submission(
    classroom_id: str,
    task_id: str,
    student_id: str,
    data: ClassroomGradeIn,
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    classroom = row("SELECT id,teacher_id FROM classrooms WHERE id=?", (classroom_id,))
    task = row(
        "SELECT id,points FROM classroom_tasks WHERE id=? AND classroom_id=?",
        (task_id, classroom_id),
    )
    if not classroom or not task or not classroom_accessible(classroom, user):
        raise HTTPException(404, "Assignment not found")
    if classroom["teacher_id"] != user_id and user["role"] != "institution_admin":
        raise HTTPException(403, "Only the classroom teacher or Institution Admin can grade submissions")
    if data.grade > int(task["points"]):
        raise HTTPException(422, "Grade cannot exceed the assignment points")
    conn = db()
    try:
        updated = conn.execute(
            """UPDATE classroom_submissions SET grade=?,feedback=?,graded_at=?
            WHERE task_id=? AND student_id=?""",
            (data.grade, data.feedback.strip(), now_iso(), task_id, student_id),
        ).rowcount
        conn.commit()
    finally:
        conn.close()
    if not updated:
        raise HTTPException(404, "Student submission not found")
    return {"task_id": task_id, "student_id": student_id, "grade": data.grade}


@app.get("/api/courses/{course_id}/resources")
def course_resources(
    course_id: str,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    require_course_access(course_id, user)
    items = rows(
        """SELECT id,course_id,institution_id,created_by,title,url,description,kind,created_at
        FROM course_resources WHERE course_id=? ORDER BY created_at DESC""",
        (course_id,),
    )
    return [
        item for item in items
        if item["institution_id"] == user.get("institution_id")
        or (item["institution_id"] is None and not user.get("institution_id") and item["created_by"] == user_id)
    ]


@app.post("/api/courses/{course_id}/resources")
def create_course_resource(
    course_id: str,
    data: CourseResourceIn,
    user_id: str = "demo-teacher",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    course_data = require_course_access(course_id, user)
    if user["role"] == "institution_admin":
        require_institution_admin(user_id)
    elif course_data["created_by"] != user_id and course_data["created_by"] != "seed":
        raise HTTPException(403, "You can only add resources to your own or shared IKS courses")
    resource_id = uid("resource")
    conn = db()
    try:
        conn.execute(
            """INSERT INTO course_resources(
            id,course_id,institution_id,created_by,title,url,description,kind,created_at
            ) VALUES (?,?,?,?,?,?,?,?,?)""",
            (
                resource_id, course_id, user.get("institution_id"), user_id,
                data.title.strip(), data.url, data.description.strip(), data.kind, now_iso(),
            ),
        )
        conn.commit()
    finally:
        conn.close()
    return {"id": resource_id, "message": "Course resource added"}


@app.get("/api/scholars")
def scholars():
    return rows("SELECT * FROM scholars ORDER BY name")


@app.get("/api/lessons/{lesson_id}")
def lesson(lesson_id: str, user_id: str = "demo-student", language: str = "en", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id); l = row("SELECT l.*,c.title course_title,c.category course_category FROM lessons l JOIN courses c ON c.id=l.course_id WHERE l.id=?", (lesson_id,))
    if not l: raise HTTPException(404, "Lesson not found")
    require_course_access(l["course_id"], user)
    hydrate_json(l, ["takeaways_json", "concepts_json", "checkpoint_json", "word_by_word_json"])
    l["completed"] = bool(row("SELECT completed FROM progress WHERE user_id=? AND lesson_id=?", (user_id, lesson_id)))
    l["bookmarked"] = bool(row("SELECT 1 FROM bookmarks WHERE user_id=? AND lesson_id=?", (user_id, lesson_id)))
    course_data = hydrate_course(row("SELECT * FROM courses WHERE id=?", (l["course_id"],)), user_id)
    localized = translated_course(course_data, language)
    localized_lesson = next(item for item in localized["lessons"] if item["id"] == lesson_id)
    l.update(localized_lesson)
    l["course_title"] = localized["title"]
    l["course_category"] = localized["category"]
    return l


@app.post("/api/lessons/{lesson_id}/progress")
def progress_lesson(lesson_id: str, data: ProgressIn, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id); l = row("SELECT * FROM lessons WHERE id=?", (lesson_id,))
    if not l: raise HTTPException(404, "Lesson not found")
    if user["role"] not in {"learner", "student"}:
        raise HTTPException(403, "Only students can update their learning progress")
    require_course_access(l["course_id"], user)
    conn = db(); conn.execute("INSERT INTO progress VALUES (?,?,?,?) ON CONFLICT(user_id,lesson_id) DO UPDATE SET completed=excluded.completed,updated_at=excluded.updated_at", (user_id, lesson_id, int(data.completed), now_iso())); conn.commit(); conn.close()
    if data.completed:
        mark_study_day(user_id); maybe_issue_certificate(user_id, l["course_id"]); refresh_achievement_badges(user_id)
    return {"completed": data.completed, "xp": learner_xp(user_id)}


@app.get("/api/bookmarks/{user_id}")
def bookmarks(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id)
    return rows("SELECT b.lesson_id,l.title,c.title course_title,b.created_at FROM bookmarks b JOIN lessons l ON l.id=b.lesson_id JOIN courses c ON c.id=l.course_id WHERE b.user_id=? ORDER BY b.created_at DESC", (user_id,))


@app.post("/api/bookmarks")
def toggle_bookmark(data: BookmarkIn, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    lesson_data = row("SELECT * FROM lessons WHERE id=?", (data.lesson_id,))
    if not lesson_data or not course_accessible(row("SELECT * FROM courses WHERE id=?", (lesson_data["course_id"],)), user):
        raise HTTPException(404, "Lesson not found")
    exists = row("SELECT 1 FROM bookmarks WHERE user_id=? AND lesson_id=?", (user_id, data.lesson_id)); conn = db()
    if exists: conn.execute("DELETE FROM bookmarks WHERE user_id=? AND lesson_id=?", (user_id, data.lesson_id)); result = False
    else: conn.execute("INSERT INTO bookmarks VALUES (?,?,?)", (user_id, data.lesson_id, now_iso())); result = True
    conn.commit(); conn.close(); mark_study_day(user_id)
    return {"bookmarked": result}


@app.get("/api/notes/{user_id}")
def notes(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id)
    return rows("SELECT n.id,n.lesson_id,n.content,n.created_at,n.updated_at,l.title lesson_title,c.title course_title FROM notes n JOIN lessons l ON l.id=n.lesson_id JOIN courses c ON c.id=l.course_id WHERE n.user_id=? ORDER BY n.updated_at DESC", (user_id,))


@app.post("/api/notes")
def create_note(data: NoteIn, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    lesson_data = row("SELECT * FROM lessons WHERE id=?", (data.lesson_id,))
    if not lesson_data or not course_accessible(row("SELECT * FROM courses WHERE id=?", (lesson_data["course_id"],)), user):
        raise HTTPException(404, "Lesson not found")
    now = now_iso(); nid = uid("note"); conn = db(); conn.execute("INSERT INTO notes VALUES (?,?,?,?,?,?)", (nid, user_id, data.lesson_id, data.content.strip(), now, now)); conn.commit(); conn.close(); mark_study_day(user_id); return {"id": nid, "message": "Note saved"}


@app.delete("/api/notes/{note_id}")
def delete_note(note_id: str, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id)
    conn = db(); deleted = conn.execute("DELETE FROM notes WHERE id=? AND user_id=?", (note_id, user_id)).rowcount; conn.commit(); conn.close()
    if not deleted: raise HTTPException(404, "Note not found")
    return {"message": "Note deleted"}


@app.get("/api/quizzes")
def quizzes(
    user_id: str = "demo-student", language: str = "en", include_drafts: bool = False,
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    result = []
    for q in rows("SELECT q.*,COUNT(qq.id) question_count FROM quizzes q LEFT JOIN quiz_questions qq ON qq.quiz_id=q.id GROUP BY q.id ORDER BY q.created_at"):
        if not quiz_accessible(q, user):
            continue
        q["published"] = bool(q["published"])
        q["settings"] = QuizSettingsIn.model_validate_json(q.pop("settings_json") or "{}").model_dump(mode="json")
        if not q["published"] and not (
            include_drafts and user["role"] in {"teacher", "institution_admin"}
            and (q["created_by"] == user_id or user["role"] == "institution_admin")
        ):
            continue
        last = row("SELECT percent,created_at FROM attempts WHERE user_id=? AND quiz_id=? ORDER BY created_at DESC LIMIT 1", (user_id, q["id"]))
        q["last_percent"] = last["percent"] if last and (user["role"] == "teacher" or q["settings"]["show_results_immediately"]) else None
        result.append(q)
    return translate_content("quiz_catalog", language, result)


@app.post("/api/quizzes")
def create_quiz(data: QuizIn, user_id: str = "demo-teacher", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    if hidden_quiz_title(data.title):
        raise HTTPException(422, "This quiz format is no longer available")
    if user["role"] == "institution_admin":
        require_institution_admin(user_id)
    require_course_access(data.course_id, user)
    if data.lesson_id and not row("SELECT 1 FROM lessons WHERE id=? AND course_id=?", (data.lesson_id, data.course_id)):
        raise HTTPException(404, "Topic not found in the selected course")
    if any(q.answer_index < 0 or q.answer_index >= len(q.options) for q in data.questions): raise HTTPException(400, "Correct answer index is invalid")
    qid = uid("quiz"); now = now_iso(); conn = db()
    try:
        conn.execute("BEGIN")
        conn.execute(
            """INSERT INTO quizzes(id,course_id,title,description,lesson_id,created_by,created_at,settings_json,published)
            VALUES (?,?,?,?,?,?,?,?,?)""",
            (qid, data.course_id, data.title.strip(), data.description[:1000], data.lesson_id, user_id, now,
             data.settings.model_dump_json(), int(data.published)),
        )
        for q in data.questions: conn.execute("INSERT INTO quiz_questions VALUES (?,?,?,?,?,?)", (uid("question"), qid, q.question.strip(), json.dumps(q.options, ensure_ascii=False), q.answer_index, q.explanation.strip()))
        conn.commit()
    except Exception:
        conn.rollback(); raise
    finally: conn.close()
    if user.get("institution_id"):
        tenant_resource(user["institution_id"], "quiz", qid)
    return {"id": qid, "published": data.published, "message": "Quiz published" if data.published else "Quiz saved as draft"}


@app.put("/api/quizzes/{quiz_id}")
def update_quiz(quiz_id: str, data: QuizIn, user_id: str = "demo-teacher", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] not in {"teacher", "institution_admin"}:
        raise HTTPException(403, "Teacher or Institution Admin role required")
    existing = row("SELECT * FROM quizzes WHERE id=?", (quiz_id,))
    quiz_resource = row(
        "SELECT institution_id FROM tenant_resources WHERE resource_type='quiz' AND resource_id=?",
        (quiz_id,),
    )
    tenant_admin_owns = bool(
        user["role"] == "institution_admin" and quiz_resource
        and quiz_resource["institution_id"] == user.get("institution_id")
    )
    if not existing or hidden_quiz_title(existing.get("title", "")) or (existing["created_by"] != user_id and not tenant_admin_owns):
        raise HTTPException(404, "Quiz not found")
    if hidden_quiz_title(data.title):
        raise HTTPException(422, "This quiz format is no longer available")
    if row("SELECT 1 FROM attempts WHERE quiz_id=? LIMIT 1", (quiz_id,)):
        raise HTTPException(409, "A quiz cannot be edited after students have attempted it")
    require_course_access(data.course_id, user)
    if data.lesson_id and not row("SELECT 1 FROM lessons WHERE id=? AND course_id=?", (data.lesson_id, data.course_id)):
        raise HTTPException(404, "Topic not found in the selected course")
    if any(question.answer_index < 0 or question.answer_index >= len(question.options) for question in data.questions):
        raise HTTPException(400, "Correct answer index is invalid")

    conn = db()
    try:
        conn.execute("BEGIN")
        conn.execute(
            """UPDATE quizzes SET course_id=?,title=?,description=?,lesson_id=?,settings_json=?,published=?
            WHERE id=? AND created_by=?""",
            (data.course_id, data.title.strip(), data.description[:1000], data.lesson_id,
             data.settings.model_dump_json(), int(data.published), quiz_id, user_id),
        )
        conn.execute("DELETE FROM quiz_questions WHERE quiz_id=?", (quiz_id,))
        for question in data.questions:
            conn.execute(
                "INSERT INTO quiz_questions VALUES (?,?,?,?,?,?)",
                (uid("question"), quiz_id, question.question.strip(), json.dumps(question.options, ensure_ascii=False),
                 question.answer_index, question.explanation.strip()),
            )
        conn.commit()
    except Exception:
        conn.rollback(); raise
    finally:
        conn.close()
    if user.get("institution_id") and not quiz_resource:
        tenant_resource(user["institution_id"], "quiz", quiz_id)
    return {"id": quiz_id, "published": data.published, "message": "Quiz updated"}


@app.get("/api/quizzes/{quiz_id}")
def quiz(
    quiz_id: str, count: Optional[int] = None, user_id: str = "demo-student", language: str = "en",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if language not in {"en", "hi", "mr"}:
        raise HTTPException(422, "Choose English, Hindi, or Marathi.")
    q = row("SELECT * FROM quizzes WHERE id=?", (quiz_id,))
    if not quiz_accessible(q, user):
        raise HTTPException(404, "Quiz not found")
    quiz_resource = row("SELECT institution_id FROM tenant_resources WHERE resource_type='quiz' AND resource_id=?", (quiz_id,))
    can_manage = bool(
        user["role"] in {"teacher", "institution_admin"}
        and (q["created_by"] == user_id or (
            user["role"] == "institution_admin" and quiz_resource
            and quiz_resource["institution_id"] == user.get("institution_id")
        ))
    )
    if not q["published"] and not can_manage:
        raise HTTPException(404, "Quiz not found")
    settings = QuizSettingsIn.model_validate_json(q.pop("settings_json") or "{}")
    now = datetime.now(timezone.utc)
    if not can_manage:
        if settings.start_at and now < (settings.start_at.replace(tzinfo=timezone.utc) if settings.start_at.tzinfo is None else settings.start_at):
            raise HTTPException(409, "This quiz is not open yet")
        if settings.end_at and now > (settings.end_at.replace(tzinfo=timezone.utc) if settings.end_at.tzinfo is None else settings.end_at):
            raise HTTPException(409, "The quiz deadline has passed")
    q["settings"] = settings.model_dump(mode="json")
    q["published"] = bool(q["published"])
    q["questions"] = []
    questions = rows("SELECT id,question,options_json,explanation,answer_index FROM quiz_questions WHERE quiz_id=? ORDER BY id", (quiz_id,))
    if count is not None and (count < 1 or count > 200):
        raise HTTPException(422, "Choose between 1 and 200 questions")
    if count is not None and count > len(questions):
        raise HTTPException(422, f"This topic has only {len(questions)} available questions")
    if count is not None and settings.randomize_questions:
        previously_seen = {
            item["question_id"] for item in rows(
                "SELECT DISTINCT question_id FROM attempt_questions WHERE user_id=? AND quiz_id=?",
                (user_id, quiz_id),
            )
        }
        unseen = [question for question in questions if question["id"] not in previously_seen]
        if len(unseen) >= count:
            questions = random.sample(unseen, count)
        else:
            seen = [question for question in questions if question["id"] in previously_seen]
            questions = unseen + random.sample(seen, count - len(unseen))
            random.shuffle(questions)
    elif count is not None:
        questions = questions[:count]
    elif settings.randomize_questions and not can_manage:
        random.shuffle(questions)
    q["attempts_used"] = int(row("SELECT COUNT(*) count FROM attempts WHERE user_id=? AND quiz_id=?", (user_id, quiz_id))["count"])
    q["attempts_remaining"] = (
        max(0, (settings.max_attempts if settings.allow_retake else 1) - q["attempts_used"])
        if settings.max_attempts or not settings.allow_retake else None
    )

    if language != "en" and questions:
        source_questions = []
        for question in sorted(questions, key=lambda item: item["id"]):
            source_questions.append({
                "id": question["id"],
                "question": question["question"],
                "options": json.loads(question.pop("options_json") or "[]"),
                "explanation": question["explanation"],
            })
        localized_questions = []
        for offset in range(0, len(source_questions), 20):
            question_batch = source_questions[offset:offset + 20]
            question_set = hashlib.sha256(
                ",".join(item["id"] for item in question_batch).encode("utf-8")
            ).hexdigest()[:20]
            localized_questions.extend(translate_content(
                f"quiz:{quiz_id}:questions:{question_set}", language, question_batch,
            ))
        title_description = translate_content(
            f"quiz:{quiz_id}:metadata", language,
            {"title": q["title"], "description": q.get("description") or ""},
        )
        q.update(title_description)
        localized_by_id = {item["id"]: item for item in localized_questions}
        for question in questions:
            localized = localized_by_id[question["id"]]
            question["question"] = localized["question"]
            question["options_json"] = json.dumps(localized["options"], ensure_ascii=False)
            question["explanation"] = localized["explanation"]
    q["unseen_question_count"] = len(questions) if count is None else len([
        question for question in rows(
            """SELECT id FROM quiz_questions WHERE quiz_id=?
            AND id NOT IN (SELECT question_id FROM attempt_questions WHERE user_id=? AND quiz_id=?)""",
            (quiz_id, user_id, quiz_id),
        )
    ])
    for question in questions:
        opts = json.loads(question.pop("options_json") or "[]")
        option_order = list(range(len(opts)))
        if settings.randomize_options and not can_manage:
            random.shuffle(option_order)
        question["option_order"] = option_order
        question["options"] = [opts[i] for i in option_order]
        if can_manage:
            question["answer_index"] = question["answer_index"]
        else:
            question.pop("answer_index", None)
            question.pop("explanation", None)
        q["questions"].append(question)
    return q


@app.post("/api/quizzes/{quiz_id}/attempt")
def quiz_attempt(quiz_id: str, data: QuizAnswerIn, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    if user["role"] not in {"learner", "student"}:
        raise HTTPException(403, "Only student accounts can submit quiz attempts")
    quiz_data = row("SELECT * FROM quizzes WHERE id=?", (quiz_id,))
    if not quiz_accessible(quiz_data, user):
        raise HTTPException(404, "Quiz not found")
    if not quiz_data["published"]:
        raise HTTPException(404, "Quiz not found")
    settings = QuizSettingsIn.model_validate_json(quiz_data["settings_json"] or "{}")
    now = datetime.now(timezone.utc)
    if settings.start_at and now < (settings.start_at.replace(tzinfo=timezone.utc) if settings.start_at.tzinfo is None else settings.start_at):
        raise HTTPException(409, "This quiz is not open yet")
    if settings.end_at and now > (settings.end_at.replace(tzinfo=timezone.utc) if settings.end_at.tzinfo is None else settings.end_at):
        raise HTTPException(409, "The quiz deadline has passed")
    attempts_used = int(row("SELECT COUNT(*) count FROM attempts WHERE user_id=? AND quiz_id=?", (user_id, quiz_id))["count"])
    attempt_limit = settings.max_attempts if settings.allow_retake else 1
    if attempt_limit and attempts_used >= attempt_limit:
        raise HTTPException(409, "You have used all attempts allowed for this quiz")
    if data.question_ids:
        if len(set(data.question_ids)) != len(data.question_ids):
            raise HTTPException(400, "Question list contains duplicates")
        qs = []
        for question_id in data.question_ids:
            question = row("SELECT * FROM quiz_questions WHERE id=? AND quiz_id=?", (question_id, quiz_id))
            if not question:
                raise HTTPException(400, "A submitted question does not belong to this quiz")
            qs.append(question)
    else:
        qs = rows("SELECT * FROM quiz_questions WHERE quiz_id=? ORDER BY id", (quiz_id,))
    if not qs: raise HTTPException(404, "Quiz has no questions")
    if len(data.answers) != len(qs): raise HTTPException(400, f"Answer all {len(qs)} questions before submitting")
    if data.classroom_assignment_id and not row(
        """SELECT 1 FROM classroom_assignments a JOIN classroom_members m ON m.classroom_id=a.classroom_id
        WHERE a.id=? AND a.quiz_id=? AND m.user_id=?""",
        (data.classroom_assignment_id, quiz_id, user_id),
    ):
        raise HTTPException(403, "This quiz is not assigned to one of your classes")
    review = []; score = 0
    for idx, answer in enumerate(data.answers):
        opts = json.loads(qs[idx]["options_json"] or "[]")
        if answer < -1 or answer >= len(opts): raise HTTPException(400, "Invalid answer option")
        correct = answer == qs[idx]["answer_index"]
        if correct:
            score += settings.marks_per_question
        elif answer >= 0:
            score -= settings.negative_marking
        review_item = {
            "question_id": qs[idx]["id"],
            "question": qs[idx]["question"],
            "selected_index": answer,
            "correct": correct,
            "correct_index": qs[idx]["answer_index"],
            "correct_answer": opts[qs[idx]["answer_index"]],
        }
        if settings.show_explanations:
            review_item["explanation"] = qs[idx]["explanation"]
        review.append(review_item)
    total = len(qs) * settings.marks_per_question
    score = max(0, score)
    pct = round(score / total * 100, 1)
    aid = uid("attempt")
    completed_at = datetime.now(timezone.utc)
    started_at = completed_at - timedelta(seconds=data.elapsed_seconds) if data.elapsed_seconds is not None else None
    conn = db()
    try:
        conn.execute(
            """INSERT INTO attempts(id,user_id,quiz_id,score,total,percent,elapsed_seconds,started_at,
            classroom_assignment_id,created_at) VALUES (?,?,?,?,?,?,?,?,?,?)""",
            (
                aid, user_id, quiz_id, score, total, pct, data.elapsed_seconds,
                started_at.isoformat() if started_at else None, data.classroom_assignment_id, completed_at.isoformat(),
            ),
        )
        for question in qs:
            conn.execute(
                "INSERT INTO attempt_questions(attempt_id,user_id,quiz_id,question_id) VALUES (?,?,?,?)",
                (aid, user_id, quiz_id, question["id"]),
            )
        conn.commit()
    finally:
        conn.close()
    mark_study_day(user_id); refresh_achievement_badges(user_id)
    return {
        "attempt_id": aid,
        "score": score if settings.show_results_immediately else None,
        "total": total if settings.show_results_immediately else None,
        "percent": pct if settings.show_results_immediately else None,
        "passed": score >= settings.passing_marks if settings.show_results_immediately and settings.passing_marks else None,
        "results_released": settings.show_results_immediately,
        "elapsed_seconds": data.elapsed_seconds,
        "started_at": started_at.isoformat() if started_at else None,
        "completed_at": completed_at.isoformat(),
        "review": review if settings.show_results_immediately else [],
        "xp": learner_xp(user_id),
    }


@app.get("/api/flashcards")
def flashcards(
    course_id: Optional[str] = None,
    language: str = "en",
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization)
    user = current_user(user_id)
    cards = rows("SELECT * FROM flashcards WHERE course_id=? ORDER BY position", (course_id,)) if course_id else rows("SELECT * FROM flashcards ORDER BY position")
    cards = [
        card for card in cards
        if course_accessible(row("SELECT * FROM courses WHERE id=?", (card["course_id"],)), user)
    ]
    return translate_content(f"flashcards:{course_id or 'all'}", language, cards)


@app.get("/api/flashcards/review")
def flashcard_review_queue(user_id: str = "demo-student", language: str = "en", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    now = now_iso()
    cards = rows(
        """SELECT f.*,r.repetitions,r.interval_days,r.due_at,r.last_reviewed_at,
        CASE WHEN r.card_id IS NULL OR r.due_at<=? THEN 1 ELSE 0 END due
        FROM flashcards f LEFT JOIN flashcard_reviews r ON r.card_id=f.id AND r.user_id=?
        ORDER BY CASE WHEN r.card_id IS NULL OR r.due_at<=? THEN 0 ELSE 1 END,f.position""",
        (now, user_id, now),
    )
    cards = [
        card for card in cards
        if course_accessible(row("SELECT * FROM courses WHERE id=?", (card["course_id"],)), user)
    ]
    result = {
        "cards": cards,
        "due_count": sum(card["due"] for card in cards),
        "total_count": len(cards),
    }
    result["cards"] = translate_content("flashcards:review", language, result["cards"])
    return result


@app.post("/api/flashcards/{card_id}/review")
def review_flashcard(
    card_id: str,
    data: FlashcardReviewIn,
    user_id: str = "demo-student",
    authorization: Optional[str] = Header(default=None),
):
    auth_row_for_user(user_id, authorization); current_user(user_id)
    card = row("SELECT * FROM flashcards WHERE id=?", (card_id,))
    if not card or not course_accessible(row("SELECT * FROM courses WHERE id=?", (card["course_id"],)), current_user(user_id)):
        raise HTTPException(404, "Flashcard not found")

    previous = row(
        "SELECT repetitions,interval_days FROM flashcard_reviews WHERE user_id=? AND card_id=?",
        (user_id, card_id),
    )
    repetitions = int(previous["repetitions"]) if previous else 0
    interval_days = float(previous["interval_days"]) if previous else 0
    if data.rating == "again":
        repetitions = 0
        interval_days = 1 / 144
    elif data.rating == "hard":
        repetitions += 1
        interval_days = max(1, round(interval_days * 1.2)) if interval_days else 1
    elif data.rating == "good":
        interval_days = 1 if repetitions == 0 else 3 if repetitions == 1 else max(1, round(interval_days * 2.5))
        repetitions += 1
    else:
        interval_days = 4 if repetitions == 0 else max(4, round(interval_days * 3.2))
        repetitions += 1
    interval_days = min(interval_days, 365)

    reviewed_at = datetime.now(timezone.utc)
    due_at = reviewed_at + timedelta(days=interval_days)
    conn = db()
    try:
        conn.execute(
            """INSERT INTO flashcard_reviews(user_id,card_id,repetitions,interval_days,due_at,last_reviewed_at)
            VALUES (?,?,?,?,?,?)
            ON CONFLICT(user_id,card_id) DO UPDATE SET repetitions=excluded.repetitions,
            interval_days=excluded.interval_days,due_at=excluded.due_at,last_reviewed_at=excluded.last_reviewed_at""",
            (user_id, card_id, repetitions, interval_days, due_at.isoformat(), reviewed_at.isoformat()),
        )
        conn.commit()
    finally:
        conn.close()
    mark_study_day(user_id)
    return {
        "card_id": card_id,
        "rating": data.rating,
        "repetitions": repetitions,
        "interval_days": interval_days,
        "due_at": due_at.isoformat(),
    }


@app.get("/api/analytics/{user_id}")
def analytics(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); user = current_user(user_id)
    course_ids = accessible_course_ids(user)
    if course_ids:
        course_marks = ",".join("?" for _ in course_ids)
        course_values = tuple(sorted(course_ids))
        cats = rows(
            f"""SELECT c.category,COUNT(DISTINCT l.id) total,
            SUM(CASE WHEN p.completed=1 THEN 1 ELSE 0 END) completed,
            COALESCE(SUM(CASE WHEN p.completed=1 THEN l.minutes ELSE 0 END),0) minutes_completed,
            COALESCE(SUM(l.minutes),0) minutes_total
            FROM courses c JOIN lessons l ON l.course_id=c.id
            LEFT JOIN progress p ON p.lesson_id=l.id AND p.user_id=?
            WHERE c.id IN ({course_marks}) GROUP BY c.category ORDER BY c.category""",
            (user_id, *course_values),
        )
    else:
        cats = []
    quiz_ids = accessible_quiz_ids(user)
    if quiz_ids:
        quiz_marks = ",".join("?" for _ in quiz_ids)
        q = dict(row(
            f"""SELECT COUNT(*) attempts,COALESCE(AVG(percent),0) avg_score,
            COALESCE(MAX(percent),0) best FROM attempts
            WHERE user_id=? AND quiz_id IN ({quiz_marks})""",
            (user_id, *sorted(quiz_ids)),
        ))
    else:
        q = {"attempts": 0, "avg_score": 0, "best": 0}
    xp = learner_xp(user_id)
    return {"by_category": cats, "quiz": q, "streak": streak_payload(user_id), "xp": xp, "rank": rank_for_xp(xp)}


@app.get("/api/achievements/{user_id}")
def achievements(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id); refresh_achievement_badges(user_id)
    badges_data = rows("SELECT b.*,ub.awarded_at FROM badges b LEFT JOIN user_badges ub ON ub.badge_id=b.id AND ub.user_id=? ORDER BY (ub.awarded_at IS NULL),ub.awarded_at DESC,b.name", (user_id,))
    certs = rows("SELECT c.id,c.certificate_no,c.issued_at,co.title,co.icon FROM certificates c JOIN courses co ON co.id=c.course_id WHERE c.user_id=? ORDER BY c.issued_at DESC", (user_id,))
    xp = learner_xp(user_id); return {"badges": badges_data, "certificates": certs, "xp": xp, "rank": rank_for_xp(xp)}


@app.get("/api/certificates/{certificate_id}/pdf")
def certificate_pdf(certificate_id: str, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id)
    c = row("SELECT cert.*,u.name user_name,co.title course_title FROM certificates cert JOIN users u ON u.id=cert.user_id JOIN courses co ON co.id=cert.course_id WHERE cert.id=?", (certificate_id,))
    if not c: raise HTTPException(404, "Certificate not found")
    if c["user_id"] != user_id: raise HTTPException(403, "You do not have access to this certificate")
    buff = BytesIO(); pdf = canvas.Canvas(buff, pagesize=landscape(A4)); w, h = landscape(A4); pdf.setTitle(f"VidyaDhara Certificate - {c['certificate_no']}"); pdf.setLineWidth(5); pdf.rect(30, 30, w - 60, h - 60)
    pdf.setFont("Helvetica-Bold", 30); pdf.drawCentredString(w/2, h-115, "VidyaDhara"); pdf.setFont("Helvetica", 14); pdf.drawCentredString(w/2, h-145, "Certificate of Learning")
    pdf.setFont("Helvetica-Bold", 25); pdf.drawCentredString(w/2, h-205, c["user_name"]); pdf.setFont("Helvetica", 15); pdf.drawCentredString(w/2, h-235, "has successfully completed")
    pdf.setFont("Helvetica-Bold", 20); pdf.drawCentredString(w/2, h-275, c["course_title"]); pdf.setFont("Helvetica", 11); pdf.drawCentredString(w/2, h-325, f"Certificate No: {c['certificate_no']} • Issued: {c['issued_at'][:10]}")
    pdf.setFont("Helvetica-Oblique", 10); pdf.drawCentredString(w/2, 74, "VidyaDhara • Indian Knowledge Systems learning platform"); pdf.save()
    return Response(buff.getvalue(), media_type="application/pdf", headers={"Content-Disposition": f"attachment; filename={c['certificate_no']}.pdf"})


def offline_answer(message: str) -> str:
    q = message.lower()
    answers = {
        "zero": "Śūnya (zero) is best studied as part of the historical development of positional arithmetic. Brahmagupta's 7th-century work gives explicit arithmetic rules involving zero; the broader history spans multiple traditions and stages.",
        "shunya": "Śūnya (zero) is best studied as part of the historical development of positional arithmetic. Brahmagupta's 7th-century work gives explicit arithmetic rules involving zero; the broader history spans multiple traditions and stages.",
        "panini": "Pāṇini's Aṣṭādhyāyī is a compact rule system for Sanskrit grammar. It is useful to compare its formal structure with computational grammars, but it should not be treated as literally identical to a modern programming language.",
        "aryabhata": "Āryabhaṭa's Aryabhaṭīya uses mathematical astronomy and a moving-boat analogy to explain apparent stellar motion relative to an observer. The useful modern comparison is reference-frame reasoning.",
        "wootz": "Wootz refers to a South Asian crucible-steel tradition. Modern materials science can analyze composition, processing and microstructure, but historical production methods varied and should be described from the evidence available for a particular site or text.",
        "tridosha": "Tridoṣa is a classical Ayurvedic framework using vāta, pitta and kapha. In VidyaDhara it is presented as a historical medical model and should be kept distinct from modern biomedical physiology.",
        "sanskrit": "Use the Sanskrit Guide for vowels, consonants and IAST. Long vowels are marked with macrons: ā ī ū. Retroflex sounds use dots: ṭ ḍ ṇ ṣ. Anusvāra is ṃ and visarga is ḥ.",
        "certificate": "VidyaDhara issues a certificate when all lessons in a course are completed. The record includes a unique certificate number and a PDF download.",
    }
    for key, answer in answers.items():
        if key in q: return answer
    return "I can help with IKS topics, scholars, Sanskrit reading, courses, quizzes, learning paths, history, and modern comparisons. For live, model-powered answers, add your GROQ_API_KEY; without it, I will stay within the built-in curated knowledge base."


def retrieve_context(message: str) -> str:
    words = [w for w in "".join(ch.lower() if ch.isalnum() or ch.isspace() else " " for ch in message).split() if len(w) >= 4][:12]
    if not words: return ""
    clauses = " OR ".join(["lower(title||' '||overview||' '||content||' '||concepts_json) LIKE ?"] * len(words))
    params = tuple(f"%{w}%" for w in words)
    matches = rows(f"SELECT id,title,source,historical_context,modern_parallel FROM lessons WHERE {clauses} ORDER BY position LIMIT 6", params)
    lines = []
    for m in matches:
        lines.append(f"Lesson: {m['title']}\nSource: {m.get('source','')}\nHistorical context: {m.get('historical_context','')}\nModern parallel: {m.get('modern_parallel','')}")
    return "\n\n".join(lines)


@app.get("/api/ai/history/{user_id}")
def ai_history(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id); return rows("SELECT id,role,content,created_at FROM chat_messages WHERE user_id=? ORDER BY created_at DESC LIMIT 40", (user_id,))[::-1]


@app.delete("/api/ai/history/{user_id}")
def ai_clear(user_id: str, authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id); conn = db(); conn.execute("DELETE FROM chat_messages WHERE user_id=?", (user_id,)); conn.commit(); conn.close(); return {"message": "Conversation cleared"}


@app.get("/api/sanskrit-guide")
def sanskrit_guide():
    return SANSKRIT_GUIDE


@app.post("/api/ai/ask")
async def ai_ask(data: AskIn, user_id: str = "demo-student", authorization: Optional[str] = Header(default=None)):
    auth_row_for_user(user_id, authorization); current_user(user_id)
    message = data.message.strip(); retrieved = retrieve_context(message)
    history = [m for m in data.history if m.get("role") in {"user", "assistant"} and isinstance(m.get("content"), str)][-12:]
    stored = rows("SELECT role,content FROM chat_messages WHERE user_id=? ORDER BY created_at DESC LIMIT 12", (user_id,))[::-1]
    if stored: history = stored
    system = (
        "You are Samvāda, the study mentor inside VidyaDhara. Answer the user's question helpfully, including general academic questions, while prioritizing Indian Knowledge Systems when relevant. "
        "Be historically careful. Separate primary-text evidence, scholarly reconstruction, traditional interpretation and modern analogy. Never invent a quote, date, attribution, experiment, or citation. "
        "When evidence is uncertain, say so. Do not present traditional medicine as a substitute for medical care. Use clear language and explain Sanskrit terms briefly. "
        "The user may be learning on a phone, so prefer structured answers with short headings, short paragraphs and bullets. "
        "Use valid Markdown headings (## heading), **bold** for emphasis, and a blank line between paragraphs and sections. "
        "Do not print Markdown markers as literal decoration. If a table is useful, use a proper Markdown table with one row per line; otherwise use bullets instead. "
        "Never compress multiple table rows into one line.\n\n"
        f"Curated IKS context:\n{retrieved or 'No directly matching lesson context.'}\n\nUser-provided context:\n{data.context.strip() or 'None'}"
    )
    messages = [{"role": "system", "content": system}, *history, {"role": "user", "content": message}]
    if GROQ_API_KEY:
        payload = {"model": GROQ_MODEL, "messages": messages, "temperature": 0.25, "max_completion_tokens": 3000}
        try:
            async with httpx.AsyncClient(timeout=90) as client:
                r = await client.post("https://api.groq.com/openai/v1/chat/completions", headers={"Authorization": f"Bearer {GROQ_API_KEY}", "Content-Type": "application/json"}, json=payload)
                r.raise_for_status(); body = r.json(); answer = ((body.get("choices") or [{}])[0].get("message") or {}).get("content", "").strip()
                if answer:
                    for _ in range(2):
                        choice = (body.get("choices") or [{}])[0]
                        if choice.get("finish_reason") != "length":
                            break
                        payload["messages"] = [
                            *messages,
                            {"role": "assistant", "content": answer},
                            {"role": "user", "content": "Continue from the exact point you stopped. Do not repeat earlier text. Finish the explanation and conclusion."},
                        ]
                        r = await client.post(
                            "https://api.groq.com/openai/v1/chat/completions",
                            headers={"Authorization": r.request.headers["Authorization"], "Content-Type": "application/json"},
                            json=payload,
                        )
                        r.raise_for_status()
                        body = r.json()
                        continuation = ((body.get("choices") or [{}])[0].get("message") or {}).get("content", "").strip()
                        if not continuation:
                            break
                        answer = f"{answer}\n\n{continuation}"
                    conn = db(); conn.execute("INSERT INTO chat_messages VALUES (?,?,?,?,?)", (uid("chat"), user_id, "user", message, now_iso())); conn.execute("INSERT INTO chat_messages VALUES (?,?,?,?,?)", (uid("chat"), user_id, "assistant", answer, now_iso())); conn.commit(); conn.close(); mark_study_day(user_id)
                    return {"answer": answer, "offline": False, "model": GROQ_MODEL, "sources": ["VidyaDhara curated lessons", "Groq model response"], "retrieved": bool(retrieved)}
        except Exception as exc:
            offline = offline_answer(message)
            return {"answer": offline + f"\n\n(Groq could not answer this request right now: {type(exc).__name__}.)", "offline": True, "model": GROQ_MODEL, "sources": ["VidyaDhara curated knowledge"]}
    offline = offline_answer(message)
    conn = db(); conn.execute("INSERT INTO chat_messages VALUES (?,?,?,?,?)", (uid("chat"), user_id, "user", message, now_iso())); conn.execute("INSERT INTO chat_messages VALUES (?,?,?,?,?)", (uid("chat"), user_id, "assistant", offline, now_iso())); conn.commit(); conn.close(); mark_study_day(user_id)
    return {"answer": offline, "offline": True, "model": None, "sources": ["VidyaDhara curated knowledge"]}


ensure_db()
