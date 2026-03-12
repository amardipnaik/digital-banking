import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { updateUserStatus } from '../api/authApi'
import { listCustomers } from '../api/customerApi'
import { getApiErrorMessage } from '../lib/http'

const DEFAULT_FILTERS = {
  search: '',
  userStatus: '',
  kycStatus: '',
}

export default function AdminCustomersPage() {
  const [filters, setFilters] = useState(DEFAULT_FILTERS)
  const [data, setData] = useState({ items: [], page: 0, totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [busyUserId, setBusyUserId] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadCustomers = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const response = await listCustomers({
        search: filters.search.trim() || undefined,
        userStatus: filters.userStatus || undefined,
        kycStatus: filters.kycStatus || undefined,
        page: 0,
        size: 20,
      })
      setData(response.data)
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [filters])

  useEffect(() => {
    loadCustomers()
  }, [loadCustomers])

  function onFilterChange(event) {
    const { name, value } = event.target
    setFilters((previous) => ({ ...previous, [name]: value }))
  }

  async function handleStatus(userId, status) {
    setError('')
    setMessage('')
    setBusyUserId(userId)
    try {
      const response = await updateUserStatus(userId, {
        status,
        reason: status === 'DISABLED' ? 'Disabled by admin review.' : 'Re-enabled by admin review.',
      })
      setMessage(response?.message || 'Status updated.')
      await loadCustomers()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setBusyUserId(null)
    }
  }

  function resetFilters() {
    setFilters(DEFAULT_FILTERS)
  }

  function userStatusPillClass(status) {
    if (status === 'ACTIVE') return 'pill pill-success'
    if (status === 'PENDING_VERIFICATION' || status === 'LOCKED') return 'pill pill-warning'
    return 'pill pill-danger'
  }

  function kycPillClass(status) {
    if (status === 'APPROVED') return 'pill pill-success'
    if (status === 'PENDING') return 'pill pill-warning'
    return 'pill pill-danger'
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>Admin: Customer Management</h2>
          <p className="profile-subtitle">Review, verify KYC, and control account status.</p>
        </div>
      </div>

      <form className="form-grid customer-filter-grid" onSubmit={(event) => event.preventDefault()}>
        <label>
          Search
          <input name="search" value={filters.search} onChange={onFilterChange} placeholder="Name, email, or mobile" />
        </label>
        <label>
          User Status
          <select name="userStatus" value={filters.userStatus} onChange={onFilterChange}>
            <option value="">All</option>
            <option value="PENDING_VERIFICATION">PENDING_VERIFICATION</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="LOCKED">LOCKED</option>
            <option value="DISABLED">DISABLED</option>
          </select>
        </label>
        <label>
          KYC Status
          <select name="kycStatus" value={filters.kycStatus} onChange={onFilterChange}>
            <option value="">All</option>
            <option value="PENDING">PENDING</option>
            <option value="APPROVED">APPROVED</option>
            <option value="REJECTED">REJECTED</option>
          </select>
        </label>
        <div className="full-width customer-filter-actions">
          <button type="button" className="button button-secondary" onClick={loadCustomers}>
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
        <p>Loading customers...</p>
      ) : data.items?.length ? (
        <div className="customer-table-wrap">
          <table className="customer-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Mobile</th>
                <th>User Status</th>
                <th>KYC</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((customer) => (
                <tr key={customer.userId}>
                  <td className="customer-name-cell">{customer.fullName}</td>
                  <td className="customer-contact-cell">{customer.email}</td>
                  <td className="customer-contact-cell">{customer.mobileNumber}</td>
                  <td>
                    <span className={userStatusPillClass(customer.userStatus)}>{customer.userStatus}</span>
                  </td>
                  <td>
                    <span className={kycPillClass(customer.kycStatus)}>{customer.kycStatus}</span>
                  </td>
                  <td className="customer-actions">
                    <Link className="button button-secondary customer-action-btn customer-action-view" to={`/admin/customers/${customer.userId}`}>
                      View
                    </Link>
                    <button
                      className="button button-secondary customer-action-btn"
                      type="button"
                      onClick={() => handleStatus(customer.userId, customer.userStatus === 'DISABLED' ? 'ACTIVE' : 'DISABLED')}
                      disabled={busyUserId === customer.userId}
                    >
                      {customer.userStatus === 'DISABLED' ? 'Enable' : 'Disable'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p>No customers found for selected filters.</p>
      )}

      <p className="profile-subtitle">Showing page {data.page + 1} of {Math.max(data.totalPages, 1)}.</p>
    </section>
  )
}

