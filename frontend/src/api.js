const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '');
let session = { token: '', userId: '' };

export function setApiSession(next) { session = { token: next?.token || '', userId: next?.userId || '' }; }
export function clearApiSession() { session = { token: '', userId: '' }; }

const OFFLINE_CACHE_PREFIX = 'iksphere_cache:';
const OFFLINE_CACHEABLE = /^(\/courses(?:\/[^?]+)?|\/home\/[^?]+|\/paths|\/scholars|\/flashcards|\/quizzes(?:\/[^?]+)?|\/lessons\/[^?]+|\/sanskrit-guide)/;

function cacheKey(path) { return `${OFFLINE_CACHE_PREFIX}${path}`; }
function canCache(path, options) { return (options.method || 'GET').toUpperCase() === 'GET' && OFFLINE_CACHEABLE.test(path); }

async function request(path, options = {}) {
  const headers = { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) };
  if (session.token) headers.Authorization = `Bearer ${session.token}`;
  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  } catch (error) {
    if (canCache(path, options)) {
      try {
        const cached = localStorage.getItem(cacheKey(path));
        if (cached) return JSON.parse(cached);
      } catch {}
    }
    throw new Error('Network unavailable. Please reconnect and try again.');
  }
  const contentType = res.headers.get('content-type') || '';
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try { const data = contentType.includes('json') ? await res.json() : { detail: await res.text() }; if (Array.isArray(data.detail)) message = data.detail.map(x => x.msg || 'Invalid input').join('; '); else message = data.detail || data.message || message; } catch {}
    throw new Error(message);
  }
  if (contentType.includes('application/pdf')) return res.blob();
  const payload = res.status === 204 ? null : await res.json();
  if (payload !== null && canCache(path, options)) { try { localStorage.setItem(cacheKey(path), JSON.stringify(payload)); } catch {} }
  return payload;
}

async function formRequest(path, formData) {
  const headers = { Accept: 'application/json' };
  if (session.token) headers.Authorization = `Bearer ${session.token}`;
  const res = await fetch(`${API_BASE}${path}`, { method: 'POST', headers, body: formData });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      message = data.detail || data.message || message;
    } catch {}
    throw new Error(message);
  }
  return res.json();
}

