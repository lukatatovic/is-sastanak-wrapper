import React from "react";
import { useAuth } from "../../context/AuthContext";
import Seal from "./Seal";
import { NavLink } from "react-router-dom";
import {
  BarChart3,
  Building2,
  CalendarClock,
  PlusCircle,
  User2,
  UserCog,
} from "lucide-react";

const linkBase =
  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors";
const linkActive = "bg-accent-soft text-accent";
const linkInactive = "text-ink-soft hover:bg-paper hover:text-ink";

export default function Sidebar() {
  const { hasRole } = useAuth();
  const isRukovodilac = hasRole("RUKOVODILAC") || hasRole("ADMINISTRATOR");
  const isAdmin = hasRole("ADMINISTRATOR");

  return (
    <aside className="flex h-screen w-64 flex-none flex-col border-r border-line bg-surface px-4 py-5">
      <div className="mb-8 flex items-center gap-3 px-1">
        <Seal />
        <div>
          <p className="font-display text-base leading-tight text-ink">
            IS SASTANAK
          </p>
          <p className="text-xs text-ink-faint">informacioni sistem</p>
        </div>
      </div>

      <nav className="flex flex-1 flex-col gap-1">
        <NavLink
          to="/meetings"
          end
          className={({ isActive }) =>
            `${linkBase} ${isActive ? linkActive : linkInactive}`
          }
        >
          <CalendarClock size={18} /> Moji sastanci
        </NavLink>

        {isRukovodilac && (
          <NavLink
            to="/meetings/new"
            className={({ isActive }) =>
              `${linkBase} ${isActive ? linkActive : linkInactive}`
            }
          >
            {" "}
            <PlusCircle size={18} />
            Novi sastanak
          </NavLink>
        )}

        <NavLink
          to="/stats"
          className={({ isActive }) =>
            `${linkBase} ${isActive ? linkActive : linkInactive}`
          }
        >
          <BarChart3 size={18} /> Statistika ucesca
        </NavLink>

        {isAdmin && (
          <>
            <div className="mt-4 mb-1 px-3 text-xs font-semibold uppercase tracking-wide text-ink-faint">
              Administracija
            </div>
            <NavLink
              to="/admin/users"
              className={({ isActive }) =>
                `${linkBase} ${isActive ? linkActive : linkInactive}`
              }
            >
              <User2 size={18} /> Korisnici
            </NavLink>
            <NavLink
              to="/admin/temporary-roles"
              className={({ isActive }) =>
                `${linkBase} ${isActive ? linkActive : linkInactive}`
              }
            >
              <UserCog size={18} /> Privremene uloge
            </NavLink>
            <NavLink
              to="/admin/org-units"
              className={({ isActive }) =>
                `${linkBase} ${isActive ? linkActive : linkInactive}`
              }
            >
              <Building2 size={18} /> Organizacione jedinice
            </NavLink>
          </>
        )}
      </nav>
    </aside>
  );
}
