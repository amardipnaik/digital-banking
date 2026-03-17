import { useCallback, useEffect, useMemo, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { listMyAccounts } from '../api/accountApi'
import { postDeposit, postTransfer, postWithdrawal } from '../api/transactionApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

function randomKey(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

export default function TransactionsPage() {
  const { user } = useAuth()
  const [type, setType] = useState('DEPOSIT')
  const [accounts, setAccounts] = useState([])
  const [loadingAccounts, setLoadingAccounts] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    accountId: '',
    targetAccountId: '',
    amount: '',
    currencyCode: 'INR',
    remarks: '',
  })

  const activeAccounts = useMemo(() => accounts.filter((item) => item.status === 'ACTIVE'), [accounts])

  const loadAccounts = useCallback(async () => {
    setLoadingAccounts(true)
    setError('')
    try {
      const response = await listMyAccounts({ page: 0, size: 100, sort: 'createdAt,desc' })
      const items = response.data?.items || []
      setAccounts(items)
      if (!form.accountId && items.length) {
        setForm((prev) => ({ ...prev, accountId: String(items[0].accountId), currencyCode: items[0].currencyCode || 'INR' }))
      }
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoadingAccounts(false)
    }
  }, [form.accountId])

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  function onFormChange(event) {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setMessage('')
    try {
      const amount = Number(form.amount)
      if (!amount || amount <= 0) {
        setError('Amount must be greater than 0.')
        setSubmitting(false)
        return
      }

      const idempotencyKey = randomKey(type.toLowerCase())
      let response
      if (type === 'DEPOSIT') {
        response = await postDeposit({
          accountId: Number(form.accountId),
          amount,
          currencyCode: form.currencyCode.trim().toUpperCase(),
          remarks: form.remarks,
          idempotencyKey,
        })
      } else if (type === 'WITHDRAWAL') {
        response = await postWithdrawal({
          accountId: Number(form.accountId),
          amount,
          currencyCode: form.currencyCode.trim().toUpperCase(),
          remarks: form.remarks,
          idempotencyKey,
        })
      } else {
        response = await postTransfer({
          sourceAccountId: Number(form.accountId),
          targetAccountId: Number(form.targetAccountId),
          amount,
          currencyCode: form.currencyCode.trim().toUpperCase(),
          remarks: form.remarks,
          idempotencyKey,
        })
      }

      setMessage(response.data?.message || 'Transaction posted successfully.')
      setForm((prev) => ({ ...prev, amount: '', remarks: '' }))
      await loadAccounts()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (user?.role !== 'CUSTOMER') {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin/transactions' : '/me'} replace />
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>Transactions</h2>
          <p className="profile-subtitle">Deposit, withdraw, or transfer from your active accounts.</p>
        </div>
      </div>

      <div className="customer-filter-actions">
        <button type="button" className={`button ${type === 'DEPOSIT' ? '' : 'button-secondary'}`} onClick={() => setType('DEPOSIT')}>
          Deposit
        </button>
        <button type="button" className={`button ${type === 'WITHDRAWAL' ? '' : 'button-secondary'}`} onClick={() => setType('WITHDRAWAL')}>
          Withdrawal
        </button>
        <button type="button" className={`button ${type === 'TRANSFER' ? '' : 'button-secondary'}`} onClick={() => setType('TRANSFER')}>
          Transfer
        </button>
      </div>

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Source Account
          <select name="accountId" value={form.accountId} onChange={onFormChange} disabled={loadingAccounts || !activeAccounts.length}>
            {activeAccounts.length ? (
              activeAccounts.map((account) => (
                <option key={account.accountId} value={account.accountId}>
                  {account.accountNumber} ({account.currencyCode})
                </option>
              ))
            ) : (
              <option value="">No active account</option>
            )}
          </select>
        </label>

        {type === 'TRANSFER' ? (
          <label>
            Target Account ID
            <input name="targetAccountId" value={form.targetAccountId} onChange={onFormChange} placeholder="Enter target account id" required />
          </label>
        ) : (
          <label>
            Currency
            <input name="currencyCode" value={form.currencyCode} onChange={onFormChange} maxLength={3} />
          </label>
        )}

        <label>
          Amount
          <input type="number" min="0" step="0.01" name="amount" value={form.amount} onChange={onFormChange} required />
        </label>

        <label>
          Remarks
          <input name="remarks" value={form.remarks} onChange={onFormChange} />
        </label>

        <div className="full-width">
          <button className="button" type="submit" disabled={submitting || loadingAccounts || !activeAccounts.length}>
            {submitting ? 'Processing...' : `Submit ${type}`}
          </button>
        </div>
      </form>

      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}
    </section>
  )
}

