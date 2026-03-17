import { useCallback, useEffect, useState } from 'react'
import { listAdminAccounts, updateAdminAccountStatus } from '../api/accountApi'
import { getApiErrorMessage } from '../lib/http'

const DEFAULT_FILTERS = {
  search: '',
  status: '',
  accountType: '',
}

export default function AdminAccountsPage() {
  const [filters, setFilters] = useState(DEFAULT_FILTERS)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [busyAccountId, setBusyAccountId] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadAccounts = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const response = await listAdminAccounts({
        search: filters.search.trim() || undefined,
        status: filters.status || undefined,
        accountType: filters.accountType || undefined,
        page: 0,
        size: 20,
      })
      setItems(response.data?.items || [])
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [filters])

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  function onFilterChange(event) {
    const { name, value } = event.target
    setFilters((previous) => ({ ...previous, [name]: value }))
  }

  function resetFilters() {
    setFilters(DEFAULT_FILTERS)
  }

  function statusActions(status) {
    if (status === 'PENDING_APPROVAL') return ['ACTIVE', 'CLOSED']
    if (status === 'ACTIVE') return ['FROZEN', 'CLOSED']
    if (status === 'FROZEN') return ['ACTIVE', 'CLOSED']
    return []
  }

  async function handleStatus(accountId, nextStatus) {
    setBusyAccountId(accountId)
    setError('')
    setMessage('')
    try {
      const response = await updateAdminAccountStatus(accountId, {
        status: nextStatus,
        reason: `Updated by admin to ${nextStatus}`,
      })
      setMessage(`Account ${response.data.accountNumber} moved to ${response.data.status}.`)
      await loadAccounts()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setBusyAccountId(null)
    }
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>Account Management</h2>
          <p className="profile-subtitle">Review accounts and control lifecycle states.</p>
        </div>
      </div>

      <form className="form-grid customer-filter-grid" onSubmit={(event) => event.preventDefault()}>
        <label>
          Search
          <input name="search" value={filters.search} onChange={onFilterChange} placeholder="Account no, name, email, mobile" />
        </label>
        <label>
          Account Type
          <select name="accountType" value={filters.accountType} onChange={onFilterChange}>
            <option value="">All</option>
            <option value="SAVINGS">SAVINGS</option>
            <option value="CURRENT">CURRENT</option>
          </select>
        </label>
        <label>
          Status
          <select name="status" value={filters.status} onChange={onFilterChange}>
            <option value="">All</option>
            <option value="PENDING_APPROVAL">PENDING_APPROVAL</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="FROZEN">FROZEN</option>
            <option value="CLOSED">CLOSED</option>
          </select>
        </label>
        <div className="full-width customer-filter-actions">
          <button type="button" className="button button-secondary" onClick={loadAccounts}>
            Refresh
          </button>
          <button type="button" className="button button-secondary" onClick={resetFilters}>
            Reset Filters
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
                <th>Customer</th>
                <th>Email</th>
                <th>Type</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {items.map((account) => {
                const actions = statusActions(account.status)
                return (
                  <tr key={account.accountId}>
                    <td className="customer-name-cell">{account.accountNumber}</td>
                    <td>{account.customerName || '-'}</td>
                    <td className="customer-contact-cell">{account.customerEmail}</td>
                    <td>{account.accountType}</td>
                    <td>
                      <span className={`pill ${account.status === 'ACTIVE' ? 'pill-success' : account.status === 'CLOSED' ? 'pill-danger' : 'pill-warning'}`}>
                        {account.status}
                      </span>
                    </td>
                    <td className="customer-actions">
                      {actions.length ? (
                        actions.map((nextStatus) => (
                          <button
                            key={nextStatus}
                            className="button button-secondary customer-action-btn"
                            type="button"
                            disabled={busyAccountId === account.accountId}
                            onClick={() => handleStatus(account.accountId, nextStatus)}
                          >
                            {nextStatus === 'ACTIVE' ? 'Approve/Enable' : nextStatus === 'FROZEN' ? 'Freeze' : 'Close'}
                          </button>
                        ))
                      ) : (
                        <span className="profile-subtitle">No actions</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      ) : (
        <p>No accounts found for selected filters.</p>
      )}
    </section>
  )
}

