import json
import os
from pathlib import Path
import tempfile
import pytest
from fastapi.testclient import TestClient

ROOT = Path(__file__).resolve().parents[1]
TEST_DB_DIR = tempfile.TemporaryDirectory(prefix='iksphere-tests-')
os.environ['IKS_DB_PATH'] = str(Path(TEST_DB_DIR.name) / 'iksphere.db')

from app import main
from app.main import app, postgres_sql

@pytest.fixture()
def client():
    with TestClient(app) as c:
        yield c

def login(client, email='test@example.com', role='learner', name='Test Learner'):
    r = client.post('/api/auth/login', json={'name':name,'email':email,'role':role})
    assert r.status_code == 200, r.text
    return r.json()

def test_health_and_legacy_catalog_removed(client):
    health = client.get('/api/health')
    assert health.status_code == 200
    body = health.json()
    assert body['status'] == 'ok'
    assert body['version'].startswith('5.0')
    assert 'gemini' not in body
    assert client.get('/api/syllabus').status_code == 404

def test_requested_computing_syllabus_is_in_course_catalog_without_institution_details(client):
    response = client.get('/api/courses', params={'user_id':'demo-student'})
    assert response.status_code == 200
    course = next(item for item in response.json() if item['id'] == 'course_computing_ancient_india')
    assert course['title'] == 'Computing in Ancient India'
    assert len(course['lessons']) == 5
    lessons = {lesson['id']: lesson for lesson in course['lessons']}
    assert lessons['les_computing_intro']['title'] == 'Introduction to Computing in Ancient India'
    assert lessons['les_computing_binary']['title'] == "Binary Numbers and Pingala's Metrical Tradition"
    assert lessons['les_computing_katapayadi']['title'] == 'Katapayadi and Modern Hashing Comparisons'
    topics = [item['title'] for item in lessons['les_computing_katapayadi']['study_outline']]
    assert 'What is BNF notation?' in topics
    logic_topics = [item['title'] for item in lessons['les_computing_formal_logic']['study_outline']]
    assert 'Predicate calculus terminology' in logic_topics
    assert 'Introduction to Panini-Backus forms in formal-language syntax' in topics
    assert len({lesson['id'] for lesson in course['lessons']}) == 5
    assert all(lesson['study_outline'] for lesson in course['lessons'])
    serialized = json.dumps(course, ensure_ascii=False).lower()
    assert '2400ikst3b' not in serialized
    assert 'maeer' not in serialized


def test_seeded_course_lessons_include_expanded_explanations(client):
    response = client.get('/api/courses', params={'user_id':'demo-student'})
    assert response.status_code == 200
    seeded_courses = [
        course for course in response.json()
        if course['id'].startswith(('course_ganita','course_khagola','course_ayurveda',
            'course_sthapatya','course_rasayana','course_darshana','course_vyakarana',
            'course_krishi','course_jala','course_computing_ancient_india'))
    ]
    assert len(seeded_courses) == 10
    assert all(len(lesson['content']) >= 300 for course in seeded_courses for lesson in course['lessons'])


def test_home_dashboard_loads_and_includes_computing_course(client):
    user = login(client, email='home-syllabus@example.com', name='Home Syllabus')
    response = client.get(f"/api/home/{user['id']}")
    assert response.status_code == 200, response.text
    body = response.json()
    assert isinstance(body['daily_quiz'], dict)
    assert any(course['id'] == 'course_computing_ancient_india' for course in body['featured_courses'])

def test_postgres_query_adapter_translates_sqlite_helpers():
    assert postgres_sql('SELECT * FROM users WHERE id=?') == 'SELECT * FROM users WHERE id=%s'
    assert postgres_sql('INSERT OR IGNORE INTO users(id) VALUES (?)') == 'INSERT INTO users(id) VALUES (%s) ON CONFLICT DO NOTHING'
    assert postgres_sql('ALTER TABLE users ADD COLUMN provider TEXT') == 'ALTER TABLE users ADD COLUMN IF NOT EXISTS provider TEXT'
    assert postgres_sql('PRAGMA foreign_keys=ON') is None

def test_hosted_configuration_requires_firebase_and_disables_demo_role_selection(client, monkeypatch):
    monkeypatch.setattr(main, 'FIREBASE_PROJECT_ID', '')
    monkeypatch.setattr(main, 'SUPABASE_DB_URL', 'postgresql://hosted.example/iksphere')
    blocked_login = client.post('/api/auth/login', json={'name':'Untrusted Teacher','email':'fake@example.com','role':'teacher'})
    assert blocked_login.status_code == 403
    blocked_data = client.get('/api/courses', params={'user_id':'demo-student'})
    assert blocked_data.status_code == 401
    blocked_token = client.post('/api/auth/firebase', json={'id_token':'unverified-token-with-enough-length','name':'Unverified','email':'user@example.com','provider':'password'})
    assert blocked_token.status_code == 503

