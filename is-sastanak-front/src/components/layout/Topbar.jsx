import React from "react";
import { useAuth } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { ROLE_LABELS } from "../../utils/labels";
import { LogOut } from "lucide-react";

export default function Topbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };
  return (
    <header className="flex h-16 flex-none items-center justify-between border-b border-line bg-surface px-6">
      <div />
      <div className="flex items-center gap-4">
        <div className="text-right">
          <p className="text-sm font-medium text-ink">{user?.fullName}</p>
          <p className="text-xs text-ink-faint">
            {(user?.roles || []).map((r) => ROLE_LABELS[r] || r).join(", ")}
          </p>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 rounded-md px-3 py-2 text-sm text-ink-soft hover:bg-paper hover:text-brick"
          title="Odjava"
        >
          <LogOut size={16} /> Odjava
        </button>
      </div>
    </header>
  );
}
