import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { SuperAdminLayout } from './components/SuperAdminLayout';
import { PlatformDashboardPage } from './pages/PlatformDashboardPage';
import { TenantManagementPage } from './pages/TenantManagementPage';
import { MasterDataPage } from './pages/MasterDataPage';
import { MasterIamPage } from './pages/MasterIamPage';
import { FinancialPoliciesPage } from './pages/FinancialPoliciesPage';
import { IntegrationsGatewayPage } from './pages/IntegrationsGatewayPage';
import { PlatformSecurityPage } from './pages/PlatformSecurityPage';
import { StorageHardwarePage } from './pages/StorageHardwarePage';
import { AuditCompliancePage } from './pages/AuditCompliancePage';
import { LoginPage } from './pages/LoginPage';
import { AuthProvider, useAuth } from './context/AuthContext';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <SuperAdminLayout>{children}</SuperAdminLayout>;
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          
          {/* Module 1: SA_DASHBOARD */}
          <Route path="/" element={<ProtectedRoute><PlatformDashboardPage /></ProtectedRoute>} />
          <Route path="/dashboard" element={<ProtectedRoute><PlatformDashboardPage /></ProtectedRoute>} />
          <Route path="/dashboard/*" element={<ProtectedRoute><PlatformDashboardPage /></ProtectedRoute>} />
          
          {/* Module 2: SA_TENANT_MGMT */}
          <Route path="/tenants" element={<ProtectedRoute><TenantManagementPage /></ProtectedRoute>} />
          <Route path="/tenants/*" element={<ProtectedRoute><TenantManagementPage /></ProtectedRoute>} />
          
          {/* Module 3: SA_MASTER_DATA */}
          <Route path="/master-data" element={<ProtectedRoute><MasterDataPage /></ProtectedRoute>} />
          <Route path="/master-data/*" element={<ProtectedRoute><MasterDataPage /></ProtectedRoute>} />
          
          {/* Module 4: SA_MASTER_IAM */}
          <Route path="/master-iam" element={<ProtectedRoute><MasterIamPage /></ProtectedRoute>} />
          <Route path="/master-iam/*" element={<ProtectedRoute><MasterIamPage /></ProtectedRoute>} />
          
          {/* Module 5: SA_FIN_POLICIES */}
          <Route path="/financial-policies" element={<ProtectedRoute><FinancialPoliciesPage /></ProtectedRoute>} />
          <Route path="/financial-policies/*" element={<ProtectedRoute><FinancialPoliciesPage /></ProtectedRoute>} />
          
          {/* Module 6: SA_INTEGRATIONS */}
          <Route path="/integrations" element={<ProtectedRoute><IntegrationsGatewayPage /></ProtectedRoute>} />
          <Route path="/integrations/*" element={<ProtectedRoute><IntegrationsGatewayPage /></ProtectedRoute>} />
          
          {/* Module 7: SA_SEC_AML */}
          <Route path="/security-aml" element={<ProtectedRoute><PlatformSecurityPage /></ProtectedRoute>} />
          <Route path="/security-aml/*" element={<ProtectedRoute><PlatformSecurityPage /></ProtectedRoute>} />
          
          {/* Module 8: SA_STORAGE_HARDWARE */}
          <Route path="/storage-hardware" element={<ProtectedRoute><StorageHardwarePage /></ProtectedRoute>} />
          <Route path="/storage-hardware/*" element={<ProtectedRoute><StorageHardwarePage /></ProtectedRoute>} />
          
          {/* Module 9: SA_AUDIT_COMPLIANCE */}
          <Route path="/audit-compliance" element={<ProtectedRoute><AuditCompliancePage /></ProtectedRoute>} />
          <Route path="/audit-compliance/*" element={<ProtectedRoute><AuditCompliancePage /></ProtectedRoute>} />
          
          {/* Legacy Aliases */}
          <Route path="/global-config" element={<Navigate to="/master-iam" replace />} />
          <Route path="/audit-logs" element={<Navigate to="/audit-compliance" replace />} />
          
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}