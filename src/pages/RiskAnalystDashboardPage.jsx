import { Link } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useRiskAnalystDashboard } from '../hooks/useRiskAnalystDashboard';
import { mapAxiosError } from '../api/axiosClient';
import { flagCustomerForAdminReview } from '../api/riskAssessment';

function formatScore(score) {
  return score == null ? 'Insufficient data' : score.toFixed(2);
}

function formatTrend(trend) {
  if (trend == null) {
    return <span className="risk-dashboard-insufficient">Insufficient data</span>;
  }
  if (trend === 0) {
    return <span className="risk-dashboard-trend risk-dashboard-trend-flat">0.00</span>;
  }
  const direction = trend > 0 ? 'up' : 'down';
  return (
    <span className={`risk-dashboard-trend risk-dashboard-trend-${direction}`}>
      {trend > 0 ? '+' : ''}{trend.toFixed(2)}
    </span>
  );
}

function levelLabel(level) {
  return level ? level.charAt(0) + level.slice(1).toLowerCase() : 'Insufficient data';
}

function actionCell(row, onFlag, isFlagging) {
  const reviewPath = `/risk-dashboard/${row.customerId}/risk-assessment`;
  if (row.action === 'FLAG_FOR_ADMIN_REVIEW') {
    return (
      <div className="risk-dashboard-actions">
        <button type="button" onClick={() => onFlag(row.customerId)} disabled={isFlagging}>
          {isFlagging ? 'Sending...' : 'Flag for admin review'}
        </button>
        <Link className="button-link subtle" to={reviewPath}>View details</Link>
      </div>
    );
  }
  if (row.action === 'REVIEW_FLAGGED') {
    return <span className="risk-dashboard-flagged-label">Flag sent to admin</span>;
  }
  if (row.action === 'INVESTIGATE') {
    return <Link className="button-link subtle" to={reviewPath}>Investigate</Link>;
  }
  return <Link className="button-link subtle" to={reviewPath}>View details</Link>;
}

export function RiskAnalystDashboardPage() {
  const dashboardQuery = useRiskAnalystDashboard();
  const queryClient = useQueryClient();
  const flagMutation = useMutation({
    mutationFn: flagCustomerForAdminReview,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['risk-analyst-dashboard'] })
  });
  const error = dashboardQuery.error ? mapAxiosError(dashboardQuery.error) : null;
  const flagError = flagMutation.error ? mapAxiosError(flagMutation.error) : null;
  const dashboard = dashboardQuery.data;

  return (
    <div className="risk-dashboard stack">
      <section className="risk-dashboard-header">
        <div>
          <p className="risk-dashboard-eyebrow">Operations / Risk</p>
          <h1>Risk Dashboard</h1>
          <p className="muted">Real time monitoring of customer risk vectors and platform health</p>
        </div>
        <div className="risk-dashboard-refresh-status">
          <span className="risk-dashboard-status-dot" />
          Live data
        </div>
      </section>

      {dashboardQuery.isLoading ? <div className="banner info">Loading risk dashboard...</div> : null}
      {error ? <div className="banner error">{error.message}</div> : null}
      {flagError ? <div className="banner error">{flagError.message}</div> : null}

      {dashboard ? (
        <>
          <section className="risk-dashboard-summary-grid" aria-label="Risk dashboard summary">
            <article className="risk-dashboard-summary-card risk-dashboard-summary-alert">
              <span className="risk-dashboard-card-label">Total Flagged Accounts</span>
              <strong>{dashboard.totalFlaggedAccounts}</strong>
              <span className="risk-dashboard-card-note">High-risk customers requiring action</span>
            </article>
            <article className="risk-dashboard-summary-card">
              <span className="risk-dashboard-card-label">Average Risk Score</span>
              <strong>{dashboard.averageRiskScore == null ? '—' : dashboard.averageRiskScore.toFixed(2)}</strong>
              <span className="risk-dashboard-card-note">Across customers with complete data</span>
            </article>
            <article className="risk-dashboard-summary-card">
              <span className="risk-dashboard-card-label">System Confidence</span>
              <strong>{dashboard.systemConfidence.toFixed(2)}%</strong>
              <span className="risk-dashboard-card-note">
                {dashboard.customersWithCompleteData} of {dashboard.totalCustomers} customers fully scored
              </span>
            </article>
          </section>

          <section className="table-shell risk-dashboard-table-shell">
            <div className="risk-dashboard-table-heading">
              <div>
                <h2>Customer Risk Vectors</h2>
                <p className="muted">Latest persisted score compared with the previous saved assessment.</p>
              </div>
              <span className="risk-dashboard-count">{dashboard.customers.length} customers</span>
            </div>
            {dashboard.customers.length > 0 ? (
              <table>
                <thead>
                  <tr>
                    <th>Customer ID</th>
                    <th>Risk Score</th>
                    <th>Trend</th>
                    <th>Primary Factor</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboard.customers.map((row) => (
                    <tr key={row.customerId}>
                      <td className="code">#{row.customerId}</td>
                      <td>
                        <div className="risk-dashboard-score-cell">
                          <strong>{formatScore(row.riskScore)}</strong>
                          {row.level ? <span className={`badge risk-${row.level.toLowerCase()}`}>{levelLabel(row.level)}</span> : null}
                        </div>
                      </td>
                      <td>{formatTrend(row.trend)}</td>
                      <td>{row.primaryFactor.replaceAll('_', ' ')}</td>
                      <td>{actionCell(row, (customerId) => flagMutation.mutate(customerId),
                        flagMutation.isPending && String(flagMutation.variables) === String(row.customerId))}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <div className="risk-dashboard-empty">No customers are available for risk monitoring.</div>
            )}
          </section>
        </>
      ) : null}
    </div>
  );
}