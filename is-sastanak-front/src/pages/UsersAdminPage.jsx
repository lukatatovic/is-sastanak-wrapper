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
  const [formErrors, setFormErrors] = useState({});
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

  const validate = () => {
    let errors = {};

    if (!form.firstName.trim()) errors.firstName = "Ime je obavezno";
    if (!form.lastName.trim()) errors.lastName = "Prezime je obavezno";

    if (!form.jmbg.trim()) {
      errors.jmbg = "JMBG je obavezan";
    } else if (!/^\d{13}$/.test(form.jmbg)) {
      errors.jmbg = "JMBG mora imati tacno 13 cifara";
    }

    if (!form.email.trim()) {
      errors.email = "Email je obavezan.";
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      errors.email = "Nevalidan format email adrese";
    }

    if (!form.username.trim()) errors.username = "Korisnicko ime je obavezno";

    if (!form.password.trim()) {
      errors.password = "Lozinka je obavezna";
    }

    if (!form.fatherName.trim()) errors.fatherName = "Ime oca je obavezno";

    if (!form.jobTitle.trim()) errors.jobTitle = "Naziv pozicije je obavezan";

    const phoneRegex = /^(\+381|0)\d{8,9}$/;

    if (!form.officePhone.trim()) {
      errors.mobilePhone = "Poslovni telefon je obavezan";
    } else if (
      form.officePhone &&
      !phoneRegex.test(form.officePhone.replace(/[\s-]/g, ""))
    ) {
      errors.officePhone = "Format mora biti +381... ili 06...";
    }

    if (!form.mobilePhone.trim()) {
      errors.mobilePhone = "Mobilni telefon je obavezan";
    } else if (!phoneRegex.test(form.mobilePhone.replace(/[\s-]/g, ""))) {
      errors.mobilePhone = "Format mora biti +381... ili 06...";
    }

    if (!form.organizationalUnitId) {
      errors.organizationalUnitId = "Izaberite organizacionu jedinicu";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!validate()) {
      return;
    }

    setSubmitting(true);
    try {
      await authClient.post("/api/users", {
        ...form,
        organizationalUnitId: Number(form.organizationalUnitId) || null,
      });
      setForm(emptyForm);
      setFormErrors({});
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
              {formErrors.firstName && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.firstName}
                </span>
              )}
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
              {formErrors.lastName && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.lastName}
                </span>
              )}
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
              {formErrors.fatherName && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.fatherName}
                </span>
              )}
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
              {formErrors.jobTitle && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.jobTitle}
                </span>
              )}
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
              {formErrors.jmbg && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.jmbg}
                </span>
              )}
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
              {formErrors.email && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.email}
                </span>
              )}
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
              {formErrors.username && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.username}
                </span>
              )}
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
              {formErrors.password && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.password}
                </span>
              )}
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
              {formErrors.officePhone && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.officePhone}
                </span>
              )}
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
              {formErrors.mobilePhone && (
                <span style={{ color: "red", fontSize: "12px" }}>
                  {formErrors.mobilePhone}
                </span>
              )}
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
            {formErrors.organizationalUnitId && (
              <span style={{ color: "red", fontSize: "12px" }}>
                {formErrors.organizationalUnitId}
              </span>
            )}
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
