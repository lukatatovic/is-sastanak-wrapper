import React, { useEffect, useState } from "react";
import meetingsClient from "../api/meetingClient";
import Spinner from "../components/ui/Spinner";
import { Calendar, CalendarDays, CalendarRange, Icon } from "lucide-react";

export default function StatsPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      meetingsClient.get("/api/stats/weekly"),
      meetingsClient.get("/api/stats/monthly"),
      meetingsClient.get("/api/stats/yearly"),
    ])
      .then(([w, m, y]) =>
        setStats({
          weekly: w.data.count,
          monthly: m.data.count,
          yearly: y.data.count,
        }),
      )
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  const cards = [
    { label: "Prosle nedelje", value: stats.weekly, icon: CalendarDays },
    { label: "Proslog meseca", value: stats.monthly, icon: CalendarRange },
    { label: "Prosle godine", value: stats.yearly, icon: Calendar },
  ];
  return (
    <div>
      <h1 className="mb-6 font-display text-2xl text-ink">Statistika ucesca</h1>
      <div className="grid grid-cols-3 gap-4">
        {cards.map(({ label, value, icon: Icon }) => (
          <div key={label} className="card p-6">
            <Icon size={20} className="mb-3 text-accent" />
            <p className="font-display text-3xl text-ink">{value}</p>
            <p className="text-sm text-ink-soft">{label}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
