import http from '../lib/http'

export async function registerCustomer(payload) {
  const response = await http.post('/api/auth/register/customer', payload)
  return response.data
}

export async function login(payload) {
  const response = await http.post('/api/auth/login', payload)
  return response.data
}

export async function requestVerification(payload) {
  const response = await http.post('/api/auth/verification/request', payload)
  return response.data
}

export async function confirmVerification(payload) {
  const response = await http.post('/api/auth/verification/confirm', payload)
  return response.data
}

export async function forgotPassword(payload) {
  const response = await http.post('/api/auth/password/forgot', payload)
  return response.data
}

export async function resetPassword(payload) {
  const response = await http.post('/api/auth/password/reset', payload)
  return response.data
}

export async function logout() {
  const response = await http.post('/api/auth/logout', {})
  return response.data
}

export async function me() {
  const response = await http.get('/api/auth/me')
  return response.data
}

export async function updateMyProfile(payload) {
  const response = await http.patch('/api/auth/me/profile', payload)
  return response.data
}

export async function updateUserStatus(userId, payload) {
  const response = await http.patch(`/api/admin/auth/users/${userId}/status`, payload)
  return response.data
}

export async function checkApiHealth() {
  const response = await http.get('/api/health')
  return response.data
}
