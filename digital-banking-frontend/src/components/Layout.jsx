import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

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
        <Link to="/" className="brand">
          Digital Banking
        </Link>
        <nav className="top-nav">
          <NavLink to="/" className={linkClass}>
            Home
          </NavLink>
          <NavLink to="/register" className={linkClass}>
            Register
          </NavLink>
          <NavLink to="/login" className={linkClass}>
            Login
          </NavLink>
          <NavLink to="/verify" className={linkClass}>
            Verify
          </NavLink>
          <NavLink to="/password" className={linkClass}>
            Password
          </NavLink>
          <NavLink to="/me" className={linkClass}>
            My Profile
          </NavLink>
          {isAdmin && (
            <NavLink to="/admin/user-status" className={linkClass}>
              Admin
            </NavLink>
          )}
        </nav>
        <div className="session-actions">
          <span className="session-user">{isAuthenticated ? `${user?.role || 'USER'}` : 'Guest'}</span>
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
