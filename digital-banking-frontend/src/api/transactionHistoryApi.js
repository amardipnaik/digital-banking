import http from '../lib/http'

export async function listMyTransactionHistory(params) {
  const response = await http.get('/api/transactions/history', { params })
  return response.data
}

export async function getMyTransactionDetail(transactionRef) {
  const response = await http.get(`/api/transactions/history/${transactionRef}`)
  return response.data
}

export async function listAdminTransactionHistory(params) {
  const response = await http.get('/api/admin/transactions/history', { params })
  return response.data
}

export async function getAdminTransactionDetail(transactionRef) {
  const response = await http.get(`/api/admin/transactions/history/${transactionRef}`)
  return response.data
}

