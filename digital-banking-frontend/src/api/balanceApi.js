import http from '../lib/http'

export async function getMyBalance(accountId) {
  const response = await http.get(`/api/balances/accounts/${accountId}`)
  return response.data
}

export async function getMyMiniStatement(accountId, params) {
  const response = await http.get(`/api/balances/accounts/${accountId}/mini-statement`, { params })
  return response.data
}

export async function getAdminBalance(accountId) {
  const response = await http.get(`/api/admin/balances/accounts/${accountId}`)
  return response.data
}

export async function getAdminMiniStatement(accountId, params) {
  const response = await http.get(`/api/admin/balances/accounts/${accountId}/mini-statement`, { params })
  return response.data
}

