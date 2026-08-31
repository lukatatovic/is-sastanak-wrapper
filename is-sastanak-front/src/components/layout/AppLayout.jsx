import React, { useEffect } from "react";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import { Outlet } from "react-router-dom";
import meetingsClient from "../../api/meetingClient";
import toast, { Toaster } from "react-hot-toast";

export default function AppLayout() {
  useEffect(() => {
    const checkNotifications = () => {
      meetingsClient.get("/api/notifications/unread").then((res) => {
        res.data.forEach((n) => {
          const icon =
            n.type === "WARNING" ? "⚠️" : n.type === "SUCCESS" ? "✅" : "ℹ️";
          toast(n.message, { icon });
          meetingsClient.patch(`/api/notifications/${n.id}/read`);
        });
      });
    };

    checkNotifications();
    const interval = setInterval(checkNotifications, 20000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="flex h-screen bg-paper">
      <Toaster position="top-right" />
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden">
        <Topbar />
        <main className="flex-1 overflow-y-auto px-8 py-6">
          <div className="mx-auto max-w-5xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
