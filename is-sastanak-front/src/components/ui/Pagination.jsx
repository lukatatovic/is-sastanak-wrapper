import React from "react";

export default function Pagination({ page, totalPages, onChange }) {
  return (
    <div className="flex items-center justify-center gap-2 py-4 text-sm">
      <button
        className="btn-secondary px-3 py-1.5"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        Prethodna
      </button>
      <span className="text-ink-soft">
        Strana {page + 1} od {Math.max(totalPages, 1)}
      </span>
      <button
        className="btn-secondary px-3 py-1.5"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Sledeca
      </button>
    </div>
  );
}
