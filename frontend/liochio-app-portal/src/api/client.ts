import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

export const API_BASE_URL = (import.meta as any).env?.VITE_API_URL || 'http://localhost:8000/api/v1';
export const PYTHON_IOT_URL = (import.meta as any).env?.VITE_PYTHON_IOT_URL || 'http://localhost:8000/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Helper to generate UUIDv4 for Idempotency
function generateUUID(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

// Request Interceptor: Attach Bearer JWT Token & Idempotency-Key
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('liochio_jwt_token') || localStorage.getItem('app_token') || localStorage.getItem('corp_token');
    if (token && config.headers) {
      config.headers.Authorization = 'Bearer ' + token;
    }

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response Interceptor: Global Error & 401 Unauthorized handling
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Auto clear token if invalid or expired
      localStorage.removeItem('liochio_jwt_token');
      localStorage.removeItem('app_token');
      localStorage.removeItem('corp_token');
      localStorage.removeItem('app_user');
      localStorage.removeItem('corp_user');
      localStorage.removeItem('tenant_id');
      localStorage.removeItem('app_user_id');
      localStorage.removeItem('app_username');
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
