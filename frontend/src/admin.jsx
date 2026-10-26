import React, { useEffect, useState } from 'react';
import { Building2, Plus, RefreshCw } from 'lucide-react';
import { api } from './api';

function SectionTitle({ eyebrow, title, sub }) {
  return <div className="section-head"><div><span className="eyebrow">{eyebrow}</span><h2>{title}</h2>{sub && <p>{sub}</p>}</div></div>;
}

function Empty({ children }) {
  return <div className="empty"><p>{children}</p></div>;
}

function Metric({ label, value }) {
  return <div className="stat"><div className="stat-icon"><Building2 size={18}/></div><div><span>{label}</span><b>{value ?? 0}</b></div></div>;
}

function parseMemberCsv(value) {
  const parseLine = line => {
    const result = [];
    let field = '';
    let quoted = false;
    for (let index = 0; index < line.length; index += 1) {
      const character = line[index];
      if (character === '"' && quoted && line[index + 1] === '"') {
        field += '"';
        index += 1;
      } else if (character === '"') {
        quoted = !quoted;
      } else if (character === ',' && !quoted) {
        result.push(field.trim());
        field = '';
      } else {
        field += character;
      }
    }
    if (quoted) throw new Error('A CSV row has an unmatched quote.');
    result.push(field.trim());
    return result;
  };
  const rows = value.split(/\r?\n/).map(line => line.trim()).filter(Boolean).map(parseLine);
  if (rows[0]?.[0]?.toLowerCase() === 'name') rows.shift();
  if (!rows.length || rows.some(row => row.length < 3 || !row[0] || !row[1] ||
    !row[1].includes('@') || !['student', 'teacher'].includes(row[2].toLowerCase()))) {
    throw new Error('Use CSV rows with name,email,student-or-teacher.');
  }
  return rows.map(([name, email, role]) => ({ name, email, role: role.toLowerCase() }));
}

export function PlatformAdmin({ user, notify }) {
  const [data, setData] = useState(null);
  const [plans, setPlans] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ name: '', short_name: '', admin_name: '', admin_email: '', plan_id: 'starter' });

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [summary, tenants, availablePlans] = await Promise.all([
        api.platformDashboard(user.id), api.institutions(user.id), api.plans(user.id),
      ]);
      setData(summary);
      setInstitutions(tenants);
      setPlans(availablePlans);
      if (!availablePlans.some(plan => plan.id === form.plan_id) && availablePlans.length) {
        setForm(current => ({ ...current, plan_id: availablePlans[0].id }));
      }
    } catch (cause) {
      setError(cause.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [user.id]);

  const create = async () => {
    if (form.name.trim().length < 2 || form.short_name.trim().length < 2 ||
      form.admin_name.trim().length < 2 || !form.admin_email.includes('@')) {
      notify('Enter the institution and administrator details.');
      return;
    }
    try {
      await api.createInstitution({ ...form, name: form.name.trim(), short_name: form.short_name.trim(),
        admin_name: form.admin_name.trim(), admin_email: form.admin_email.trim() }, user.id);
      setForm(current => ({ ...current, name: '', short_name: '', admin_name: '', admin_email: '' }));
      notify('Trial institution created; its admin can join using the invited email.');
      await load();
    } catch (cause) {
      notify(cause.message);
    }
  };

  const changeStatus = async (institution, status) => {
    try {
      await api.updateInstitutionStatus(institution.id, status, user.id);
      notify(`Institution ${status}.`);
      await load();
    } catch (cause) {
      notify(cause.message);
    }
  };

  const changePlan = async (institution, planId) => {
    try {
      await api.updateInstitutionPlan(institution.id, planId, user.id);
      notify('Institution plan updated.');
      await load();
    } catch (cause) {
      notify(cause.message);
    }
  };

  return <div className="page-stack">
    <SectionTitle eyebrow="IKSPHERE SAAS" title="Platform administration"
      sub="Manage institution onboarding, trial status, plans, and tenant usage."/>
    {data && <div className="stats-grid">
      {[['Institutions', data.institutions], ['Active', data.active_institutions],
        ['Trials', data.trial_institutions], ['Suspended', data.suspended_institutions],
        ['Students', data.students], ['Teachers', data.teachers], ['Tenant courses', data.courses]]
        .map(([label, value]) => <Metric key={label} label={label} value={value}/>)}
    </div>}
    {error && <div className="form-error">{error} <button className="text-btn" onClick={load}>Retry</button></div>}
    {loading && !data && <div className="loader"><RefreshCw className="spin"/><p>Loading platform data…</p></div>}
    <section className="panel form-stack">
      <SectionTitle eyebrow="ONBOARDING" title="Create an institution"
        sub="A trial is created without processing payments."/>
      <div className="institution-form-grid">
        <label>Institution name<input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Example College"/></label>
        <label>Short name<input value={form.short_name} onChange={e => setForm({ ...form, short_name: e.target.value })} placeholder="example-college"/></label>
        <label>Institution Admin name<input value={form.admin_name} onChange={e => setForm({ ...form, admin_name: e.target.value })} placeholder="Admin name"/></label>
        <label>Institution Admin email<input type="email" value={form.admin_email} onChange={e => setForm({ ...form, admin_email: e.target.value })} placeholder="admin@example.edu"/></label>
        <label>Trial plan<select value={form.plan_id} onChange={e => setForm({ ...form, plan_id: e.target.value })}>
          {plans.map(plan => <option key={plan.id} value={plan.id}>{plan.name} • up to {plan.student_limit} students</option>)}
        </select></label>
      </div>
      <button className="primary" onClick={create}><Plus size={16}/> Create trial institution</button>
    </section>
    <section className="panel">
      <SectionTitle eyebrow="TENANTS" title="Institutions"/>
      {institutions.length ? <div className="tenant-list">{institutions.map(institution =>
        <article className="tenant-row" key={institution.id}>
          <div><b>{institution.name}</b><small>{institution.short_name} • {institution.plan?.name || institution.plan_id} • {institution.subscription_status}</small>
            <small>Students {institution.usage.students}/{institution.limits.students} • Teachers {institution.usage.teachers}/{institution.limits.teachers} • Courses {institution.usage.courses}/{institution.limits.courses}</small></div>
          <div className="tenant-actions">
            <select aria-label={`Plan for ${institution.name}`} value={institution.plan_id} onChange={e => changePlan(institution, e.target.value)}>
              {plans.map(plan => <option key={plan.id} value={plan.id}>{plan.name}</option>)}
            </select>
            <select aria-label={`Status for ${institution.name}`} value={institution.status} onChange={e => changeStatus(institution, e.target.value)}>
              {['trial', 'active', 'suspended', 'cancelled'].map(status => <option key={status} value={status}>{status}</option>)}
            </select>
          </div>
        </article>)}</div> : <Empty>No institutions created yet.</Empty>}
    </section>
    <p className="muted">Plans are prototype limits only; payment processing is not configured.</p>
  </div>;
}

