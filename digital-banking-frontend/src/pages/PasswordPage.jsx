import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword, resetPassword } from '../api/authApi'
import { getApiErrorMessage } from '../lib/http'

export default function PasswordPage() {
  const [forgotForm, setForgotForm] = useState({ loginId: '' })
  const [resetForm, setResetForm] = useState({
    loginId: '',
    token: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [forgotMessage, setForgotMessage] = useState('')
  const [resetMessage, setResetMessage] = useState('')
  const [error, setError] = useState('')

  function onForgotChange(event) {
    const { name, value } = event.target
    setForgotForm((previous) => ({ ...previous, [name]: value }))
  }

  function onResetChange(event) {
    const { name, value } = event.target
    setResetForm((previous) => ({ ...previous, [name]: value }))
  }

  async function submitForgot(event) {
    event.preventDefault()
    setError('')
    setForgotMessage('')
    try {
      const response = await forgotPassword(forgotForm)
      setForgotMessage(response?.data?.message || 'Forgot password request submitted.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  async function submitReset(event) {
    event.preventDefault()
    setError('')
    setResetMessage('')
    try {
      const response = await resetPassword(resetForm)
      setResetMessage(response?.data?.message || 'Password reset completed.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  return (
    <section className="login-stage">
      <div className="login-hero-card">
        <span className="login-eyebrow">Access Recovery</span>
        <h1>Recover your banking access with one guided OTP flow.</h1>
        <p className="login-hero-copy">
          Request an OTP, confirm your identity, and set a new password to regain access to your banking account securely.
        </p>
        <div className="login-trust-grid">
          <div className="trust-tile">
            <span className="trust-kicker">Step 1</span>
            <strong>Request OTP</strong>
            <p>Use your registered email or mobile number to generate the reset OTP for your account.</p>
          </div>
          <div className="trust-tile">
            <span className="trust-kicker">Step 2</span>
            <strong>Confirm identity</strong>
            <p>Enter the OTP provided for your registered login identifier to continue securely.</p>
          </div>
          <div className="trust-tile">
            <span className="trust-kicker">Step 3</span>
            <strong>Set new password</strong>
            <p>Create a new password and return to login with the updated credentials.</p>
          </div>
        </div>
      </div>

      <div className="panel auth-card auth-card-login">
        <span className="login-eyebrow">Password Recovery</span>
        <h2>Forgot your password?</h2>
        <p className="auth-card-copy">Complete the OTP verification and choose a new password to restore access.</p>

        <div className="recovery-shell recovery-shell-open">
          <div className="recovery-block">
            <h3>Request reset OTP</h3>
            <form className="form-grid auth-form" onSubmit={submitForgot}>
              <label>
                Email or Mobile
                <input name="loginId" value={forgotForm.loginId} onChange={onForgotChange} placeholder="Registered email or mobile" required />
              </label>
              <div className="full-width">
                <button className="button button-secondary auth-submit" type="submit">
                  Request Reset OTP
                </button>
              </div>
            </form>
            {forgotMessage ? <p className="status-success auth-inline-message">{forgotMessage}</p> : null}
          </div>

          <div className="recovery-block">
            <h3>Set new password</h3>
            <form className="form-grid auth-form" onSubmit={submitReset}>
              <label>
                Email or Mobile
                <input name="loginId" value={resetForm.loginId} onChange={onResetChange} required />
              </label>
              <label>
                Reset OTP
                <input name="token" value={resetForm.token} onChange={onResetChange} placeholder="Enter OTP" required />
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
                <button className="button auth-submit" type="submit">
                  Reset Password
                </button>
              </div>
            </form>
            {resetMessage ? <p className="status-success auth-inline-message">{resetMessage}</p> : null}
          </div>
        </div>

        <div className="login-help-row">
          <Link to="/login" className="text-link">
            Back to Login
          </Link>
          <Link to="/register" className="text-link">
            Need an account? Register
          </Link>
        </div>

        {error ? <p className="status-error auth-inline-message">{error}</p> : null}
      </div>
    </section>
  )
}
