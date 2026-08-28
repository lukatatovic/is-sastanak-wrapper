import React, { useEffect, useState } from "react";
import meetingsClient from "../api/meetingClient";
import { Link } from "react-router-dom";
import Spinner from "../components/ui/Spinner";
import EmptyState from "../components/ui/EmptyState";
import { CalendarClock } from "lucide-react";
import { CATEGORY_LABELS } from "../utils/labels";
import StatusBadge from "../components/ui/StatusBadge";
import Pagination from "../components/ui/Pagination";
import { formatDate, formatTime } from "../utils/format";

export default function MeetingsListPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);

  useEffect(() => {
    setLoading(true);
    meetingsClient
      .get("/api/meetings/mine", {
        params: { page, size: 5, sort: "scheduledDate,desc" },
      })
      .then((res) => setData(res.data))
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl text-ink">Moji sastanci</h1>
        </div>
        <Link to="/meetings/new" className="btn-primary">
          Novi sastanak
        </Link>
      </div>

      {loading && <Spinner />}

      {!loading && data?.content?.length === 0 && (
        <EmptyState
          title={"Nema zakazanih sastanaka"}
          description={"Sastanak ce se pojaviti kada buete pozvani na njega"}
        />
      )}

      {!loading && data?.content?.length > 0 && (
        <div className="card divide-y divide-line">
          {data.content.map((m) => (
            <Link
              key={m.id}
              to={`/meetings/${m.id}`}
              className="flex items-center justify-between gap-4 px-5 py-4 hover:bg-paper/80"
            >
              <div className="flex items-center gap-4">
                <div className="flex h-10 w-10 flex-none items-center justify-center rounded-full bg-accent-soft text-accent">
                  <CalendarClock size={18} />
                </div>
                <div>
                  <p className="font-medium text-ink">{m.title}</p>
                  <p className="text-xs text-ink-faint">
                    {CATEGORY_LABELS[m.category]} · {m.organizationalUnitName} ·
                    rukovodilac {m.organizerFullName}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-4 text-right">
                <div className="text-sm text-ink-soft">
                  {formatDate(m.scheduledDate)} u {formatTime(m.scheduledTime)}
                </div>
                <StatusBadge status={m.status} />
              </div>
            </Link>
          ))}
        </div>
      )}

      {data && (
        <Pagination
          page={page}
          totalPages={data.totalPages}
          onChange={setPage}
        />
      )}
    </div>
  );
}
