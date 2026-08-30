import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { minDateForScheduling } from "../utils/format";
import authClient from "../api/authClient";
import meetingsClient from "../api/meetingClient";
import ErrorBanner from "../components/ui/ErrorBanner";
import {
  CATEGORY_LABELS,
  FREQUENCY_LABELS,
  LOCATION_LABELS,
} from "../utils/labels";
import { Plus, Trash2 } from "lucide-react";

const emptyAgendaItem = () => ({ title: "", description: "" });
const emptyParticipant = () => ({
  userId: "",
  externalFirstName: "",
  externalLastName: "",
  externalOrganizationalUnit: "",
  externalCountry: "",
  externalJobTitle: "",
});

export default function MeetingCreatePage() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({
    title: "",
    category: "REFERISANJE",
    type: "VANREDNI",
    frequency: "",
    locationType: "MATICNA_ORG_JEDINICA",
    room: "",
    scheduledDate: minDateForScheduling(),
    scheduledTime: "10:00",
    recorderId: "",
    actNumber: "",
    actDate: "",
    actIssuingOrganization: "",
  });

  const [agendaItems, setAgendaItems] = useState([emptyAgendaItem()]);
  const [participants, setParticipants] = useState([emptyParticipant()]);

  useEffect(() => {
    authClient
      .get("/api/users")
      .then((res) => setUsers(res.data))
      .catch(() => setUsers([]));
  }, []);

  const update = (field, value) => setForm((f) => ({ ...f, [field]: value }));

  const updateAgendaItem = (idx, field, value) => {
    setAgendaItems((items) =>
      items.map((it, i) => (i === idx ? { ...it, [field]: value } : it)),
    );
  };

  const updateParticipant = (idx, field, value) => {
    setParticipants((ps) =>
      ps.map((p, i) => (i === idx ? { ...p, [field]: value } : p)),
    );
  };

  const validate = () => {
    let errors = {};

    if (!form.title.trim()) {
      errors.title = "Tema sastanka je obavezna";
    }

    if (!form.room.trim()) {
      errors.room = "Prostorija je obavezna";
    }

    if (!form.recorderId) {
      errors.recorderId = "Morate izabrati zapisnicara";
    }

    if (form.recorderId) {
      const isRecorderInParticipants = participants.some(
        (p) => Number(p.userId) === Number(form.recorderId),
      );
      if (isRecorderInParticipants) {
        errors.participants =
          "Zapisnicar ne može biti i u listi ucesnika na sastanku.";
      }
    }

    if (form.type === "STALNI" && !form.frequency) {
      errors.frequency = "Ucestalost je obavezna za stalne sastanke.";
    }

    const hasActNumber = form.actNumber.trim() !== "";
    const hasActDate = form.actDate && form.actDate.trim() !== "";
    const hasActOrganization = form.actIssuingOrganization.trim() !== "";

    if (
      (hasActNumber || hasActDate || hasActOrganization) &&
      !(hasActNumber && hasActDate && hasActOrganization)
    ) {
      if (!hasActNumber)
        errors.actNumber = "Broj akta je obavezum ako se unosi akt";
      if (!hasActDate)
        errors.actDate = "Datum akta je obavezan ako se unosi akt";
      if (!hasActOrganization)
        errors.actIssuingOrganization =
          "Organizacija je obavezna ako se unosi akt";

      errors.actGroup =
        "Ako unosite podatke o aktu, morate popuniti sva tri polja (broj, datum i organizaciju).";
    }

    const validAgenda = agendaItems.filter((a) => a.title.trim() !== "");
    if (validAgenda.length === 0) {
      errors.agenda = "Sastanak mora imati bar 1 stavku dnevnog reda.";
    } else {
      const titles = validAgenda.map((a) => a.title.trim().toLowerCase());
      const hasDuplicateAgenda = new Set(titles).size !== titles.length;
      if (hasDuplicateAgenda) {
        errors.agenda = "Stavke dnevnog reda ne smeju imati iste nazive";
      }
    }

    const validParticipants = participants.filter(
      (p) => p.userId || p.externalFirstName.trim() !== "",
    );
    if (validParticipants.length < 2) {
      errors.participants = "Sastanak mora imati bar 2 ucesnika.";
    } else {
      const userIds = validParticipants
        .filter((p) => p.userId)
        .map((p) => p.userId);
      const hasDuplicateUsers = new Set(userIds).size !== userIds.length;

      if (hasDuplicateUsers) {
        errors.participants = "Isti ucesnik ne moze biti dodat vise puta.";
      }
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
      const payload = {
        ...form,
        recorderId: Number(form.recorderId),
        frequency: form.type === "STALNI" ? form.frequency : null,
        actDate: form.actDate ? form.actDate : null,
        agendaItems: agendaItems.filter((a) => a.title.trim() !== ""),
        participants: participants
          .filter((p) => p.userId || p.externalFirstName)
          .map((p) => (p.userId ? { userId: Number(p.userId) } : p)),
      };
      const { data } = await meetingsClient.post("/api/meetings", payload);
      navigate(`/meetings/${data.id}`);
    } catch (err) {
      setError(err?.response?.data?.message || "Sastanak nije moguce zakazati");
    } finally {
      setSubmitting(false);
    }
  };
  return (
    <div>
      <h1 className="mb-6 font-display text-2xl text-ink">Novi sastanak</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <ErrorBanner message={error} />

        <section className="card p-5 space-y-4">
          <h2 className="font-display text-lg text-ink">Osnovni podaci</h2>

          <div>
            <label className="label">Tema sastanka</label>
            <input
              className="input"
              required
              value={form.title}
              onChange={(e) => update("title", e.target.value)}
            />
            {formErrors.title && (
              <span className="text-xs text-red-500 mt-1">
                {formErrors.title}
              </span>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="">Kategorija</label>
              <select
                className="input"
                value={form.category}
                onChange={(e) => update("category", e.target.value)}
              >
                {Object.entries(CATEGORY_LABELS).map(([k, v]) => (
                  <option key={k} value={k}>
                    {v}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Tip sastanka</label>
              <select
                className="input"
                value={form.type}
                onChange={(e) => update("type", e.target.value)}
              >
                <option value="VANREDNI">Vanredni</option>
                <option value="STALNI">Stalni</option>
              </select>
            </div>
          </div>

          {form.type === "STALNI" && (
            <div>
              <label className="label">Ucestalost</label>
              <select
                className="input"
                value={form.frequency}
                onChange={(e) => update("frequency", e.target.value)}
                required
              >
                <option value="">Izaberite...</option>
                {Object.entries(FREQUENCY_LABELS).map(([k, v]) => (
                  <option key={k} value={k}>
                    {v}
                  </option>
                ))}
              </select>
              {formErrors.frequency && (
                <span className="text-xs text-red-500 mt-1">
                  {formErrors.frequency}
                </span>
              )}
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Datum odrzavanja</label>
              <input
                type="date"
                className="input"
                required
                min={minDateForScheduling()}
                value={form.scheduledDate}
                onChange={(e) => update("scheduledDate", e.target.value)}
              />
              <p className="mt-1 text-xs text-ink-faint">
                Minimum 3 dana unapred
              </p>
            </div>
            <div>
              <label className="label">Vreme</label>
              <input
                type="time"
                className="input"
                required
                value={form.scheduledTime}
                onChange={(e) => update("scheduledTime", e.target.value)}
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Mesto odrzavanja</label>
              <select
                className="input"
                value={form.locationType}
                onChange={(e) => update("locationType", e.target.value)}
              >
                {Object.entries(LOCATION_LABELS).map(([k, v]) => (
                  <option key={k} value={k}>
                    {v}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">Prostorija</label>
              <input
                className="input"
                value={form.room}
                onChange={(e) => update("room", e.target.value)}
                placeholder="Kancelarija 73"
              />
              {formErrors.room && (
                <span className="text-xs text-red-500 mt-1">
                  {formErrors.room}
                </span>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Zapisnicar</label>
              <select
                className="input"
                required
                value={form.recorderId}
                onChange={(e) => update("recorderId", e.target.value)}
              >
                <option value="">Izaberite zapisnicara...</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.firstName} {u.lastName} ({u.username})
                  </option>
                ))}
              </select>
              {formErrors.recorderId && (
                <span className="text-xs text-red-500 mt-1">
                  {formErrors.recorderId}
                </span>
              )}
              {users.length === 0 && (
                <p className="mt-1 text-xs text-ink-faint">
                  Lista korisnika nije dostupna
                </p>
              )}
            </div>
            <div>
              <label className="label">Organizacija koja je donela akt</label>
              <input
                className="input"
                value={form.actIssuingOrganization}
                onChange={(e) =>
                  update("actIssuingOrganization", e.target.value)
                }
              />
              {formErrors.actGroup && (
                <span className="text-xs text-red-500 mt-1">
                  {formErrors.actGroup}
                </span>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Datum donosenja akta</label>
              <input
                type="date"
                className="input"
                max={new Date().toISOString().split("T")[0]}
                value={form.actDate}
                onChange={(e) => update("actDate", e.target.value)}
              />
              {formErrors.actGroup && (
                <span className="text-xs text-red-500 mt-1">
                  {formErrors.actGroup}
                </span>
              )}
            </div>
            <div>
              <label className="label">Broj akta</label>
              <input
                className="input"
                value={form.actNumber}
                onChange={(e) => update("actNumber", e.target.value)}
              />
              {formErrors.actGroup && (
                <span className="text-xs text-red-500 mt-1">
                  {formErrors.actGroup}
                </span>
              )}
            </div>
          </div>
        </section>

        <section className="card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-lg text-ink">Dnevni red</h2>
            <button
              type="button"
              className="btn-secnodary"
              onClick={() => setAgendaItems((a) => [...a, emptyAgendaItem()])}
            >
              <Plus size={16} /> Dodaj tacku
            </button>
          </div>
          {formErrors.agenda && (
            <span className="text-xs text-red-500 mt-1">
              {formErrors.agenda}
            </span>
          )}
          <div className="space-y-3">
            {agendaItems.map((item, idx) => (
              <div
                key={idx}
                className="flex items-start gap-3 border-b border-line pb-3 last:border-0"
              >
                <span className="mt-2 font-display text-ink-faint">
                  {idx + 1}.
                </span>
                <div className="flex-1 space-y-2">
                  <input
                    className="input"
                    placeholder="Naziv tacke dnevnog reda"
                    value={item.title}
                    onChange={(e) =>
                      updateAgendaItem(idx, "title", e.target.value)
                    }
                  />
                  <textarea
                    className="input"
                    placeholder="Opis"
                    rows={2}
                    hidden
                    value={item.description}
                    onChange={(e) =>
                      updateAgendaItem(idx, "description", e.target.value)
                    }
                  />
                </div>
                <button
                  type="button"
                  className="mt-2 text-ink-faint hover:text-brick"
                  onClick={() =>
                    setAgendaItems((a) => a.filter((_, i) => i !== idx))
                  }
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        </section>

        <section className="card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="font-display text-lg text-ink">Ucesnici</h2>
            <button
              type="button"
              className="btn-secnodary"
              onClick={() => setParticipants((p) => [...p, emptyParticipant()])}
            >
              <Plus size={16} /> Dodaj ucesnika
            </button>
          </div>
          {formErrors.participants && (
            <span className="text-xs text-red-500 mt-1">
              {formErrors.participants}
            </span>
          )}
          <div className="space-y-4">
            {participants.map((p, idx) => (
              <div
                key={idx}
                className="p-4 border border-line rounded-lg space-y-3 bg-white"
              >
                <div className="flex items-center gap-3">
                  <select
                    className="input flex-1"
                    value={p.userId}
                    onChange={(e) =>
                      updateParticipant(idx, "userId", e.target.value)
                    }
                  >
                    <option value="">Registrovani zaposleni...</option>
                    {users.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.firstName} {u.lastName}
                      </option>
                    ))}
                  </select>

                  <span className="text-xs text-ink-faint whitespace-nowrap">
                    ili unesite eksterno
                  </span>

                  <button
                    type="button"
                    className="text-ink-faint hover:text-brick p-2"
                    onClick={() =>
                      setParticipants((ps) => ps.filter((_, i) => i !== idx))
                    }
                  >
                    <Trash2 size={16} />
                  </button>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-5 gap-2 pt-2 border-t border-line">
                  <input
                    className="input"
                    placeholder="Ime (eksterno)"
                    disabled={!!p.userId}
                    value={p.externalFirstName}
                    onChange={(e) =>
                      updateParticipant(
                        idx,
                        "externalFirstName",
                        e.target.value,
                      )
                    }
                  />
                  <input
                    className="input"
                    placeholder="Prezime (eksterno)"
                    disabled={!!p.userId}
                    value={p.externalLastName}
                    onChange={(e) =>
                      updateParticipant(idx, "externalLastName", e.target.value)
                    }
                  />
                  <input
                    className="input"
                    placeholder="Organizacija (eksterno)"
                    disabled={!!p.userId}
                    value={p.externalOrganizationalUnit}
                    onChange={(e) =>
                      updateParticipant(
                        idx,
                        "externalOrganizationalUnit",
                        e.target.value,
                      )
                    }
                  />
                  <input
                    className="input"
                    placeholder="Posao (eksterno)"
                    disabled={!!p.userId}
                    value={p.externalJobTitle}
                    onChange={(e) =>
                      updateParticipant(idx, "externalJobTitle", e.target.value)
                    }
                  />
                  <input
                    className="input"
                    placeholder="Drzava (eksterno)"
                    disabled={!!p.userId}
                    value={p.externalCountry}
                    onChange={(e) =>
                      updateParticipant(idx, "externalCountry", e.target.value)
                    }
                  />
                </div>
              </div>
            ))}
          </div>
        </section>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            className="btn-secondary"
            onClick={() => navigate(-1)}
          >
            Otkazi
          </button>
          <button type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? "Zakazivanje..." : "Zakazi sastanak"}
          </button>
        </div>
      </form>
    </div>
  );
}
