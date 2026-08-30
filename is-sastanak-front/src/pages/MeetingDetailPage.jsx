import React, { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import meetingsClient from "../api/meetingClient";
import Spinner from "../components/ui/Spinner";
import ErrorBanner from "../components/ui/ErrorBanner";
import StatusBadge from "../components/ui/StatusBadge";
import { CATEGORY_LABELS, LOCATION_LABELS } from "../utils/labels";
import { formatDate, formatTime } from "../utils/format";
import {
  CalendarX2,
  CheckCircle2,
  FileDown,
  FileSpreadsheet,
  FileText,
  XCircle,
} from "lucide-react";
import Modal from "../components/ui/Modal";
import { useAuth } from "../context/AuthContext";

function download(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  window.URL.revokeObjectURL(url);
}

export default function MeetingDetailPage() {
  const { id } = useParams();
  const [meeting, setMeeting] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showFull, setShowFull] = useState(false);
  const [report, setReport] = useState(null);
  const [statusModal, setStatusModal] = useState(null);
  const [reason, setReason] = useState("");
  const [attendance, setAttendance] = useState({});
  const { user } = useAuth();
  const [agendaItems, setAgendaItems] = useState([]);
  const [finalConclusion, setFinalConclusion] = useState("");

  const isAdminOrRukovodilac = user?.roles?.some((role) =>
    ["ADMINISTRATOR", "RUKOVODILAC"].includes(role),
  );

  const isAdminOrRukovodilacOrZapisnicar = user?.roles?.some((role) =>
    ["ADMINISTRATOR", "RUKOVODILAC", "ZAPISNICAR"].includes(role),
  );

  const isCancelledOrPostponed = ["OTKAZAN", "ODLOZEN"].includes(
    meeting?.status,
  );

  const load = useCallback(() => {
    setLoading(true);
    meetingsClient
      .get(`/api/meetings/${id}`)
      .then((res) => {
        setMeeting(res.data);
        const initialAttendance = {};
        setAgendaItems(res.data.agendaItems || []);
        setFinalConclusion(res.data.finalConclusion || "");
        res.data.participants.forEach(
          (p) => (initialAttendance[p.id] = p.actuallyAttended),
        );
        setAttendance(initialAttendance);
      })
      .catch(() =>
        setError("Sastanak nije pronadjen ili nemate pravo pristupa."),
      )
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const loadReport = (full) => {
    setShowFull(full);
    meetingsClient
      .get(`/api/meetings/${id}/report/${full ? "full" : "short"}`)
      .then((res) => setReport(res.data));
  };

  const handleExport = async (format) => {
    const res = await meetingsClient.get(
      `/api/meetings/${id}/export/${format}`,
      {
        params: { full: showFull },
        responseType: "blob",
      },
    );
    download(res.data, `izvestaj-${id}.${format}`);
  };

  const submitStatusChange = async () => {
    await meetingsClient.patch(`/api/meetings/${id}/status`, {
      status: statusModal,
      reason,
    });
    setStatusModal(null);
    setReason("");
    load();
  };

  const submitAttendance = async () => {
    const updates = Object.entries(attendance).map(
      ([participantId, actuallyAttended]) => ({
        participantId: Number(participantId),
        actuallyAttended,
      }),
    );
    await meetingsClient.post(`/api/meetings/${id}/attendance`, updates);
    load();
  };

  const handleAgendaDescriptionChange = (id, newDescription) => {
    setAgendaItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, description: newDescription } : item,
      ),
    );
  };

  const submitAgenda = async () => {
    try {
      const payload = {
        agendaItems: agendaItems.map((item) => ({
          id: item.id,
          orderNum: item.orderNum,
          title: item.title,
          description: item.description,
        })),
        finalConclusion: finalConclusion,
      };
      await meetingsClient.patch(`/api/meetings/${id}/agenda`, payload);
      load();
    } catch (err) {
      setError(
        err?.response?.data?.message || "Nije moguce promeniti dnevni red",
      );
    }
  };

  if (loading) return <Spinner />;
  if (error) return <ErrorBanner message={error} />;
  if (!meeting) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="font-display text-2xl text-ink">{meeting.title}</h1>
          <p className="text-sm text-ink-soft">
            {CATEGORY_LABELS[meeting.category]} ·{" "}
            {LOCATION_LABELS[meeting.locationType]}
            {meeting.room ? ` · ${meeting.room}` : ""}
          </p>
        </div>
        <StatusBadge status={meeting.status} />
      </div>

      <section className="card grid grid-cols-2 gap-4 p-5 text-sm sm:grid-cols-4">
        <div>
          <p className="text-xs text-ink-faint">Datum</p>
          <p className="font-medium text-ink">
            {formatDate(meeting.scheduledDate)}
          </p>
        </div>
        <div>
          <p className="text-xs text-ink-faint">Vreme</p>
          <p className="font-medium text-ink">
            {formatTime(meeting.scheduledTime)}
          </p>
        </div>
        <div>
          <p className="text-xs text-ink-faint">Rukovodilac</p>
          <p className="font-medium text-ink">{meeting.organizerFullName}</p>
        </div>
        <div>
          <p className="text-xs text-ink-faint">Zapisničar</p>
          <p className="font-medium text-ink">{meeting.recorderFullName}</p>
        </div>
      </section>

      {meeting.status === "ZAKAZAN" && (
        <div className="flex gap-2">
          <button
            className="btn-secondary"
            onClick={() => setStatusModal("ODLOZEN")}
          >
            <CalendarX2 size={16} /> Odlozi
          </button>
          <button
            className="btn-danger"
            onClick={() => setStatusModal("OTKAZAN")}
          >
            <XCircle size={16} /> Otkazi
          </button>
        </div>
      )}

      <section className="card p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="mb-4 font-display text-lg text-ink">Dnevni red</h2>
          {isAdminOrRukovodilac && (
            <button
              className="btn-primary"
              onClick={submitAgenda}
              disabled={isCancelledOrPostponed}
            >
              Sacuvaj dnevni red
            </button>
          )}
        </div>
        <ol className="space-y-4">
          {agendaItems.map((item) => (
            <li key={item.id} className="flex gap-4">
              <span className="font-display text-lg text-accent">
                {item.orderNum}.
              </span>
              <div className="flex-1 border-b border-dotted border-line pb-3">
                <p className="font-medium text-ink">{item.title}</p>
                <input
                  className="input mt-2"
                  value={item.description || ""}
                  disabled={
                    !isAdminOrRukovodilacOrZapisnicar || isCancelledOrPostponed
                  }
                  onChange={(e) =>
                    handleAgendaDescriptionChange(item.id, e.target.value)
                  }
                />
              </div>
            </li>
          ))}
          {agendaItems.length === 0 && (
            <p className="text-sm text-ink-faint">Dnevni red nije definisan</p>
          )}
        </ol>
        <div className="mt-4">
          <p className="text-2xl text-ink">Zakljucak</p>
          <textarea
            className="input mt-2 "
            value={finalConclusion || ""}
            rows={6}
            disabled={
              !isAdminOrRukovodilacOrZapisnicar || isCancelledOrPostponed
            }
            onChange={(e) => setFinalConclusion(e.target.value)}
          />
        </div>
      </section>

      <section className="card p-5">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-display text-lg text-ink">
            Ucesnici i prisustvo
          </h2>
          {isAdminOrRukovodilac && (
            <button
              className="btn-primary"
              onClick={submitAttendance}
              disabled={isCancelledOrPostponed}
            >
              Sacuvaj prisustvo
            </button>
          )}
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line text-left text-xs text-ink-faint">
              <th className="py-2">Ime i prezime</th>
              <th className="py-2">Organizacija</th>
              <th className="py-2">Planirano</th>
              <th className="py-2">Prisustvovao</th>
            </tr>
          </thead>
          <tbody>
            {meeting.participants.map((p) => (
              <tr key={p.id} className="border-b border-line last:border-0">
                <td className="py-2 font-medium text-ink">{p.fullName}</td>
                <td className="py-2 text-ink-soft">
                  {p.organizationalUnitOrCompany || "—"}
                </td>
                <td className="py-2">
                  {p.plannedToAttend ? (
                    <CheckCircle2 size={16} className="text-moss" />
                  ) : (
                    <XCircle size={16} className="text-ink-faint" />
                  )}
                </td>
                <td className="py-2">
                  <input
                    type="checkbox"
                    checked={!!attendance[p.id]}
                    onChange={(e) =>
                      setAttendance((a) => ({ ...a, [p.id]: e.target.checked }))
                    }
                    className="h-4 w-4 rounded border-line text-accent focus:ring-accent"
                    disabled={!isAdminOrRukovodilac || isCancelledOrPostponed}
                  />
                </td>
              </tr>
            ))}
            {meeting.participants.length === 0 && (
              <tr>
                <td colSpan={4} className="py-4 text-center text-ink-faint">
                  Nema evidentiranih ucesnika
                </td>
              </tr>
            )}
          </tbody>
        </table>
        <p className="mt-2 text-xs text-ink-faint">
          Evidencija prisustva moguca je najkasnije 72 sata od kraja sastanka
        </p>
      </section>

      <section className="card p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="font-display text-lg text-ink">Izveštaj</h2>
          <div className="flex gap-2">
            <button
              className={`btn-secondary ${!showFull ? "bg-accent-soft text-accent" : ""}`}
              onClick={() => loadReport(false)}
            >
              Skraceni
            </button>
            <button
              className={`btn-secondary ${showFull ? "bg-accent-soft text-accent" : ""}`}
              onClick={() => loadReport(true)}
            >
              Potpuni
            </button>
          </div>
        </div>

        {report && (
          <div className="mb-4 space-y-3 rounded-md bg-paper p-4 text-sm">
            {report.agendaItems.map((item) => (
              <div key={item.orderNum}>
                <p className="font-medium text-ink">
                  {item.orderNum}. {item.title}
                </p>
                {report.fullReport && item.description && (
                  <p className="text-ink-soft">{item.description}</p>
                )}
                {report.fullReport &&
                  item.discussionEntries?.map((d, i) => (
                    <p key={i} className="ml-4 mt-1 text-ink-soft">
                      <em>{d.speakerName}:</em> {d.content}
                    </p>
                  ))}
                {item.conclusion && (
                  <p className="text-moss">Zaključak: {item.conclusion}</p>
                )}
              </div>
            ))}
          </div>
        )}

        <div className="flex gap-2">
          <button className="btn-secondary" onClick={() => handleExport("pdf")}>
            <FileDown size={16} /> PDF
          </button>
          <button
            className="btn-secondary"
            onClick={() => handleExport("xlsx")}
          >
            <FileSpreadsheet size={16} /> Excel
          </button>
          <button
            className="btn-secondary"
            onClick={() => handleExport("docx")}
          >
            <FileText size={16} /> Word
          </button>
        </div>
      </section>
      <Modal
        open={!!statusModal}
        onClose={() => setStatusModal(null)}
        title={
          statusModal === "ODLOZEN"
            ? "Odlaganje sastanka"
            : "Otkazivanje sastanka"
        }
        footer={
          <>
            <button
              className="btn-secondary"
              onClick={() => setStatusModal(null)}
            >
              Nazad
            </button>
            <button
              className="btn-danger"
              onClick={submitStatusChange}
              disabled={!reason.trim()}
            >
              Potvrdi
            </button>
          </>
        }
      >
        <label className="label">Obrazlozenje (obavezno)</label>
        <textarea
          className="input"
          rows={3}
          value={reason}
          onChange={(e) => setReason(e.target.value)}
        />
      </Modal>
    </div>
  );
}
