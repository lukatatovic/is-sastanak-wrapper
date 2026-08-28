export function formatDate(isoDate) {
  if (!isoDate) return "—";
  const d = new Date(isoDate);
  return d.toLocaleDateString("sr-RS", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

export function formatTime(timeStr) {
  if (!timeStr) return "—";
  return timeStr.slice(0, 5);
}

export function formatDateTime(isoDateTime) {
  if (!isoDateTime) return "—";
  const d = new Date(isoDateTime);
  return d.toLocaleString("sr-RS", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function minDateForScheduling() {
  const d = new Date();
  d.setDate(d.getDate() + 3);
  return d.toISOString().split("T")[0];
}
