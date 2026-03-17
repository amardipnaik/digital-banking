import { useCallback, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { createAccount, listMyAccounts } from '../api/accountApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

export default function AccountsPage() {
  const { user } = useAuth()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({ accountType: 'SAVINGS', currencyCode: 'INR' })

  const loadAccounts = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const response = await listMyAccounts({ page: 0, size: 20, sort: 'createdAt,desc' })
      setItems(response.data?.items || [])
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  async function handleCreate(event) {
    event.preventDefault()
    setCreating(true)
    setError('')
    setMessage('')
    try {
      const response = await createAccount({
        accountType: form.accountType,
        currencyCode: form.currencyCode.trim().toUpperCase() || 'INR',
      })
      setMessage(`Account ${response.data.accountNumber} created in ${response.data.status} status.`)
      await loadAccounts()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setCreating(false)
    }
  }

  if (user?.role !== 'CUSTOMER') {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin/customers' : '/me'} replace />
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>My Accounts</h2>
          <p className="profile-subtitle">Open and track your bank accounts.</p>
        </div>
      </div>

      <form className="form-grid" onSubmit={handleCreate}>
        <label>
          Account Type
          <select
            value={form.accountType}
            onChange={(event) => setForm((prev) => ({ ...prev, accountType: event.target.value }))}
          >
            <option value="SAVINGS">SAVINGS</option>
            <option value="CURRENT">CURRENT</option>
          </select>
        </label>
        <label>
          Currency
          <input
            value={form.currencyCode}
            onChange={(event) => setForm((prev) => ({ ...prev, currencyCode: event.target.value }))}
            maxLength={3}
          />
        </label>
        <div className="full-width">
          <button className="button" type="submit" disabled={creating}>
            {creating ? 'Creating...' : 'Open New Account'}
          </button>
        </div>
      </form>

      {message ? <p className="status-success">{message}</p> : null}
      {error ? <p className="status-error">{error}</p> : null}

      {loading ? (
        <p>Loading accounts...</p>
      ) : items.length ? (
        <div className="customer-table-wrap">
          <table className="customer-table">
            <thead>
              <tr>
                <th>Account Number</th>
                <th>Type</th>
                <th>Currency</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {items.map((account) => (
                <tr key={account.accountId}>
                  <td className="customer-name-cell">{account.accountNumber}</td>
                  <td>{account.accountType}</td>
                  <td>{account.currencyCode}</td>
                  <td>
                    <span className={`pill ${account.status === 'ACTIVE' ? 'pill-success' : account.status === 'CLOSED' ? 'pill-danger' : 'pill-warning'}`}>
                      {account.status}
                    </span>
                  </td>
                  <td>{account.createdAt ? new Date(account.createdAt).toLocaleString() : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>No accounts found. Create your first account using the form above.</p>
      )}
    </section>
  )
}

