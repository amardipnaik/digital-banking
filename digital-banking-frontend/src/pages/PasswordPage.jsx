import { useState } from 'react'
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
  const [message, setMessage] = useState('')
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
    setMessage('')
    try {
      const response = await forgotPassword(forgotForm)
      setMessage(response?.data?.message || 'Forgot password request submitted.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  async function submitReset(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const response = await resetPassword(resetForm)
      setMessage(response?.data?.message || 'Password reset completed.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  return (
    <div className="stack">
      <section className="panel">
        <h2>Forgot Password</h2>
        <form className="form-grid" onSubmit={submitForgot}>
          <label>
            Email or Mobile
            <input name="loginId" value={forgotForm.loginId} onChange={onForgotChange} required />
          </label>
          <div className="full-width">
            <button className="button" type="submit">Request Reset</button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>Reset Password</h2>
        <form className="form-grid" onSubmit={submitReset}>
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
            <button className="button" type="submit">Reset Password</button>
          </div>
        </form>
      </section>

      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}
    </div>
  )
}
