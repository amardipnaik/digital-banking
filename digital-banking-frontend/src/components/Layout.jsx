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
        {isAuthenticated ? (
          <nav className="top-nav">
            <NavLink to="/me" className={linkClass}>
              My Profile
            </NavLink>
            {!isAdmin && (
              <>
                <NavLink to="/accounts" className={linkClass}>
                  Accounts
                </NavLink>
                <NavLink to="/transactions" className={linkClass}>
                  Transactions
                </NavLink>
                <NavLink to="/balances" className={linkClass}>
                  Balances
                </NavLink>
                <NavLink to="/transactions/history" className={linkClass}>
                  History
                </NavLink>
              </>
            )}
            {isAdmin && (
              <>
                <NavLink to="/admin/customers" className={linkClass}>
                  Customers
                </NavLink>
                <NavLink to="/admin/accounts" className={linkClass}>
                  Accounts
                </NavLink>
                <NavLink to="/admin/transactions" className={linkClass}>
                  Transactions
                </NavLink>
                <NavLink to="/admin/balances" className={linkClass}>
                  Balances
                </NavLink>
                <NavLink to="/admin/transactions/history" className={linkClass}>
                  History
                </NavLink>
              </>
            )}
          </nav>
        ) : (
          <div className="topbar-spacer" />
        )}
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
