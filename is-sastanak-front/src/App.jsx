import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";
import MeetingsListPage from "./pages/MeetingsListPage";
import ProtectedRoute from "./components/ProtectedRoute";
import AppLayout from "./components/layout/AppLayout";
import MeetingCreatePage from "./pages/MeetingCreatePage";
import MeetingDetailPage from "./pages/MeetingDetailPage";
import StatsPage from "./pages/StatsPage";
import UsersAdminPage from "./pages/UsersAdminPage";
import TemporaryRolesAdminPage from "./pages/TemporaryRolesAdminPage";
import OrgUnitsAdminPage from "./pages/OrgUnitsAdminPage";
import NotFoundPage from "./pages/NotFoundPage";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route index element={<Navigate to="/meetings" replace />} />
              <Route path="meetings" element={<MeetingsListPage />} />
              <Route path="meetings/new" element={<MeetingCreatePage />} />
              <Route path="meetings/:id" element={<MeetingDetailPage />} />
              <Route path="stats" element={<StatsPage />} />

              <Route element={<ProtectedRoute requireRole="ADMINISTRATOR" />}>
                <Route path="admin/users" element={<UsersAdminPage />} />
                <Route
                  path="admin/temporary-roles"
                  element={<TemporaryRolesAdminPage />}
                />
                <Route path="admin/org-units" element={<OrgUnitsAdminPage />} />
              </Route>
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
