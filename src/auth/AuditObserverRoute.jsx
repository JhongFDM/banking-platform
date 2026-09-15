import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function AuditObserverRoute() {
  const { isAdmin, isComplianceObserver } = useAuth();

  if (!isAdmin && !isComplianceObserver) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
