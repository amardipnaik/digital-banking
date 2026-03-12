import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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
    <section className="login-stage">
      <div className="login-hero-card">
        <span className="login-eyebrow">Customer Onboarding</span>
        <h1>Open your digital banking access in a few guided steps.</h1>
        <p className="login-hero-copy">
          Create your customer account, then continue into profile verification and secure banking access from the same protected system.
        </p>
        <div className="login-trust-grid">
          <div className="trust-tile">
            <span className="trust-kicker">Identity</span>
            <strong>Verified onboarding flow</strong>
            <p>We capture the essential profile details needed to start your banking journey.</p>
          </div>
          <div className="trust-tile">
            <span className="trust-kicker">Security</span>
            <strong>Password-protected access</strong>
            <p>Your credentials are secured and tied to role-based access rules from day one.</p>
          </div>
          <div className="trust-tile">
            <span className="trust-kicker">Next step</span>
            <strong>Continue after sign in</strong>
            <p>Once registered, you can sign in and complete email and mobile verification from your profile.</p>
          </div>
        </div>
      </div>

      <div className="panel auth-card auth-card-login">
        <span className="login-eyebrow">New Customer Registration</span>
        <h2>Create your account</h2>
        <p className="auth-card-copy">Enter your details carefully so we can create your banking profile correctly.</p>

        <form onSubmit={onSubmit} className="form-grid auth-form">
          <label>
            Full Name
            <input name="fullName" value={form.fullName} onChange={onChange} placeholder="Enter your full name" required />
          </label>
          <label>
            Email
            <input type="email" name="email" value={form.email} onChange={onChange} placeholder="name@example.com" required />
          </label>
          <label>
            Mobile Number
            <input name="mobileNumber" value={form.mobileNumber} onChange={onChange} placeholder="Enter mobile number" required />
          </label>
          <label>
            Date of Birth
            <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={onChange} />
          </label>
          <label>
            Password
            <input type="password" name="password" value={form.password} onChange={onChange} placeholder="Create password" required />
          </label>
          <label>
            Confirm Password
            <input type="password" name="confirmPassword" value={form.confirmPassword} onChange={onChange} placeholder="Re-enter password" required />
          </label>
          <div className="full-width">
            <button className="button auth-submit" type="submit" disabled={loading}>
              {loading ? 'Registering...' : 'Create Banking Account'}
            </button>
          </div>
        </form>

        <div className="login-help-row">
          <Link to="/login" className="text-link">
            Already have an account? Login
          </Link>
          <Link to="/forgot-password" className="text-link">
            Forgot Password?
          </Link>
        </div>

        {message ? <p className="status-success auth-inline-message">{message}</p> : null}
        {error ? <p className="status-error auth-inline-message">{error}</p> : null}
      </div>
    </section>
  )
}
