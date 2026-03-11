import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import AuthWorkspacePage from './pages/AuthWorkspacePage'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<AuthWorkspacePage />} />
        <Route path="/status" element={<HomePage />} />
        <Route path="/login" element={<Navigate to="/" replace />} />
        <Route path="/register" element={<Navigate to="/?tab=register" replace />} />
        <Route path="/verify" element={<Navigate to="/?tab=verify" replace />} />
        <Route path="/password" element={<Navigate to="/?tab=password" replace />} />
        <Route path="/me" element={<Navigate to="/?tab=me" replace />} />
        <Route path="/admin/user-status" element={<Navigate to="/?tab=admin" replace />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  )
}

export default App
