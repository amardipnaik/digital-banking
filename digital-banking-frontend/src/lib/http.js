import axios from 'axios'
import { getToken } from './tokenStore'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function getApiErrorMessage(error) {
  if (error?.response?.data?.error?.message) {
    return error.response.data.error.message
  }
  if (error?.message) {
    return error.message
  }
  return 'Something went wrong. Please try again.'
}

export default http
