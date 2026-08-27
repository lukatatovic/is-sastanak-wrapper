import axios from "axios";

const authClient = axios.create({
  baseURL: import.meta.env.VITE_AUTH_SERVICE_URL || "http://localhost:8081",
});

authClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("is_sastanak_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default authClient;
