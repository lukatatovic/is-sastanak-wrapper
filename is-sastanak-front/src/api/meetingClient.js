import axios from "axios";

const meetingsClient = axios.create({
  baseURL: import.meta.env.VITE_MEETINGS_SERVICE_URL || "http://localhost:8082",
});

meetingsClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("is_sastanak_token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default meetingsClient;
