import React, { useEffect, useState } from "react";
import authClient from "../api/authClient";
import Spinner from "../components/ui/Spinner";
import { Building2, Plus } from "lucide-react";
import ErrorBanner from "../components/ui/ErrorBanner";

export default function OrgUnitsAdminPage() {
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  const load = () => {
    setLoading(true);
    authClient
      .get("/api/org-units")
      .then((res) => setUnits(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      await authClient.post("/api/org-units", {
        name,
      });
      setName("");
      load();
    } catch {
      setError("Organizacionu jedinicu nije moguce kreirati");
    }
  };

  if (loading) return <Spinner />;
  return (
    <div>
      <h1 className="font-display text-2xl text-ink">Organizacione jedinice</h1>
      <section className="card divide-y divide-line">
        {units.map((u) => (
          <div key={u.id} className="flex items-center gap-3 px-5 py-3">
            <Building2 size={16} className="text-accent" />
            <span className="font-medium text-ink">{u.name}</span>
          </div>
        ))}
        {units.length === 0 && (
          <p className="px-5 py-6 text-center text-ink-faint">
            Ne postoje organizacione jedinice
          </p>
        )}
      </section>

      <section className="card p-5 mt-5">
        <h2 className="mb-4 font-display text-lg text-ink">
          Nova organizaciona jedinica
        </h2>
        <form onSubmit={handleSubmit} className="flex items-end gap-3">
          <ErrorBanner message={error} />
          <div className="flex-1">
            <label className="label">Naziv</label>
            <input
              className="input"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>
          <button type="submit" className="btn-primary">
            <Plus size={16} /> Dodaj
          </button>
        </form>
      </section>
    </div>
  );
}
