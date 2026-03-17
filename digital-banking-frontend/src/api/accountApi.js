import http from '../lib/http'

export async function createAccount(payload) {
  const response = await http.post('/api/accounts', payload)
  return response.data
}

export async function listMyAccounts(params) {
  const response = await http.get('/api/accounts', { params })
  return response.data
}

export async function getMyAccount(accountId) {
  const response = await http.get(`/api/accounts/${accountId}`)
  return response.data
}

export async function listAdminAccounts(params) {
  const response = await http.get('/api/admin/accounts', { params })
  return response.data
}

export async function updateAdminAccountStatus(accountId, payload) {
  const response = await http.patch(`/api/admin/accounts/${accountId}/status`, payload)
  return response.data
}

export async function getAdminAccountHistory(accountId, params) {
  const response = await http.get(`/api/admin/accounts/${accountId}/history`, { params })
  return response.data
}

