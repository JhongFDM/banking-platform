import { useQuery } from '@tanstack/react-query';
import { getRiskAnalystDashboard } from '../api/riskAssessment';

export function useRiskAnalystDashboard() {
  return useQuery({
    queryKey: ['risk-analyst-dashboard'],
    queryFn: getRiskAnalystDashboard,
    staleTime: 30_000
  });
}