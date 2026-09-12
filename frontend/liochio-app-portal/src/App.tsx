import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { UnifiedLayout } from './components/UnifiedLayout';
import { UnifiedLoginPage } from './pages/UnifiedLoginPage';

// Corporate Pages
import { ApprovalsCenterPage } from './pages/corp/ApprovalsCenterPage';
import { DashboardPage as CorpDashboardPage } from './pages/corp/DashboardPage';
import { BranchesPage } from './pages/corp/BranchesPage';
import { StaffRbacPage } from './pages/corp/StaffRbacPage';

// Business Ops Pages
import { IotFleetPage } from './pages/corp/IotFleetPage';
import { CustomerOnboardingPage } from './pages/corp/CustomerOnboardingPage';
import { BrandingPage } from './pages/corp/BrandingPage';
import { CorpMailConfigPage } from './pages/corp/CorpMailConfigPage';

// Retail Pages
import { SmartPiggyPage } from './pages/retail/piggy/SmartPiggyPage';
import { WalletsPage } from './pages/retail/wallets/WalletsPage';
import { ParentalPage } from './pages/retail/parental/ParentalPage';
import { AiRoboAdvisorPage } from './pages/retail/ai/AiRoboAdvisorPage';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredWorkspace: 'corp' | 'business' | 'retail';
  adminOnly?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredWorkspace, adminOnly = false }) => {
  const token = localStorage.getItem('app_token') || localStorage.getItem('corp_token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  const storedUserJson = localStorage.getItem('app_user') || localStorage.getItem('corp_user') || '{}';
  let currentUser: any = {};
  try {
    currentUser = JSON.parse(storedUserJson);
  } catch (e) {
    currentUser = {};
  }

  const userRoles: string[] = currentUser.roles || [];

  // 1. Chặn tuyệt đối SuperAdmin trên App Portal (:5173)
  if (userRoles.includes('ROLE_SUPER_ADMIN') || currentUser.username?.toLowerCase() === 'superadmin') {
    localStorage.clear();
    return <Navigate to="/login" replace />;
  }

  // 2. Tính toán danh sách workspace được phép theo Role
  const allowedWorkspaces: ('corp' | 'business' | 'retail')[] = [];
  if (userRoles.includes('ROLE_CORP_ADMIN')) {
    allowedWorkspaces.push('corp', 'business');
  } else if (userRoles.includes('ROLE_MAKER') || userRoles.includes('ROLE_CHECKER')) {
    allowedWorkspaces.push('corp');
  } else if (userRoles.includes('ROLE_CUSTOMER') || userRoles.includes('ROLE_PARENT') || userRoles.includes('ROLE_CHILD')) {
    allowedWorkspaces.push('retail');
  } else {
    if (currentUser.domain === 'CORP_PORTAL') {
      allowedWorkspaces.push('corp');
    } else {
      allowedWorkspaces.push('retail');
    }
  }

  // 3. Kiểm tra thẩm quyền truy cập phân hệ
  if (!allowedWorkspaces.includes(requiredWorkspace) || (adminOnly && !userRoles.includes('ROLE_CORP_ADMIN'))) {
    // Điều hướng về trang chủ mặc định được phép của tài khoản
    if (allowedWorkspaces.includes('corp')) {
      const defaultCorp = userRoles.includes('ROLE_CORP_ADMIN') ? '/corp/dashboard' : '/corp/approvals';
      return <Navigate to={defaultCorp} replace />;
    } else if (allowedWorkspaces.includes('retail')) {
      return <Navigate to="/retail/piggy" replace />;
    }
    return <Navigate to="/login" replace />;
  }

  return <UnifiedLayout>{children}</UnifiedLayout>;
};

const RootRedirect: React.FC = () => {
  const token = localStorage.getItem('app_token') || localStorage.getItem('corp_token');
  if (!token) return <Navigate to="/login" replace />;

  const storedUserJson = localStorage.getItem('app_user') || localStorage.getItem('corp_user') || '{}';
  try {
    const user = JSON.parse(storedUserJson);
    const roles: string[] = user.roles || [];
    if (roles.includes('ROLE_CORP_ADMIN')) return <Navigate to="/corp/dashboard" replace />;
    if (roles.includes('ROLE_MAKER') || roles.includes('ROLE_CHECKER')) return <Navigate to="/corp/approvals" replace />;
    if (roles.includes('ROLE_CUSTOMER') || roles.includes('ROLE_PARENT') || roles.includes('ROLE_CHILD')) return <Navigate to="/retail/piggy" replace />;
  } catch (e) {}

  return <Navigate to="/login" replace />;
};

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Authentication */}
        <Route path="/login" element={<UnifiedLoginPage />} />

        {/* Corporate Workspace */}
        <Route
          path="/corp/approvals"
          element={
            <ProtectedRoute requiredWorkspace="corp">
              <ApprovalsCenterPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/corp/dashboard"
          element={
            <ProtectedRoute requiredWorkspace="corp">
              <CorpDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/corp/branches"
          element={
            <ProtectedRoute requiredWorkspace="corp">
              <BranchesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/corp/staff-rbac"
          element={
            <ProtectedRoute requiredWorkspace="corp" adminOnly={true}>
              <StaffRbacPage />
            </ProtectedRoute>
          }
        />

        {/* Business Workspace */}
        <Route
          path="/business/iot-fleet"
          element={
            <ProtectedRoute requiredWorkspace="business">
              <IotFleetPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/business/customer-onboarding"
          element={
            <ProtectedRoute requiredWorkspace="business">
              <CustomerOnboardingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/business/branding"
          element={
            <ProtectedRoute requiredWorkspace="business">
              <BrandingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/business/mail-config"
          element={
            <ProtectedRoute requiredWorkspace="business">
              <CorpMailConfigPage />
            </ProtectedRoute>
          }
        />

        {/* Retail Workspace */}
        <Route
          path="/retail/piggy"
          element={
            <ProtectedRoute requiredWorkspace="retail">
              <SmartPiggyPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/retail/wallets"
          element={
            <ProtectedRoute requiredWorkspace="retail">
              <WalletsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/retail/goals"
          element={
            <ProtectedRoute requiredWorkspace="retail">
              <SmartPiggyPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/retail/parental"
          element={
            <ProtectedRoute requiredWorkspace="retail">
              <ParentalPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/retail/ai"
          element={
            <ProtectedRoute requiredWorkspace="retail">
              <AiRoboAdvisorPage />
            </ProtectedRoute>
          }
        />

        {/* Default Route */}
        <Route path="/" element={<RootRedirect />} />
        <Route path="*" element={<RootRedirect />} />
      </Routes>
    </BrowserRouter>
  );
}
