import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ requireRole }) {
  const { user } = useAuth();

  if (!user) return <Navigate to="/login" replace />;
  if (requireRole && !user.roles.includes(requireRole))
    return <Navigate to="/meetings" replace />;

  return <Outlet />;
}
