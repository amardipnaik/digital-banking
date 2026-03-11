import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import RegisterPage from './pages/RegisterPage'
import LoginPage from './pages/LoginPage'
import MePage from './pages/MePage'
import AdminUserStatusPage from './pages/AdminUserStatusPage'
import ProtectedRoute from './routes/ProtectedRoute'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/password" element={<Navigate to="/login" replace />} />
        <Route
          path="/me"
          element={
            <ProtectedRoute>
              <MePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/user-status"
          element={
            <ProtectedRoute requireAdmin>
              <AdminUserStatusPage />
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