def test_guest_and_firebase_free_config(client):
    guest = client.post('/api/auth/guest', json={'name':'Phone Guest'})
    assert guest.status_code == 200
    user = guest.json()
    assert user['is_guest'] == 1
    assert user['provider'] == 'anonymous'
    # Local demo mode accepts a well-formed JWT payload without needing a Firebase project id.
    header = lambda value: {'Authorization': f'Bearer {value}'}
    import base64, json as _json
    payload = base64.urlsafe_b64encode(_json.dumps({'sub':'firebase-demo-uid'}).encode()).decode().rstrip('=')
    token = f'aaa.{payload}.bbb'
    fb = client.post('/api/auth/firebase', json={'id_token': token, 'name':'Firebase Demo', 'email':'firebase@example.com', 'provider':'password'}, headers=header(token))
    assert fb.status_code == 200, fb.text
    assert fb.json()['provider'] == 'password'

def test_courses_start_incomplete_and_progress(client):
    user = login(client)
    courses = client.get('/api/courses', params={'user_id':user['id']}).json()
    assert len(courses) >= 9
    assert not courses[0]['lessons'][0]['completed']
    lesson_id = courses[0]['lessons'][0]['id']
    complete = client.post(f'/api/lessons/{lesson_id}/progress', params={'user_id':user['id']}, json={'completed':True})
    assert complete.status_code == 200
    lesson = client.get(f'/api/lessons/{lesson_id}', params={'user_id':user['id']}).json()
    assert lesson['completed'] is True
    assert complete.json()['xp'] >= 20

def test_bookmark_and_notes_are_lesson_specific(client):
    user = login(client, email='library@example.com', name='Library User')
    c = client.get('/api/courses', params={'user_id':user['id']}).json()[0]
    first, second = c['lessons'][0]['id'], c['lessons'][1]['id']
    b = client.post('/api/bookmarks', params={'user_id':user['id']}, json={'lesson_id':second})
    assert b.status_code == 200 and b.json()['bookmarked'] is True
    bookmarks = client.get(f'/api/bookmarks/{user["id"]}').json()
    assert [x['lesson_id'] for x in bookmarks] == [second]
    n = client.post('/api/notes', params={'user_id':user['id']}, json={'lesson_id':first,'content':'Remember this carefully.'})
    assert n.status_code == 200
    notes = client.get(f'/api/notes/{user["id"]}').json()
    assert notes[0]['lesson_id'] == first
    d = client.delete(f'/api/notes/{n.json()["id"]}', params={'user_id':user['id']})
    assert d.status_code == 200

def test_quiz_safe_payload_and_scoring(client):
    user = login(client, email='quiz@example.com', name='Quiz User')
    quizzes = client.get('/api/quizzes', params={'user_id':user['id']}).json()
    assert quizzes and quizzes[0]['question_count'] >= 3
    q = client.get(f'/api/quizzes/{quizzes[0]["id"]}', params={'user_id':user['id']}).json()
    assert q['questions'] and all('answer_index' not in item for item in q['questions'])
    answers = [0 for _ in q['questions']]
    r = client.post(f'/api/quizzes/{q["id"]}/attempt', params={'user_id':user['id']}, json={'answers':answers})
    assert r.status_code == 200
    assert r.json()['total'] == len(answers)

def test_flashcard_spaced_review_is_per_user_and_schedules_ratings(client):
    learner = login(client, email='reviewer@example.com', name='Review Learner')
    other = login(client, email='other-reviewer@example.com', name='Other Reviewer')
    queue_url = '/api/flashcards/review'
    queue = client.get(queue_url, params={'user_id':learner['id']})
    assert queue.status_code == 200
    initial = queue.json()
    assert initial['total_count'] > 0
    assert initial['due_count'] == initial['total_count']
    card_id = initial['cards'][0]['id']

    reviewed = client.post(f'/api/flashcards/{card_id}/review', params={'user_id':learner['id']}, json={'rating':'good'})
    assert reviewed.status_code == 200
    assert reviewed.json()['interval_days'] == 1
    after_first = client.get(queue_url, params={'user_id':learner['id']}).json()
    assert after_first['due_count'] == initial['due_count'] - 1
    assert next(card for card in after_first['cards'] if card['id'] == card_id)['due'] == 0
    assert client.get(queue_url, params={'user_id':other['id']}).json()['due_count'] == initial['due_count']

    for card, rating, interval in zip(initial['cards'][1:5], ['again','hard','good','easy'], [1/144,1,1,4]):
        result = client.post(f'/api/flashcards/{card["id"]}/review', params={'user_id':learner['id']}, json={'rating':rating})
        assert result.status_code == 200
        assert result.json()['interval_days'] == interval

    second_review = client.post(f'/api/flashcards/{card_id}/review', params={'user_id':learner['id']}, json={'rating':'good'})
    assert second_review.status_code == 200
    assert second_review.json()['interval_days'] == 3
    assert second_review.json()['repetitions'] == 2
    assert client.post(f'/api/flashcards/{card_id}/review', params={'user_id':learner['id']}, json={'rating':'unknown'}).status_code == 422
    assert client.post('/api/flashcards/missing-card/review', params={'user_id':learner['id']}, json={'rating':'good'}).status_code == 404

