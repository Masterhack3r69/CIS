import React, { FormEvent, useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

type ApiResponse<T> = { success: boolean; message: string; data: T; errors?: { field: string; message: string }[] };
type PageResponse<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
type Option = { label: string; value: string };
type Field =
  | { name: string; label: string; type?: "text" | "number" | "password" | "email" | "date"; required?: boolean; createOnly?: boolean }
  | { name: string; label: string; type: "select"; options: Option[]; required?: boolean }
  | { name: string; label: string; type: "multi"; optionsKey: "roles"; required?: boolean };
type Resource = {
  key: string;
  title: string;
  endpoint: string;
  searchable?: boolean;
  status?: "activeStatus" | "booleanActive";
  fields: Field[];
  columns: { key: string; label: string }[];
  prepare?: (values: Record<string, string>) => Record<string, unknown>;
};
type Screen = { key: string; title: string; group: "Admin Setup" | "Registrar"; resource?: Resource; custom?: "students" | "enrollments" };
type Session = { accessToken: string; refreshToken: string; user: UserSummary };
type UserSummary = { id: string; username: string; email: string; fullName: string; active: boolean; roles: string[]; permissions: string[] };
type Role = { id: string; name: string; description: string; permissions: string[] };
type Row = Record<string, unknown>;

const API_BASE = import.meta.env.VITE_API_BASE ?? "";
const ACTIVE = opts(["ACTIVE", "INACTIVE"]);
const DEGREE = opts(["BACHELOR", "ASSOCIATE", "DIPLOMA", "CERTIFICATE", "GRADUATE_PROGRAM"]);
const COURSE = opts(["MAJOR", "PROFESSIONAL_COURSE", "GENERAL_EDUCATION", "PHYSICAL_EDUCATION", "NSTP", "ELECTIVE", "LABORATORY", "SEMINAR", "THESIS_CAPSTONE"]);
const EMPLOYMENT = opts(["FULL_TIME", "PART_TIME", "CONTRACTUAL", "VISITING_LECTURER", "INACTIVE"]);
const FACULTY_TYPE = opts(["INSTRUCTOR", "PROFESSOR", "LECTURER", "DEAN", "PROGRAM_HEAD"]);
const STUDENT_STATUS = opts(["ACTIVE", "INACTIVE", "GRADUATED", "TRANSFERRED", "DROPPED", "ON_LEAVE"]);
const GENDER = opts(["MALE", "FEMALE"]);
const CLASSIFICATION = opts(["NEW", "OLD", "TRANSFEREE", "RETURNEE", "CROSS_ENROLLEE"]);
const ACADEMIC_STATUS = opts(["REGULAR", "IRREGULAR", "PROBATIONARY", "WARNING", "DISMISSED", "GRADUATED"]);
const ENROLLMENT_STATUS = opts(["DRAFT", "VALIDATED", "CONFIRMED", "CANCELLED"]);
const DOC_STATUS = opts(["PENDING", "VERIFIED", "REJECTED"]);

function opts(values: string[]) {
  return values.map((value) => ({ value, label: value.replaceAll("_", " ") }));
}

function getSession(): Session | null {
  const raw = localStorage.getItem("sis.session");
  return raw ? JSON.parse(raw) : null;
}

function setSession(session: Session | null) {
  if (session) localStorage.setItem("sis.session", JSON.stringify(session));
  else localStorage.removeItem("sis.session");
}

async function api<T>(path: string, options: RequestInit = {}, token?: string): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData)) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const payload = (await response.json().catch(() => null)) as ApiResponse<T> | null;
  if (!response.ok || !payload?.success) {
    const detail = payload?.errors?.map((error) => `${error.field}: ${error.message}`).join(", ");
    throw new Error(detail || payload?.message || `Request failed (${response.status})`);
  }
  return payload.data;
}

function App() {
  const [session, setSessionState] = useState<Session | null>(getSession());
  const [message, setMessage] = useState("");
  const onSession = (next: Session | null) => {
    setSession(next);
    setSessionState(next);
  };

  if (!session) return <Login onLogin={onSession} message={message} setMessage={setMessage} />;
  return <Shell session={session} logout={() => onSession(null)} message={message} setMessage={setMessage} />;
}

function Login({ onLogin, message, setMessage }: { onLogin: (session: Session) => void; message: string; setMessage: (message: string) => void }) {
  const [usernameOrEmail, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    try {
      const auth = await api<{ accessToken: string; refreshToken: string; user: UserSummary }>("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({ usernameOrEmail, password })
      });
      onLogin(auth);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Login failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="loginPage">
      <form className="loginBox" onSubmit={submit}>
        <div>
          <p className="eyebrow">Student Information System</p>
          <h1>Operations Console</h1>
        </div>
        {message && <div className="alert">{message}</div>}
        <label>Username or email<input value={usernameOrEmail} onChange={(e) => setUsername(e.target.value)} /></label>
        <label>Password<input value={password} onChange={(e) => setPassword(e.target.value)} type="password" /></label>
        <button className="primary" disabled={busy}>{busy ? "Signing in..." : "Sign in"}</button>
      </form>
    </main>
  );
}

