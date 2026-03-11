import { useState } from 'react'
import { clearStoredUser, clearToken, getStoredUser, getToken, setStoredUser, setToken } from '../lib/tokenStore'
import { logout as logoutApi } from '../api/authApi'
import AuthContext from './auth-context-value'

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(getToken())
  const [user, setUserState] = useState(getStoredUser())

  function setAuth(loginToken, loginUser) {
    setToken(loginToken)
    setStoredUser(loginUser)
    setTokenState(loginToken)
    setUserState(loginUser)
  }

  async function clearAuth(callLogout = false) {
    if (callLogout && token) {
      try {
        await logoutApi()
      } catch {
        // Ignore logout errors while clearing local auth.
      }
    }
    clearToken()
    clearStoredUser()
    setTokenState(null)
    setUserState(null)
  }

  const value = {
    token,
    user,
    isAuthenticated: Boolean(token),
    isAdmin: user?.role === 'ADMIN',
    setAuth,
    clearAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
