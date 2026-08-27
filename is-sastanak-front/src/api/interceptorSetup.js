import authClient from "./authClient";
import meetingsClient from "./meetingClient";

export function attachUnauthorizedHandler(onUnauthorized) {
  const handler = (error) => {
    if (error?.response?.status === 401) {
      onUnauthorized();
    }
    return Promise.reject(error);
  };

  authClient.interceptors.response.use((r) => r, handler);
  meetingsClient.interceptors.response.use((r) => r, handler);
}
