import axios from "axios";

const http = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("blocksy_admin_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem("blocksy_admin_token");
      localStorage.removeItem("blocksy_admin_username");
      if (location.pathname !== "/login") {
        location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default http;
