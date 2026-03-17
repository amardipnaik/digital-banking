import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import PasswordPage from './pages/PasswordPage'
import MePage from './pages/MePage'
import AdminCustomersPage from './pages/AdminCustomersPage'
import AdminCustomerDetailPage from './pages/AdminCustomerDetailPage'
import AdminAccountsPage from './pages/AdminAccountsPage'
import AccountsPage from './pages/AccountsPage'
import ProtectedRoute from './routes/ProtectedRoute'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<PasswordPage />} />
        <Route path="/password" element={<Navigate to="/forgot-password" replace />} />
        <Route
          path="/me"
          element={
            <ProtectedRoute>
              <MePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/accounts"
          element={
            <ProtectedRoute>
              <AccountsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/customers"
          element={
            <ProtectedRoute requireAdmin>
              <AdminCustomersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/accounts"
          element={
            <ProtectedRoute requireAdmin>
              <AdminAccountsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/customers/:userId"
          element={
            <ProtectedRoute requireAdmin>
              <AdminCustomerDetailPage />
            </ProtectedRoute>
          }
        />
        <Route path="/verify" element={<Navigate to="/me" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  )
}

export default App
