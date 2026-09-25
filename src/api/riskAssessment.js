import { accountApiClient } from "./axiosClient";

export async function listRiskHistory(customerId){
    const response = await accountApiClient.get(`/api/risk_score/customers/${customerId}/history`)
    console.log(response.data)
    return response.data;
}

export async function calculateRiskScore(customerId){
    const response = await accountApiClient.post(`/api/risk_score/customers/${customerId}`)
    console.log(response.data)
    return response.data;
}

export async function getRiskAnalystDashboard() {
    const response = await accountApiClient.get('/api/risk-analyst/dashboard');
    return response.data;
}

export async function flagCustomerForAdminReview(customerId) {
    const response = await accountApiClient.post(`/api/risk-analyst/reviews/customers/${customerId}`);
    return response.data;
}

export async function listPendingRiskReviews() {
    const response = await accountApiClient.get('/api/admin/risk-reviews');
    return response.data;
}

export async function resolveRiskReview(reviewId) {
    const response = await accountApiClient.post(`/api/admin/risk-reviews/${reviewId}/resolve`);
    return response.data;
}