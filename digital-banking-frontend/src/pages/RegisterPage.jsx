import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registerCustomer } from '../api/authApi'
import { getApiErrorMessage } from '../lib/http'

const initialForm = {
  fullName: '',
  email: '',
  mobileNumber: '',
  dateOfBirth: '',
  password: '',
  confirmPassword: '',
}

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function onChange(event) {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setMessage('')

    try {
      await registerCustomer(form)
      setMessage('Customer registered successfully.')
      setForm(initialForm)
      navigate('/login', { replace: true })
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel">
      <h2>Customer Registration</h2>
      <form onSubmit={onSubmit} className="form-grid">
        <label>
          Full Name
          <input name="fullName" value={form.fullName} onChange={onChange} required />
        </label>
        <label>
          Email
          <input type="email" name="email" value={form.email} onChange={onChange} required />
        </label>
        <label>
          Mobile Number
          <input name="mobileNumber" value={form.mobileNumber} onChange={onChange} required />
        </label>
        <label>
          Date of Birth
          <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={onChange} />
        </label>
        <label>
          Password
          <input type="password" name="password" value={form.password} onChange={onChange} required />
        </label>
        <label>
          Confirm Password
          <input type="password" name="confirmPassword" value={form.confirmPassword} onChange={onChange} required />
        </label>
        <div className="full-width">
          <button className="button" type="submit" disabled={loading}>
            {loading ? 'Registering...' : 'Register'}
          </button>
        </div>
      </form>
      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}
    </section>
  )
}
