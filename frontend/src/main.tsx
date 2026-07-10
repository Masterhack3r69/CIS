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
type Screen = { key: string; title: string; group: "Admin Setup" | "Academic Management" | "Registrar" | "Cashier"; resource?: Resource; custom?: "curricula" | "schedules" | "students" | "enrollments" | "fees" | "assessments" };
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
const STUDENT_STATUS = opts(["APPLICANT", "ACTIVE", "ENROLLED", "INACTIVE", "DROPPED", "TRANSFERRED", "GRADUATED", "ARCHIVED"]);
const GENDER = opts(["MALE", "FEMALE", "OTHER"]);
const CLASSIFICATION = opts(["REGULAR", "IRREGULAR", "TRANSFEREE", "RETURNEE", "CROSS_ENROLLEE", "GRADUATING"]);
const ACADEMIC_STATUS = opts(["REGULAR", "IRREGULAR", "PROBATION", "CANDIDATE_FOR_GRADUATION", "GRADUATED", "DISMISSED", "ON_LEAVE"]);
const ENROLLMENT_STATUS = opts(["DRAFT", "VALIDATED", "CONFIRMED", "CANCELLED"]);
const DOC_STATUS = opts(["PENDING", "VERIFIED", "REJECTED"]);
const CURRICULUM_STATUS = opts(["DRAFT", "ACTIVE", "INACTIVE", "ARCHIVED"]);
const REQUIRED_STATUS = opts(["REQUIRED", "OPTIONAL", "ELECTIVE"]);
const SCHEDULE_STATUS = opts(["DRAFT", "ACTIVE", "CANCELLED", "ARCHIVED"]);
const DAYS = opts(["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]);
const ASSESSMENT_STATUS = opts(["DRAFT", "UNPAID", "PAID", "VOID"]);
const FEE_RULE_TYPE = opts(["FIXED", "PER_UNIT"]);

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
    if (response.status === 401) {
      setSession(null);
      window.dispatchEvent(new Event("sis:auth-expired"));
    }
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

  useEffect(() => {
    const clear = () => {
      setMessage("Session expired. Please sign in again.");
      setSessionState(null);
    };
    window.addEventListener("sis:auth-expired", clear);
    return () => window.removeEventListener("sis:auth-expired", clear);
  }, []);

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
    const optionalPage = async <T extends Row | Role>(path: string): Promise<PageResponse<T>> => {
      try {
        return await api<PageResponse<T>>(path, {}, session.accessToken);
      } catch (error) {
        if (error instanceof Error && error.message === "Access denied") {
          return { items: [], page: 0, size: 0, totalElements: 0, totalPages: 0 };
        }
        throw error;
      }
    };
    const [roles, departments, programs, schoolYears, semesters, users, curricula, sections, students, schedules, courses, faculty, rooms] = await Promise.all([
      optionalPage<Role>("/api/v1/roles?size=300"),
      api<PageResponse<Row>>("/api/v1/departments?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/programs?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/school-years?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/semesters?size=300", {}, session.accessToken),
      optionalPage<Row>("/api/v1/users?size=300"),
      api<PageResponse<Row>>("/api/v1/curricula?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/sections?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/students?size=300", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/schedules?size=500&status=ACTIVE", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/courses?size=500", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/faculty?size=500", {}, session.accessToken),
      api<PageResponse<Row>>("/api/v1/rooms?size=500", {}, session.accessToken)
    ]);
    setLookupRows({ curricula: curricula.items, sections: sections.items, students: students.items, schedules: schedules.items, courses: courses.items, faculty: faculty.items, rooms: rooms.items });
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
      schedules: schedules.items.map(scheduleOption),
      courses: courses.items.map((item) => option(item, "id", ["courseCode", "courseTitle"])),
      faculty: faculty.items.map((item) => ({ value: String(item.id), label: `${item.employeeNumber ?? ""} - ${[item.firstName, item.middleName, item.lastName].filter(Boolean).join(" ")}`.replace(" -  - ", " - ") })),
      rooms: rooms.items.map((item) => option(item, "id", ["roomCode", "roomName"]))
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
        {screen.custom === "curricula" && <CurriculaPanel token={session.accessToken} lookups={lookups} lookupRows={lookupRows} setMessage={setMessage} refreshLookups={loadLookups} />}
        {screen.custom === "schedules" && <SchedulesPanel token={session.accessToken} lookups={lookups} setMessage={setMessage} refreshLookups={loadLookups} />}
        {screen.custom === "students" && <StudentsPanel token={session.accessToken} lookups={lookups} setMessage={setMessage} refreshLookups={loadLookups} />}
        {screen.custom === "enrollments" && <EnrollmentsPanel token={session.accessToken} lookups={lookups} lookupRows={lookupRows} setMessage={setMessage} refreshLookups={loadLookups} />}
        {screen.custom === "fees" && <FeeSetupPanel token={session.accessToken} lookups={lookups} setMessage={setMessage} />}
        {screen.custom === "assessments" && <AssessmentsPanel token={session.accessToken} lookups={lookups} setMessage={setMessage} />}
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
    try {
      await api(path, { method: editing ? "PUT" : "POST", body }, token);
      setSelected(null);
      await load();
      await refreshLookups();
      setMessage(`${resource.title} saved`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : `Unable to save ${resource.title}`);
    }
  }

  async function setStatus(row: Row) {
    const body = resource.status === "booleanActive"
      ? { active: !(row.active as boolean) }
      : { status: row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE" };
    try {
      await api(`${resource.endpoint}/${row.id}/status`, { method: "PATCH", body: JSON.stringify(body) }, token);
      await load();
      await refreshLookups();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : `Unable to update ${resource.title} status`);
    }
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
    try {
      const saved = await api<Row>(editing ? `/api/v1/students/${selected?.id}` : "/api/v1/students", { method: editing ? "PUT" : "POST", body }, token);
      await load();
      await refreshLookups();
      setMessage("Student saved");
      if (!editing && saved.personal && typeof saved.personal === "object") {
        const personal = saved.personal as Row;
        setSelected({ id: personal.id });
        setDetail(saved);
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to save student");
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
  const [curriculumCourses, setCurriculumCourses] = useState<Row[]>([]);
  const [academicRecords, setAcademicRecords] = useState<Row[]>([]);
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

  const selectedStudent = (lookupRows.students ?? []).find((student) => String(student.id) === form.studentId);
  const filteredSectionRows = (lookupRows.sections ?? []).filter((section) => {
    if (section.status && section.status !== "ACTIVE") return false;
    if (selectedStudent?.programId && section.programId !== selectedStudent.programId) return false;
    if (selectedStudent?.yearLevel && Number(section.yearLevel) !== Number(selectedStudent.yearLevel)) return false;
    if (form.schoolYearId && section.schoolYearId !== form.schoolYearId) return false;
    if (form.semesterId && section.semesterId !== form.semesterId) return false;
    return true;
  });
  const filteredSectionOptions = filteredSectionRows.map((item) => option(item, "id", ["sectionCode"]));

  useEffect(() => {
    if (!form.sectionId) return;
    if (!filteredSectionRows.some((section) => String(section.id) === form.sectionId)) {
      setForm((current) => ({ ...current, sectionId: "" }));
    }
  }, [form.studentId, form.schoolYearId, form.semesterId, form.sectionId, lookupRows.sections]);

  async function open(row: Row) {
    setSelected(row);
    const next = await api<Row>(`/api/v1/enrollments/${row.id}`, {}, token);
    setDetail(next);
    await loadRequirementContext(next);
  }

  async function loadRequirementContext(enrollment: Row) {
    const studentId = String(enrollment.studentId ?? "");
    if (!studentId) {
      setCurriculumCourses([]);
      setAcademicRecords([]);
      return;
    }
    try {
      const recordsResponse = await api<Row>(`/api/v1/students/${studentId}/academic-records`, {}, token);
      const records = Array.isArray(recordsResponse.records) ? recordsResponse.records as Row[] : [];
      setAcademicRecords(records);
      const curriculumId = String(recordsResponse.curriculumId ?? "");
      if (!curriculumId) {
        setCurriculumCourses([]);
        return;
      }
      const curriculum = await api<Row>(`/api/v1/curricula/${curriculumId}`, {}, token);
      setCurriculumCourses(Array.isArray(curriculum.courses) ? curriculum.courses as Row[] : []);
    } catch {
      setCurriculumCourses([]);
      setAcademicRecords([]);
    }
  }

  async function create(event: FormEvent) {
    event.preventDefault();
    try {
      const created = await api<Row>("/api/v1/enrollments", { method: "POST", body: JSON.stringify(clean(form)) }, token);
      await load();
      await refreshLookups();
      await open(created);
      setMessage("Enrollment draft created");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to create enrollment draft");
    }
  }

  async function action(path: string, message: string) {
    if (!selected?.id) return;
    try {
      const result = await api<Row>(`/api/v1/enrollments/${selected.id}/${path}`, { method: "POST" }, token);
      if (path === "validate") setMessage(`${message}: ${result.valid ? "valid" : "has issues"}`);
      else setMessage(message);
      await load();
      await open(selected);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Request failed");
      await open(selected);
    }
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
  }).map(scheduleOption);
  const validationSubjectIds = validationIssueIds(detail?.validation, "subjectId");
  const validationScheduleIds = validationIssueIds(detail?.validation, "scheduleId");

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
        <div className="paneHead"><h2>Enrollment</h2>{selected && <button onClick={() => { setSelected(null); setDetail(null); setCurriculumCourses([]); setAcademicRecords([]); }}>Close</button>}</div>
        {!selected && (
          <form className="formGrid" onSubmit={create}>
            <Select name="studentId" label="Student" options={lookups.students ?? []} values={form} setValues={setForm} required />
            <Select name="schoolYearId" label="School year" options={lookups.schoolYears ?? []} values={form} setValues={setForm} required />
            <Select name="semesterId" label="Semester" options={lookups.semesters ?? []} values={form} setValues={setForm} required />
            <Select name="sectionId" label="Section" options={filteredSectionOptions} values={form} setValues={setForm} />
            {form.studentId && form.schoolYearId && form.semesterId && filteredSectionOptions.length === 0 && <span className="mutedLine">No matching active sections for this student's program, year level, and selected term.</span>}
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
            <ScheduleRequirementHint
              scheduleId={scheduleId}
              schedules={lookupRows.schedules ?? []}
              curriculumCourses={curriculumCourses}
              academicRecords={academicRecords}
              selectedSubjects={Array.isArray(detail.subjects) ? detail.subjects as Row[] : []}
            />
            <DataTable rows={Array.isArray(detail.subjects) ? detail.subjects as Row[] : []} columns={[
              { key: "courseCode", label: "Course" },
              { key: "courseTitle", label: "Title" },
              { key: "creditUnits", label: "Units" },
              { key: "facultyName", label: "Faculty" },
              { key: "roomCode", label: "Room" },
              { key: "status", label: "Status" }
            ]} rowClassName={(row) => validationSubjectIds.has(String(row.id)) || validationScheduleIds.has(String(row.scheduleId)) ? "rowIssue" : ""} actions={(row) => <button onClick={() => dropSubject(String(row.id))}>Drop</button>} />
            {Boolean(detail.validation) && <ValidationBox validation={detail.validation as Row} />}
          </div>
        )}
      </aside>
    </div>
  );
}

function FeeSetupPanel({ token, lookups, setMessage }: { token: string; lookups: Record<string, Option[]>; setMessage: (message: string) => void }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Row | null>(null);
  const [form, setForm] = useState<Record<string, string>>(emptyFee());
  const [rules, setRules] = useState<Row[]>([emptyFeeRule()]);
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const query = new URLSearchParams({ size: "50" });
      if (search) query.set("search", search);
      const page = await api<PageResponse<Row>>(`/api/v1/fees?${query.toString()}`, {}, token);
      setRows(page.items.map(normalizeFeeRow));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load fees");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function open(row: Row) {
    const detail = await api<Row>(`/api/v1/fees/${row.id}`, {}, token);
    setSelected(detail);
    setForm(flattenFee(detail));
    setRules(Array.isArray(detail.rules) && detail.rules.length > 0 ? (detail.rules as Row[]).map(flattenFeeRule) : [emptyFeeRule()]);
  }

  function startNew() {
    setSelected({});
    setForm(emptyFee());
    setRules([emptyFeeRule()]);
  }

  function updateRule(index: number, key: string, value: string) {
    setRules(rules.map((rule, itemIndex) => itemIndex === index ? { ...rule, [key]: value } : rule));
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    const editing = Boolean(selected?.id);
    try {
      const saved = await api<Row>(editing ? `/api/v1/fees/${selected?.id}` : "/api/v1/fees", {
        method: editing ? "PUT" : "POST",
        body: JSON.stringify(feePayload(form, rules))
      }, token);
      setSelected(saved);
      setForm(flattenFee(saved));
      setRules(Array.isArray(saved.rules) && saved.rules.length > 0 ? (saved.rules as Row[]).map(flattenFeeRule) : [emptyFeeRule()]);
      await load();
      setMessage("Fee saved");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to save fee");
    }
  }

  async function updateStatus(row: Row, status: string) {
    try {
      const saved = await api<Row>(`/api/v1/fees/${row.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status })
      }, token);
      await load();
      if (selected?.id === row.id) {
        setSelected(saved);
        setForm(flattenFee(saved));
        setRules(Array.isArray(saved.rules) && saved.rules.length > 0 ? (saved.rules as Row[]).map(flattenFeeRule) : [emptyFeeRule()]);
      }
      setMessage(`Fee ${status === "ACTIVE" ? "activated" : "deactivated"}`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to update fee status");
    }
  }

  return (
    <div className="contentGrid wide feeGrid">
      <div className="listPane">
        <div className="toolbar">
          <input placeholder="Search fees" value={search} onChange={(e) => setSearch(e.target.value)} onKeyDown={(e) => e.key === "Enter" && load()} />
          <button onClick={load}>Search</button>
          <button className="primary" onClick={startNew}>New</button>
        </div>
        <DataTable rows={rows} busy={busy} columns={[
          { key: "feeCode", label: "Code" },
          { key: "feeName", label: "Name" },
          { key: "status", label: "Status" },
          { key: "ruleCount", label: "Rules" }
        ]} actions={(row) => (
          <>
            <button onClick={() => open(row)}>Open</button>
            <button onClick={() => updateStatus(row, row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE")}>{row.status === "ACTIVE" ? "Deactivate" : "Activate"}</button>
          </>
        )} />
      </div>
      <aside className="editPane detailPane">
        {!selected ? <div className="emptyState">Select a fee or create a new one.</div> : (
          <form className="formGrid" onSubmit={save}>
            <div className="paneHead">
              <h2>{selected.id ? "Fee Detail" : "New Fee"}</h2>
              <button type="button" onClick={() => setSelected(null)}>Close</button>
            </div>
            <div className="formGrid two">
              <Text name="feeCode" label="Fee code" values={form} setValues={setForm} required />
              <Text name="feeName" label="Fee name" values={form} setValues={setForm} required />
              <Select name="status" label="Status" options={ACTIVE} values={form} setValues={setForm} required />
              <Text name="description" label="Description" values={form} setValues={setForm} />
            </div>
            <SectionTitle text="Rules" />
            <div className="ruleEditor">
              {rules.map((rule, index) => (
                <div className="ruleRow" key={index}>
                  <label>Rule type<select value={String(rule.ruleType ?? "FIXED")} onChange={(e) => updateRule(index, "ruleType", e.target.value)}>{FEE_RULE_TYPE.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
                  <label>Amount<input type="number" min="0" step="0.01" value={String(rule.amount ?? "")} onChange={(e) => updateRule(index, "amount", e.target.value)} required /></label>
                  <label>Program<select value={String(rule.programId ?? "")} onChange={(e) => updateRule(index, "programId", e.target.value)}><option value="">Any program</option>{(lookups.programs ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
                  <label>School year<select value={String(rule.schoolYearId ?? "")} onChange={(e) => updateRule(index, "schoolYearId", e.target.value)}><option value="">Any SY</option>{(lookups.schoolYears ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
                  <label>Semester<select value={String(rule.semesterId ?? "")} onChange={(e) => updateRule(index, "semesterId", e.target.value)}><option value="">Any semester</option>{(lookups.semesters ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
                  <label>Year level<input type="number" min="1" value={String(rule.yearLevel ?? "")} placeholder="Any" onChange={(e) => updateRule(index, "yearLevel", e.target.value)} /></label>
                  <label>Status<select value={String(rule.status ?? "ACTIVE")} onChange={(e) => updateRule(index, "status", e.target.value)}>{ACTIVE.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select></label>
                  <button type="button" onClick={() => setRules(rules.length === 1 ? [emptyFeeRule()] : rules.filter((_, itemIndex) => itemIndex !== index))}>Remove</button>
                </div>
              ))}
              <button type="button" onClick={() => setRules([...rules, emptyFeeRule()])}>Add Rule</button>
            </div>
            <button className="primary">Save Fee</button>
          </form>
        )}
      </aside>
    </div>
  );
}

function AssessmentsPanel({ token, lookups, setMessage }: { token: string; lookups: Record<string, Option[]>; setMessage: (message: string) => void }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [selected, setSelected] = useState<Row | null>(null);
  const [filters, setFilters] = useState<Record<string, string>>({ search: "", studentId: "", schoolYearId: "", semesterId: "", status: "" });
  const [statusForm, setStatusForm] = useState({ status: "UNPAID", remarks: "" });
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const query = new URLSearchParams({ size: "50" });
      Object.entries(filters).forEach(([key, value]) => { if (value) query.set(key, value); });
      const page = await api<PageResponse<Row>>(`/api/v1/assessments?${query.toString()}`, {}, token);
      setRows(page.items.map(normalizeAssessmentRow));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load assessments");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function open(row: Row) {
    const detail = await api<Row>(`/api/v1/assessments/${row.id}`, {}, token);
    setSelected(normalizeAssessmentRow(detail));
    setStatusForm({ status: String(detail.status ?? "UNPAID"), remarks: String(detail.remarks ?? "") });
  }

  async function recalculate() {
    if (!selected?.id) return;
    try {
      const next = await api<Row>(`/api/v1/assessments/${selected.id}/recalculate`, { method: "POST" }, token);
      setSelected(normalizeAssessmentRow(next));
      await load();
      setMessage("Assessment recalculated");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to recalculate assessment");
    }
  }

  async function updateStatus(event: FormEvent) {
    event.preventDefault();
    if (!selected?.id) return;
    try {
      const next = await api<Row>(`/api/v1/assessments/${selected.id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: statusForm.status, remarks: statusForm.remarks || null })
      }, token);
      setSelected(normalizeAssessmentRow(next));
      await load();
      setMessage("Assessment status updated");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to update assessment status");
    }
  }

  return (
    <div className="contentGrid wide cashierGrid">
      <div className="listPane">
        <div className="toolbar filterBar">
          <input placeholder="Search assessment/student" value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} onKeyDown={(e) => e.key === "Enter" && load()} />
          <select value={filters.studentId} onChange={(e) => setFilters({ ...filters, studentId: e.target.value })}><option value="">All students</option>{(lookups.students ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.schoolYearId} onChange={(e) => setFilters({ ...filters, schoolYearId: e.target.value })}><option value="">All school years</option>{(lookups.schoolYears ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.semesterId} onChange={(e) => setFilters({ ...filters, semesterId: e.target.value })}><option value="">All semesters</option>{(lookups.semesters ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}><option value="">All statuses</option>{ASSESSMENT_STATUS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <button onClick={load}>Filter</button>
        </div>
        <DataTable rows={rows} busy={busy} columns={[
          { key: "assessmentNumber", label: "Assessment" },
          { key: "studentNumber", label: "Student no." },
          { key: "studentName", label: "Name" },
          { key: "termLabel", label: "Term" },
          { key: "status", label: "Status" },
          { key: "totalUnits", label: "Units" },
          { key: "totalAmountText", label: "Total" }
        ]} actions={(row) => <button onClick={() => open(row)}>Open</button>} />
      </div>
      <aside className="editPane detailPane">
        {!selected ? <div className="emptyState">Select an assessment to view charges.</div> : (
          <div className="detailStack">
            <div className="paneHead">
              <h2>{String(selected.assessmentNumber)}</h2>
              <button onClick={() => setSelected(null)}>Close</button>
            </div>
            <div className="summaryBox assessmentTotal">
              <strong>{String(selected.studentName)}</strong>
              <span>{String(selected.studentNumber)} / {String(selected.termLabel)} / {String(selected.status)}</span>
              <span>Units: {String(selected.totalUnits)} / Total: {String(selected.totalAmountText)}</span>
              {Boolean(selected.remarks) && <span>Remarks: {String(selected.remarks)}</span>}
            </div>
            <div className="actions wrap">
              <button onClick={recalculate} disabled={selected.status === "PAID" || selected.status === "VOID"}>Recalculate</button>
            </div>
            <form className="formGrid" onSubmit={updateStatus}>
              <Select name="status" label="Status" options={ASSESSMENT_STATUS} values={statusForm} setValues={setStatusForm} required />
              <Text name="remarks" label="Remarks" values={statusForm} setValues={setStatusForm} />
              <button className="primary">Update Status</button>
            </form>
            <DataTable rows={Array.isArray(selected.items) ? normalizeAssessmentItems(selected.items as Row[]) : []} columns={[
              { key: "sortOrder", label: "#" },
              { key: "feeCode", label: "Code" },
              { key: "feeName", label: "Fee" },
              { key: "ruleType", label: "Rule" },
              { key: "quantity", label: "Qty" },
              { key: "unitAmountText", label: "Unit" },
              { key: "lineAmountText", label: "Line" }
            ]} />
          </div>
        )}
      </aside>
    </div>
  );
}

function SchedulesPanel({ token, lookups, setMessage, refreshLookups }: { token: string; lookups: Record<string, Option[]>; setMessage: (message: string) => void; refreshLookups: () => Promise<void> }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [selected, setSelected] = useState<Row | null>(null);
  const [form, setForm] = useState<Record<string, string>>(emptySchedule());
  const [meetings, setMeetings] = useState<Row[]>([emptyMeeting()]);
  const [conflicts, setConflicts] = useState<Row[]>([]);
  const [filters, setFilters] = useState<Record<string, string>>({ search: "", schoolYearId: "", semesterId: "", programId: "", sectionId: "", facultyId: "", roomId: "", courseId: "", dayOfWeek: "", status: "" });
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const query = new URLSearchParams({ size: "50" });
      Object.entries(filters).forEach(([key, value]) => { if (value) query.set(key, value); });
      const page = await api<PageResponse<Row>>(`/api/v1/schedules?${query.toString()}`, {}, token);
      setRows(page.items.map(normalizeScheduleRow));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load schedules");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function open(row: Row) {
    const detail = await api<Row>(`/api/v1/schedules/${row.id}`, {}, token);
    setSelected(detail);
    setForm(flattenSchedule(detail));
    setMeetings(Array.isArray(detail.meetings) && detail.meetings.length > 0 ? (detail.meetings as Row[]).map(flattenMeeting) : [emptyMeeting()]);
    setConflicts([]);
  }

  function startNew() {
    setSelected({});
    setForm(emptySchedule());
    setMeetings([emptyMeeting()]);
    setConflicts([]);
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    const editing = Boolean(selected?.id);
    await api(editing ? `/api/v1/schedules/${selected?.id}` : "/api/v1/schedules", {
      method: editing ? "PUT" : "POST",
      body: JSON.stringify(schedulePayload(form, meetings))
    }, token);
    await load();
    await refreshLookups();
    setSelected(null);
    setMessage("Schedule saved");
  }

  async function checkConflict() {
    const response = await api<Row>("/api/v1/schedules/check-conflict", {
      method: "POST",
      body: JSON.stringify({
        ignoreScheduleId: selected?.id ?? null,
        sectionId: form.sectionId,
        facultyId: form.facultyId,
        roomId: form.roomId,
        schoolYearId: form.schoolYearId,
        semesterId: form.semesterId,
        meetings: meetingsPayload(meetings)
      })
    }, token);
    const next = Array.isArray(response.conflicts) ? response.conflicts as Row[] : [];
    setConflicts(next.map(normalizeConflictRow));
    setMessage(response.hasConflicts ? "Schedule conflicts found" : "No schedule conflicts found");
  }

  async function archive(row: Row) {
    await api(`/api/v1/schedules/${row.id}`, { method: "DELETE" }, token);
    await load();
    await refreshLookups();
    if (selected?.id === row.id) setSelected(null);
    setMessage("Schedule archived");
  }

  function updateMeeting(index: number, key: string, value: string) {
    setMeetings(meetings.map((meeting, itemIndex) => itemIndex === index ? { ...meeting, [key]: value } : meeting));
  }

  return (
    <div className="contentGrid wide scheduleGrid">
      <div className="listPane">
        <div className="toolbar filterBar">
          <input placeholder="Search schedules" value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} onKeyDown={(e) => e.key === "Enter" && load()} />
          <select value={filters.schoolYearId} onChange={(e) => setFilters({ ...filters, schoolYearId: e.target.value })}><option value="">All school years</option>{(lookups.schoolYears ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.semesterId} onChange={(e) => setFilters({ ...filters, semesterId: e.target.value })}><option value="">All semesters</option>{(lookups.semesters ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.programId} onChange={(e) => setFilters({ ...filters, programId: e.target.value })}><option value="">All programs</option>{(lookups.programs ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.sectionId} onChange={(e) => setFilters({ ...filters, sectionId: e.target.value })}><option value="">All sections</option>{(lookups.sections ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.facultyId} onChange={(e) => setFilters({ ...filters, facultyId: e.target.value })}><option value="">All faculty</option>{(lookups.faculty ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.roomId} onChange={(e) => setFilters({ ...filters, roomId: e.target.value })}><option value="">All rooms</option>{(lookups.rooms ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.courseId} onChange={(e) => setFilters({ ...filters, courseId: e.target.value })}><option value="">All courses</option>{(lookups.courses ?? []).map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.dayOfWeek} onChange={(e) => setFilters({ ...filters, dayOfWeek: e.target.value })}><option value="">All days</option>{DAYS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}><option value="">All statuses</option>{SCHEDULE_STATUS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
          <button onClick={load}>Filter</button>
          <button className="primary" onClick={startNew}>New</button>
        </div>
        <DataTable rows={rows} busy={busy} columns={[
          { key: "courseLabel", label: "Course" },
          { key: "sectionCode", label: "Section" },
          { key: "facultyName", label: "Faculty" },
          { key: "roomCode", label: "Room" },
          { key: "termLabel", label: "Term" },
          { key: "meetingsText", label: "Meetings" },
          { key: "capacity", label: "Cap." },
          { key: "status", label: "Status" }
        ]} actions={(row) => (
          <>
            <button onClick={() => open(row)}>Open</button>
            <button onClick={() => archive(row)}>Archive</button>
          </>
        )} />
      </div>
      <aside className="editPane detailPane">
        {!selected ? <div className="emptyState">Select a schedule or create a new one.</div> : (
          <>
            <div className="paneHead"><h2>{selected.id ? "Schedule Detail" : "New Schedule"}</h2><button onClick={() => setSelected(null)}>Close</button></div>
            <form className="formGrid two" onSubmit={save}>
              <Select name="sectionId" label="Section" options={lookups.sections ?? []} values={form} setValues={setForm} required />
              <Select name="courseId" label="Course" options={lookups.courses ?? []} values={form} setValues={setForm} required />
              <Select name="facultyId" label="Faculty" options={lookups.faculty ?? []} values={form} setValues={setForm} required />
              <Select name="roomId" label="Room" options={lookups.rooms ?? []} values={form} setValues={setForm} required />
              <Select name="schoolYearId" label="School year" options={lookups.schoolYears ?? []} values={form} setValues={setForm} required />
              <Select name="semesterId" label="Semester" options={lookups.semesters ?? []} values={form} setValues={setForm} required />
              <Text name="capacity" label="Capacity" type="number" values={form} setValues={setForm} />
              <Select name="status" label="Status" options={SCHEDULE_STATUS} values={form} setValues={setForm} required />
              <SectionTitle text="Meetings" />
              <div className="meetingEditor">
                {meetings.map((meeting, index) => (
                  <div className="meetingRow" key={index}>
                    <select value={String(meeting.dayOfWeek ?? "")} onChange={(e) => updateMeeting(index, "dayOfWeek", e.target.value)} required><option value="">Day...</option>{DAYS.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}</select>
                    <input type="time" value={String(meeting.startTime ?? "")} onChange={(e) => updateMeeting(index, "startTime", e.target.value)} required />
                    <input type="time" value={String(meeting.endTime ?? "")} onChange={(e) => updateMeeting(index, "endTime", e.target.value)} required />
                    <button type="button" onClick={() => setMeetings(meetings.filter((_, itemIndex) => itemIndex !== index))} disabled={meetings.length === 1}>Remove</button>
                  </div>
                ))}
                <button type="button" onClick={() => setMeetings([...meetings, emptyMeeting()])}>Add Meeting</button>
              </div>
              <button type="button" onClick={checkConflict}>Check Conflict</button>
              <button className="primary">Save Schedule</button>
            </form>
            {conflicts.length > 0 && (
              <section className="summaryBox conflictBox">
                <strong>Conflicts</strong>
                <DataTable rows={conflicts} columns={[
                  { key: "conflictType", label: "Type" },
                  { key: "courseLabel", label: "Course" },
                  { key: "sectionCode", label: "Section" },
                  { key: "facultyName", label: "Faculty" },
                  { key: "roomCode", label: "Room" },
                  { key: "timeLabel", label: "Time" }
                ]} />
              </section>
            )}
          </>
        )}
      </aside>
    </div>
  );
}

function CurriculaPanel({ token, lookups, lookupRows, setMessage, refreshLookups }: { token: string; lookups: Record<string, Option[]>; lookupRows: Record<string, Row[]>; setMessage: (message: string) => void; refreshLookups: () => Promise<void> }) {
  const [rows, setRows] = useState<Row[]>([]);
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Row | null>(null);
  const [detail, setDetail] = useState<Row | null>(null);
  const [checklist, setChecklist] = useState<Row | null>(null);
  const [mode, setMode] = useState<"manage" | "checklist">("manage");
  const [header, setHeader] = useState(emptyCurriculum());
  const [courseForm, setCourseForm] = useState(emptyCurriculumCourse());
  const [editingCourseId, setEditingCourseId] = useState("");
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const page = await api<PageResponse<Row>>(`/api/v1/curricula?size=50${search ? `&search=${encodeURIComponent(search)}` : ""}`, {}, token);
      setRows(page.items);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to load curricula");
    } finally {
      setBusy(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function open(row: Row) {
    setSelected(row);
    const next = await api<Row>(`/api/v1/curricula/${row.id}`, {}, token);
    setDetail(next);
    setChecklist(null);
    setMode("manage");
    setHeader(flattenCurriculum(next.curriculum as Row));
    setCourseForm(emptyCurriculumCourse());
    setEditingCourseId("");
  }

  async function reloadDetail(curriculumId = String(selected?.id ?? "")) {
    if (!curriculumId) return;
    const next = await api<Row>(`/api/v1/curricula/${curriculumId}`, {}, token);
    setDetail(next);
    setSelected(next.curriculum as Row);
    setHeader(flattenCurriculum(next.curriculum as Row));
  }

  async function saveHeader(event: FormEvent) {
    event.preventDefault();
    const editing = Boolean(selected?.id);
    const saved = await api<Row>(editing ? `/api/v1/curricula/${selected?.id}` : "/api/v1/curricula", {
      method: editing ? "PUT" : "POST",
      body: JSON.stringify(clean(header))
    }, token);
    await load();
    await refreshLookups();
    if (editing) await reloadDetail(String(selected?.id));
    else await open(saved);
    setMessage("Curriculum saved");
  }

  async function activate() {
    if (!selected?.id) return;
    await api<Row>(`/api/v1/curricula/${selected.id}/activate`, { method: "POST" }, token);
    await load();
    await refreshLookups();
    await reloadDetail(String(selected.id));
    setMessage("Curriculum activated");
  }

  async function loadChecklist() {
    if (!selected?.id) return;
    setChecklist(await api<Row>(`/api/v1/curricula/${selected.id}/checklist`, {}, token));
    setMode("checklist");
  }

  async function saveCourse(event: FormEvent) {
    event.preventDefault();
    if (!selected?.id) return;
    const body = JSON.stringify(curriculumCoursePayload(courseForm));
    await api(
      editingCourseId ? `/api/v1/curricula/${selected.id}/courses/${editingCourseId}` : `/api/v1/curricula/${selected.id}/courses`,
      { method: editingCourseId ? "PUT" : "POST", body },
      token
    );
    setCourseForm(emptyCurriculumCourse());
    setEditingCourseId("");
    await reloadDetail(String(selected.id));
    if (mode === "checklist") await loadChecklist();
    setMessage("Curriculum course saved");
  }

  async function deleteCourse(courseId: string) {
    if (!selected?.id) return;
    await api(`/api/v1/curricula/${selected.id}/courses/${courseId}`, { method: "DELETE" }, token);
    await reloadDetail(String(selected.id));
    setCourseForm(emptyCurriculumCourse());
    setEditingCourseId("");
    setMessage("Curriculum course removed");
  }

  function editCourse(row: Row) {
    setMode("manage");
    setEditingCourseId(String(row.id));
    setCourseForm(flattenCurriculumCourse(row));
  }

  const assignedCourses = Array.isArray(detail?.courses) ? detail?.courses as Row[] : [];
  const linkOptions = assignedCourses
    .filter((course) => String(course.courseId) !== courseForm.courseId)
    .map((course) => ({ value: String(course.courseId), label: `${course.courseCode} - ${course.courseTitle}` }));
  const terms = Array.isArray(checklist?.terms) ? checklist.terms as Row[] : [];

  return (
    <div className="contentGrid wide curriculumGrid">
      <div className="listPane">
        <div className="toolbar">
          <input placeholder="Search curricula" value={search} onChange={(e) => setSearch(e.target.value)} onKeyDown={(e) => e.key === "Enter" && load()} />
          <button onClick={load}>Search</button>
          <button className="primary" onClick={() => { setSelected({}); setDetail(null); setChecklist(null); setHeader(emptyCurriculum()); setMode("manage"); }}>New</button>
        </div>
        <DataTable rows={rows} busy={busy} columns={[
          { key: "curriculumCode", label: "Code" },
          { key: "curriculumName", label: "Name" },
          { key: "programCode", label: "Program" },
          { key: "effectiveSchoolYear", label: "Effective SY" },
          { key: "version", label: "Version" },
          { key: "status", label: "Status" }
        ]} actions={(row) => <button onClick={() => open(row)}>Open</button>} />
      </div>
      <aside className="editPane detailPane">
        {!selected ? <div className="emptyState">Select a curriculum or create a new one.</div> : (
          <>
            <div className="paneHead">
              <h2>{selected.id ? "Curriculum Detail" : "New Curriculum"}</h2>
              <button onClick={() => { setSelected(null); setDetail(null); setChecklist(null); }}>Close</button>
            </div>
            <form className="formGrid two" onSubmit={saveHeader}>
              <Select name="programId" label="Program" options={lookups.programs ?? []} values={header} setValues={setHeader} required />
              <Text name="curriculumCode" label="Curriculum code" values={header} setValues={setHeader} required />
              <Text name="curriculumName" label="Curriculum name" values={header} setValues={setHeader} required />
              <Text name="effectiveSchoolYear" label="Effective school year" values={header} setValues={setHeader} required />
              <Text name="version" label="Version" values={header} setValues={setHeader} required />
              <Select name="status" label="Status" options={CURRICULUM_STATUS} values={header} setValues={setHeader} required />
              <Text name="description" label="Description" values={header} setValues={setHeader} />
              <button className="primary">Save Header</button>
            </form>
            {selected.id && (
              <div className="detailStack">
                <div className="actions wrap">
                  <button onClick={() => { setMode("manage"); setChecklist(null); }}>Manage Courses</button>
                  <button onClick={loadChecklist}>Checklist</button>
                  <button onClick={activate}>Activate</button>
                </div>
                {mode === "manage" && (
                  <>
                    <form className="formGrid two" onSubmit={saveCourse}>
                      <SectionTitle text={editingCourseId ? "Edit Curriculum Course" : "Add Curriculum Course"} />
                      <Text name="yearLevel" label="Year level" type="number" values={courseForm} setValues={setCourseForm} required />
                      <Text name="semester" label="Semester" values={courseForm} setValues={setCourseForm} required />
                      <Select name="courseId" label="Course" options={lookups.courses ?? []} values={courseForm} setValues={setCourseForm} required />
                      <Text name="sortOrder" label="Sort order" type="number" values={courseForm} setValues={setCourseForm} required />
                      <Select name="requiredStatus" label="Required status" options={REQUIRED_STATUS} values={courseForm} setValues={setCourseForm} required />
                      <MultiSelect name="prerequisiteCourseIds" label="Prerequisites" options={linkOptions} values={courseForm} setValues={setCourseForm} />
                      <MultiSelect name="corequisiteCourseIds" label="Corequisites" options={linkOptions} values={courseForm} setValues={setCourseForm} />
                      <button className="primary">{editingCourseId ? "Update Course" : "Add Course"}</button>
                      {editingCourseId && <button type="button" onClick={() => { setEditingCourseId(""); setCourseForm(emptyCurriculumCourse()); }}>Cancel Edit</button>}
                    </form>
                    <GroupedCurriculumCourses courses={assignedCourses} editCourse={editCourse} deleteCourse={(id) => deleteCourse(id)} />
                  </>
                )}
                {mode === "checklist" && (
                  <div className="detailStack">
                    {terms.length === 0 ? <div className="emptyState">Checklist is empty.</div> : terms.map((term, index) => (
                      <CurriculumTerm key={`${term.yearLevel}-${term.semester}-${index}`} term={term} />
                    ))}
                  </div>
                )}
              </div>
            )}
          </>
        )}
      </aside>
    </div>
  );
}

function GroupedCurriculumCourses({ courses, editCourse, deleteCourse }: { courses: Row[]; editCourse: (row: Row) => void; deleteCourse: (id: string) => void }) {
  const groups = groupCurriculumCourses(courses);
  if (courses.length === 0) return <div className="emptyState">No courses assigned yet.</div>;
  return (
    <div className="detailStack">
      {groups.map((group) => (
        <section className="summaryBox curriculumTerm" key={group.key}>
          <strong>Year {group.yearLevel} / {group.semester}</strong>
          <DataTable rows={group.courses} columns={[
            { key: "sortOrder", label: "#" },
            { key: "courseCode", label: "Course" },
            { key: "courseTitle", label: "Title" },
            { key: "lectureHoursPerWeek", label: "Lec" },
            { key: "laboratoryHoursPerWeek", label: "Lab" },
            { key: "creditUnits", label: "Units" },
            { key: "requiredStatus", label: "Type" },
            { key: "prerequisitesText", label: "Prereq" },
            { key: "corequisitesText", label: "Coreq" }
          ]} actions={(row) => (
            <>
              <button onClick={() => editCourse(row)}>Edit</button>
              <button onClick={() => deleteCourse(String(row.id))}>Delete</button>
            </>
          )} />
        </section>
      ))}
    </div>
  );
}

function CurriculumTerm({ term }: { term: Row }) {
  const courses = Array.isArray(term.courses) ? normalizeCurriculumCourses(term.courses as Row[]) : [];
  return (
    <section className="summaryBox curriculumTerm">
      <strong>Year {String(term.yearLevel)} / {String(term.semester)}</strong>
      <span>Lecture: {String(term.totalLectureHours)} / Lab: {String(term.totalLaboratoryHours)} / Units: {String(term.totalCreditUnits)}</span>
      <DataTable rows={courses} columns={[
        { key: "sortOrder", label: "#" },
        { key: "courseCode", label: "Course" },
        { key: "courseTitle", label: "Title" },
        { key: "lectureHoursPerWeek", label: "Lec" },
        { key: "laboratoryHoursPerWeek", label: "Lab" },
        { key: "creditUnits", label: "Units" },
        { key: "requiredStatus", label: "Type" },
        { key: "prerequisitesText", label: "Prereq" },
        { key: "corequisitesText", label: "Coreq" }
      ]} />
    </section>
  );
}

function ValidationBox({ validation }: { validation: Row }) {
  const blocking = Array.isArray(validation.blockingIssues) ? validation.blockingIssues as Row[] : [];
  const warnings = Array.isArray(validation.warnings) ? validation.warnings as Row[] : [];
  return (
    <div className={`summaryBox validationBox ${validation.valid ? "valid" : "invalid"}`}>
      <strong>Validation: {validation.valid ? "Valid" : "Needs attention"}</strong>
      {blocking.length > 0 && <span className="issueGroup">Blocking</span>}
      {blocking.map((issue, index) => <span className="issueLine blocking" key={`blocking-${index}`}>{String(issue.code)}: {String(issue.message)}</span>)}
      {warnings.length > 0 && <span className="issueGroup">Warnings</span>}
      {warnings.map((issue, index) => <span className="issueLine warning" key={`warning-${index}`}>{String(issue.code)}: {String(issue.message)}</span>)}
      {[...blocking, ...warnings].length === 0 && <span>No issues.</span>}
    </div>
  );
}

function ScheduleRequirementHint({ scheduleId, schedules, curriculumCourses, academicRecords, selectedSubjects }: { scheduleId: string; schedules: Row[]; curriculumCourses: Row[]; academicRecords: Row[]; selectedSubjects: Row[] }) {
  if (!scheduleId) return null;
  const schedule = schedules.find((item) => String(item.id) === scheduleId);
  const courseId = String(schedule?.courseId ?? "");
  const curriculumCourse = curriculumCourses.find((item) => String(item.courseId) === courseId);
  if (!schedule || !curriculumCourse) {
    return <div className="summaryBox requirementHint"><strong>Requirements</strong><span>No curriculum requirement details found for this schedule.</span></div>;
  }
  const passedCourseIds = new Set(academicRecords
    .filter((record) => record.remark === "PASSED" && ["APPROVED", "LOCKED"].includes(String(record.gradeStatus)))
    .map((record) => String(record.courseId)));
  const activeSelectedCourseIds = new Set(selectedSubjects
    .filter((subject) => subject.status === "ENROLLED")
    .map((subject) => String(subject.courseId)));
  const prerequisites = Array.isArray(curriculumCourse.prerequisites) ? curriculumCourse.prerequisites as Row[] : [];
  const corequisites = Array.isArray(curriculumCourse.corequisites) ? curriculumCourse.corequisites as Row[] : [];
  const requirementRows = [
    ...prerequisites.map((course) => ({
      type: "Prerequisite",
      course,
      satisfied: passedCourseIds.has(String(course.id)),
      note: passedCourseIds.has(String(course.id)) ? "passed" : "missing"
    })),
    ...corequisites.map((course) => {
      const passed = passedCourseIds.has(String(course.id));
      const selected = activeSelectedCourseIds.has(String(course.id));
      return {
        type: "Corequisite",
        course,
        satisfied: passed || selected,
        note: passed ? "passed" : selected ? "selected in draft" : "missing"
      };
    })
  ];

  return (
    <div className="summaryBox requirementHint">
      <strong>Requirements for {String(schedule.courseCode ?? "")}</strong>
      {requirementRows.length === 0 ? <span>No prerequisites or corequisites.</span> : requirementRows.map((item, index) => (
        <span className={item.satisfied ? "requirementOk" : "requirementMissing"} key={index}>
          {item.type}: {String(item.course.courseCode ?? "")} - {item.note}
        </span>
      ))}
    </div>
  );
}

function DataTable({ rows, columns, busy, actions, extraHeader, extraCell, rowClassName }: { rows: Row[]; columns: { key: string; label: string }[]; busy?: boolean; actions?: (row: Row) => React.ReactNode; extraHeader?: string; extraCell?: (row: Row) => React.ReactNode; rowClassName?: (row: Row) => string }) {
  return (
    <div className="tableWrap">
      <table>
        <thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}{extraHeader && <th>{extraHeader}</th>}{actions && <th></th>}</tr></thead>
        <tbody>
          {busy ? <tr><td colSpan={columns.length + 2}>Loading...</td></tr> : rows.length === 0 ? <tr><td colSpan={columns.length + 2}>No records found.</td></tr> : rows.map((row, index) => (
            <tr className={rowClassName?.(row)} key={String(row.id ?? index)}>
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

function MultiSelect<T extends Record<string, string>>({ name, label, options, values, setValues }: { name: keyof T & string; label: string; options: Option[]; values: T; setValues: (values: T) => void }) {
  const selected = new Set((values[name] ?? "").split(",").filter(Boolean));
  return (
    <label>{label}
      <select multiple value={[...selected]} onChange={(e) => setValues({ ...values, [name]: Array.from(e.target.selectedOptions).map((option) => option.value).join(",") })}>
        {options.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
      </select>
    </label>
  );
}

function SectionTitle({ text }: { text: string }) {
  return <h3 className="sectionTitle">{text}</h3>;
}

function buildScreens(lookups: Record<string, Option[]>): Screen[] {
  return [
    { key: "curricula", title: "Curricula", group: "Academic Management", custom: "curricula" },
    { key: "schedules", title: "Schedules", group: "Academic Management", custom: "schedules" },
    { key: "students", title: "Students", group: "Registrar", custom: "students" },
    { key: "enrollments", title: "Enrollments", group: "Registrar", custom: "enrollments" },
    { key: "fees", title: "Fee Setup", group: "Cashier", custom: "fees" },
    { key: "assessments", title: "Assessments", group: "Cashier", custom: "assessments" },
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
    schoolYearAdmitted: "", classification: "REGULAR", academicStatus: "REGULAR"
  };
}

function emptyCurriculum() {
  return { programId: "", curriculumCode: "", curriculumName: "", effectiveSchoolYear: "", version: "", status: "DRAFT", description: "" };
}

function emptyCurriculumCourse() {
  return { yearLevel: "1", semester: "First Semester", courseId: "", sortOrder: "1", requiredStatus: "REQUIRED", prerequisiteCourseIds: "", corequisiteCourseIds: "" };
}

function emptySchedule() {
  return { sectionId: "", courseId: "", facultyId: "", roomId: "", schoolYearId: "", semesterId: "", capacity: "40", status: "DRAFT" };
}

function emptyFee() {
  return { feeCode: "", feeName: "", description: "", status: "ACTIVE" };
}

function emptyFeeRule(): Row {
  return { ruleType: "FIXED", amount: "", programId: "", schoolYearId: "", semesterId: "", yearLevel: "", status: "ACTIVE" };
}

function emptyMeeting(): Row {
  return { dayOfWeek: "MONDAY", startTime: "08:00", endTime: "09:00" };
}

function flattenSchedule(schedule: Row) {
  return { ...emptySchedule(), ...stringifyRow(schedule) };
}

function flattenMeeting(meeting: Row): Row {
  return {
    dayOfWeek: String(meeting.dayOfWeek ?? "MONDAY"),
    startTime: String(meeting.startTime ?? "08:00").slice(0, 5),
    endTime: String(meeting.endTime ?? "09:00").slice(0, 5)
  };
}

function schedulePayload(values: Record<string, string>, meetings: Row[]) {
  return {
    sectionId: values.sectionId,
    courseId: values.courseId,
    facultyId: values.facultyId,
    roomId: values.roomId,
    schoolYearId: values.schoolYearId,
    semesterId: values.semesterId,
    capacity: numberOrNull(values.capacity),
    status: values.status,
    meetings: meetingsPayload(meetings)
  };
}

function meetingsPayload(meetings: Row[]) {
  return meetings.map((meeting) => ({
    dayOfWeek: String(meeting.dayOfWeek),
    startTime: String(meeting.startTime),
    endTime: String(meeting.endTime)
  }));
}

function normalizeScheduleRow(row: Row): Row {
  return {
    ...row,
    courseLabel: `${row.courseCode ?? ""} - ${row.courseTitle ?? ""}`,
    termLabel: `${row.schoolYear ?? ""} / ${row.semesterName ?? ""}`,
    meetingsText: meetingsText(row.meetings)
  };
}

function normalizeConflictRow(row: Row): Row {
  return {
    ...row,
    courseLabel: `${row.courseCode ?? ""} - ${row.courseTitle ?? ""}`,
    timeLabel: `${row.dayOfWeek ?? ""} ${timeRange(row.existingStartTime, row.existingEndTime)} conflicts with ${timeRange(row.requestedStartTime, row.requestedEndTime)}`
  };
}

function scheduleOption(item: Row) {
  return { value: String(item.id), label: `${item.courseCode ?? ""} - ${item.courseTitle ?? ""} / ${item.sectionCode ?? ""} / ${item.facultyName ?? ""} / ${item.roomCode ?? ""} / ${meetingsText(item.meetings)}` };
}

function normalizeFeeRow(row: Row): Row {
  const rules = Array.isArray(row.rules) ? row.rules as Row[] : [];
  return {
    ...row,
    ruleCount: rules.length,
    rulesText: rules.map((rule) => feeRuleLabel(rule)).join("; ")
  };
}

function flattenFee(fee: Row) {
  return {
    ...emptyFee(),
    feeCode: String(fee.feeCode ?? ""),
    feeName: String(fee.feeName ?? ""),
    description: String(fee.description ?? ""),
    status: String(fee.status ?? "ACTIVE")
  };
}

function flattenFeeRule(rule: Row): Row {
  return {
    ruleType: String(rule.ruleType ?? "FIXED"),
    amount: String(rule.amount ?? ""),
    programId: String(rule.programId ?? ""),
    schoolYearId: String(rule.schoolYearId ?? ""),
    semesterId: String(rule.semesterId ?? ""),
    yearLevel: rule.yearLevel == null ? "" : String(rule.yearLevel),
    status: String(rule.status ?? "ACTIVE")
  };
}

function feePayload(values: Record<string, string>, rules: Row[]) {
  return {
    feeCode: values.feeCode,
    feeName: values.feeName,
    description: values.description || null,
    status: values.status,
    rules: rules
      .filter((rule) => String(rule.amount ?? "") !== "")
      .map((rule) => ({
        ruleType: String(rule.ruleType ?? "FIXED"),
        amount: numberOrZero(String(rule.amount ?? "")),
        programId: nullableString(rule.programId),
        schoolYearId: nullableString(rule.schoolYearId),
        semesterId: nullableString(rule.semesterId),
        yearLevel: numberOrNull(String(rule.yearLevel ?? "")),
        status: String(rule.status ?? "ACTIVE")
      }))
  };
}

function feeRuleLabel(rule: Row) {
  return `${rule.ruleType ?? ""} ${money(rule.amount)} ${rule.status ?? ""}`.trim();
}

function normalizeAssessmentRow(row: Row): Row {
  return {
    ...row,
    termLabel: `${row.schoolYear ?? ""} / ${row.semesterName ?? ""}`,
    subtotalAmountText: money(row.subtotalAmount),
    discountAmountText: money(row.discountAmount),
    totalAmountText: money(row.totalAmount)
  };
}

function normalizeAssessmentItems(items: Row[]) {
  return items
    .map((item): Row => ({
      ...item,
      unitAmountText: money(item.unitAmount),
      lineAmountText: money(item.lineAmount)
    }))
    .sort((a, b) => Number(a.sortOrder ?? 0) - Number(b.sortOrder ?? 0));
}

function meetingsText(value: unknown) {
  if (!Array.isArray(value) || value.length === 0) return "No meetings";
  return value.map((meeting) => `${(meeting as Row).dayOfWeek} ${timeRange((meeting as Row).startTime, (meeting as Row).endTime)}`).join("; ");
}

function timeRange(start: unknown, end: unknown) {
  return `${String(start ?? "").slice(0, 5)}-${String(end ?? "").slice(0, 5)}`;
}

function money(value: unknown) {
  const amount = Number(value ?? 0);
  return amount.toLocaleString("en-PH", { style: "currency", currency: "PHP" });
}

function flattenCurriculum(curriculum?: Row) {
  return { ...emptyCurriculum(), ...stringifyRow(curriculum) };
}

function flattenCurriculumCourse(course: Row) {
  return {
    ...emptyCurriculumCourse(),
    ...stringifyRow(course),
    prerequisiteCourseIds: linksToCsv(course.prerequisites),
    corequisiteCourseIds: linksToCsv(course.corequisites)
  };
}

function curriculumCoursePayload(values: Record<string, string>) {
  return {
    yearLevel: numberOrZero(values.yearLevel),
    semester: values.semester,
    courseId: values.courseId,
    sortOrder: numberOrZero(values.sortOrder),
    requiredStatus: values.requiredStatus,
    prerequisiteCourseIds: csv(values.prerequisiteCourseIds),
    corequisiteCourseIds: csv(values.corequisiteCourseIds)
  };
}

function linksToCsv(value: unknown) {
  return Array.isArray(value) ? value.map((item) => String((item as Row).id)).join(",") : "";
}

function normalizeCurriculumCourses(courses: Row[]) {
  return courses
    .map((course): Row => ({
      ...course,
      prerequisitesText: linkText(course.prerequisites),
      corequisitesText: linkText(course.corequisites)
    }))
    .sort((a, b) => Number(a.sortOrder ?? 0) - Number(b.sortOrder ?? 0));
}

function groupCurriculumCourses(courses: Row[]) {
  const normalized = normalizeCurriculumCourses(courses);
  const groups = new Map<string, { key: string; yearLevel: string; semester: string; courses: Row[] }>();
  normalized.forEach((course) => {
    const key = `${course.yearLevel}|${course.semester}`;
    if (!groups.has(key)) groups.set(key, { key, yearLevel: String(course.yearLevel), semester: String(course.semester), courses: [] });
    groups.get(key)?.courses.push(course);
  });
  return [...groups.values()].sort((a, b) => Number(a.yearLevel) - Number(b.yearLevel) || a.semester.localeCompare(b.semester));
}

function linkText(value: unknown) {
  if (!Array.isArray(value) || value.length === 0) return "None";
  return value.map((item) => (item as Row).courseCode).filter(Boolean).join(", ");
}

function validationIssueIds(validation: unknown, key: "subjectId" | "scheduleId") {
  const row = validation as Row | null | undefined;
  const blocking = Array.isArray(row?.blockingIssues) ? row.blockingIssues as Row[] : [];
  return new Set(blocking.map((issue) => String(issue[key] ?? "")).filter(Boolean));
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

function numberOrNull(value: string) {
  return value === "" ? null : Number(value);
}

function nullableString(value: unknown) {
  const text = String(value ?? "");
  return text === "" ? null : text;
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
