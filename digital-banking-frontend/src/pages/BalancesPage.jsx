import { useCallback, useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { listMyAccounts } from '../api/accountApi'
import { getMyBalance, getMyMiniStatement } from '../api/balanceApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

export default function BalancesPage() {
  const { user } = useAuth()
  const [accounts, setAccounts] = useState([])
  const [accountId, setAccountId] = useState('')
  const [balance, setBalance] = useState(null)
  const [statement, setStatement] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadAccounts = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const response = await listMyAccounts({ page: 0, size: 100, sort: 'createdAt,desc' })
      const items = response.data?.items || []
      setAccounts(items)
      if (items.length && !accountId) {
        setAccountId(String(items[0].accountId))
      }
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [accountId])

  const loadBalance = useCallback(async () => {
    if (!accountId) {
      setBalance(null)
      setStatement([])
      return
    }
    setLoading(true)
    setError('')
    try {
      const [balanceRes, statementRes] = await Promise.all([
        getMyBalance(accountId),
        getMyMiniStatement(accountId, { limit: 10 }),
      ])
      setBalance(balanceRes.data)
      setStatement(statementRes.data?.items || [])
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  useEffect(() => {
    if (accountId) {
      loadBalance()
    }
  }, [accountId, loadBalance])

  if (user?.role !== 'CUSTOMER') {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin/balances' : '/me'} replace />
  }

  return (
    <section className="panel stack">
      <div className="profile-header">
        <div>
          <h2>Balance Enquiry</h2>
          <p className="profile-subtitle">Check available and ledger balance with a mini statement.</p>
        </div>
      </div>

      <form className="form-grid" onSubmit={(event) => event.preventDefault()}>
        <label>
          Account
          <select value={accountId} onChange={(event) => setAccountId(event.target.value)} disabled={!accounts.length}>
            {accounts.length ? (
              accounts.map((account) => (
                <option key={account.accountId} value={account.accountId}>
                  {account.accountNumber}
                </option>
              ))
            ) : (
              <option value="">No accounts</option>
            )}
          </select>
        </label>
        <div className="full-width customer-filter-actions">
          <button type="button" className="button button-secondary" onClick={loadBalance} disabled={!accountId || loading}>
            Refresh
          </button>
        </div>
      </form>

      {error ? <p className="status-error">{error}</p> : null}

      {balance ? (
        <div className="profile-kv-grid">
          <div className="kv-item">
            <span className="kv-label">Available Balance</span>
            <span className="kv-value">{balance.currencyCode} {balance.availableBalance}</span>
          </div>
          <div className="kv-item">
            <span className="kv-label">Ledger Balance</span>
            <span className="kv-value">{balance.currencyCode} {balance.ledgerBalance}</span>
          </div>
          <div className="kv-item">
            <span className="kv-label">Account Status</span>
            <span className="kv-value">{balance.accountStatus}</span>
          </div>
          <div className="kv-item">
            <span className="kv-label">As Of</span>
            <span className="kv-value">{balance.asOf ? new Date(balance.asOf).toLocaleString() : '-'}</span>
          </div>
        </div>
      ) : null}

      <div>
        <h3>Mini Statement</h3>
        {loading ? (
          <p>Loading mini statement...</p>
        ) : statement.length ? (
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
                {statement.map((item) => (
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
          <p>No transactions found for this account.</p>
        )}
      </div>
    </section>
  )
}

