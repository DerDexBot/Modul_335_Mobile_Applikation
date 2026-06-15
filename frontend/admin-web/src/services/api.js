import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8000';

const api = axios.create({ baseURL: API_BASE });

const clearAuthSession = () => {
  ['token', 'role', 'username', 'userId'].forEach(key => localStorage.removeItem(key));
};

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token && !config.url.includes('/api/auth/login')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const isLoginEndpoint = err.config?.url?.includes('/api/auth/login');
    const alreadyOnLogin = window.location.pathname === '/login';
    const sessionRejected = err.response?.status === 401 || err.response?.status === 403;
    if (sessionRejected && !isLoginEndpoint && !alreadyOnLogin) {
      clearAuthSession();
      window.location.replace('/login');
    }
    return Promise.reject(err);
  }
);

export default api;
