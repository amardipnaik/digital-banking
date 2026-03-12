import http from '../lib/http'

export async function listCustomers(params) {
  const response = await http.get('/api/admin/customers', { params })
  return response.data
}

export async function getCustomer(userId) {
  const response = await http.get(`/api/admin/customers/${userId}`)
  return response.data
}

export async function updateCustomerProfile(userId, payload) {
  const response = await http.patch(`/api/admin/customers/${userId}`, payload)
  return response.data
}

export async function updateCustomerKyc(userId, payload) {
  const response = await http.patch(`/api/admin/customers/${userId}/kyc`, payload)
  return response.data
}

export async function softDeleteCustomer(userId) {
  const response = await http.delete(`/api/admin/customers/${userId}`)
  return response.data
}

export async function restoreCustomer(userId) {
  const response = await http.patch(`/api/admin/customers/${userId}/restore`)
  return response.data
}

export async function getCustomerActivity(userId, params) {
  const response = await http.get(`/api/admin/customers/${userId}/activity`, { params })
  return response.data
}