def test_teacher_classrooms_assignments_and_student_join(client):
    teacher = login(client, email='class-teacher@example.com', role='teacher', name='Class Teacher')
    student = login(client, email='class-student@example.com', name='Class Student')
    course = client.get('/api/courses', params={'user_id':student['id']}).json()[0]
    quiz = client.get('/api/quizzes', params={'user_id':student['id']}).json()[0]

    created = client.post('/api/classrooms', params={'user_id':teacher['id']}, json={'name':'Ganita Cohort'})
    assert created.status_code == 200
    classroom = created.json()
    assert len(classroom['join_code']) == 8
    assert client.post('/api/classrooms', params={'user_id':student['id']}, json={'name':'Unauthorized'}).status_code == 403

    assert client.get(f"/api/classrooms/{classroom['id']}/assignments", params={'user_id':student['id']}).status_code == 403
    joined = client.post('/api/classrooms/join', params={'user_id':student['id']}, json={'join_code':classroom['join_code'].lower()})
    assert joined.status_code == 200
    assert client.post('/api/classrooms/join', params={'user_id':student['id']}, json={'join_code':'NOTACODE'}).status_code == 404

    assigned_course = client.post(f"/api/classrooms/{classroom['id']}/assignments", params={'user_id':teacher['id']}, json={'course_id':course['id']})
    assigned_quiz = client.post(f"/api/classrooms/{classroom['id']}/assignments", params={'user_id':teacher['id']}, json={'quiz_id':quiz['id']})
    assert assigned_course.status_code == 200
    assert assigned_quiz.status_code == 200
    assert client.post(f"/api/classrooms/{classroom['id']}/assignments", params={'user_id':teacher['id']}, json={'course_id':course['id'],'quiz_id':quiz['id']}).status_code == 422
    assignments = client.get(f"/api/classrooms/{classroom['id']}/assignments", params={'user_id':student['id']})
    assert assignments.status_code == 200
    assert {x['course_id'] for x in assignments.json() if x['course_id']} == {course['id']}
    assert {x['quiz_id'] for x in assignments.json() if x['quiz_id']} == {quiz['id']}

def test_teacher_create_course_quiz_path(client):
    teacher = login(client, email='teacher-owner@example.com', role='teacher', name='Teacher Owner')
    lesson = {'title':'Test IKS lesson','sanskrit':'ज्ञानम्','iast':'jñānam','overview':'Know the term.','content':'A teacher-authored IKS lesson.','english_meaning':'Knowledge.','historical_context':'Historical study context.','modern_parallel':'Modern comparison kept separate.','source':'Teacher-authored source note','takeaways':['Understand the term.'],'word_by_word':['ज्ञान — jñāna — knowledge'],'practical':'Use it as a revision cue.','minutes':10,'difficulty':'Beginner','checkpoint':{'question':'What is this lesson about?','options':['Knowledge','Steel','Water','Astronomy'],'answer_index':0,'explanation':'ज्ञान means knowledge.'}}
    c = client.post('/api/courses', params={'user_id':teacher['id']}, json={'title':'IKS Teacher Course','category':'Philosophy','icon':'📘','level':'Beginner','duration':'1 week','description':'Teacher course','era':'Historical study','lessons':[lesson]})
    assert c.status_code == 200, c.text
    cid = c.json()['id']
    lesson_id = client.get(f'/api/courses/{cid}', params={'user_id':teacher['id']}).json()['lessons'][0]['id']
    p = client.post('/api/paths', params={'user_id':teacher['id']}, json={'title':'IKS Teacher Path','weeks':2,'course_ids':[cid]})
    assert p.status_code == 200, p.text
    q = client.post('/api/quizzes', params={'user_id':teacher['id']}, json={'course_id':cid,'lesson_id':lesson_id,'title':'Classroom topic checkpoint','description':'Checkpoint','questions':[{'question':'Pick IKS','options':['IKS','Other'],'answer_index':0,'explanation':'IKS is correct.'}]})
    assert q.status_code == 200, q.text
    assert client.get(f"/api/quizzes/{q.json()['id']}", params={'user_id':teacher['id']}).json()['lesson_id'] == lesson_id
    learner = login(client, email='learner2@example.com', name='Learner 2')
    non_teacher = client.post('/api/courses', params={'user_id':learner['id']}, json={'title':'Should fail','lessons':[]})
    assert non_teacher.status_code == 403

