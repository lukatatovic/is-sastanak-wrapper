import React, { useEffect, useState } from "react";
import authClient from "../api/authClient";
import Spinner from "../components/ui/Spinner";
import RoleBadge from "../components/ui/RoleBadge";
import ErrorBanner from "../components/ui/ErrorBanner";
import { Plus } from "lucide-react";

const ALL_ROLES = ["ADMINISTRATOR", "RUKOVODILAC", "ZAPISNICAR", "UCESNIK"];

const emptyForm = {
  firstName: "",
  lastName: "",
  fatherName: "",
  jobTitle: "",
  jmbg: "",
  email: "",
  username: "",
  password: "",
  officePhone: "",
  mobilePhone: "",
  organizationalUnitId: "",
  primaryRole: "UCESNIK",
};

export default function UsersAdminPage() {
  const [users, setUsers] = useState([]);
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    Promise.all([
      authClient.get("/api/users"),
      authClient.get("/api/org-units"),
    ])
      .then(([u, o]) => {
        setUsers(u.data);
        setUnits(o.data);

        if (o.data.length > 0) {
          setForm((f) => ({
            ...f,
            organizationalUnitId: String(o.data[0].id),
          }));
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await authClient.post("/api/users", {
        ...form,
        organizationalUnitId: Number(form.organizationalUnitId) || null,
      });
      setForm(emptyForm);
      load();
    } catch (err) {
      setError(
        err?.response?.data?.message || "Nije moguce kreirati korisnika",
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Spinner />;
  return (
    <div className="space-y-6">
      <h1 className="font-display text-2xl text-ink">Korisnici</h1>

      <section className="card divide-y divide-line">
        {users.map((u) => (
          <div
            key={u.id}
            className="flex items-center justify-between px-5 py-3"
          >
            <div>
              <p className="font-medium text-ink">
                {u.firstName} {u.lastName}{" "}
                <span className="text-ink-faint text-sm">({u.username})</span>
              </p>
              <p className="text-xs text-ink-faint">
                {u.email} · {u.organizationalUnitName || "Nema org. jedinicu"}
              </p>
            </div>
            <RoleBadge role={u.primaryRole} />
          </div>
        ))}
      </section>

      <section className="card p-5">
        <h2 className="mb-4 font-display text-lg text-ink">Novi krosnik</h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          <ErrorBanner message={error} />
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Ime</label>
              <input
                className="input"
                required
                value={form.firstName}
                onChange={(e) =>
                  setForm((f) => ({ ...f, firstName: e.target.value }))
                }
              />
            </div>
            <div>
              <label className="label">Prezime</label>
              <input
                className="input"
                required
                value={form.lastName}
                onChange={(e) =>
                  setForm((f) => ({ ...f, lastName: e.target.value }))
                }
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Ime oca</label>
              <input
                className="input"
                value={form.fatherName}
                onChange={(e) =>
                  setForm((f) => ({ ...f, fatherName: e.target.value }))
                }
              />
            </div>
            <div>
              <label className="label">Naziv pozicije</label>
              <input
                className="input"
                required
                value={form.jobTitle}
                onChange={(e) =>
                  setForm((f) => ({ ...f, jobTitle: e.target.value }))
                }
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">JMBG</label>
              <input
                className="input"
                required
                value={form.jmbg}
                onChange={(e) =>
                  setForm((f) => ({ ...f, jmbg: e.target.value }))
                }
              />
            </div>
            <div>
              <label className="label">Email</label>
              <input
                className="input"
                required
                value={form.email}
                onChange={(e) =>
                  setForm((f) => ({ ...f, email: e.target.value }))
                }
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Korisnicko ime</label>
              <input
                className="input"
                required
                value={form.username}
                onChange={(e) =>
                  setForm((f) => ({ ...f, username: e.target.value }))
                }
              />
            </div>

            <div>
              <label className="label">Lozinka</label>
              <input
                className="input"
                required
                value={form.password}
                onChange={(e) =>
                  setForm((f) => ({ ...f, password: e.target.value }))
                }
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Poslovni telefon</label>
              <input
                className="input"
                value={form.officePhone}
                onChange={(e) =>
                  setForm((f) => ({ ...f, officePhone: e.target.value }))
                }
              />
            </div>
            <div>
              <label className="label">Mobilni telefon</label>
              <input
                className="input"
                required
                value={form.mobilePhone}
                onChange={(e) =>
                  setForm((f) => ({ ...f, mobilePhone: e.target.value }))
                }
              />
            </div>
          </div>
          <div>
            <label className="label">Organizaciona jedinica</label>
            <select
              className="input"
              value={form.organizationalUnitId}
              onChange={(e) =>
                setForm((f) => ({
                  ...f,
                  organizationalUnitId: e.target.value,
                }))
              }
            >
              {units.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Stalna uloga</label>
            <div className="flex flex-wrap gap-2">
              {ALL_ROLES.map((role) => (
                <button
                  type="button"
                  key={role}
                  onClick={() => setForm((f) => ({ ...f, primaryRole: role }))}
                  className={`rounded-full px-3 py-1 text-xs font-medium border ${
                    form.primaryRole === role
                      ? "bg-accent text-white border-accent"
                      : "border-line text-ink-soft"
                  }`}
                >
                  {role}
                </button>
              ))}
            </div>
          </div>
          <button type="submit" className="btn-primary" disabled={submitting}>
            <Plus size={16} />{" "}
            {submitting ? "Kreiranje..." : "Kreiraj korisnika"}
          </button>
        </form>
      </section>
    </div>
  );
}
