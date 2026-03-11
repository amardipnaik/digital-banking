import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { forgotPassword, login, resetPassword } from '../api/authApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'
import { getOrCreateDeviceId } from '../lib/deviceId'

export default function LoginPage() {
  const [form, setForm] = useState({ loginId: '', password: '' })
  const [forgotForm, setForgotForm] = useState({ loginId: '' })
  const [resetForm, setResetForm] = useState({
    loginId: '',
    token: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [showForgotFlow, setShowForgotFlow] = useState(false)
  const [loading, setLoading] = useState(false)
  const [forgotLoading, setForgotLoading] = useState(false)
  const [resetLoading, setResetLoading] = useState(false)
  const [forgotMessage, setForgotMessage] = useState('')
  const [resetMessage, setResetMessage] = useState('')
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const { setAuth } = useAuth()

  function onChange(event) {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
  }

  function onForgotChange(event) {
    const { name, value } = event.target
    setForgotForm((previous) => ({ ...previous, [name]: value }))
  }

  function onResetChange(event) {
    const { name, value } = event.target
    setResetForm((previous) => ({ ...previous, [name]: value }))
  }

  function toggleForgotFlow() {
    const prefillLoginId = form.loginId.trim()
    if (prefillLoginId) {
      setForgotForm((previous) => ({ ...previous, loginId: previous.loginId || prefillLoginId }))
      setResetForm((previous) => ({ ...previous, loginId: previous.loginId || prefillLoginId }))
    }
    setShowForgotFlow((previous) => !previous)
    setError('')
  }

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setForgotMessage('')
    setResetMessage('')

    try {
      const response = await login({ ...form, deviceId: getOrCreateDeviceId() })
      setAuth(response.data.accessToken, response.data.user)
      const redirectTo = location.state?.from || '/me'
      navigate(redirectTo)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function submitForgot(event) {
    event.preventDefault()
    setForgotLoading(true)
    setError('')
    setForgotMessage('')
    setResetMessage('')

    try {
      const response = await forgotPassword(forgotForm)
      setForgotMessage(response?.data?.message || 'Reset token request submitted.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setForgotLoading(false)
    }
  }

  async function submitSetNewPassword(event) {
    event.preventDefault()
    setResetLoading(true)
    setError('')
    setResetMessage('')

    try {
      const response = await resetPassword(resetForm)
      setResetMessage(response?.data?.message || 'Password updated successfully.')
      setResetForm((previous) => ({
        ...previous,
        token: '',
        newPassword: '',
        confirmPassword: '',
      }))
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setResetLoading(false)
    }
  }

  return (
    <div className="stack">
      <section className="panel">
        <h2>Login</h2>
        <form onSubmit={onSubmit} className="form-grid">
          <label>
            Email or Mobile
            <input name="loginId" value={form.loginId} onChange={onChange} required />
          </label>
          <label>
            Password
            <input type="password" name="password" value={form.password} onChange={onChange} required />
          </label>
          <div className="full-width login-help-row">
            <button type="button" className="link-button" onClick={toggleForgotFlow}>
              {showForgotFlow ? 'Hide Forgot Password' : 'Forgot Password?'}
            </button>
          </div>
          <div className="full-width">
            <button className="button" type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Login'}
            </button>
          </div>
        </form>
      </section>

      {showForgotFlow ? (
        <section className="panel">
          <h3>Forgot Password</h3>
          <form className="form-grid" onSubmit={submitForgot}>
            <label>
              Email or Mobile
              <input name="loginId" value={forgotForm.loginId} onChange={onForgotChange} required />
            </label>
            <div className="full-width">
              <button className="button" type="submit" disabled={forgotLoading}>
                {forgotLoading ? 'Sending...' : 'Send Reset Token'}
              </button>
            </div>
          </form>
          {forgotMessage ? <p className="status-success">{forgotMessage}</p> : null}

          <h3 style={{ marginTop: '1rem' }}>Set New Password</h3>
          <form className="form-grid" onSubmit={submitSetNewPassword}>
            <label>
              Email or Mobile
              <input name="loginId" value={resetForm.loginId} onChange={onResetChange} required />
            </label>
            <label>
              Reset Token
              <input name="token" value={resetForm.token} onChange={onResetChange} required />
            </label>
            <label>
              New Password
              <input type="password" name="newPassword" value={resetForm.newPassword} onChange={onResetChange} required />
            </label>
            <label>
              Confirm Password
              <input type="password" name="confirmPassword" value={resetForm.confirmPassword} onChange={onResetChange} required />
            </label>
            <div className="full-width">
              <button className="button" type="submit" disabled={resetLoading}>
                {resetLoading ? 'Updating...' : 'Set New Password'}
              </button>
            </div>
          </form>
          {resetMessage ? <p className="status-success">{resetMessage}</p> : null}
        </section>
      ) : null}

      {error ? <p className="status-error">{error}</p> : null}
    </div>
  )
}