def test_platform_onboarding_and_tenant_resource_isolation(client):
    platform = login(client, email='platform-admin@example.com', name='Platform Admin')
    conn = main.db()
    try:
        conn.execute("UPDATE users SET role='super_admin' WHERE id=?", (platform['id'],))
        conn.commit()
    finally:
        conn.close()

    assert client.get('/api/platform/dashboard', params={'user_id':platform['id']}).status_code == 200
    first = client.post('/api/institutions', params={'user_id':platform['id']}, json={
        'name':'College A', 'short_name':'college-a', 'admin_name':'Admin A',
        'admin_email':'admin-a@example.edu', 'plan_id':'starter',
    })
    second = client.post('/api/institutions', params={'user_id':platform['id']}, json={
        'name':'College B', 'short_name':'college-b', 'admin_name':'Admin B',
        'admin_email':'admin-b@example.edu', 'plan_id':'starter',
    })
    assert first.status_code == 200, first.text
    assert second.status_code == 200, second.text

    admins = [
        login(client, email='admin-a@example.edu', name='Admin A'),
        login(client, email='admin-b@example.edu', name='Admin B'),
    ]
    assert all(admin['role'] == 'institution_admin' for admin in admins)
    teachers, students = [], []
    for index, admin in enumerate(admins):
        suffix = 'a' if index == 0 else 'b'
        institution_id = admin['institution_id']
        for role, name in [('teacher', f'Teacher {suffix.upper()}'), ('student', f'Student {suffix.upper()}')]:
            email = f'{role}-{suffix}@example.edu'
            invite = client.post(f'/api/institutions/{institution_id}/members',
                params={'user_id':admin['id']}, json={'name':name, 'email':email, 'role':role})
            assert invite.status_code == 200, invite.text
            account = login(client, email=email, name=name)
            assert account['role'] == role
            if role == 'teacher':
                teachers.append(account)
            else:
                students.append(account)

    courses, quizzes, classrooms, classroom_codes = [], [], [], []
    for index, teacher in enumerate(teachers):
        suffix = 'A' if index == 0 else 'B'
        lesson_count = 2 if index == 0 else 1
        course = client.post('/api/courses', params={'user_id':teacher['id']}, json={
            'title':f'Private course {suffix}',
            'lessons':[{'title':f'Private lesson {suffix} {number}'} for number in range(lesson_count)],
        })
        assert course.status_code == 200, course.text
        courses.append(course.json()['id'])
        quiz = client.post('/api/quizzes', params={'user_id':teacher['id']}, json={
            'course_id':courses[-1], 'title':f'Private quiz {suffix}',
            'questions':[{'question':'Tenant question?', 'options':['A','B'], 'answer_index':0}],
        })
        assert quiz.status_code == 200, quiz.text
        quizzes.append(quiz.json()['id'])
        classroom = client.post('/api/classrooms', params={'user_id':teacher['id']}, json={'name':f'Class {suffix}'})
        assert classroom.status_code == 200, classroom.text
        classrooms.append(classroom.json()['id'])
        classroom_codes.append(classroom.json()['join_code'])

    conn = main.db()
    try:
        conn.execute("INSERT INTO flashcards VALUES (?,?,?,?,?,?,?)",
            ('private-card-b', courses[1], 'Test', 'B-only prompt', 'B-only answer', '', 0))
        conn.commit()
    finally:
        conn.close()

    assert client.get(f'/api/courses/{courses[0]}', params={'user_id':students[0]['id']}).status_code == 200
    assert client.get(f'/api/courses/{courses[0]}', params={'user_id':students[1]['id']}).status_code == 404
    assert client.get(f'/api/quizzes/{quizzes[0]}', params={'user_id':students[0]['id']}).status_code == 200
    assert client.get(f'/api/quizzes/{quizzes[0]}', params={'user_id':students[1]['id']}).status_code == 404
    cards_a = client.get('/api/flashcards', params={'user_id':students[0]['id']}).json()
    cards_b = client.get('/api/flashcards', params={'user_id':students[1]['id']}).json()
    assert 'private-card-b' not in {card['id'] for card in cards_a}
    assert 'private-card-b' in {card['id'] for card in cards_b}
    assert client.get(f'/api/classrooms/{classrooms[0]}/assignments',
        params={'user_id':students[1]['id']}).status_code == 404
    assert client.post('/api/classrooms/join', params={'user_id':students[0]['id']},
        json={'join_code':classroom_codes[0]}).status_code == 200
    task = client.post(f"/api/classrooms/{classrooms[0]}/tasks",
        params={'user_id':teachers[0]['id']},
        json={'title':'Reflection', 'instructions':'Explain the core idea.', 'points':20})
    assert task.status_code == 200, task.text
    task_id = task.json()['id']
    assert client.get(f"/api/classrooms/{classrooms[0]}/tasks",
        params={'user_id':students[0]['id']}).status_code == 200
    assert client.get(f"/api/classrooms/{classrooms[0]}/tasks",
        params={'user_id':students[1]['id']}).status_code == 404
    submission = client.post(f"/api/classrooms/{classrooms[0]}/tasks/{task_id}/submissions",
        params={'user_id':students[0]['id']}, json={'body':'A considered answer'})
    assert submission.status_code == 200, submission.text
    graded = client.put(f"/api/classrooms/{classrooms[0]}/tasks/{task_id}/submissions/{students[0]['id']}",
        params={'user_id':teachers[0]['id']}, json={'grade':18,'feedback':'Good reasoning.'})
    assert graded.status_code == 200, graded.text
    task_list = client.get(f"/api/classrooms/{classrooms[0]}/tasks",
        params={'user_id':students[0]['id']}).json()
    assert task_list[0]['submission']['grade'] == 18
    scheduled = client.post(f"/api/classrooms/{classrooms[0]}/events",
        params={'user_id':teachers[0]['id']},
        json={'title':'A live class', 'starts_at':main.now_iso(), 'meeting_url':'https://meet.example/class-a'})
    assert scheduled.status_code == 200, scheduled.text
    event_id = scheduled.json()['id']
    assert len(client.get(f"/api/classrooms/{classrooms[0]}/events",
        params={'user_id':students[0]['id']}).json()) == 1
    assert client.get(f"/api/classrooms/{classrooms[0]}/events",
        params={'user_id':students[1]['id']}).status_code == 404
    marked = client.put(f"/api/classrooms/{classrooms[0]}/events/{event_id}/attendance",
        params={'user_id':teachers[0]['id']},
        json={'entries':[{'user_id':students[0]['id'], 'status':'present'}]})
    assert marked.status_code == 200, marked.text
    attendance = client.get(f"/api/classrooms/{classrooms[0]}/events/{event_id}/attendance",
        params={'user_id':students[0]['id']})
    assert attendance.status_code == 200 and attendance.json()[0]['status'] == 'present'
    assert client.post('/api/announcements', params={'user_id':admins[0]['id']},
        json={'title':'College A notice', 'body':'Private institutional update'}).status_code == 200
    notices_a = client.get('/api/announcements', params={'user_id':students[0]['id']}).json()
    notices_b = client.get('/api/announcements', params={'user_id':students[1]['id']}).json()
    assert any(item['title'] == 'College A notice' for item in notices_a)
    assert not any(item['title'] == 'College A notice' for item in notices_b)
    assert client.post('/api/announcements', params={'user_id':teachers[0]['id']},
        json={'title':'Class notice', 'body':'For A only', 'classroom_id':classrooms[0]}).status_code == 200
    discussion = client.post(f"/api/classrooms/{classrooms[0]}/discussions",
        params={'user_id':students[0]['id']}, json={'body':'Question for our class'})
    assert discussion.status_code == 200, discussion.text
    assert len(client.get(f"/api/classrooms/{classrooms[0]}/discussions",
        params={'user_id':students[0]['id']}).json()) == 1
    assert client.get(f"/api/classrooms/{classrooms[0]}/discussions",
        params={'user_id':students[1]['id']}).status_code == 404
    resource = client.post(f"/api/courses/{courses[0]}/resources",
        params={'user_id':teachers[0]['id']},
        json={'title':'Course video', 'url':'https://example.edu/video', 'kind':'video'})
    assert resource.status_code == 200, resource.text
    assert len(client.get(f"/api/courses/{courses[0]}/resources",
        params={'user_id':students[0]['id']}).json()) == 1
    assert client.get(f"/api/courses/{courses[0]}/resources",
        params={'user_id':students[1]['id']}).status_code == 404
    bulk = client.post(f"/api/institutions/{students[0]['institution_id']}/members/bulk",
        params={'user_id':admins[0]['id']},
        json={'members':[
            {'name':'Bulk Invitee','email':'bulk-invitee@example.edu','role':'student'},
            {'name':'Duplicate Student','email':'student-a@example.edu','role':'student'},
        ]})
    assert bulk.status_code == 200, bulk.text
    assert bulk.json()['created'] == 1 and bulk.json()['failed'] == 1
    assert client.get('/api/institutions', params={'user_id':students[0]['id']}).status_code == 403
    conn = main.db()
    try:
        conn.execute("INSERT INTO plans(id,name,student_limit,teacher_limit,course_limit,active) VALUES (?,?,?,?,?,1)",
            ('test-zero', 'Test zero', 0, 0, 0))
        conn.commit()
    finally:
        conn.close()
    assert client.put(f"/api/institutions/{students[0]['institution_id']}/plan",
        params={'user_id':platform['id'], 'plan_id':'test-zero'}).status_code == 409
    dashboard_a = client.get(f"/api/users/{students[0]['id']}/dashboard").json()
    dashboard_b = client.get(f"/api/users/{students[1]['id']}/dashboard").json()
    assert dashboard_a['stats']['total_lessons'] == dashboard_b['stats']['total_lessons'] + 1
    analytics_a = client.get(f"/api/analytics/{students[0]['id']}").json()
    analytics_b = client.get(f"/api/analytics/{students[1]['id']}").json()
    assert sum(item['total'] for item in analytics_a['by_category']) == sum(item['total'] for item in analytics_b['by_category']) + 1
    assert client.get(f"/api/institutions/{students[1]['institution_id']}/members",
        params={'user_id':admins[0]['id']}).status_code == 404
    suspended = client.put(f"/api/institutions/{students[0]['institution_id']}/status",
        params={'user_id':platform['id']}, json={'status':'suspended'})
    assert suspended.status_code == 200
    assert client.get('/api/courses', params={'user_id':students[0]['id']}).status_code == 403
    resumed = client.put(f"/api/institutions/{students[0]['institution_id']}/status",
        params={'user_id':platform['id']}, json={'status':'active'})
    assert resumed.status_code == 200
    conn = main.db()
    try:
        conn.execute("UPDATE institutions SET trial_ends_at='2000-01-01T00:00:00+00:00' WHERE id=?",
            (students[1]['institution_id'],))
        conn.commit()
    finally:
        conn.close()
    assert client.get('/api/courses', params={'user_id':students[1]['id']}).status_code == 403


