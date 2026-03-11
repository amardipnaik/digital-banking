import { Link, NavLink, useNavigate } from 'react-router-dom'
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
        <Link to="/" className="brand">
          Digital Banking
        </Link>
        <nav className="top-nav">
          <NavLink to="/" className={linkClass}>
            Auth
          </NavLink>
          <NavLink to="/status" className={linkClass}>
            Status
          </NavLink>
          <Link to="/?tab=me" className="nav-link">
            My Profile
          </Link>
          {isAdmin && (
            <Link to="/?tab=admin" className="nav-link">
              Admin
            </Link>
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
