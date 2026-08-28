import React from "react";
import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center text-center gap-3 bg-paper">
      <p className="font-display text-4xl text-ink">404</p>
      <p className="text-ink-soft">Stranica ne postoji</p>
      <Link to="/meetings" className="btn-primary mt-2">
        Nazad na pocetnu
      </Link>
    </div>
  );
}