def test_teacher_quiz_drafts_settings_permissions_and_attempt_rules(client):
    teacher = login(client, email='quiz-author@example.com', role='teacher', name='Quiz Author')
    student = login(client, email='quiz-taker@example.com', name='Quiz Taker')
    course = client.get('/api/courses', params={'user_id':teacher['id']}).json()[0]
    payload = {
        'course_id':course['id'],
        'title':'Teacher-configured course review',
        'description':'A teacher-controlled checkpoint',
        'questions':[
            {'question':'Which subject studies number?','options':['Ganita','Ayurveda'],'answer_index':0,'explanation':'Ganita is mathematics.'},
            {'question':'Which is a water tradition?','options':['Jala Prabandhana','Vyakarana'],'answer_index':0,'explanation':'Jala Prabandhana concerns water management.'},
        ],
        'settings':{
            'time_limit_minutes':5,
            'marks_per_question':2,
            'passing_marks':2,
            'negative_marking':1,
            'max_attempts':1,
            'randomize_questions':False,
            'randomize_options':False,
            'show_results_immediately':True,
            'show_correct_answers':False,
            'show_explanations':False,
            'allow_retake':False,
        },
        'published':False,
    }
    created = client.post('/api/quizzes', params={'user_id':teacher['id']}, json=payload)
    assert created.status_code == 200, created.text
    quiz_id = created.json()['id']
    assert quiz_id not in {quiz['id'] for quiz in client.get('/api/quizzes', params={'user_id':teacher['id']}).json()}
    assert quiz_id in {quiz['id'] for quiz in client.get('/api/quizzes', params={'user_id':teacher['id'],'include_drafts':True}).json()}
    assert quiz_id not in {quiz['id'] for quiz in client.get('/api/quizzes', params={'user_id':student['id']}).json()}
    assert client.get(f'/api/quizzes/{quiz_id}', params={'user_id':student['id']}).status_code == 404
    assert client.get(f'/api/quizzes/{quiz_id}', params={'user_id':login(client, email='other-author@example.com', role='teacher')['id']}).status_code == 404

    update = client.put(f'/api/quizzes/{quiz_id}', params={'user_id':teacher['id']}, json={**payload, 'published':True})
    assert update.status_code == 200, update.text
    quiz = client.get(f'/api/quizzes/{quiz_id}', params={'user_id':student['id']}).json()
    assert quiz['settings']['time_limit_minutes'] == 5
    assert quiz['settings']['marks_per_question'] == 2
    assert all('answer_index' not in question and 'explanation' not in question for question in quiz['questions'])

    correct_text = {
        'Which subject studies number?':'Ganita',
        'Which is a water tradition?':'Jala Prabandhana',
    }
    answers = []
    for question in quiz['questions']:
        display_index = question['options'].index(correct_text[question['question']])
        original_index = question['option_order'][display_index]
        answers.append(
            original_index if question['question']=='Which subject studies number?'
            else question['option_order'][(display_index + 1) % len(question['option_order'])]
        )
    attempt = client.post(
        f'/api/quizzes/{quiz_id}/attempt',
        params={'user_id':student['id']},
        json={
            'answers':answers,
            'question_ids':[question['id'] for question in quiz['questions']],
        },
    )
    assert attempt.status_code == 200, attempt.text
    assert attempt.json()['score'] == 1
    assert attempt.json()['total'] == 4
    assert attempt.json()['percent'] == 25
    review = attempt.json()['review']
    by_question = {item['question']:item for item in review}
    assert by_question['Which subject studies number?']['correct'] is True
    assert by_question['Which subject studies number?']['correct_answer'] == 'Ganita'
    assert by_question['Which is a water tradition?']['correct'] is False
    assert by_question['Which is a water tradition?']['selected_index'] == 1
    assert by_question['Which is a water tradition?']['correct_answer'] == 'Jala Prabandhana'
    assert all('explanation' not in item for item in review)
    assert client.post(
        f'/api/quizzes/{quiz_id}/attempt',
        params={'user_id':student['id']},
        json={'answers':[0, 0], 'question_ids':[question['id'] for question in quiz['questions']]},
    ).status_code == 409
    assert client.put(f'/api/quizzes/{quiz_id}', params={'user_id':teacher['id']}, json={**payload, 'published':False}).status_code == 409


