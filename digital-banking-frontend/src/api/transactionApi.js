import http from '../lib/http'

export async function postDeposit(payload) {
  const response = await http.post('/api/transactions/deposit', payload)
  return response.data
}

export async function postWithdrawal(payload) {
  const response = await http.post('/api/transactions/withdrawal', payload)
  return response.data
}

export async function postTransfer(payload) {
  const response = await http.post('/api/transactions/transfer', payload)
  return response.data
}

export async function postAdminAdjustment(payload) {
  const response = await http.post('/api/admin/transactions/adjustment', payload)
  return response.data
}

export async function postAdminReversal(payload) {
  const response = await http.post('/api/admin/transactions/reversal', payload)
  return response.data
}

