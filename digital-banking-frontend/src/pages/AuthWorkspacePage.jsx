import { useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import RegisterPage from './RegisterPage'
import LoginPage from './LoginPage'
import VerifyPage from './VerifyPage'
import PasswordPage from './PasswordPage'
import MePage from './MePage'
import AdminUserStatusPage from './AdminUserStatusPage'
import { useAuth } from '../context/useAuth'

const PUBLIC_TABS = [
  { id: 'login', label: 'Login' },
  { id: 'register', label: 'Register' },
  { id: 'verify', label: 'Verify' },
  { id: 'password', label: 'Password' },
]

function TabContent({ tab }) {
  if (tab === 'register') {
    return <RegisterPage />
  }
  if (tab === 'verify') {
    return <VerifyPage />
  }
  if (tab === 'password') {
    return <PasswordPage />
  }
  if (tab === 'me') {
    return <MePage />
  }
  if (tab === 'admin') {
    return <AdminUserStatusPage />
  }
  return <LoginPage />
}

export default function AuthWorkspacePage() {
  const { isAuthenticated, isAdmin } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()

  const tabs = useMemo(() => {
    const privateTabs = []
    if (isAuthenticated) {
      privateTabs.push({ id: 'me', label: 'My Profile' })
      if (isAdmin) {
        privateTabs.push({ id: 'admin', label: 'Admin' })
      }
    }
    return [...PUBLIC_TABS, ...privateTabs]
  }, [isAuthenticated, isAdmin])

  const requestedTab = searchParams.get('tab')
  const hasRequestedTab = tabs.some((tab) => tab.id === requestedTab)
  const activeTab = hasRequestedTab ? requestedTab : 'login'

  function onTabChange(nextTab) {
    if (nextTab === 'login') {
      setSearchParams({}, { replace: true })
      return
    }
    setSearchParams({ tab: nextTab }, { replace: true })
  }

  return (
    <div className="stack">
      <section className="panel">
        <h2>Authentication Workspace</h2>
        <p>All auth flows are available in one place. Login is the default screen.</p>
        <div className="tab-row" role="tablist" aria-label="Authentication Tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={tab.id === activeTab ? 'tab-button tab-button-active' : 'tab-button'}
              onClick={() => onTabChange(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </section>

      <TabContent tab={activeTab} />
    </div>
  )
}
