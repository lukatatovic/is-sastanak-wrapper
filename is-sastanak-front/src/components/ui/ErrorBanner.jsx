import React from "react";

export default function ErrorBanner({ message }) {
  if (!message) return null;
  return (
    <div className="rounded-md border border-brick bg-brick-soft px-4 py-3 text-sm text-brick">
      {message}
    </div>
  );
}
