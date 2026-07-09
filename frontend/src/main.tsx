import React, { FormEvent, useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

type ApiResponse<T> = { success: boolean; message: string; data: T; errors?: { field: string; message: string }[] };
type PageResponse<T> = { items: T[]; page: number; size: number; totalElements: number; totalPages: number };
type Option = { label: string; value: string };
type Field =
  | { name: string; label: string; type?: "text" | "number" | "password" | "email"; required?: boolean; createOnly?: boolean; updateOnly?: boolean }
  | { name: string; label: string; type: "select"; options: Option[]; required?: boolean }
  | { name: string; label: string; type: "multi"; optionsKey: "roles"; required?: boolean };
type Resource = {
  key: string;
  title: string;
  endpoint: string;
  idField?: string;
  searchable?: boolean;
  status?: "activeStatus" | "booleanActive";
  fields: Field[];
  columns: { key: string; label: string }[];
  prepare?: (values: Record<string, string>) => Record<string, unknown>;
};
type Session = { accessToken: string; refreshToken: string; user: UserSummary };
type UserSummary = { id: string; username: string; email: string; fullName: string; active: boolean; roles: string[]; permissions: string[] };
type Role = { id: string; name: string; description: string; permissions: string[] };

const API_BASE = import.meta.env.VITE_API_BASE ?? "";
const ACTIVE = ["ACTIVE", "INACTIVE"].map((value) => ({ value, label: value }));
const DEGREE = ["BACHELOR", "ASSOCIATE", "DIPLOMA", "CERTIFICATE", "GRADUATE_PROGRAM"].map((value) => ({ value, label: value }));
const COURSE = ["MAJOR", "PROFESSIONAL_COURSE", "GENERAL_EDUCATION", "PHYSICAL_EDUCATION", "NSTP", "ELECTIVE", "LABORATORY", "SEMINAR", "THESIS_CAPSTONE"].map((value) => ({ value, label: value }));
const EMPLOYMENT = ["FULL_TIME", "PART_TIME", "CONTRACTUAL", "VISITING_LECTURER", "INACTIVE"].map((value) => ({ value, label: value }));
const FACULTY_TYPE = ["INSTRUCTOR", "PROFESSOR", "LECTURER", "DEAN", "PROGRAM_HEAD"].map((value) => ({ value, label: value }));

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
  const [message, setMessage] = useState<string>("");

  function onSession(next: Session | null) {
    setSession(next);
    setSessionState(next);
  }

  if (!session) return <Login onLogin={onSession} message={message} setMessage={setMessage} />;
  return <AdminApp session={session} logout={() => onSession(null)} message={message} setMessage={setMessage} />;
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
          <h1>Admin Setup</h1>
        </div>
        {message && <div className="alert">{message}</div>}
        <label>Username or email<input value={usernameOrEmail} onChange={(e) => setUsername(e.target.value)} /></label>
        <label>Password<input value={password} onChange={(e) => setPassword(e.target.value)} type="password" /></label>
        <button className="primary" disabled={busy}>{busy ? "Signing in..." : "Sign in"}</button>
      </form>
    </main>
  );
}

function AdminApp({ session, logout, message, setMessage }: { session: Session; logout: () => void; message: string; setMessage: (message: string) => void }) {
  const [activeKey, setActiveKey] = useState("users");
  const [lookups, setLookups] = useState<Record<string, Option[]>>({});
  const resources = useMemo(() => buildResources(lookups), [lookups]);
  const resource = resources.find((item) => item.key === activeKey) ?? resources[0];

  async function loadLookups() {
    const [roles, departments, programs, schoolYears, semesters, users] = await Promise.all([
      api<PageResponse<Role>>("/api/v1/roles?size=200", {}, session.accessToken),
      api<PageResponse<Record<string, string>>>("/api/v1/departments?size=200", {}, session.accessToken),
      api<PageResponse<Record<string, string>>>("/api/v1/programs?size=200", {}, session.accessToken),
      api<PageResponse<Record<string, string>>>("/api/v1/school-years?size=200", {}, session.accessToken),
      api<PageResponse<Record<string, string>>>("/api/v1/semesters?size=200", {}, session.accessToken),
      api<PageResponse<Record<string, string>>>("/api/v1/users?size=200", {}, session.accessToken)
    ]);
    setLookups({
      roles: roles.items.map((role) => ({ value: role.id, label: role.name })),
      departments: departments.items.map((item) => ({ value: item.id, label: `${item.departmentCode} - ${item.departmentName}` })),
      programs: programs.items.map((item) => ({ value: item.id, label: `${item.programCode} - ${item.programName}` })),
      schoolYears: schoolYears.items.map((item) => ({ value: item.id, label: item.schoolYear })),
      semesters: semesters.items.map((item) => ({ value: item.id, label: item.name })),
      users: users.items.map((item) => ({ value: item.id, label: `${item.username} (${item.email})` }))
    });
  }

  useEffect(() => {
    loadLookups().catch((error) => setMessage(error instanceof Error ? error.message : "Unable to load lookups"));
  }, []);

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand"><strong>SIS</strong><span>Admin Setup</span></div>
        <nav>
          {resources.map((item) => (
            <button key={item.key} className={item.key === activeKey ? "nav active" : "nav"} onClick={() => setActiveKey(item.key)}>{item.title}</button>
          ))}
        </nav>
      </aside>
      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Signed in as {session.user.username}</p>
            <h1>{resource.title}</h1>
          </div>
          <button onClick={logout}>Sign out</button>
        </header>
        {message && <div className="alert">{message}<button onClick={() => setMessage("")}>Dismiss</button></div>}
        <ResourcePanel
          key={resource.key}
          resource={resource}
          token={session.accessToken}
          lookups={lookups}
          setMessage={setMessage}
          refreshLookups={loadLookups}
        />
      </section>
    </div>
  );
}

