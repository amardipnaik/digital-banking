import { useState } from 'react'
import { getAdminBalance, getAdminMiniStatement } from '../api/balanceApi'
import { getApiErrorMessage } from '../lib/http'

export default function AdminBalancesPage() {
  const [accountId, setAccountId] = useState('')
  const [balance, setBalance] = useState(null)
  const [statement, setStatement] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    if (!accountId.trim()) {
      setError('Enter account id.')
      return
    }
    setLoading(true)
    setError('')
    try {
      const [balanceRes, statementRes] = await Promise.all([
        getAdminBalance(accountId.trim()),
        getAdminMiniStatement(accountId.trim(), { limit: 10 }),
      ])
      setBalance(balanceRes.data)
      setStatement(statementRes.data?.items || [])
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
          <h2>Balance Enquiry</h2>
          <p className="profile-subtitle">Admin balance and mini statement lookup by account id.</p>
        </div>
      </div>

      <form className="form-grid" onSubmit={(event) => event.preventDefault()}>
        <label>
          Account ID
          <input value={accountId} onChange={(event) => setAccountId(event.target.value)} />
        </label>
        <div className="full-width customer-filter-actions">
          <button type="button" className="button button-secondary" onClick={load} disabled={loading}>
            {loading ? 'Loading...' : 'Fetch'}
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
        </div>
      ) : null}

      {statement.length ? (
        <div className="customer-table-wrap">
          <table className="customer-table">
            <thead>
              <tr>
                <th>Ref</th>
                <th>Type</th>
                <th>Side</th>
                <th>Amount</th>
                <th>Status</th>
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
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  )
}

