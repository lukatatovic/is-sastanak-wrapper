import React, { useEffect, useState } from "react";
import authClient from "../api/authClient";
import Spinner from "../components/ui/Spinner";
import ErrorBanner from "../components/ui/ErrorBanner";
import { Plus, X } from "lucide-react";
import RoleBadge from "../components/ui/RoleBadge";
import { formatDateTime } from "../utils/format";

const ROLES = ["RUKOVODILAC", "ZAPISNICAR", "UCESNIK", "ADMINISTRATOR"];

const emptyForm = {
  userId: "",
  role: "ZAPISNICAR",
  contextType: "meeting",
  meetingId: "",
  organizationalUnitId: "",
  note: "",
};

export default function TemporaryRolesAdminPage() {
  const [users, setUsers] = useState([]);
  const [units, setUnits] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [assignments, setAssignments] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    Promise.all([
      authClient.get("/api/users"),
      authClient.get("/api/org-units"),
    ])
      .then(([u, o]) => {
        setUsers(u.data);
        setUnits(o.data);
      })
      .finally(() => setLoading(false));
  }, []);

  const loadAssignments = (userId) => {
    if (!userId) {
      setAssignments([]);
      return;
    }
    authClient
      .get(`/api/temporary-roles/user/${userId}`)
      .then((res) => setAssignments(res.data));
  };

  useEffect(() => {
    loadAssignments(selectedUserId);
  }, [selectedUserId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const payload = {
        userId: Number(form.userId),
        role: form.role,
        note: form.note,
        meetingId:
          form.contextType === "meeting" ? Number(form.meetingId) : null,
        organizationalUnitId:
          form.contextType === "org" ? Number(form.organizationalUnitId) : null,
      };
      console.log(payload);
      await authClient.post("/api/temporary-roles", payload);
      setForm({ ...emptyForm, userId: form.userId });
      loadAssignments(form.userId);
      setSelectedUserId(form.userId);
    } catch (err) {
      setError(
        err?.response?.data?.message || "Nije moguce dodeliti privremenu ulogu",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleRevoke = async (id) => {
    await authClient.delete(`/api/temporary-roles/${id}`);
    loadAssignments(selectedUserId);
  };

  if (loading) return <Spinner />;
  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl text-ink">Privremene uloge</h1>
        <p className="text-sm text-ink-soft">
          Dodela uloge korisniku samo za konkretan sastanak,zamenu rukovodioca u
          organizacionoj celini...
        </p>
      </div>

      <section className="card p-5">
        <h2 className="mb-4 font-display text-lg text-ink">
          Nova privremena uloga
        </h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <ErrorBanner message={error} />
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Korisnik</label>
              <select
                className="input"
                required
                value={form.userId}
                onChange={(e) =>
                  setForm((f) => ({ ...f, userId: e.target.value }))
                }
              >
                <option value="">Izaberite korisnika...</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.firstName} {u.lastName} ({u.primaryRole})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Privremena uloga</label>
              <select
                className="input"
                value={form.role}
                onChange={(e) =>
                  setForm((f) => ({ ...f, role: e.target.value }))
                }
              >
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="label">Odnosi se na</label>
            <div className="flex gap-4 text-sm">
              <label className="flex items-center gap-2">
                <input
                  type="radio"
                  checked={form.contextType === "meeting"}
                  onChange={() =>
                    setForm((f) => ({ ...f, contextType: "meeting" }))
                  }
                />
                Konkretan sastanak
              </label>
              <label className="flex items-center gap-2">
                <input
                  type="radio"
                  checked={form.contextType === "org"}
                  onChange={() =>
                    setForm((f) => ({ ...f, contextType: "org" }))
                  }
                />
                Celu organizacionu jedinicu (zamena)
              </label>
            </div>
          </div>

          {form.contextType === "meeting" ? (
            <div>
              <label className="label">ID sastanka</label>
              <input
                type="number"
                className="input"
                required
                value={form.meetingId}
                onChange={(e) =>
                  setForm((f) => ({ ...f, meetingId: e.target.value }))
                }
              />
            </div>
          ) : (
            <div>
              <label className="label">Organizaciona jedinica</label>
              <select
                className="input"
                required
                value={form.organizationalUnitId}
                onChange={(e) =>
                  setForm((f) => ({
                    ...f,
                    organizationalUnitId: e.target.value,
                  }))
                }
              >
                <option value="">Izaberite jedinicu...</option>
                {units.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="label">Napomena</label>
            <textarea
              className="input"
              required
              rows={2}
              value={form.note}
              onChange={(e) => setForm((f) => ({ ...f, note: e.target.value }))}
            />
          </div>

          <button type="submit" className="btn-primary" disabled={submitting}>
            <Plus size={16} />{" "}
            {submitting ? "Dodeljivanje..." : "Dodeli privremenu ulogu"}
          </button>
        </form>
      </section>

      <section className="card p-5">
        <h2 className="mb-4 font-display text-lg text-ink">
          Pregled po korisniku
        </h2>
        <select
          className="input mb-4"
          value={selectedUserId}
          onChange={(e) => setSelectedUserId(e.target.value)}
        >
          <option value="">Izaberite korisnika...</option>
          {users.map((u) => (
            <option key={u.id} value={u.id}>
              {u.firstName} {u.lastName}
            </option>
          ))}
        </select>

        {selectedUserId && assignments.length === 0 && (
          <p className="text-sm text-ink-faint">
            Nema privremenih uloga za ovog korisnika
          </p>
        )}

        <div className="space-y-3">
          {assignments.map((a) => (
            <div
              key={a.id}
              className="flex items-start justify-between gap-3 rounded-md border border-line p-3"
            >
              <div>
                <div className="mb-1 flex items-center gap-2">
                  <RoleBadge role={a.role} />
                  <span className="text-xs text-ink-faint">
                    {a.meetingId
                      ? `sastanak #${a.meetingId}`
                      : `organizaciona jedinica #${a.organizationalUnitId}`}
                  </span>
                  {a.revoked && (
                    <span className="text-xs text-brick">opozvano</span>
                  )}
                </div>
                <p className="text-sm text-ink-soft">{a.note}</p>
                <p className="mt-1 text-xs text-ink-faint">
                  Dodelio: {a.assignedByAdminName} ·{" "}
                  {formatDateTime(a.assignedAt)}
                </p>
              </div>
              {!a.revoked && (
                <button
                  onClick={() => handleRevoke(a.id)}
                  className="text-ink-faint hover:text-brick"
                  title="Opozovi"
                >
                  <X size={16} />
                </button>
              )}
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