def test_teacher_can_schedule_assessment_results(client):
    teacher = login(client, email='delayed-quiz-author@example.com', role='teacher', name='Quiz Author')
    student = login(client, email='delayed-quiz-taker@example.com', name='Quiz Taker')
    course = client.get('/api/courses', params={'user_id':teacher['id']}).json()[0]
    payload = {
        'course_id':course['id'],
        'title':'Scheduled results assessment',
        'questions':[{'question':'Pick Ganita','options':['Ganita','Yoga'],'answer_index':0}],
        'settings':{'randomize_questions':False,'randomize_options':False,'show_results_immediately':False},
    }
    created = client.post('/api/quizzes', params={'user_id':teacher['id']}, json=payload)
    quiz_id = created.json()['id']
    question = client.get(f'/api/quizzes/{quiz_id}', params={'user_id':student['id']}).json()['questions'][0]
    result = client.post(f'/api/quizzes/{quiz_id}/attempt', params={'user_id':student['id']}, json={'answers':[0],'question_ids':[question['id']]})
    assert result.status_code == 200
    assert result.json()['results_released'] is False
    assert result.json()['score'] is None and result.json()['review'] == []


def test_removed_quiz_titles_are_hidden_without_deleting_history(client):
    teacher = login(client, email='hidden-quiz-teacher@example.com', role='teacher', name='Quiz Teacher')
    student = login(client, email='hidden-quiz-student@example.com', name='Quiz Student')
    course = client.get('/api/courses', params={'user_id':teacher['id']}).json()[0]
    titles = ['IKS Teacher Quiz', 'Configurable IKS quiz', 'Delayed results quiz']
    for title in titles:
        response = client.post('/api/quizzes', params={'user_id':teacher['id']}, json={
            'course_id':course['id'], 'title':title,
            'questions':[{'question':'Pick the answer','options':['A','B'],'answer_index':0}],
        })
        assert response.status_code == 422

    hidden_ids = [f'hidden_quiz_{index}' for index in range(len(titles))]
    conn = main.db()
    try:
        for quiz_id, title in zip(hidden_ids, titles):
            conn.execute(
                """INSERT INTO quizzes(id,course_id,title,description,created_by,created_at,settings_json,published)
                VALUES (?,?,?,?,?,?,?,1)""",
                (quiz_id, course['id'], title, '', teacher['id'], main.now_iso(), '{}'),
            )
        conn.commit()
    finally:
        conn.close()
    visible_ids = {quiz['id'] for quiz in client.get('/api/quizzes', params={'user_id':student['id']}).json()}
    assert not (visible_ids & set(hidden_ids))
    for quiz_id in hidden_ids:
        assert client.get(f'/api/quizzes/{quiz_id}', params={'user_id':student['id']}).status_code == 404
        assert client.post(f'/api/quizzes/{quiz_id}/attempt', params={'user_id':student['id']},
            json={'answers':[0]}).status_code == 404


