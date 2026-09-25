import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { mapAxiosError } from '../api/axiosClient';
import { listPendingRiskReviews, resolveRiskReview } from '../api/riskAssessment';

export function AdminRiskReviewQueuePage() {
  const queryClient = useQueryClient();
  const reviewsQuery = useQuery({
    queryKey: ['admin-risk-reviews'],
    queryFn: listPendingRiskReviews
  });
  const resolveMutation = useMutation({
    mutationFn: resolveRiskReview,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-risk-reviews'] });
      queryClient.invalidateQueries({ queryKey: ['risk-analyst-dashboard'] });
    }
  });
  const error = reviewsQuery.error || resolveMutation.error;

  return (
    <div className="stack">
      <section className="panel stack">
        <div>
          <h2>Risk Reviews</h2>
          <p className="muted text-top-muted">High-risk customers flagged by analysts for administrator review.</p>
        </div>
      </section>
      {reviewsQuery.isLoading ? <div className="banner info">Loading pending reviews...</div> : null}
      {error ? <div className="banner error">{mapAxiosError(error).message}</div> : null}
      {reviewsQuery.data?.length ? (
        <section className="table-shell risk-dashboard-table-shell">
          <table>
            <thead>
              <tr>
                <th>Customer</th>
                <th>Risk Score</th>
                <th>Primary Factor</th>
                <th>Flagged At</th>
                <th>Analyst User ID</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {reviewsQuery.data.map((review) => (
                <tr key={review.reviewId}>
                  <td>{review.customerName} <span className="muted">#{review.customerId}</span></td>
                  <td><span className="badge risk-high">{review.riskScore.toFixed(2)} · High</span></td>
                  <td>{review.primaryFactor.replaceAll('_', ' ')}</td>
                  <td>{new Date(review.createdAt).toLocaleString()}</td>
                  <td className="code">{review.flaggedBy}</td>
                  <td>
                    <div className="risk-dashboard-actions">
                      <Link className="button-link subtle" to={`/admin/${review.customerId}/risk-assessment`}>Review details</Link>
                      <button
                        type="button"
                        onClick={() => resolveMutation.mutate(review.reviewId)}
                        disabled={resolveMutation.isPending}
                      >
                        Mark reviewed
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      ) : !reviewsQuery.isLoading && !error ? (
        <section className="panel">
          <h3>No pending risk reviews</h3>
          <p className="muted">Analyst flags will appear here for administrator review.</p>
        </section>
      ) : null}
    </div>
  );
}