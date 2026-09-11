import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function ObserverRestrictedRoute() {
  const { isComplianceObserver } = useAuth();

  if (isComplianceObserver) {
    return <Navigate to="/audit-observer" replace />;
  }

  return <Outlet />;
}
