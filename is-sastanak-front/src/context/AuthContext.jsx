/* eslint-disable react-refresh/only-export-components */

import { useCallback, useContext, useEffect, useState } from "react";
import { createContext } from "react";
import { attachUnauthorizedHandler } from "../api/interceptorSetup";
import authClient from "../api/authClient";

const AuthContext = createContext(null);

const TOKEN_KEY = "is_sastanak_token";
const USER_KEY = "is_sastanak_user";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  const [loading, setLoading] = useState(false);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  useEffect(() => {
    attachUnauthorizedHandler(logout);
  }, [logout]);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const { data } = await authClient.post("/api/auth/login", {
        username,
        password,
      });
      localStorage.setItem(TOKEN_KEY, data.token);
      const userData = {
        id: data.userId,
        fullName: data.fullName,
        roles: data.roles,
      };
      localStorage.setItem(USER_KEY, JSON.stringify(userData));
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const hasRole = (role) => user?.roles?.includes(role);

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context)
    throw new Error("useAuth mora biti koriscen unutar AuthProvider");

  return context;
}
