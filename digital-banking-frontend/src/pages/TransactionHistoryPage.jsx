import { useCallback, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { listMyAccounts } from '../api/accountApi'
import { listMyTransactionHistory } from '../api/transactionHistoryApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

export default function TransactionHistoryPage() {
  const { user } = useAuth()
  const [accounts, setAccounts] = useState([])
  const [filters, setFilters] = useState({ accountId: '', type: '', entrySide: '', status: '' })
  const [data, setData] = useState({ items: [], page: 0, totalPages: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let mounted = true
    listMyAccounts({ page: 0, size: 100, sort: 'createdAt,desc' })
      .then((response) => {
        if (!mounted) return
        const items = response.data?.items || []
        setAccounts(items)
        if (items.length) {
          setFilters((prev) => ({ ...prev, accountId: String(items[0].accountId) }))
        }
      })
      .catch((err) => {
        if (mounted) setError(getApiErrorMessage(err))
      })
    return () => {
      mounted = false
    }
  }, [])

  const load = useCallback(async () => {
    if (!filters.accountId) {
      setData({ items: [], page: 0, totalPages: 0 })
      return
    }
    setLoading(true)
    setError('')
    try {
      const response = await listMyTransactionHistory({
        accountId: Number(filters.accountId),
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
  }, [filters])

  useEffect(() => {
    load()
  }, [load])

  function onChange(event) {
    const { name, value } = event.target
    setFilters((prev) => ({ ...prev, [name]: value }))
  }

  if (user?.role !== 'CUSTOMER') {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin/transactions/history' : '/me'} replace />
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>Transaction History</h2>
          <p className="profile-subtitle">Browse your posted transactions with filters.</p>
        </div>
      </div>

      <form className="form-grid customer-filter-grid" onSubmit={(event) => event.preventDefault()}>
        <label>
          Account
          <select name="accountId" value={filters.accountId} onChange={onChange}>
            {accounts.map((account) => (
              <option key={account.accountId} value={account.accountId}>{account.accountNumber}</option>
            ))}
          </select>
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
      </form>

      {error ? <p className="status-error">{error}</p> : null}

      {loading ? (
        <p>Loading history...</p>
      ) : data.items?.length ? (
        <div className="customer-table-wrap">
          <table className="customer-table">
            <thead>
              <tr>
                <th>Ref</th>
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
        <p>No history found for selected filters.</p>
      )}
    </section>
  )
}

