import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function RiskAnalystRoute() {
  const { isRiskAnalyst } = useAuth();

  if (!isRiskAnalyst) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}