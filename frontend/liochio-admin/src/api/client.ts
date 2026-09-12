import axios from 'axios';

const GATEWAY_BASE_URL = 'http://localhost:8080/api/v1';

export const coreApi = axios.create({
  baseURL: GATEWAY_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'X-Tenant-Id': 'SYSTEM',
  },
});

coreApi.interceptors.request.use(config => {
  const token = localStorage.getItem('superadmin_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 1. Dynamic Menus from Database (auth-service :8081 via Gateway)
export const getSuperAdminMenuTree = async (lang: string = 'vi') => {
  const res = await coreApi.get(`/menus/tree`, {
    params: { portalType: 'SUPERADMIN', lang },
  });
  return res.data;
};

// 2. Tenants Management (auth-service :8081 via Gateway)
export const getTenants = async () => {
  const res = await coreApi.get('/tenants');
  return res.data;
};

export const createTenant = async (tenantData: any) => {
  const res = await coreApi.post('/tenants', tenantData);
  return res.data;
};

// 3. IAM Staff & Roles (auth-service :8081 via Gateway)
export const getPlatformUsers = async () => {
  const res = await coreApi.get('/users');
  return res.data;
};

export const getPlatformRoles = async () => {
  const res = await coreApi.get('/roles');
  return res.data;
};

// 4. Service Registry & Health (Eureka :8761 & Actuator)
export const getEurekaApps = async () => {
  try {
    const res = await axios.get('http://localhost:8761/eureka/apps', {
      headers: { Accept: 'application/json' },
      timeout: 3000,
    });
    return res.data;
  } catch {
    return null;
  }
};

// 5. Audit & Compliance Logs (worker-service :8095 via Gateway)
export const getAuditLogs = async () => {
  try {
    const res = await coreApi.get('/worker/audit-logs');
    return res.data;
  } catch {
    return null;
  }
};