export function InstitutionAdmin({ user, notify }) {
  const [dashboard, setDashboard] = useState(null);
  const [members, setMembers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tab, setTab] = useState('dashboard');
  const [departmentName, setDepartmentName] = useState('');
  const [programName, setProgramName] = useState('');
  const [programDepartment, setProgramDepartment] = useState('');
  const [member, setMember] = useState({ name: '', email: '', role: 'student', department_id: '',
    program_id: '', academic_year: '', semester: '', division: '', batch: '', student_number: '' });
  const [bulkCsv, setBulkCsv] = useState('');
  const [bulkSummary, setBulkSummary] = useState(null);
  const [notices, setNotices] = useState([]);
  const [noticeTitle, setNoticeTitle] = useState('');
  const [noticeBody, setNoticeBody] = useState('');
  const [branding, setBranding] = useState(null);
  const institutionId = user.institution_id;

  const load = async () => {
    if (!institutionId) {
      setError('Your account is not assigned to an institution. Ask the platform administrator for an invitation.');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError('');
    try {
      const [summary, roster, departmentsList, programsList, own, announcementList] = await Promise.all([
        api.institutionDashboard(institutionId, user.id),
        api.institutionMembers(institutionId, user.id),
        api.departments(institutionId, user.id),
        api.programs(institutionId, user.id),
        api.myInstitution(user.id),
        api.announcements(user.id),
      ]);
      setDashboard(summary);
      setMembers(roster);
      setDepartments(departmentsList);
      setPrograms(programsList);
      setBranding(own.institution);
      setNotices(announcementList);
    } catch (cause) {
      setError(cause.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [user.id, institutionId]);

  const addDepartment = async () => {
    if (departmentName.trim().length < 2) return notify('Enter a department name.');
    try {
      await api.createDepartment(institutionId, { name: departmentName.trim() }, user.id);
      setDepartmentName('');
      notify('Department created.');
      await load();
    } catch (cause) { notify(cause.message); }
  };

  const addProgram = async () => {
    if (programName.trim().length < 2 || !programDepartment) return notify('Enter a program name and select a department.');
    try {
      await api.createProgram(institutionId, { name: programName.trim(), department_id: programDepartment }, user.id);
      setProgramName('');
      notify('Program created.');
      await load();
    } catch (cause) { notify(cause.message); }
  };

  const invite = async () => {
    if (member.name.trim().length < 2 || !member.email.includes('@')) return notify('Enter a valid member name and email.');
    try {
      await api.inviteInstitutionMember(institutionId, { ...member, name: member.name.trim(), email: member.email.trim(),
        department_id: member.department_id || null, program_id: member.program_id || null }, user.id);
      setMember(current => ({ ...current, name: '', email: '', department_id: '', program_id: '',
        academic_year: '', semester: '', division: '', batch: '', student_number: '' }));
      notify('Invitation saved. The member must sign in with this email.');
      await load();
    } catch (cause) { notify(cause.message); }
  };

  const bulkInvite = async () => {
    let parsed;
    try { parsed = parseMemberCsv(bulkCsv); }
    catch (cause) { notify(cause.message); return; }
    try {
      const result = await api.bulkInviteInstitutionMembers(institutionId, parsed, user.id);
      setBulkSummary(result);
      setBulkCsv('');
      notify(`${result.created} invitations created; ${result.failed} failed.`);
      await load();
    } catch (cause) { notify(cause.message); }
  };

  const saveBranding = async () => {
    if (!branding) return;
    try {
      const result = await api.updateMyInstitution(branding, user.id);
      setBranding(result.institution);
      notify('Institution branding saved.');
    } catch (cause) { notify(cause.message); }
  };

  return <div className="page-stack">
    <SectionTitle eyebrow={branding?.name || 'COLLEGE ADMIN'} title="Institution administration"
      sub="Manage members, academic structure, branding, and tenant usage."/>
    <div className="segmented admin-tabs">{[['dashboard', 'Dashboard'], ['members', 'Students & teachers'],
      ['structure', 'Departments & programs'], ['announcements', 'Announcements'], ['branding', 'Branding & settings']]
      .map(([id, label]) => <button className={tab === id ? 'active' : ''} key={id} onClick={() => setTab(id)}>{label}</button>)}
    </div>
    {error && <div className="form-error">{error} <button className="text-btn" onClick={load}>Retry</button></div>}
    {loading && !dashboard && <div className="loader"><RefreshCw className="spin"/><p>Loading institution workspace…</p></div>}
    {tab === 'dashboard' && dashboard && <>
      <div className="stats-grid">{[['Students', dashboard.institution.usage.students], ['Teachers', dashboard.institution.usage.teachers],
        ['Departments', dashboard.departments], ['Programs', dashboard.programs],
        ['Courses', dashboard.institution.usage.courses], ['Pending invites', dashboard.pending_invites]]
        .map(([label, value]) => <Metric key={label} label={label} value={value}/>)}</div>
      <section className="panel"><h3>{dashboard.institution.name}</h3><p>{dashboard.institution.plan?.name} plan • {dashboard.institution.subscription_status} • students {dashboard.institution.usage.students}/{dashboard.institution.limits.students}, teachers {dashboard.institution.usage.teachers}/{dashboard.institution.limits.teachers}, courses {dashboard.institution.usage.courses}/{dashboard.institution.limits.courses}</p></section>
    </>}
    {tab === 'members' && <>
      <section className="panel form-stack"><SectionTitle eyebrow="MEMBERS" title="Invite a student or teacher"/>
        <div className="institution-form-grid"><label>Name<input value={member.name} onChange={e => setMember({ ...member, name: e.target.value })} placeholder="Full name"/></label>
          <label>Email<input type="email" value={member.email} onChange={e => setMember({ ...member, email: e.target.value })} placeholder="name@example.edu"/></label>
          <label>Role<select value={member.role} onChange={e => setMember({ ...member, role: e.target.value })}><option value="student">Student</option><option value="teacher">Teacher</option></select></label>
          <label>Department<select value={member.department_id} onChange={e => setMember({ ...member, department_id: e.target.value, program_id: '' })}><option value="">No department</option>{departments.map(item => <option value={item.id} key={item.id}>{item.name}</option>)}</select></label>
          <label>Program<select value={member.program_id} onChange={e => setMember({ ...member, program_id: e.target.value })}><option value="">No program</option>{programs.filter(item => !member.department_id || item.department_id === member.department_id).map(item => <option value={item.id} key={item.id}>{item.name}</option>)}</select></label>
          <label>Academic year<input value={member.academic_year} onChange={e => setMember({ ...member, academic_year: e.target.value })} placeholder="2025-26"/></label>
          <label>Semester<input value={member.semester} onChange={e => setMember({ ...member, semester: e.target.value })} placeholder="Semester 1"/></label>
          <label>Division<input value={member.division} onChange={e => setMember({ ...member, division: e.target.value })} placeholder="A"/></label>
          <label>Batch<input value={member.batch} onChange={e => setMember({ ...member, batch: e.target.value })} placeholder="2025"/></label>
          <label>Student number<input value={member.student_number} onChange={e => setMember({ ...member, student_number: e.target.value })} placeholder="Optional ID"/></label>
        </div><button className="primary" onClick={invite}><Plus size={16}/> Invite member</button>
        <div className="bulk-invite"><label>Bulk invite (CSV: name,email,role)<textarea rows={5} value={bulkCsv} onChange={e => setBulkCsv(e.target.value)} placeholder={'Name,Email,Role\nAsha Rao,asha@example.edu,student\nRavi Shah,ravi@example.edu,teacher'}/></label>
          <button className="secondary" onClick={bulkInvite} disabled={!bulkCsv.trim()}>Create invitations</button>
          {bulkSummary && <small>{bulkSummary.created} invited, {bulkSummary.failed} failed. See failed rows below for details.</small>}
          {bulkSummary?.results?.filter(result => result.status === 'failed').map(result => <small className="bulk-error" key={result.row}>Row {result.row} ({result.email}): {result.error}</small>)}
        </div>
      </section>
      <section className="panel"><SectionTitle eyebrow="ROSTER" title={`${members.length} members`}/>
        {members.length ? <div className="tenant-list">{members.map(person => <article className="tenant-row" key={person.id}><div><b>{person.name}</b><small>{person.email} • {person.role} • {person.status}</small></div></article>)}</div> : <Empty>No members yet. Invite teachers and students to begin.</Empty>}
      </section>
    </>}
    {tab === 'structure' && <div className="two-col">
      <section className="panel form-stack"><SectionTitle eyebrow="ACADEMIC STRUCTURE" title="Departments"/>
        <label>Department name<input value={departmentName} onChange={e => setDepartmentName(e.target.value)} placeholder="Computer Science"/></label>
        <button className="primary" onClick={addDepartment}>Add department</button>
        {departments.map(item => <div className="mini-row" key={item.id}>{item.name}</div>)}
      </section>
      <section className="panel form-stack"><SectionTitle eyebrow="ACADEMIC STRUCTURE" title="Programs"/>
        <label>Program name<input value={programName} onChange={e => setProgramName(e.target.value)} placeholder="BSc AIML"/></label>
        <label>Department<select value={programDepartment} onChange={e => setProgramDepartment(e.target.value)}><option value="">Select department</option>{departments.map(item => <option value={item.id} key={item.id}>{item.name}</option>)}</select></label>
        <button className="primary" onClick={addProgram}>Add program</button>
        {programs.map(item => <div className="mini-row" key={item.id}>{item.name} • {item.department_name}</div>)}
      </section>
    </div>}
    {tab === 'announcements' && <section className="panel form-stack"><SectionTitle eyebrow="COLLEGE UPDATES" title="Institution announcements"/>
      <label>Title<input value={noticeTitle} onChange={e => setNoticeTitle(e.target.value)} placeholder="Academic calendar update"/></label>
      <label>Message<textarea value={noticeBody} onChange={e => setNoticeBody(e.target.value)} placeholder="Share an update with your institution…"/></label>
      <button className="primary" onClick={async () => { if (!noticeTitle.trim() || !noticeBody.trim()) return notify('Enter an announcement title and message.'); try { await api.createAnnouncement({ title: noticeTitle.trim(), body: noticeBody.trim() }, user.id); setNoticeTitle(''); setNoticeBody(''); notify('Announcement published.'); await load(); } catch (cause) { notify(cause.message); } }}>Publish announcement</button>
      {notices.map(item => <article className="notice-card" key={item.id}><b>{item.title}</b><p>{item.body}</p><small>{item.classroom_name || 'Institution-wide'} • {item.author_name}</small></article>)}
    </section>}
    {tab === 'branding' && branding && <section className="panel form-stack"><SectionTitle eyebrow="WHITE LABEL" title="Institution profile and branding"/>
      <label>Institution name<input value={branding.name} onChange={e => setBranding({ ...branding, name: e.target.value })}/></label>
      <div className="institution-form-grid"><label>Logo URL<input value={branding.logo || ''} onChange={e => setBranding({ ...branding, logo: e.target.value })}/></label>
        <label>Favicon URL<input value={branding.favicon || ''} onChange={e => setBranding({ ...branding, favicon: e.target.value })}/></label>
        <label>Primary color<input type="color" value={branding.primary_color} onChange={e => setBranding({ ...branding, primary_color: e.target.value })}/></label>
        <label>Secondary color<input type="color" value={branding.secondary_color} onChange={e => setBranding({ ...branding, secondary_color: e.target.value })}/></label>
        <label>Contact email<input value={branding.contact_email || ''} onChange={e => setBranding({ ...branding, contact_email: e.target.value })}/></label>
        <label>Website<input value={branding.website || ''} onChange={e => setBranding({ ...branding, website: e.target.value })}/></label>
      </div>
      <label>Welcome message<textarea value={branding.welcome_message || ''} onChange={e => setBranding({ ...branding, welcome_message: e.target.value })}/></label>
      <button className="primary" onClick={saveBranding}>Save branding</button>
    </section>}
  </div>;
}
