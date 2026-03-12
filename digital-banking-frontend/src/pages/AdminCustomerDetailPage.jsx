import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { updateUserStatus } from '../api/authApi'
import { getCustomer, getCustomerActivity, updateCustomerKyc, updateCustomerProfile } from '../api/customerApi'
import { getApiErrorMessage } from '../lib/http'

function toProfileForm(profile) {
  return {
    fullName: profile?.fullName || '',
    dateOfBirth: profile?.dateOfBirth || '',
    addressLine1: profile?.addressLine1 || '',
    addressLine2: profile?.addressLine2 || '',
    city: profile?.city || '',
    state: profile?.state || '',
    postalCode: profile?.postalCode || '',
    country: profile?.country || '',
    governmentId: profile?.governmentId || '',
    governmentIdType: profile?.governmentIdType || '',
  }
}

export default function AdminCustomerDetailPage() {
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState(toProfileForm(null))
  const [kycStatus, setKycStatus] = useState('PENDING')
  const [kycRemarks, setKycRemarks] = useState('')
  const [activity, setActivity] = useState([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const normalizedUserId = useMemo(() => Number(userId), [userId])

  const loadData = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [profileResponse, activityResponse] = await Promise.all([
        getCustomer(normalizedUserId),
        getCustomerActivity(normalizedUserId, { page: 0, size: 20 }),
      ])
      setProfile(profileResponse.data)
      setForm(toProfileForm(profileResponse.data))
      setKycStatus(profileResponse.data.kycStatus)
      setKycRemarks(profileResponse.data.kycRemarks || '')
      setActivity(activityResponse.data.items || [])
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [normalizedUserId])

  useEffect(() => {
    if (!Number.isFinite(normalizedUserId)) {
      setError('Invalid customer id.')
      setLoading(false)
      return
    }
    loadData()
  }, [loadData, normalizedUserId])

  function onProfileFieldChange(event) {
    const { name, value } = event.target
    setForm((previous) => ({ ...previous, [name]: value }))
  }

  async function saveProfile(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const payload = {
        ...form,
        fullName: form.fullName.trim(),
      }
      const response = await updateCustomerProfile(normalizedUserId, payload)
      setProfile(response.data)
      setMessage('Customer profile updated successfully.')
      await loadData()
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  async function saveKyc(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      await updateCustomerKyc(normalizedUserId, {
        kycStatus,
        remarks: kycRemarks,
      })
      setMessage('KYC updated successfully.')
      await loadData()
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  async function updateStatus(status) {
    setError('')
    setMessage('')
    try {
      const response = await updateUserStatus(normalizedUserId, {
        status,
        reason: status === 'DISABLED' ? 'Disabled during customer review.' : 'Re-enabled after review.',
      })
      setMessage(response?.message || 'Status updated successfully.')
      await loadData()
    } catch (err) {
      setError(getApiErrorMessage(err))
    }
  }

  return (
    <div className="stack">
      <section className="panel profile-panel">
        <div className="profile-header">
          <div>
            <h2>Customer Detail</h2>
            <p className="profile-subtitle">Manage profile, KYC, and lifecycle actions for customer #{userId}.</p>
          </div>
          <Link to="/admin/customers" className="text-link">
            Back to Customers
          </Link>
        </div>

        {message ? <p className="status-success">{message}</p> : null}
        {error ? <p className="status-error">{error}</p> : null}

        {loading ? <p>Loading customer...</p> : null}
        {profile ? (
          <div className="profile-kv-grid">
            <div className="kv-item"><span className="kv-label">Name</span><span className="kv-value">{profile.fullName}</span></div>
            <div className="kv-item"><span className="kv-label">Email</span><span className="kv-value">{profile.email}</span></div>
            <div className="kv-item"><span className="kv-label">Mobile</span><span className="kv-value">{profile.mobileNumber}</span></div>
            <div className="kv-item"><span className="kv-label">User Status</span><span className="kv-value">{profile.userStatus}</span></div>
            <div className="kv-item"><span className="kv-label">KYC</span><span className="kv-value">{profile.kycStatus}</span></div>
            <div className="kv-item"><span className="kv-label">Deleted</span><span className="kv-value">{profile.deleted ? 'Yes' : 'No'}</span></div>
          </div>
        ) : null}
      </section>

      <section className="panel">
        <h3>Edit Profile</h3>
        <form className="form-grid" onSubmit={saveProfile}>
          <label>
            Full Name
            <input name="fullName" value={form.fullName} onChange={onProfileFieldChange} required />
          </label>
          <label>
            Date of Birth
            <input type="date" name="dateOfBirth" value={form.dateOfBirth || ''} onChange={onProfileFieldChange} />
          </label>
          <label>
            Address Line 1
            <input name="addressLine1" value={form.addressLine1} onChange={onProfileFieldChange} />
          </label>
          <label>
            Address Line 2
            <input name="addressLine2" value={form.addressLine2} onChange={onProfileFieldChange} />
          </label>
          <label>
            City
            <input name="city" value={form.city} onChange={onProfileFieldChange} />
          </label>
          <label>
            State
            <input name="state" value={form.state} onChange={onProfileFieldChange} />
          </label>
          <label>
            Postal Code
            <input name="postalCode" value={form.postalCode} onChange={onProfileFieldChange} />
          </label>
          <label>
            Country
            <input name="country" value={form.country} onChange={onProfileFieldChange} />
          </label>
          <label>
            Government ID
            <input name="governmentId" value={form.governmentId} onChange={onProfileFieldChange} />
          </label>
          <label>
            Government ID Type
            <input name="governmentIdType" value={form.governmentIdType} onChange={onProfileFieldChange} />
          </label>
          <div className="full-width">
            <button type="submit" className="button">Save Profile</button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h3>KYC and Account Status</h3>
        <form className="form-grid" onSubmit={saveKyc}>
          <label>
            KYC Status
            <select value={kycStatus} onChange={(event) => setKycStatus(event.target.value)}>
              <option value="PENDING">PENDING</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </select>
          </label>
          <label>
            Remarks
            <input value={kycRemarks} onChange={(event) => setKycRemarks(event.target.value)} />
          </label>
          <div className="full-width customer-actions">
            <button type="submit" className="button">Update KYC</button>
            <button
              type="button"
              className="button button-secondary"
              onClick={() => updateStatus(profile?.userStatus === 'DISABLED' ? 'ACTIVE' : 'DISABLED')}
            >
              {profile?.userStatus === 'DISABLED' ? 'Enable' : 'Disable'}
            </button>
          </div>
        </form>
      </section>

      <section className="panel">
        <h3>Admin Activity Timeline</h3>
        {activity.length ? (
          <div className="timeline-list">
            {activity.map((item) => (
              <article key={item.id} className="timeline-item">
                <strong>{item.actionType}</strong>
                <p className="profile-subtitle">Admin #{item.adminUserId} at {new Date(item.createdAt).toLocaleString()}</p>
                {item.reason ? <p>{item.reason}</p> : null}
              </article>
            ))}
          </div>
        ) : (
          <p>No activity logged yet.</p>
        )}
      </section>
    </div>
  )
}