function Shell({ session, logout, message, setMessage }: { session: Session; logout: () => void; message: string; setMessage: (message: string) => void }) {
  const [activeKey, setActiveKey] = useState("students");
  const [lookups, setLookups] = useState<Record<string, Option[]>>({});
  const [lookupRows, setLookupRows] = useState<Record<string, Row[]>>({});
  const screens = useMemo(() => buildScreens(lookups), [lookups]);
  const screen = screens.find((item) => item.key === activeKey) ?? screens[0];

  async function loadLookups() {
    const [roles, departments, programs, schoolYears, semesters, users, curricula, sections, students, schedules] = await Promise.all([
      api<PageResponse<Role>>("/api/v1/roles?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/departments?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/programs?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/school-years?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/semesters?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/users?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/curricula?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/sections?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/students?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/schedules?size=500&status=ACTIVE", {}, session.accessToken)
    ]);
    setLookupRows({ curricula: curricula.items, sections: sections.items, students: students.items, schedules: schedules.items });
    setLookups({
      roles: roles.items.map((role) => ({ value: role.id, label: role.name })),
      departments: departments.items.map((item) => option(item, "id", ["departmentCode", "departmentName"])),
      programs: programs.items.map((item) => option(item, "id", ["programCode", "programName"])),
      schoolYears: schoolYears.items.map((item) => option(item, "id", ["schoolYear"])),
      semesters: semesters.items.map((item) => option(item, "id", ["name"])),
      users: users.items.map((item) => ({ value: String(item.id), label: `${item.username} (${item.email})` })),
      curricula: curricula.items.map((item) => option(item, "id", ["curriculumCode", "curriculumName"])),
      sections: sections.items.map((item) => option(item, "id", ["sectionCode"])),
      students: students.items.map((item) => option(item, "id", ["studentNumber", "fullName"])),
      schedules: schedules.items.map((item) => option(item, "id", ["courseCode", "courseTitle", "sectionCode"]))
    });
  }

  useEffect(() => {
    loadLookups().catch((error) => setMessage(error instanceof Error ? error.message : "Unable to load lookups"));
  }, []);

  const grouped = screens.reduce<Record<string, Screen[]>>((acc, item) => {
    acc[item.group] = [...(acc[item.group] ?? []), item];
    return acc;
  }, {});

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand"><strong>SIS</strong><span>Operations Console</span></div>
        <nav>
          {Object.entries(grouped).map(([group, items]) => (
            <div className="navGroup" key={group}>
              <p>{group}</p>
              {items.map((item) => (
                <button key={item.key} className={item.key === activeKey ? "nav active" : "nav"} onClick={() => setActiveKey(item.key)}>{item.title}</button>
              ))}
            </div>
          ))}
        </nav>
      </aside>
      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Signed in as {session.user.username}</p>
            <h1>{screen.title}</h1>
          </div>
          <button onClick={logout}>Sign out</button>
        </header>
        {message && <div className="alert">{message}<button onClick={() => setMessage("")}>Dismiss</button></div>}
        {screen.resource && (
          <ResourcePanel
            key={screen.key}
            resource={screen.resource}
            token={session.accessToken}
            lookups={lookups}
            setMessage={setMessage}
            refreshLookups={loadLookups}
          />
        )}
        {screen.custom === "students" && <StudentsPanel token={session.accessToken} lookups={lookups} setMessage={setMessage} refreshLookups={loadLookups} />}
        {screen.custom === "enrollments" && <EnrollmentsPanel token={session.accessToken} lookups={lookups} lookupRows={lookupRows} setMessage={setMessage} refreshLookups={loadLookups} />}
      </section>
    </div>
  );
}

