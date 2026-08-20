import { accountApiClient } from "./axiosClient";

export async function listRiskHistory(customerId){
    const response = await accountApiClient.get(`/api/risk_score/customers/${customerId}/history`)
    console.log(response.data)
    return response.data;
}