function ResourcePanel({ resource, token, lookups, setMessage, refreshLookups }: { resource: Resource; token: string; lookups: Record<string, Option[]>; setMessage: (message: string) => void; refreshLookups: () => Promise<void> }) {
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<Record<string, unknown> | null>(null);
  const [busy, setBusy] = useState(false);

  async function load() {
    setBusy(true);
    try {
      const query = resource.searchable && search ? `?search=${encodeURIComponent(search)}&size=50` : "?size=50";
      const page = await api<PageResponse<Record<string, unknown>>>(`${resource.endpoint}${query}`, {}, token);
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

  async function setStatus(row: Record<string, unknown>) {
    const body = resource.status === "booleanActive"
      ? { active: !(row.active as boolean) }
      : { status: row.status === "ACTIVE" ? "INACTIVE" : "ACTIVE" };
    await api(`${resource.endpoint}/${row.id}/status`, { method: "PATCH", body: JSON.stringify(body) }, token);
    await load();
    await refreshLookups();
  }

  function statusLabel(row: Record<string, unknown>) {
    return resource.status === "booleanActive" ? (row.active ? "ACTIVE" : "INACTIVE") : String(row.status);
  }

  return (
    <div className="contentGrid">
      <div className="listPane">
        <div className="toolbar">
          {resource.searchable && <input placeholder="Search" value={search} onChange={(e) => setSearch(e.target.value)} onKeyDown={(e) => e.key === "Enter" && load()} />}
          {resource.searchable && <button onClick={load}>Search</button>}
          <button className="primary" onClick={() => setSelected({})}>New</button>
        </div>
        <div className="tableWrap">
          <table>
            <thead><tr>{resource.columns.map((column) => <th key={column.key}>{column.label}</th>)}{resource.status && <th>Status</th>}<th></th></tr></thead>
            <tbody>
              {busy ? <tr><td colSpan={resource.columns.length + 2}>Loading...</td></tr> : rows.map((row) => (
                <tr key={String(row.id)}>
                  {resource.columns.map((column) => <td key={column.key}>{formatCell(row[column.key])}</td>)}
                  {resource.status && <td><span className={`pill ${statusLabel(row) === "ACTIVE" ? "good" : "muted"}`}>{statusLabel(row)}</span></td>}
                  <td className="actions">
                    <button onClick={() => setSelected(row)}>Edit</button>
                    {resource.status && <button onClick={() => setStatus(row)}>{statusLabel(row) === "ACTIVE" ? "Deactivate" : "Activate"}</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      <EditPane resource={resource} selected={selected} setSelected={setSelected} save={save} lookups={lookups} />
    </div>
  );
}

function EditPane({ resource, selected, setSelected, save, lookups }: { resource: Resource; selected: Record<string, unknown> | null; setSelected: (row: Record<string, unknown> | null) => void; save: (values: Record<string, string>) => Promise<void>; lookups: Record<string, Option[]> }) {
  const [values, setValues] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const next: Record<string, string> = {};
    resource.fields.forEach((field) => {
      const value = selected?.[field.name];
      if (field.type === "multi" && field.name === "roleIds" && Array.isArray(selected?.roles)) {
        const selectedRoles = new Set((selected.roles as unknown[]).map(String));
        next[field.name] = (lookups.roles ?? [])
          .filter((option) => selectedRoles.has(option.label))
          .map((option) => option.value)
          .join(",");
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
