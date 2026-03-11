import { createContext, useContext, useMemo, useState } from 'react'
import { clearStoredUser, clearToken, getStoredUser, getToken, setStoredUser, setToken } from '../lib/tokenStore'
import { logout as logoutApi } from '../api/authApi'

const AuthContext = createContext(null)

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

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      isAdmin: user?.role === 'ADMIN',
      setAuth,
      clearAuth,
    }),
    [token, user],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}