def test_classroom_assignment_and_submission_file_roundtrip(client, monkeypatch, tmp_path):
    monkeypatch.setattr(main, 'ASSIGNMENT_UPLOAD_DIR', tmp_path / 'assignment-files')
    teacher = login(client, email='file-assignment-teacher@example.com', role='teacher', name='File Teacher')
    student = login(client, email='file-assignment-student@example.com', name='File Student')
    other_student = login(client, email='file-assignment-other@example.com', name='Other Student')
    classroom = client.post('/api/classrooms', params={'user_id':teacher['id']},
        json={'name':'File Assignment Class'}).json()
    assert client.post('/api/classrooms/join', params={'user_id':student['id']},
        json={'join_code':classroom['join_code']}).status_code == 200
    task = client.post(f"/api/classrooms/{classroom['id']}/tasks",
        params={'user_id':teacher['id']},
        json={'title':'Read the worksheet','instructions':'Complete and return the attached worksheet.'}).json()
    base = f"/api/classrooms/{classroom['id']}/tasks/{task['id']}"
    attached = client.post(f'{base}/files', params={'user_id':teacher['id']},
        files={'upload':('worksheet.pdf',b'%PDF-1.4 worksheet','application/pdf')})
    assert attached.status_code == 200, attached.text
    student_tasks = client.get(f"/api/classrooms/{classroom['id']}/tasks",
        params={'user_id':student['id']}).json()
    assert student_tasks[0]['files'][0]['original_name'] == 'worksheet.pdf'
    task_download = client.get(f"{base}/files/{attached.json()['id']}",
        params={'user_id':student['id']})
    assert task_download.status_code == 200 and task_download.content == b'%PDF-1.4 worksheet'
    assert task_download.headers['content-disposition'].startswith('attachment;')
    assert client.get(f"{base}/files/{attached.json()['id']}",
        params={'user_id':other_student['id']}).status_code == 404

    submission = client.post(f"{base}/submissions/files", params={'user_id':student['id']},
        data={'body':'I completed the worksheet by hand.'},
        files=[('files',('answer.txt',b'my written explanation','text/plain')),
               ('files',('scan.png',b'fake image bytes','image/png'))])
    assert submission.status_code == 200, submission.text
    staff_tasks = client.get(f"/api/classrooms/{classroom['id']}/tasks",
        params={'user_id':teacher['id']}).json()
    student_files = staff_tasks[0]['submissions'][0]['files']
    assert {file['original_name'] for file in student_files} == {'answer.txt','scan.png'}
    submission_download = client.get(f"{base}/submissions/files/{student_files[0]['id']}",
        params={'user_id':teacher['id']})
    assert submission_download.status_code == 200
    assert client.get(f"{base}/submissions/files/{student_files[0]['id']}",
        params={'user_id':other_student['id']}).status_code == 404


def test_classroom_report_tracks_quiz_attempt_and_student_search(client):
    teacher = login(client, email='report-teacher@example.com', role='teacher', name='Report Teacher')
    student = login(client, email='report-student@example.com', name='Report Student')
    course = client.get('/api/courses', params={'user_id':student['id']}).json()[0]
    quiz_id = f"quiz_practice_{course['lessons'][0]['id']}"
    classroom = client.post('/api/classrooms', params={'user_id':teacher['id']}, json={'name':'Report Cohort'}).json()
    assert client.post(f"/api/classrooms/{classroom['id']}/assignments", params={'user_id':teacher['id']}, json={'quiz_id':quiz_id}).status_code == 200
    assert client.post('/api/classrooms/join', params={'user_id':student['id']}, json={'join_code':classroom['join_code']}).status_code == 200

    quiz = client.get(f'/api/quizzes/{quiz_id}', params={'user_id':student['id'], 'count':2}).json()
    attempt = client.post(
        f'/api/quizzes/{quiz_id}/attempt',
        params={'user_id':student['id']},
        json={
            'answers':[0, 0],
            'question_ids':[question['id'] for question in quiz['questions']],
            'elapsed_seconds':73,
            'classroom_assignment_id':client.get(
                f"/api/classrooms/{classroom['id']}/assignments",
                params={'user_id':teacher['id']},
            ).json()[0]['id'],
        },
    )
    assert attempt.status_code == 200, attempt.text

    report = client.get(f"/api/classrooms/{classroom['id']}/report", params={'user_id':teacher['id']})
    assert report.status_code == 200, report.text
    assert report.json()['students'][0]['name'] == 'Report Student'
    activity = next(item for item in report.json()['activities'] if item['activity_type'] == 'quiz')
    assert activity['status'] == 'completed'
    assert activity['elapsed_seconds'] == 73
    assert activity['attempt_id'] == attempt.json()['attempt_id']
    filtered = client.get(f"/api/classrooms/{classroom['id']}/report", params={'user_id':teacher['id'], 'search':'no matching student'})
    assert filtered.status_code == 200 and filtered.json()['students'] == []
    assert client.get(f"/api/classrooms/{classroom['id']}/report", params={'user_id':student['id']}).status_code == 403

