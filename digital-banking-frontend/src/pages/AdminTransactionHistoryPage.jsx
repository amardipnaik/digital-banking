import { useState } from 'react'
import { listAdminTransactionHistory } from '../api/transactionHistoryApi'
import { getApiErrorMessage } from '../lib/http'

const DEFAULT_FILTERS = {
  accountId: '',
  userId: '',
  type: '',
  entrySide: '',
  status: '',
}

export default function AdminTransactionHistoryPage() {
  const [filters, setFilters] = useState(DEFAULT_FILTERS)
  const [data, setData] = useState({ items: [], page: 0, totalPages: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  function onChange(event) {
    const { name, value } = event.target
    setFilters((prev) => ({ ...prev, [name]: value }))
  }

  async function load() {
    setLoading(true)
    setError('')
    try {
      const response = await listAdminTransactionHistory({
        accountId: filters.accountId || undefined,
        userId: filters.userId || undefined,
        type: filters.type || undefined,
        entrySide: filters.entrySide || undefined,
        status: filters.status || undefined,
        page: 0,
        size: 20,
      })
      setData(response.data)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>Transaction History</h2>
          <p className="profile-subtitle">Admin history explorer across accounts and users.</p>
        </div>
      </div>

      <form className="form-grid customer-filter-grid" onSubmit={(event) => event.preventDefault()}>
        <label>
          Account ID
          <input name="accountId" value={filters.accountId} onChange={onChange} />
        </label>
        <label>
          User ID
          <input name="userId" value={filters.userId} onChange={onChange} />
        </label>
        <label>
          Type
          <select name="type" value={filters.type} onChange={onChange}>
            <option value="">All</option>
            <option value="DEPOSIT">DEPOSIT</option>
            <option value="WITHDRAWAL">WITHDRAWAL</option>
            <option value="TRANSFER">TRANSFER</option>
            <option value="ADJUSTMENT">ADJUSTMENT</option>
            <option value="REVERSAL">REVERSAL</option>
          </select>
        </label>
        <label>
          Side
          <select name="entrySide" value={filters.entrySide} onChange={onChange}>
            <option value="">All</option>
            <option value="DEBIT">DEBIT</option>
            <option value="CREDIT">CREDIT</option>
          </select>
        </label>
        <label>
          Status
          <select name="status" value={filters.status} onChange={onChange}>
            <option value="">All</option>
            <option value="POSTED">POSTED</option>
            <option value="REVERSED">REVERSED</option>
          </select>
        </label>
        <div className="full-width customer-filter-actions">
          <button type="button" className="button button-secondary" onClick={load} disabled={loading}>
            {loading ? 'Loading...' : 'Search'}
          </button>
        </div>
      </form>

      {error ? <p className="status-error">{error}</p> : null}

      {data.items?.length ? (
        <div className="customer-table-wrap">
          <table className="customer-table">
            <thead>
              <tr>
                <th>Ref</th>
                <th>Account</th>
                <th>Type</th>
                <th>Side</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((item) => (
                <tr key={item.transactionRef}>
                  <td className="customer-contact-cell">{item.transactionRef}</td>
                  <td>{item.accountId}</td>
                  <td>{item.transactionType}</td>
                  <td>{item.entrySide}</td>
                  <td>{item.currencyCode} {item.amount}</td>
                  <td>{item.status}</td>
                  <td>{item.createdAt ? new Date(item.createdAt).toLocaleString() : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>No history found.</p>
      )}
    </section>
  )
}