async function downloadRequest(path) {
  const headers = {};
  if (session.token) headers.Authorization = `Bearer ${session.token}`;
  const res = await fetch(`${API_BASE}${path}`, { headers });
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      message = data.detail || data.message || message;
    } catch {}
    throw new Error(message);
  }
  const disposition = res.headers.get('content-disposition') || '';
  const filenameMatch = disposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/i);
  const filename = filenameMatch ? decodeURIComponent(filenameMatch[1].replace(/"$/, '')) : 'assignment-file';
  const url = URL.createObjectURL(await res.blob());
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.append(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export const api = {
  health: () => request('/health'),
  config: () => request('/config'),
  authGuest: name => request('/auth/guest', { method:'POST', body:JSON.stringify({name}) }),
  authFirebase: body => request('/auth/firebase', { method:'POST', body:JSON.stringify(body) }),
  authLocal: body => request('/auth/login', { method:'POST', body:JSON.stringify(body) }),
  platformDashboard: id => request(`/platform/dashboard?user_id=${encodeURIComponent(id)}`),
  plans: id => request(`/plans?user_id=${encodeURIComponent(id)}`),
  institutions: id => request(`/institutions?user_id=${encodeURIComponent(id)}`),
  createInstitution: (body,id) => request(`/institutions?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  updateInstitutionStatus: (institutionId,status,id) => request(`/institutions/${encodeURIComponent(institutionId)}/status?user_id=${encodeURIComponent(id)}`, {method:'PUT',body:JSON.stringify({status})}),
  updateInstitutionPlan: (institutionId,planId,id) => request(`/institutions/${encodeURIComponent(institutionId)}/plan?plan_id=${encodeURIComponent(planId)}&user_id=${encodeURIComponent(id)}`, {method:'PUT'}),
  myInstitution: id => request(`/institutions/me?user_id=${encodeURIComponent(id)}`),
  updateMyInstitution: (body,id) => request(`/institutions/me?user_id=${encodeURIComponent(id)}`, {method:'PUT',body:JSON.stringify(body)}),
  institutionDashboard: (institutionId,id) => request(`/institutions/${encodeURIComponent(institutionId)}/dashboard?user_id=${encodeURIComponent(id)}`),
  institutionMembers: (institutionId,id) => request(`/institutions/${encodeURIComponent(institutionId)}/members?user_id=${encodeURIComponent(id)}`),
  inviteInstitutionMember: (institutionId,body,id) => request(`/institutions/${encodeURIComponent(institutionId)}/members?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  bulkInviteInstitutionMembers: (institutionId,members,id) => request(`/institutions/${encodeURIComponent(institutionId)}/members/bulk?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify({members})}),
  departments: (institutionId,id) => request(`/institutions/${encodeURIComponent(institutionId)}/departments?user_id=${encodeURIComponent(id)}`),
  createDepartment: (institutionId,body,id) => request(`/institutions/${encodeURIComponent(institutionId)}/departments?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  programs: (institutionId,id) => request(`/institutions/${encodeURIComponent(institutionId)}/programs?user_id=${encodeURIComponent(id)}`),
  createProgram: (institutionId,body,id) => request(`/institutions/${encodeURIComponent(institutionId)}/programs?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  dashboard: id => request(`/users/${id}/dashboard?user_id=${encodeURIComponent(id)}`),
  home: id => request(`/home/${id}?user_id=${encodeURIComponent(id)}`),
  courses: (id, language='en') => request(`/courses?user_id=${encodeURIComponent(id)}&language=${encodeURIComponent(language)}`),
  course: (id, userId, language='en') => request(`/courses/${encodeURIComponent(id)}?user_id=${encodeURIComponent(userId)}&language=${encodeURIComponent(language)}`),
  translationCatalog: (id, language) => request(`/translations/catalog?user_id=${encodeURIComponent(id)}&language=${encodeURIComponent(language)}`),
  search: (q,id) => request(`/search?q=${encodeURIComponent(q)}&user_id=${encodeURIComponent(id)}`),
  paths: (id, language='en') => request(`/paths?user_id=${encodeURIComponent(id)}&language=${encodeURIComponent(language)}`),
  scholars: () => request('/scholars'),
  lesson: (id,userId,language='en') => request(`/lessons/${encodeURIComponent(id)}?user_id=${encodeURIComponent(userId)}&language=${encodeURIComponent(language)}`),
  progress: (id,completed,userId) => request(`/lessons/${id}/progress?user_id=${encodeURIComponent(userId)}`, {method:'POST',body:JSON.stringify({completed})}),
  bookmarks: id => request(`/bookmarks/${encodeURIComponent(id)}`),
  toggleBookmark: (lessonId,userId) => request(`/bookmarks?user_id=${encodeURIComponent(userId)}`, {method:'POST',body:JSON.stringify({lesson_id:lessonId})}),
  notes: id => request(`/notes/${encodeURIComponent(id)}`),
  createNote: (body,id) => request(`/notes?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  deleteNote: (noteId,id) => request(`/notes/${encodeURIComponent(noteId)}?user_id=${encodeURIComponent(id)}`, {method:'DELETE'}),
  quizzes: (id,language='en',includeDrafts=false) => request(`/quizzes?user_id=${encodeURIComponent(id)}&language=${encodeURIComponent(language)}&include_drafts=${includeDrafts}`),
  quiz: (id,userId,count,language='en') => request(`/quizzes/${encodeURIComponent(id)}?user_id=${encodeURIComponent(userId)}${count ? `&count=${encodeURIComponent(count)}` : ''}&language=${encodeURIComponent(language)}`),
  attempt: (id,answers,userId,questionIds,elapsedSeconds,classroomAssignmentId) => request(`/quizzes/${encodeURIComponent(id)}/attempt?user_id=${encodeURIComponent(userId)}`, {method:'POST',body:JSON.stringify({answers,question_ids:questionIds,elapsed_seconds:elapsedSeconds,classroom_assignment_id:classroomAssignmentId||null})}),
  createQuiz: (body,id) => request(`/quizzes?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  updateQuiz: (quizId,body,id) => request(`/quizzes/${encodeURIComponent(quizId)}?user_id=${encodeURIComponent(id)}`, {method:'PUT',body:JSON.stringify(body)}),
  flashcards: (courseId,userId='demo-student') => request(`/flashcards?${courseId ? `course_id=${encodeURIComponent(courseId)}&` : ''}user_id=${encodeURIComponent(userId)}`),
  flashcardReview: (id,language='en') => request(`/flashcards/review?user_id=${encodeURIComponent(id)}&language=${encodeURIComponent(language)}`),
  reviewFlashcard: (cardId,rating,userId) => request(`/flashcards/${encodeURIComponent(cardId)}/review?user_id=${encodeURIComponent(userId)}`, {method:'POST',body:JSON.stringify({rating})}),
  classrooms: id => request(`/classrooms?user_id=${encodeURIComponent(id)}`),
  createClassroom: (body,id) => request(`/classrooms?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  joinClassroom: (code,id) => request(`/classrooms/join?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify({join_code:code})}),
  classroomAssignments: (classroomId,id,language='en') => request(`/classrooms/${encodeURIComponent(classroomId)}/assignments?user_id=${encodeURIComponent(id)}&language=${encodeURIComponent(language)}`),
  classroomEvents: (classroomId,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/events?user_id=${encodeURIComponent(id)}`),
  createClassroomEvent: (classroomId,body,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/events?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  classroomAttendance: (classroomId,eventId,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/events/${encodeURIComponent(eventId)}/attendance?user_id=${encodeURIComponent(id)}`),
  recordClassroomAttendance: (classroomId,eventId,entries,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/events/${encodeURIComponent(eventId)}/attendance?user_id=${encodeURIComponent(id)}`, {method:'PUT',body:JSON.stringify({entries})}),
  classroomTasks: (classroomId,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/tasks?user_id=${encodeURIComponent(id)}`),
  createClassroomTask: (classroomId,body,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/tasks?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  submitClassroomTask: (classroomId,taskId,body,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/tasks/${encodeURIComponent(taskId)}/submissions?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  uploadClassroomTaskFile: (classroomId,taskId,file,id) => { const form=new FormData(); form.append('upload',file); return formRequest(`/classrooms/${encodeURIComponent(classroomId)}/tasks/${encodeURIComponent(taskId)}/files?user_id=${encodeURIComponent(id)}`,form); },
  downloadClassroomTaskFile: (classroomId,taskId,fileId,id) => downloadRequest(`/classrooms/${encodeURIComponent(classroomId)}/tasks/${encodeURIComponent(taskId)}/files/${encodeURIComponent(fileId)}?user_id=${encodeURIComponent(id)}`),
  submitClassroomTaskFiles: (classroomId,taskId,body,files,id) => { const form=new FormData(); form.append('body',body); files.forEach(file=>form.append('files',file)); return formRequest(`/classrooms/${encodeURIComponent(classroomId)}/tasks/${encodeURIComponent(taskId)}/submissions/files?user_id=${encodeURIComponent(id)}`,form); },
  downloadClassroomSubmissionFile: (classroomId,taskId,fileId,id) => downloadRequest(`/classrooms/${encodeURIComponent(classroomId)}/tasks/${encodeURIComponent(taskId)}/submissions/files/${encodeURIComponent(fileId)}?user_id=${encodeURIComponent(id)}`),
  gradeClassroomTask: (classroomId,taskId,studentId,body,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/tasks/${encodeURIComponent(taskId)}/submissions/${encodeURIComponent(studentId)}?user_id=${encodeURIComponent(id)}`, {method:'PUT',body:JSON.stringify(body)}),
  announcements: id => request(`/announcements?user_id=${encodeURIComponent(id)}`),
  createAnnouncement: (body,id) => request(`/announcements?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  classroomDiscussions: (classroomId,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/discussions?user_id=${encodeURIComponent(id)}`),
  createClassroomDiscussion: (classroomId,body,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/discussions?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  courseResources: (courseId,id) => request(`/courses/${encodeURIComponent(courseId)}/resources?user_id=${encodeURIComponent(id)}`),
  createCourseResource: (courseId,body,id) => request(`/courses/${encodeURIComponent(courseId)}/resources?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  classroomReport: (classroomId,id,search='') => request(`/classrooms/${encodeURIComponent(classroomId)}/report?user_id=${encodeURIComponent(id)}&search=${encodeURIComponent(search)}`),
  assignClassroomContent: (classroomId,body,id) => request(`/classrooms/${encodeURIComponent(classroomId)}/assignments?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  analytics: id => request(`/analytics/${encodeURIComponent(id)}`),
  achievements: id => request(`/achievements/${encodeURIComponent(id)}`),
  createCourse: (body,id) => request(`/courses?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  updateCourse: (id,body,userId) => request(`/courses/${id}?user_id=${encodeURIComponent(userId)}`, {method:'PUT',body:JSON.stringify(body)}),
  createPath: (body,id) => request(`/paths?user_id=${encodeURIComponent(id)}`, {method:'POST',body:JSON.stringify(body)}),
  certificateBlob: (id,userId) => request(`/certificates/${id}/pdf?user_id=${encodeURIComponent(userId)}`),
  aiHistory: id => request(`/ai/history/${encodeURIComponent(id)}`),
  clearAiHistory: id => request(`/ai/history/${encodeURIComponent(id)}`, {method:'DELETE'}),
  ask: (message,context,userId,history) => request(`/ai/ask?user_id=${encodeURIComponent(userId)}`, {method:'POST',body:JSON.stringify({message,context,history})}),
  sanskritGuide: () => request('/sanskrit-guide'),
};
