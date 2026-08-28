import React from "react";

export default function Spinner({ label = "Ucitava se..." }) {
  return (
    <div className="flex items-center justify-center gap-3 py-16 text-ink-soft text-sm">
      <div className="h-4 w-4 animate-spin rounded-full border-2 border-line border-t-accent" />
      {label}
    </div>
  );
}
