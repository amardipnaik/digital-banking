import { useState } from 'react'
import { confirmVerification, requestVerification } from '../api/authApi'
import { getApiErrorMessage } from '../lib/http'

export default function VerifyPage() {
  const [requestForm, setRequestForm] = useState({ loginId: '', channel: 'EMAIL' })
  const [confirmForm, setConfirmForm] = useState({ loginId: '', channel: 'EMAIL', token: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function onRequestChange(event) {
    const { name, value } = event.target
    setRequestForm((previous) => ({ ...previous, [name]: value }))
  }

  function onConfirmChange(event) {
    const { name, value } = event.target
    setConfirmForm((previous) => ({ ...previous, [name]: value }))
  }

  async function submitRequest(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const response = await requestVerification(requestForm)
      setMessage(response?.data?.message || 'Verification token requested successfully.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  async function submitConfirm(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const response = await confirmVerification(confirmForm)
      setMessage(response?.data?.message || 'Verification confirmed successfully.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  return (
    <div className="stack">
      <section className="panel">
        <h2>Request Verification Token</h2>
        <form className="form-grid" onSubmit={submitRequest}>
          <label>
            Email or Mobile
            <input name="loginId" value={requestForm.loginId} onChange={onRequestChange} required />
          </label>
          <label>
            Channel
            <select name="channel" value={requestForm.channel} onChange={onRequestChange}>
              <option value="EMAIL">EMAIL</option>
              <option value="MOBILE">MOBILE</option>
            </select>
          </label>
          <div className="full-width">
            <button className="button" type="submit">Request Token</button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h2>Confirm Verification</h2>
        <form className="form-grid" onSubmit={submitConfirm}>
          <label>
            Email or Mobile
            <input name="loginId" value={confirmForm.loginId} onChange={onConfirmChange} required />
          </label>
          <label>
            Channel
            <select name="channel" value={confirmForm.channel} onChange={onConfirmChange}>
              <option value="EMAIL">EMAIL</option>
              <option value="MOBILE">MOBILE</option>
            </select>
          </label>
          <label>
            Token
            <input name="token" value={confirmForm.token} onChange={onConfirmChange} required />
          </label>
          <div className="full-width">
            <button className="button" type="submit">Confirm Token</button>
          </div>
        </form>
      </section>

      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}
    </div>
  )
}
