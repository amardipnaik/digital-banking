import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { login } from '../api/authApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

export default function LoginPage() {
  const [form, setForm] = useState({ loginId: '', password: '', deviceId: '' })
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
      const response = await login(form)
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
        <label>
          Device ID
          <input name="deviceId" value={form.deviceId} onChange={onChange} />
        </label>
        <div className="full-width">
          <button className="button" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Login'}
          </button>
        </div>
      </form>
      {error ? <p className="status-error">{error}</p> : null}
    </section>
  )
}
