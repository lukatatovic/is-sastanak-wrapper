import React from "react";
import { STATUS_LABELS, STATUS_STYLES } from "../../utils/labels";

export default function StatusBadge({ status }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_STYLES[status] || "bg-line text-ink-soft"}`}
    >
      {STATUS_LABELS[status] || status}
    </span>
  );
}
