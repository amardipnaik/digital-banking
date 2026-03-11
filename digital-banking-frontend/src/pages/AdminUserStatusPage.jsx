import { useState } from 'react'
import { updateUserStatus } from '../api/authApi'
import { getApiErrorMessage } from '../lib/http'

export default function AdminUserStatusPage() {
  const [form, setForm] = useState({ userId: '', status: 'DISABLED', reason: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function onChange(event) {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const response = await updateUserStatus(form.userId, {
        status: form.status,
        reason: form.reason,
      })
      setMessage(response?.data?.message || 'User status updated successfully.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  return (
    <section className="panel">
      <h2>Admin: Update User Status</h2>
      <form className="form-grid" onSubmit={onSubmit}>
        <label>
          User ID
          <input name="userId" value={form.userId} onChange={onChange} required />
        </label>
        <label>
          Status
          <select name="status" value={form.status} onChange={onChange}>
            <option value="DISABLED">DISABLED</option>
            <option value="ACTIVE">ACTIVE</option>
          </select>
        </label>
        <label className="full-width">
          Reason
          <input name="reason" value={form.reason} onChange={onChange} />
        </label>
        <div className="full-width">
          <button type="submit" className="button">Update Status</button>
        </div>
      </form>
      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}
    </section>
  )
}
