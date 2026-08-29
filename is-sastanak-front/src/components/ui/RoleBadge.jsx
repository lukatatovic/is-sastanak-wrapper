import React from "react";
import { ROLE_LABELS } from "../../utils/labels";

export default function RoleBadge({ role }) {
  return (
    <span className="inline-flex items-center rounded-full bg-accent-soft px-2.5 py-0.5 text-xs font-medium text-accent mr-1 mb-1">
      {ROLE_LABELS[role] || role}
    </span>
  );
}
