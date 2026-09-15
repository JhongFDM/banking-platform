import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { listCustomers } from '../api/customers';
import { mapAxiosError } from '../api/axiosClient';

export default function ComplianceAuditObserverPage() {
  const customersQuery = useQuery({
    queryKey: ['audit-observer-customers'],
    queryFn: listCustomers
  });

  return (
    <div className="stack">
      <section className="panel stack">
        <h2>Compliance &amp; Audit Review</h2>
        <p className="muted text-top-muted">
          Read-only access to account control history and risk score explanations.
          Reviewers cannot freeze, unfreeze, delete, or otherwise change accounts.
        </p>
      </section>
      {customersQuery.isLoading ? <div className="banner info">Loading customers...</div> : null}
      {customersQuery.error ? (
        <div className="banner error">{mapAxiosError(customersQuery.error).message}</div>
      ) : null}
      {(customersQuery.data || []).map((customer) => (
        <section className="panel stack" key={customer.customerId}>
          <div className="section-header">
            <div>
              <h3 className="zero-margin">{customer.name}</h3>
              <p className="muted compact-text">Customer #{customer.customerId}</p>
            </div>
            <Link className="button-link subtle" to={`/audit-observer/${customer.customerId}/risk-assessment`}>
              Review Risk Explanation
            </Link>
          </div>
          {(customer.accounts || []).length > 0 ? (
            <div className="account-picker-list">
              {customer.accounts.map((account) => (
                <Link className="account-picker-item" key={account.accountId} to={`/accounts/${account.accountId}`}>
                  <span className="account-picker-id">#{account.accountId}</span>
                  <span className="account-picker-meta">
                    {account.accountType} · {account.status} · View freeze/unfreeze history
                  </span>
                </Link>
              ))}
            </div>
          ) : <p className="muted compact-text">No active accounts to review.</p>}
        </section>
      ))}
    </div>
  );
}