function ResourcePanel({ resource, token, lookups, setMessage, refreshLookups }: { resource: Resource; token: string; lookups: Record<string, Option[]>; setMessage: (message: string) => void; refreshLookups: () => Promise<void> }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Row | null>(null);
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const query = resource.searchable && search ? `?search=${encodeURIComponent(search)}&size=50` : "?size=50";
      const page = await api<PageResponse<Row>>(`${resource.endpoint}${query}`, {}, token);
      setRows(page.items);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load records");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, [resource.key]);

  async function save(values: Record<string, string>) {
    const editing = Boolean(selected?.id);
    const body = JSON.stringify(resource.prepare ? resource.prepare(values) : clean(values));
    const path = editing ? `${resource.endpoint}/${selected?.id}` : resource.endpoint;
    await api(path, { method: editing ? "PUT" : "POST", body }, token);
    setSelected(null);
    await load();
    await refreshLookups();
    setMessage(`${resource.title} saved`);
  }

  async function setStatus(row: Row) {
    const body = resource.status === "booleanActive"
      ? { active: !(row.active as boolean) }
      : { status: row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE" };
    await api(`${resource.endpoint}/${row.id}/status`, { method: "PATCH", body: JSON.stringify(body) }, token);
    await load();
    await refreshLookups();
  }

  const statusLabel = (row: Row) => resource.status === "booleanActive" ? (row.active ? "ACTIVE" : "INACTIVE") : String(row.status);

  return (
    <div className="contentGrid">
      <div className="listPane">
        <div className="toolbar">
          {resource.searchable && <input placeholder="Search" value={search} onChange={(e) => setSearch(e.target.value)} onKeyDown={(e) => e.key === "Enter" && load()} />}
          {resource.searchable && <button onClick={load}>Search</button>}
          <button className="primary" onClick={() => setSelected({})}>New</button>
        </div>
        <DataTable rows={rows} columns={resource.columns} busy={busy} extraHeader={resource.status ? "Status" : undefined} extraCell={(row) => resource.status ? <span className={`pill ${statusLabel(row) === "ACTIVE" ? "good" : "muted"}`}>{statusLabel(row)}</span> : null} actions={(row) => (
          <>
            <button onClick={() => setSelected(row)}>Edit</button>
            {resource.status && <button onClick={() => setStatus(row)}>{statusLabel(row) === "ACTIVE" ? "Deactivate" : "Activate"}</button>}
          </>
        )} />
      </div>
      <EditPane resource={resource} selected={selected} setSelected={setSelected} save={save} lookups={lookups} />
    </div>
  );
}

function StudentsPanel({ token, lookups, setMessage, refreshLookups }: { token: string; lookups: Record<string, Option[]>; setMessage: (message: string) => void; refreshLookups: () => Promise<void> }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Row | null>(null);
  const [detail, setDetail] = useState<Row | null>(null);
  const [documents, setDocuments] = useState<Row[]>([]);
  const [records, setRecords] = useState<Row[]>([]);
  const [values, setValues] = useState<Record<string, string>>(emptyStudent());
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const page = await api<PageResponse<Row>>(`/api/v1/students?size=50${search ? `&search=${encodeURIComponent(search)}` : ""}`, {}, token);
      setRows(page.items);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load students");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function select(row: Row) {
    setSelected(row);
    const student = await api<Row>(`/api/v1/students/${row.id}`, {}, token);
    setDetail(student);
    setValues(flattenStudent(student));
    await Promise.all([loadDocuments(String(row.id)), loadRecords(String(row.id))]);
  }

  async function loadDocuments(id: string) {
    setDocuments(await api<Row[]>(`/api/v1/students/${id}/documents`, {}, token));
  }

  async function loadRecords(id: string) {
    const data = await api<Row>(`/api/v1/students/${id}/academic-records`, {}, token);
    setRecords(Array.isArray(data.records) ? data.records as Row[] : []);
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    const body = JSON.stringify(studentPayload(values));
    const editing = Boolean(selected?.id);
    const saved = await api<Row>(editing ? `/api/v1/students/${selected?.id}` : "/api/v1/students", { method: editing ? "PUT" : "POST", body }, token);
    await load();
    await refreshLookups();
    setMessage("Student saved");
    if (!editing && saved.personal && typeof saved.personal === "object") {
      const personal = saved.personal as Row;
      setSelected({ id: personal.id });
      setDetail(saved);
    }
  }

  async function updateStatus(status: string) {
    if (!selected?.id) return;
    await api(`/api/v1/students/${selected.id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }, token);
    await load();
    await select(selected);
  }

  return (
    <div className="contentGrid wide">
      <div className="listPane">
        <div className="toolbar">
          <input placeholder="Search students" value={search} onChange={(e) => setSearch(e.target.value)} onKeyDown={(e) => e.key === "Enter" && load()} />
          <button onClick={load}>Search</button>
          <button className="primary" onClick={() => { setSelected({}); setDetail(null); setValues(emptyStudent()); setDocuments([]); setRecords([]); }}>New</button>
        </div>
        <DataTable rows={rows} busy={busy} columns={[
          { key: "studentNumber", label: "Student no." },
          { key: "fullName", label: "Name" },
          { key: "programCode", label: "Program" },
          { key: "yearLevel", label: "Year" },
          { key: "sectionCode", label: "Section" },
          { key: "status", label: "Status" }
        ]} actions={(row) => <button onClick={() => select(row)}>Open</button>} />
      </div>
      <aside className="editPane detailPane">
        {!selected ? <div className="emptyState">Select a student or create a new record.</div> : (
          <>
            <div className="paneHead"><h2>{selected.id ? "Student Detail" : "New Student"}</h2><button onClick={() => setSelected(null)}>Close</button></div>
            <form className="formGrid two" onSubmit={save}>
              <SectionTitle text="Personal" />
              <Text name="studentNumber" label="Student no." values={values} setValues={setValues} required />
              <Text name="firstName" label="First name" values={values} setValues={setValues} required />
              <Text name="middleName" label="Middle name" values={values} setValues={setValues} />
              <Text name="lastName" label="Last name" values={values} setValues={setValues} required />
              <Select name="gender" label="Gender" options={GENDER} values={values} setValues={setValues} />
              <Text name="birthdate" label="Birthdate" type="date" values={values} setValues={setValues} required />
              <Select name="status" label="Status" options={STUDENT_STATUS} values={values} setValues={setValues} required />
              <SectionTitle text="Contact" />
              <Text name="emailAddress" label="Email" type="email" values={values} setValues={setValues} />
              <Text name="mobileNumber" label="Mobile" values={values} setValues={setValues} />
              <Text name="currentAddress" label="Current address" values={values} setValues={setValues} />
              <Text name="emergencyContactName" label="Emergency contact" values={values} setValues={setValues} />
              <Text name="emergencyContactNumber" label="Emergency number" values={values} setValues={setValues} />
              <SectionTitle text="Academic" />
              <Select name="programId" label="Program" options={lookups.programs ?? []} values={values} setValues={setValues} required />
              <Select name="curriculumId" label="Curriculum" options={lookups.curricula ?? []} values={values} setValues={setValues} required />
              <Text name="yearLevel" label="Year level" type="number" values={values} setValues={setValues} required />
              <Text name="semester" label="Semester label" values={values} setValues={setValues} />
              <Select name="sectionId" label="Section" options={lookups.sections ?? []} values={values} setValues={setValues} />
              <Text name="dateAdmitted" label="Date admitted" type="date" values={values} setValues={setValues} required />
              <Text name="schoolYearAdmitted" label="SY admitted" values={values} setValues={setValues} required />
              <Select name="classification" label="Classification" options={CLASSIFICATION} values={values} setValues={setValues} />
              <Select name="academicStatus" label="Academic status" options={ACADEMIC_STATUS} values={values} setValues={setValues} />
              <button className="primary">Save Student</button>
              {Boolean(selected.id) && <button type="button" onClick={() => updateStatus(values.status === "ACTIVE" ? "INACTIVE" : "ACTIVE")}>{values.status === "ACTIVE" ? "Deactivate" : "Activate"}</button>}
            </form>
            {Boolean(selected.id) && <StudentSubpanels studentId={String(selected.id)} token={token} documents={documents} records={records} reloadDocuments={loadDocuments} setMessage={setMessage} />}
            {detail && <p className="mutedLine">Loaded {String((detail.personal as Row)?.fullName ?? "")}</p>}
          </>
        )}
      </aside>
    </div>
  );
}

function StudentSubpanels({ studentId, token, documents, records, reloadDocuments, setMessage }: { studentId: string; token: string; documents: Row[]; records: Row[]; reloadDocuments: (id: string) => Promise<void>; setMessage: (message: string) => void }) {
  const [documentType, setDocumentType] = useState("Form 138");
  const [remarks, setRemarks] = useState("");
  const [file, setFile] = useState<File | null>(null);

  async function upload(event: FormEvent) {
    event.preventDefault();
    if (!file) return;
    const data = new FormData();
    data.append("documentType", documentType);
    data.append("remarks", remarks);
    data.append("file", file);
    await api(`/api/v1/students/${studentId}/documents`, { method: "POST", body: data }, token);
    setFile(null);
    await reloadDocuments(studentId);
    setMessage("Document uploaded");
  }

  async function verify(documentId: string, status: string) {
    await api(`/api/v1/students/${studentId}/documents/${documentId}/verify`, { method: "PATCH", body: JSON.stringify({ status, remarks }) }, token);
    await reloadDocuments(studentId);
  }

  return (
    <div className="subpanels">
      <section>
        <h3>Documents</h3>
        <form className="inlineForm" onSubmit={upload}>
          <input value={documentType} onChange={(e) => setDocumentType(e.target.value)} placeholder="Document type" />
          <input value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="Remarks" />
          <input type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
          <button>Upload</button>
        </form>
        <DataTable rows={documents} columns={[{ key: "documentType", label: "Type" }, { key: "fileName", label: "File" }, { key: "verificationStatus", label: "Status" }]} actions={(row) => (
          <>
            <button onClick={() => verify(String(row.id), "VERIFIED")}>Verify</button>
            <button onClick={() => verify(String(row.id), "REJECTED")}>Reject</button>
          </>
        )} />
      </section>
      <section>
        <h3>Academic Records</h3>
        <DataTable rows={records} columns={[
          { key: "schoolYear", label: "SY" },
          { key: "semesterName", label: "Sem" },
          { key: "courseCode", label: "Course" },
          { key: "finalGrade", label: "Grade" },
          { key: "specialGrade", label: "Special" },
          { key: "remark", label: "Remark" },
          { key: "earnedUnits", label: "Earned" }
        ]} />
      </section>
    </div>
  );
}

function EnrollmentsPanel({ token, lookups, lookupRows, setMessage, refreshLookups }: { token: string; lookups: Record<string, Option[]>; lookupRows: Record<string, Row[]>; setMessage: (message: string) => void; refreshLookups: () => Promise<void> }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [selected, setSelected] = useState<Row | null>(null);
  const [detail, setDetail] = useState<Row | null>(null);
  const [form, setForm] = useState({ studentId: "", schoolYearId: "", semesterId: "", sectionId: "", remarks: "" });
  const [scheduleId, setScheduleId] = useState("");
  const [status, setStatus] = useState("");
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const page = await api<PageResponse<Row>>(`/api/v1/enrollments?size=50${status ? `&status=${status}` : ""}`, {}, token);
      setRows(page.items);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load enrollments");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function open(row: Row) {
    setSelected(row);
    setDetail(await api<Row>(`/api/v1/enrollments/${row.id}`, {}, token));
  }

  async function create(event: FormEvent) {
    event.preventDefault();
    const created = await api<Row>("/api/v1/enrollments", { method: "POST", body: JSON.stringify(clean(form)) }, token);
    await load();
    await refreshLookups();
    await open(created);
    setMessage("Enrollment draft created");
  }

  async function action(path: string, message: string) {
    if (!selected?.id) return;
    const result = await api<Row>(`/api/v1/enrollments/${selected.id}/${path}`, { method: "POST" }, token);
    if (path === "validate") setMessage(`${message}: ${result.valid ? "valid" : "has issues"}`);
    else setMessage(message);
    await load();
    await open(selected);
  }

  async function addSubject() {
    if (!selected?.id || !scheduleId) return;
    await api(`/api/v1/enrollments/${selected.id}/subjects`, { method: "POST", body: JSON.stringify({ scheduleId }) }, token);
    await open(selected);
    setScheduleId("");
  }

  async function dropSubject(subjectId: string) {
    if (!selected?.id) return;
    await api(`/api/v1/enrollments/${selected.id}/subjects/${subjectId}`, { method: "DELETE" }, token);
    await open(selected);
  }

  const filteredSchedules = (lookupRows.schedules ?? []).filter((schedule) => {
    if (!detail) return true;
    return schedule.schoolYearId === detail.schoolYearId && schedule.semesterId === detail.semesterId;
  }).map((item) => option(item, "id", ["courseCode", "courseTitle", "sectionCode"]));

  return (
    <div className="contentGrid wide">
      <div className="listPane">
        <div className="toolbar">
          <select value={status} onChange={(e) => setStatus(e.target.value)}><option value="">All statuses</option>{ENROLLMENT_STATUS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <button onClick={load}>Filter</button>
        </div>
        <DataTable rows={rows} busy={busy} columns={[
          { key: "studentNumber", label: "Student no." },
          { key: "studentName", label: "Name" },
          { key: "schoolYear", label: "SY" },
          { key: "semesterName", label: "Semester" },
          { key: "status", label: "Status" },
          { key: "subjectCount", label: "Subjects" },
          { key: "totalCreditUnits", label: "Units" }
        ]} actions={(row) => <button onClick={() => open(row)}>Open</button>} />
      </div>
      <aside className="editPane detailPane">
        <div className="paneHead"><h2>Enrollment</h2>{selected && <button onClick={() => { setSelected(null); setDetail(null); }}>Close</button>}</div>
        {!selected && (
          <form className="formGrid" onSubmit={create}>
            <Select name="studentId" label="Student" options={lookups.students ?? []} values={form} setValues={setForm} required />
            <Select name="schoolYearId" label="School year" options={lookups.schoolYears ?? []} values={form} setValues={setForm} required />
            <Select name="semesterId" label="Semester" options={lookups.semesters ?? []} values={form} setValues={setForm} required />
            <Select name="sectionId" label="Section" options={lookups.sections ?? []} values={form} setValues={setForm} />
            <Text name="remarks" label="Remarks" values={form} setValues={setForm} />
            <button className="primary">Create Draft</button>
          </form>
        )}
        {detail && (
          <div className="detailStack">
            <div className="summaryBox">
              <strong>{String(detail.studentName)}</strong>
              <span>{String(detail.schoolYear)} / {String(detail.semesterName)} / {String(detail.status)}</span>
              <span>{String(detail.subjectCount)} subjects, {String(detail.totalCreditUnits)} units</span>
            </div>
            <div className="actions wrap">
              <button onClick={() => action("validate", "Validation completed")}>Validate</button>
              <button onClick={() => action("confirm", "Enrollment confirmed")}>Confirm</button>
              <button onClick={() => action("generate-assessment", "Assessment generated")}>Generate Assessment</button>
              <button onClick={() => action("cancel", "Enrollment cancelled")}>Cancel</button>
            </div>
            <div className="inlineForm">
              <select value={scheduleId} onChange={(e) => setScheduleId(e.target.value)}><option value="">Select schedule...</option>{filteredSchedules.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
              <button onClick={addSubject}>Add Subject</button>
            </div>
            <DataTable rows={Array.isArray(detail.subjects) ? detail.subjects as Row[] : []} columns={[
              { key: "courseCode", label: "Course" },
              { key: "courseTitle", label: "Title" },
              { key: "creditUnits", label: "Units" },
              { key: "facultyName", label: "Faculty" },
              { key: "roomCode", label: "Room" },
              { key: "status", label: "Status" }
            ]} actions={(row) => <button onClick={() => dropSubject(String(row.id))}>Drop</button>} />
            {Boolean(detail.validation) && <ValidationBox validation={detail.validation as Row} />}
          </div>
        )}
      </aside>
    </div>
  );
}

function ValidationBox({ validation }: { validation: Row }) {
  const blocking = Array.isArray(validation.blockingIssues) ? validation.blockingIssues as Row[] : [];
  const warnings = Array.isArray(validation.warnings) ? validation.warnings as Row[] : [];
  return (
    <div className="summaryBox">
      <strong>Validation: {validation.valid ? "Valid" : "Needs attention"}</strong>
      {[...blocking, ...warnings].length === 0 ? <span>No issues.</span> : [...blocking, ...warnings].map((issue, index) => <span key={index}>{String(issue.code)}: {String(issue.message)}</span>)}
    </div>
  );
}

function DataTable({ rows, columns, busy, actions, extraHeader, extraCell }: { rows: Row[]; columns: { key: string; label: string }[]; busy?: boolean; actions?: (row: Row) => React.ReactNode; extraHeader?: string; extraCell?: (row: Row) => React.ReactNode }) {
  return (
    <div className="tableWrap">
      <table>
        <thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}{extraHeader && <th>{extraHeader}</th>}{actions && <th></th>}</tr></thead>
        <tbody>
          {busy ? <tr><td colSpan={columns.length + 2}>Loading...</td></tr> : rows.length === 0 ? <tr><td colSpan={columns.length + 2}>No records found.</td></tr> : rows.map((row, index) => (
            <tr key={String(row.id ?? index)}>
              {columns.map((column) => <td key={column.key}>{formatCell(row[column.key])}</td>)}
              {extraCell && <td>{extraCell(row)}</td>}
              {actions && <td className="actions">{actions(row)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function EditPane({ resource, selected, setSelected, save, lookups }: { resource: Resource; selected: Row | null; setSelected: (row: Row | null) => void; save: (values: Record<string, string>) => Promise<void>; lookups: Record<string, Option[]> }) {
  const [values, setValues] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const next: Record<string, string> = {};
    resource.fields.forEach((field) => {
      const value = selected?.[field.name];
      if (field.type === "multi" && field.name === "roleIds" && Array.isArray(selected?.roles)) {
        const selectedRoles = new Set((selected.roles as unknown[]).map(String));
        next[field.name] = (lookups.roles ?? []).filter((option) => selectedRoles.has(option.label)).map((option) => option.value).join(",");
      } else {
        next[field.name] = Array.isArray(value) ? value.join(",") : value == null ? "" : String(value);
      }
    });
    setValues(next);
  }, [selected, resource.key, lookups]);

  if (!selected) return <aside className="editPane empty">Select a row or create a new record.</aside>;

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    try {
      await save(values);
    } finally {
      setBusy(false);
    }
  }

  return (
    <aside className="editPane">
      <div className="paneHead">
        <h2>{selected.id ? "Edit" : "New"} {resource.title}</h2>
        <button onClick={() => setSelected(null)}>Close</button>
      </div>
      <form onSubmit={submit} className="formGrid">
        {resource.fields.filter((field) => !(selected.id && "createOnly" in field && field.createOnly)).map((field) => (
          <FieldControl key={field.name} field={field} value={values[field.name] ?? ""} setValue={(value) => setValues({ ...values, [field.name]: value })} lookups={lookups} />
        ))}
        <button className="primary" disabled={busy}>{busy ? "Saving..." : "Save"}</button>
      </form>
    </aside>
  );
}

function FieldControl({ field, value, setValue, lookups }: { field: Field; value: string; setValue: (value: string) => void; lookups: Record<string, Option[]> }) {
  if (field.type === "select") {
    return <label>{field.label}<select value={value} required={field.required} onChange={(e) => setValue(e.target.value)}><option value="">Select...</option>{field.options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>;
  }
  if (field.type === "multi") {
    const selected = new Set(value ? value.split(",") : []);
    return (
      <label>{field.label}
        <select multiple value={[...selected]} onChange={(e) => setValue(Array.from(e.target.selectedOptions).map((option) => option.value).join(","))}>
          {(lookups[field.optionsKey] ?? []).map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
        </select>
      </label>
    );
  }
  return <label>{field.label}<input value={value} required={field.required} type={field.type ?? "text"} onChange={(e) => setValue(e.target.value)} /></label>;
}

function Text<T extends Record<string, string>>({ name, label, values, setValues, type = "text", required }: { name: keyof T & string; label: string; values: T; setValues: (values: T) => void; type?: string; required?: boolean }) {
  return <label>{label}<input value={values[name] ?? ""} required={required} type={type} onChange={(e) => setValues({ ...values, [name]: e.target.value })} /></label>;
}

function Select<T extends Record<string, string>>({ name, label, options, values, setValues, required }: { name: keyof T & string; label: string; options: Option[]; values: T; setValues: (values: T) => void; required?: boolean }) {
  return <label>{label}<select value={values[name] ?? ""} required={required} onChange={(e) => setValues({ ...values, [name]: e.target.value })}><option value="">Select...</option>{options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>;
}

function SectionTitle({ text }: { text: string }) {
  return <h3 className="sectionTitle">{text}</h3>;
}

function buildScreens(lookups: Record<string, Option[]>): Screen[] {
  return [
    { key: "students", title: "Students", group: "Registrar", custom: "students" },
    { key: "enrollments", title: "Enrollments", group: "Registrar", custom: "enrollments" },
    ...buildResources(lookups).map((resource) => ({ key: resource.key, title: resource.title, group: "Admin Setup" as const, resource }))
  ];
}

function buildResources(lookups: Record<string, Option[]>): Resource[] {
  const departments = lookups.departments ?? [];
  const programs = lookups.programs ?? [];
  const schoolYears = lookups.schoolYears ?? [];
  const semesters = lookups.semesters ?? [];
  const users = lookups.users ?? [];
  return [
    {
      key: "users", title: "Users", endpoint: "/api/v1/users", searchable: true, status: "booleanActive",
      fields: [
        { name: "username", label: "Username", required: true },
        { name: "email", label: "Email", type: "email", required: true },
        { name: "fullName", label: "Full name", required: true },
        { name: "password", label: "Password", type: "password" },
        { name: "active", label: "Active", type: "select", options: [{ value: "true", label: "Active" }, { value: "false", label: "Inactive" }] },
        { name: "roleIds", label: "Roles", type: "multi", optionsKey: "roles" }
      ],
      columns: [{ key: "username", label: "Username" }, { key: "email", label: "Email" }, { key: "fullName", label: "Name" }, { key: "roles", label: "Roles" }],
      prepare: (v) => ({ ...clean(v), active: v.active !== "false", roleIds: csv(v.roleIds) })
    },
    {
      key: "departments", title: "Departments", endpoint: "/api/v1/departments", searchable: true, status: "activeStatus",
      fields: [{ name: "departmentCode", label: "Code", required: true }, { name: "departmentName", label: "Name", required: true }, { name: "dean", label: "Dean" }, { name: "description", label: "Description" }, { name: "status", label: "Status", type: "select", options: ACTIVE }],
      columns: [{ key: "departmentCode", label: "Code" }, { key: "departmentName", label: "Name" }, { key: "dean", label: "Dean" }]
    },
    {
      key: "programs", title: "Programs", endpoint: "/api/v1/programs", searchable: true,
      fields: [{ name: "programCode", label: "Code", required: true }, { name: "programName", label: "Name", required: true }, { name: "departmentId", label: "Department", type: "select", options: departments, required: true }, { name: "degreeType", label: "Degree", type: "select", options: DEGREE, required: true }, { name: "programDuration", label: "Duration", type: "number" }, { name: "description", label: "Description" }, { name: "status", label: "Status", type: "select", options: ACTIVE }],
      columns: [{ key: "programCode", label: "Code" }, { key: "programName", label: "Name" }, { key: "departmentCode", label: "Department" }, { key: "degreeType", label: "Degree" }]
    },
    {
      key: "courses", title: "Courses", endpoint: "/api/v1/courses", searchable: true,
      fields: [{ name: "courseCode", label: "Code", required: true }, { name: "courseTitle", label: "Title", required: true }, { name: "courseDescription", label: "Description" }, { name: "lectureHoursPerWeek", label: "Lecture hours", type: "number", required: true }, { name: "laboratoryHoursPerWeek", label: "Lab hours", type: "number", required: true }, { name: "creditUnits", label: "Units", type: "number", required: true }, { name: "courseType", label: "Type", type: "select", options: COURSE, required: true }, { name: "departmentId", label: "Department", type: "select", options: departments, required: true }, { name: "status", label: "Status", type: "select", options: ACTIVE }],
      columns: [{ key: "courseCode", label: "Code" }, { key: "courseTitle", label: "Title" }, { key: "creditUnits", label: "Units" }, { key: "courseType", label: "Type" }]
    },
    {
      key: "faculty", title: "Faculty", endpoint: "/api/v1/faculty", searchable: true, status: "activeStatus",
      fields: [{ name: "employeeNumber", label: "Employee no.", required: true }, { name: "firstName", label: "First name", required: true }, { name: "middleName", label: "Middle name" }, { name: "lastName", label: "Last name", required: true }, { name: "suffix", label: "Suffix" }, { name: "email", label: "Email", type: "email", required: true }, { name: "contactNumber", label: "Contact no." }, { name: "userId", label: "Linked user", type: "select", options: users }, { name: "departmentId", label: "Department", type: "select", options: departments, required: true }, { name: "employmentStatus", label: "Employment", type: "select", options: EMPLOYMENT, required: true }, { name: "facultyType", label: "Type", type: "select", options: FACULTY_TYPE, required: true }, { name: "specialization", label: "Specialization" }, { name: "status", label: "Status", type: "select", options: ACTIVE }],
      columns: [{ key: "employeeNumber", label: "Employee no." }, { key: "lastName", label: "Last name" }, { key: "email", label: "Email" }, { key: "username", label: "User" }]
    },
    {
      key: "rooms", title: "Rooms", endpoint: "/api/v1/rooms", searchable: true, status: "activeStatus",
      fields: [{ name: "roomCode", label: "Code", required: true }, { name: "roomName", label: "Name", required: true }, { name: "capacity", label: "Capacity", type: "number" }, { name: "status", label: "Status", type: "select", options: ACTIVE }],
      columns: [{ key: "roomCode", label: "Code" }, { key: "roomName", label: "Name" }, { key: "capacity", label: "Capacity" }]
    },
    {
      key: "schoolYears", title: "School Years", endpoint: "/api/v1/school-years",
      fields: [{ name: "schoolYear", label: "School year", required: true }, { name: "active", label: "Active", type: "select", options: [{ value: "true", label: "Active" }, { value: "false", label: "Inactive" }] }],
      columns: [{ key: "schoolYear", label: "School year" }, { key: "active", label: "Active" }],
      prepare: (v) => ({ schoolYear: v.schoolYear, active: v.active === "true" })
    },
    {
      key: "semesters", title: "Semesters", endpoint: "/api/v1/semesters",
      fields: [{ name: "name", label: "Name", required: true }, { name: "sortOrder", label: "Sort order", type: "number", required: true }, { name: "active", label: "Active", type: "select", options: [{ value: "true", label: "Active" }, { value: "false", label: "Inactive" }] }],
      columns: [{ key: "name", label: "Name" }, { key: "sortOrder", label: "Sort" }, { key: "active", label: "Active" }],
      prepare: (v) => ({ name: v.name, sortOrder: numberOrZero(v.sortOrder), active: v.active === "true" })
    },
    {
      key: "sections", title: "Sections", endpoint: "/api/v1/sections", searchable: true, status: "activeStatus",
      fields: [{ name: "sectionCode", label: "Code", required: true }, { name: "programId", label: "Program", type: "select", options: programs, required: true }, { name: "schoolYearId", label: "School year", type: "select", options: schoolYears, required: true }, { name: "semesterId", label: "Semester", type: "select", options: semesters, required: true }, { name: "yearLevel", label: "Year level", type: "number", required: true }, { name: "status", label: "Status", type: "select", options: ACTIVE }],
      columns: [{ key: "sectionCode", label: "Code" }, { key: "programCode", label: "Program" }, { key: "schoolYear", label: "School year" }, { key: "semesterName", label: "Semester" }]
    }
  ];
}

function emptyStudent() {
  return {
    studentNumber: "", firstName: "", middleName: "", lastName: "", gender: "", birthdate: "", status: "ACTIVE",
    emailAddress: "", mobileNumber: "", currentAddress: "", emergencyContactName: "", emergencyContactNumber: "",
    programId: "", curriculumId: "", yearLevel: "1", semester: "", sectionId: "", dateAdmitted: new Date().toISOString().slice(0, 10),
    schoolYearAdmitted: "", classification: "NEW", academicStatus: "REGULAR"
  };
}

function flattenStudent(student: Row) {
  const personal = student.personal as Row;
  const contact = student.contact as Row;
  const academic = student.academic as Row;
  return { ...emptyStudent(), ...stringifyRow(personal), ...stringifyRow(contact), ...stringifyRow(academic) };
}

function stringifyRow(row?: Row) {
  return Object.fromEntries(Object.entries(row ?? {}).map(([key, value]) => [key, value == null ? "" : String(value)]));
}

function studentPayload(values: Record<string, string>) {
  return {
    personal: pickClean(values, ["studentNumber", "firstName", "middleName", "lastName", "gender", "birthdate", "status"]),
    contact: pickClean(values, ["emailAddress", "mobileNumber", "currentAddress", "emergencyContactName", "emergencyContactNumber"]),
    family: {},
    educational: {},
    academic: {
      ...pickClean(values, ["programId", "curriculumId", "semester", "sectionId", "dateAdmitted", "schoolYearAdmitted", "classification", "academicStatus"]),
      yearLevel: numberOrZero(values.yearLevel)
    }
  };
}

function pickClean(values: Record<string, string>, keys: string[]) {
  return Object.fromEntries(keys.map((key) => [key, values[key] === "" ? null : values[key]]));
}

function option(item: Row, valueKey: string, labelKeys: string[]) {
  return { value: String(item[valueKey]), label: labelKeys.map((key) => item[key]).filter(Boolean).join(" - ") };
}

function clean(values: Record<string, string>) {
  return Object.fromEntries(Object.entries(values).map(([key, value]) => [key, value === "" ? null : coerce(value)]));
}

function coerce(value: string) {
  if (/^-?\d+(\.\d+)?$/.test(value)) return Number(value);
  return value;
}

function numberOrZero(value: string) {
  return value === "" ? 0 : Number(value);
}

function csv(value: string) {
  return value ? value.split(",").filter(Boolean) : [];
}

function formatCell(value: unknown) {
  if (Array.isArray(value)) return value.join(", ");
  if (typeof value === "boolean") return value ? "Yes" : "No";
  return value == null || value === "" ? "..." : String(value);
}

createRoot(document.getElementById("root")!).render(<App />);
