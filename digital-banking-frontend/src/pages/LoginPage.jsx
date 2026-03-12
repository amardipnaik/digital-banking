import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from '../api/authApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'
import { getOrCreateDeviceId } from '../lib/deviceId'

export default function LoginPage() {
  const [form, setForm] = useState({ loginId: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const location = useLocation()
  const { setAuth } = useAuth()

  function onChange(event) {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')

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

  return (
    <section className="login-stage">
      <div className="login-hero-card">
        <span className="login-eyebrow">Digital Banking Access</span>
        <h1>Welcome back to your secure banking space.</h1>
        <p className="login-hero-copy">
          Sign in to continue with customer onboarding, profile verification, and role-based banking access from one secure gateway.
        </p>
        <div className="login-trust-grid">
          <div className="trust-tile">
            <span className="trust-kicker">Security</span>
            <strong>JWT session protection</strong>
            <p>Protected login flow with lock policy and audit logging.</p>
          </div>
          <div className="trust-tile">
            <span className="trust-kicker">Verification</span>
            <strong>Email and mobile checks</strong>
            <p>Users can complete onboarding after first sign-in from their profile.</p>
          </div>
          <div className="trust-tile">
            <span className="trust-kicker">Control</span>
            <strong>Admin-aware access</strong>
            <p>Role-driven screens and account status controls stay inside one backend contract.</p>
          </div>
        </div>
      </div>

      <div className="panel auth-card auth-card-login">
        <span className="login-eyebrow">Secure Sign In</span>
        <h2>Access your account</h2>
        <p className="auth-card-copy">Use your registered email or mobile number to enter the banking portal.</p>

        <form onSubmit={onSubmit} className="form-grid auth-form" autoComplete="off">
          <label>
            Email or Mobile
            <input
              name="loginId"
              value={form.loginId}
              onChange={onChange}
              autoComplete="off"
              autoCorrect="off"
              spellCheck={false}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={onChange}
              autoComplete="new-password"
              required
            />
          </label>
          <div className="full-width login-help-row">
            <Link to="/register" className="text-link">
              New to banking? Register here
            </Link>
            <Link to="/forgot-password" className="text-link">
              Forgot Password?
            </Link>
          </div>
          <div className="full-width">
            <button className="button auth-submit" type="submit" disabled={loading}>
              {loading ? 'Signing in...' : 'Enter Banking Portal'}
            </button>
          </div>
        </form>

        {error ? <p className="status-error auth-inline-message">{error}</p> : null}
      </div>
    </section>
  )
}