def test_practice_banks_have_unique_questions_and_prioritize_unseen(client):
    user = login(client, email='unique-practice@example.com', name='Practice Learner')
    course = client.get('/api/courses', params={'user_id':user['id']}).json()[0]
    lesson_id = course['lessons'][0]['id']
    topic_quiz_id = f'quiz_practice_{lesson_id}'

    first = client.get(f'/api/quizzes/{topic_quiz_id}', params={'user_id':user['id'], 'count':10}).json()
    assert first['unseen_question_count'] == 50
    assert len(first['questions']) == 10
    assert len({question['question'] for question in first['questions']}) == 10
    submitted = client.post(
        f'/api/quizzes/{topic_quiz_id}/attempt',
        params={'user_id':user['id']},
        json={'answers':[0] * 10, 'question_ids':[question['id'] for question in first['questions']]},
    )
    assert submitted.status_code == 200, submitted.text
    next_set = client.get(f'/api/quizzes/{topic_quiz_id}', params={'user_id':user['id'], 'count':10}).json()
    assert not ({question['id'] for question in first['questions']} & {question['id'] for question in next_set['questions']})

    course_bank = client.get(
        f"/api/quizzes/quiz_practice_course_{course['id']}",
        params={'user_id':user['id'], 'count':200},
    )
    assert course_bank.status_code == 200, course_bank.text
    assert len(course_bank.json()['questions']) == 200
    assert len({question['question'] for question in course_bank.json()['questions']}) == 200

def test_certificate_issued_after_course_completion(client):
    user = login(client, email='cert@example.com', name='Certificate User')
    course = client.get('/api/courses', params={'user_id':user['id']}).json()[0]
    for lesson in course['lessons']:
        r = client.post(f'/api/lessons/{lesson["id"]}/progress', params={'user_id':user['id']}, json={'completed':True})
        assert r.status_code == 200
    achievements = client.get(f'/api/achievements/{user["id"]}').json()
    assert achievements['certificates']
    cert_id = achievements['certificates'][0]['id']
    pdf = client.get(f'/api/certificates/{cert_id}/pdf', params={'user_id':user['id']})
    assert pdf.status_code == 200 and pdf.headers['content-type'].startswith('application/pdf')

def test_sanskrit_guide_and_ai_offline(client):
    user = login(client, email='ai@example.com', name='AI User')
    guide = client.get('/api/sanskrit-guide')
    assert guide.status_code == 200
    assert any(item[0]=='अ' for item in guide.json()['vowels'])
    r = client.post('/api/ai/ask', params={'user_id':user['id']}, json={'message':'Explain shunya in IKS'})
    assert r.status_code == 200
    body = r.json()
    assert 'zero' in body['answer'].lower() or 'śūnya' in body['answer'].lower()
    assert body['model'] is None or isinstance(body['model'], str)

def test_search_excludes_retired_content(client):
    user = login(client, email='search@example.com', name='Search User')
    r = client.get('/api/search', params={'q':'college syllabus','user_id':user['id']})
    assert r.status_code == 200
    body = r.json()
    assert body['courses'] == [] and body['lessons'] == [] and body['scholars'] == []

def test_firebase_guest_and_bearer_binding(client):
    import base64, json as _json
    payload = base64.urlsafe_b64encode(_json.dumps({'sub':'fb-guest-uid'}).encode()).decode().rstrip('=')
    token = f'a.{payload}.b'
    guest = client.post('/api/auth/firebase', json={'id_token':token,'name':'Firebase Guest','email':'','provider':'anonymous'}, headers={'Authorization':f'Bearer {token}'})
    assert guest.status_code == 200, guest.text
    assert guest.json()['is_guest'] == 1
    registered = guest.json()

    other = login(client, email='other-user@example.com', name='Other User')
    blocked = client.get(f'/api/users/{other["id"]}/dashboard', headers={'Authorization':f'Bearer {token}'})
    assert blocked.status_code == 403
    assert client.get(f'/api/users/{registered["id"]}/dashboard', headers={'Authorization':f'Bearer {token}'}).status_code == 200


def test_course_contains_source_metadata_not_retired_syllabus(client):
    user = login(client, email='sources@example.com', name='Sources User')
    courses = client.get('/api/courses', params={'user_id':user['id']}).json()
    assert courses
    assert any(c.get('texts') for c in courses)
    assert all('Data Visualization' not in (c.get('title','') + c.get('description','')) for c in courses)
