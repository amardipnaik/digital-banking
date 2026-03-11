import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

function linkClass({ isActive }) {
  return isActive ? 'nav-link nav-link-active' : 'nav-link'
}

export default function Layout({ children }) {
  const { isAuthenticated, isAdmin, user, clearAuth } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await clearAuth(true)
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to="/" className="brand">
          Digital Banking
        </NavLink>
        <nav className="top-nav">
          {!isAuthenticated && (
            <>
              <NavLink to="/login" className={linkClass}>
                Login
              </NavLink>
              <NavLink to="/register" className={linkClass}>
                Register
              </NavLink>
            </>
          )}
          {isAuthenticated && (
            <NavLink to="/me" className={linkClass}>
              My Profile
            </NavLink>
          )}
          {isAdmin && (
            <NavLink to="/admin/user-status" className={linkClass}>
              Admin
            </NavLink>
          )}
        </nav>
        <div className="session-actions">
          {isAuthenticated ? <span className="session-user">{user?.role || 'USER'}</span> : null}
          {isAuthenticated ? (
            <button type="button" className="button button-secondary" onClick={handleLogout}>
              Logout
            </button>
          ) : null}
        </div>
      </header>
      <main className="content">{children}</main>
    </div>
  )
}
