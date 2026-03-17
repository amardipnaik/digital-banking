import { useState } from 'react'
import { postAdminAdjustment, postAdminReversal } from '../api/transactionApi'
import { getApiErrorMessage } from '../lib/http'

function randomKey(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

export default function AdminTransactionsPage() {
  const [adjustment, setAdjustment] = useState({
    accountId: '',
    entrySide: 'CREDIT',
    amount: '',
    currencyCode: 'INR',
    reason: '',
  })
  const [reversal, setReversal] = useState({ originalTransactionRef: '', reason: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submittingAdjustment, setSubmittingAdjustment] = useState(false)
  const [submittingReversal, setSubmittingReversal] = useState(false)

  function onAdjustmentChange(event) {
    const { name, value } = event.target
    setAdjustment((prev) => ({ ...prev, [name]: value }))
  }

  function onReversalChange(event) {
    const { name, value } = event.target
    setReversal((prev) => ({ ...prev, [name]: value }))
  }

  async function submitAdjustment(event) {
    event.preventDefault()
    setSubmittingAdjustment(true)
    setError('')
    setMessage('')
    try {
      const response = await postAdminAdjustment({
        accountId: Number(adjustment.accountId),
        entrySide: adjustment.entrySide,
        amount: Number(adjustment.amount),
        currencyCode: adjustment.currencyCode.trim().toUpperCase(),
        reason: adjustment.reason,
        idempotencyKey: randomKey('adj'),
      })
      setMessage(response.data?.message || 'Adjustment posted successfully.')
      setAdjustment((prev) => ({ ...prev, amount: '', reason: '' }))
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSubmittingAdjustment(false)
    }
  }

  async function submitReversal(event) {
    event.preventDefault()
    setSubmittingReversal(true)
    setError('')
    setMessage('')
    try {
      const response = await postAdminReversal({
        originalTransactionRef: reversal.originalTransactionRef.trim(),
        reason: reversal.reason,
      })
      setMessage(response.data?.message || 'Reversal posted successfully.')
      setReversal({ originalTransactionRef: '', reason: '' })
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSubmittingReversal(false)
    }
  }

  return (
    <section className="stack">
      <section className="panel stack">
        <div className="profile-header">
          <div>
            <h2>Transaction Operations</h2>
            <p className="profile-subtitle">Post controlled adjustments for operational corrections.</p>
          </div>
        </div>

        <form className="form-grid" onSubmit={submitAdjustment}>
          <label>
            Account ID
            <input name="accountId" value={adjustment.accountId} onChange={onAdjustmentChange} required />
          </label>
          <label>
            Entry Side
            <select name="entrySide" value={adjustment.entrySide} onChange={onAdjustmentChange}>
              <option value="CREDIT">CREDIT</option>
              <option value="DEBIT">DEBIT</option>
            </select>
          </label>
          <label>
            Amount
            <input type="number" min="0" step="0.01" name="amount" value={adjustment.amount} onChange={onAdjustmentChange} required />
          </label>
          <label>
            Currency
            <input name="currencyCode" value={adjustment.currencyCode} onChange={onAdjustmentChange} maxLength={3} required />
          </label>
          <label className="full-width">
            Reason
            <input name="reason" value={adjustment.reason} onChange={onAdjustmentChange} required />
          </label>
          <div className="full-width">
            <button className="button" type="submit" disabled={submittingAdjustment}>
              {submittingAdjustment ? 'Posting...' : 'Post Adjustment'}
            </button>
          </div>
        </form>
      </section>

      <section className="panel stack">
        <div className="profile-header">
          <div>
            <h3>Reverse Transaction</h3>
            <p className="profile-subtitle">Create a compensating reversal entry for a posted transaction reference.</p>
          </div>
        </div>

        <form className="form-grid" onSubmit={submitReversal}>
          <label>
            Original Transaction Ref
            <input name="originalTransactionRef" value={reversal.originalTransactionRef} onChange={onReversalChange} required />
          </label>
          <label>
            Reason
            <input name="reason" value={reversal.reason} onChange={onReversalChange} required />
          </label>
          <div className="full-width">
            <button className="button" type="submit" disabled={submittingReversal}>
              {submittingReversal ? 'Reversing...' : 'Post Reversal'}
            </button>
          </div>
        </form>
      </section>

      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}
    </section>
  )
}

