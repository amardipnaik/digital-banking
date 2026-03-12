import { useCallback, useEffect, useState } from 'react'
import { confirmVerification, me, requestVerification, updateMyProfile } from '../api/authApi'
import { useAuth } from '../context/useAuth'
import { getApiErrorMessage } from '../lib/http'

function statusPillClass(status) {
  if (status === 'ACTIVE') {
    return 'pill pill-success'
  }
  if (status === 'PENDING_VERIFICATION') {
    return 'pill pill-warning'
  }
  return 'pill pill-danger'
}

function verificationPillClass(isVerified) {
  return isVerified ? 'pill pill-success' : 'pill pill-warning'
}

export default function MePage() {
  const { user: sessionUser, setAuth, token } = useAuth()
  const [profile, setProfile] = useState(sessionUser)
  const [profileForm, setProfileForm] = useState({
    fullName: '',
    dateOfBirth: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    state: '',
    postalCode: '',
    country: '',
    governmentId: '',
    governmentIdType: '',
  })
  const [emailToken, setEmailToken] = useState('')
  const [mobileToken, setMobileToken] = useState('')
  const [emailMessage, setEmailMessage] = useState('')
  const [mobileMessage, setMobileMessage] = useState('')
  const [profileMessage, setProfileMessage] = useState('')
  const [emailOtpRequested, setEmailOtpRequested] = useState(false)
  const [mobileOtpRequested, setMobileOtpRequested] = useState(false)
  const [error, setError] = useState('')
  const [loadingEmail, setLoadingEmail] = useState(false)
  const [loadingMobile, setLoadingMobile] = useState(false)
  const [savingProfile, setSavingProfile] = useState(false)

  function mapUser(responseData) {
    return {
      id: responseData.id,
      role: responseData.role,
      status: responseData.status,
      email: responseData.email,
      mobileNumber: responseData.mobileNumber,
      emailVerified: responseData.emailVerified,
      mobileVerified: responseData.mobileVerified,
      fullName: responseData.fullName || '',
      dateOfBirth: responseData.dateOfBirth || '',
      addressLine1: responseData.addressLine1 || '',
      addressLine2: responseData.addressLine2 || '',
      city: responseData.city || '',
      state: responseData.state || '',
      postalCode: responseData.postalCode || '',
      country: responseData.country || '',
      governmentId: responseData.governmentId || '',
      governmentIdType: responseData.governmentIdType || '',
      kycStatus: responseData.kycStatus || '',
    }
  }

  function mapForm(userData) {
    return {
      fullName: userData.fullName || '',
      dateOfBirth: userData.dateOfBirth || '',
      addressLine1: userData.addressLine1 || '',
      addressLine2: userData.addressLine2 || '',
      city: userData.city || '',
      state: userData.state || '',
      postalCode: userData.postalCode || '',
      country: userData.country || '',
      governmentId: userData.governmentId || '',
      governmentIdType: userData.governmentIdType || '',
    }
  }

  const loadProfile = useCallback(async () => {
    const response = await me()
    const freshUser = mapUser(response.data)
    setProfile(freshUser)
    setProfileForm(mapForm(freshUser))
    setAuth(token, freshUser)
  }, [setAuth, token])

  useEffect(() => {
    let mounted = true
    me()
      .then((response) => {
        if (!mounted) {
          return
        }
        const freshUser = mapUser(response.data)
        setProfile(freshUser)
        setProfileForm(mapForm(freshUser))
        setAuth(token, freshUser)
      })
      .catch((err) => {
        if (mounted) {
          setError(getApiErrorMessage(err))
        }
      })
    return () => {
      mounted = false
    }
  }, [setAuth, token])

  async function handleRequest(channel) {
    if (!profile) {
      return
    }
    setError('')
    if (channel === 'EMAIL') {
      setLoadingEmail(true)
      setEmailMessage('')
    } else {
      setLoadingMobile(true)
      setMobileMessage('')
    }

    try {
      const loginId = channel === 'EMAIL' ? profile.email : profile.mobileNumber
      const response = await requestVerification({ loginId, channel })
      const message = response?.data?.message || `${channel} verification token requested successfully.`
      if (channel === 'EMAIL') {
        setEmailToken('')
        setEmailOtpRequested(true)
        setEmailMessage(message)
      } else {
        setMobileToken('')
        setMobileOtpRequested(true)
        setMobileMessage(message)
      }
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      if (channel === 'EMAIL') {
        setLoadingEmail(false)
      } else {
        setLoadingMobile(false)
      }
    }
  }

  async function handleConfirm(channel) {
    if (!profile) {
      return
    }
    setError('')
    const tokenValue = channel === 'EMAIL' ? emailToken : mobileToken
    if (!tokenValue.trim()) {
      setError(`Enter ${channel} verification token.`)
      return
    }

    if (channel === 'EMAIL') {
      setLoadingEmail(true)
      setEmailMessage('')
    } else {
      setLoadingMobile(true)
      setMobileMessage('')
    }

    try {
      const loginId = channel === 'EMAIL' ? profile.email : profile.mobileNumber
      const response = await confirmVerification({ loginId, channel, token: tokenValue.trim() })
      const message = response?.data?.message || `${channel} verified successfully.`

      if (channel === 'EMAIL') {
        setEmailMessage(message)
        setEmailToken('')
        setEmailOtpRequested(false)
      } else {
        setMobileMessage(message)
        setMobileToken('')
        setMobileOtpRequested(false)
      }

      await loadProfile()
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      if (channel === 'EMAIL') {
        setLoadingEmail(false)
      } else {
        setLoadingMobile(false)
      }
    }
  }

  function onProfileChange(event) {
    const { name, value } = event.target
    setProfileForm((previous) => ({ ...previous, [name]: value }))
  }

  async function saveProfile(event) {
    event.preventDefault()
    setError('')
    setProfileMessage('')
    setSavingProfile(true)
    try {
      const payload = {
        fullName: profileForm.fullName.trim(),
        dateOfBirth: profileForm.dateOfBirth || null,
        addressLine1: profileForm.addressLine1,
        addressLine2: profileForm.addressLine2,
        city: profileForm.city,
        state: profileForm.state,
        postalCode: profileForm.postalCode,
        country: profileForm.country,
        governmentId: profileForm.governmentId,
        governmentIdType: profileForm.governmentIdType,
      }
      const response = await updateMyProfile(payload)
      const freshUser = mapUser(response.data)
      setProfile(freshUser)
      setProfileForm(mapForm(freshUser))
      setAuth(token, freshUser)
      setProfileMessage('Profile updated successfully.')
    } catch (err) {
      setError(getApiErrorMessage(err))
    } finally {
      setSavingProfile(false)
    }
  }

  return (
    <div className="stack">
      <section className="panel profile-panel">
        <div className="profile-header">
          <div>
            <h2>My Profile</h2>
            <p className="profile-subtitle">Manage your account details and verification status.</p>
          </div>
          {profile ? (
            <div className="profile-badges">
              <span className="pill pill-role">{profile.role}</span>
              <span className={statusPillClass(profile.status)}>{profile.status}</span>
            </div>
          ) : null}
        </div>

        {error ? <p className="status-error">{error}</p> : null}
        {profile ? (
          <div className="profile-kv-grid">
            <div className="kv-item">
              <span className="kv-label">Email</span>
              <span className="kv-value">{profile.email}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Mobile</span>
              <span className="kv-value">{profile.mobileNumber}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Full Name</span>
              <span className="kv-value">{profile.fullName || '-'}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Email Verification</span>
              <span className={verificationPillClass(profile.emailVerified)}>{profile.emailVerified ? 'Verified' : 'Pending'}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Mobile Verification</span>
              <span className={verificationPillClass(profile.mobileVerified)}>{profile.mobileVerified ? 'Verified' : 'Pending'}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">Date of Birth</span>
              <span className="kv-value">{profile.dateOfBirth || '-'}</span>
            </div>
            <div className="kv-item">
              <span className="kv-label">KYC Status</span>
              <span className="kv-value">{profile.kycStatus || '-'}</span>
            </div>
          </div>
        ) : (
          <p>Loading profile...</p>
        )}
      </section>

      {profile?.role === 'CUSTOMER' ? (
        <section className="panel profile-panel">
          <div className="profile-header">
            <div>
              <h3>Edit Profile</h3>
              <p className="profile-subtitle">Keep your personal and KYC profile details up to date.</p>
            </div>
          </div>
          <form className="form-grid" onSubmit={saveProfile}>
            <label>
              Full Name
              <input name="fullName" value={profileForm.fullName} onChange={onProfileChange} required />
            </label>
            <label>
              Date of Birth
              <input type="date" name="dateOfBirth" value={profileForm.dateOfBirth || ''} onChange={onProfileChange} />
            </label>
            <label>
              Address Line 1
              <input name="addressLine1" value={profileForm.addressLine1} onChange={onProfileChange} />
            </label>
            <label>
              Address Line 2
              <input name="addressLine2" value={profileForm.addressLine2} onChange={onProfileChange} />
            </label>
            <label>
              City
              <input name="city" value={profileForm.city} onChange={onProfileChange} />
            </label>
            <label>
              State
              <input name="state" value={profileForm.state} onChange={onProfileChange} />
            </label>
            <label>
              Postal Code
              <input name="postalCode" value={profileForm.postalCode} onChange={onProfileChange} />
            </label>
            <label>
              Country
              <input name="country" value={profileForm.country} onChange={onProfileChange} />
            </label>
            <label>
              Government ID
              <input name="governmentId" value={profileForm.governmentId} onChange={onProfileChange} />
            </label>
            <label>
              Government ID Type
              <input name="governmentIdType" value={profileForm.governmentIdType} onChange={onProfileChange} />
            </label>
            <div className="full-width">
              <button className="button" type="submit" disabled={savingProfile}>
                {savingProfile ? 'Saving...' : 'Save Profile'}
              </button>
            </div>
          </form>
          {profileMessage ? <p className="status-success">{profileMessage}</p> : null}
        </section>
      ) : null}

      {profile && (!profile.emailVerified || !profile.mobileVerified) ? (
        <section className="panel verification-panel">
          <h3>Complete Verification</h3>
          <p className="profile-subtitle">Verify your email and mobile to complete onboarding.</p>
          <div className="verification-grid">
            {!profile.emailVerified ? (
              <div className="verify-card">
                <div className="verify-card-head">
                  <h4>Verify Email</h4>
                  <span className="pill pill-warning">Pending</span>
                </div>
                <p className="verify-target">{profile.email}</p>
                <div className="verify-form">
                  <button className="button button-secondary auth-submit" type="button" onClick={() => handleRequest('EMAIL')} disabled={loadingEmail}>
                    {loadingEmail ? 'Requesting...' : emailOtpRequested ? 'Resend Email OTP' : 'Request Email OTP'}
                  </button>
                  {emailOtpRequested ? (
                    <div className="otp-panel">
                      <label className="verify-label">
                        Enter Email OTP
                        <input value={emailToken} onChange={(event) => setEmailToken(event.target.value)} placeholder="6-digit OTP" />
                      </label>
                      <button className="button auth-submit" type="button" onClick={() => handleConfirm('EMAIL')} disabled={loadingEmail}>
                        {loadingEmail ? 'Verifying...' : 'Verify Email'}
                      </button>
                    </div>
                  ) : (
                    <p className="verify-note">Click the button above to receive the OTP and continue verification.</p>
                  )}
                  {emailMessage ? <p className="status-success">{emailMessage}</p> : null}
                </div>
              </div>
            ) : null}

            {!profile.mobileVerified ? (
              <div className="verify-card">
                <div className="verify-card-head">
                  <h4>Verify Mobile</h4>
                  <span className="pill pill-warning">Pending</span>
                </div>
                <p className="verify-target">{profile.mobileNumber}</p>
                <div className="verify-form">
                  <button className="button button-secondary auth-submit" type="button" onClick={() => handleRequest('MOBILE')} disabled={loadingMobile}>
                    {loadingMobile ? 'Requesting...' : mobileOtpRequested ? 'Resend Mobile OTP' : 'Request Mobile OTP'}
                  </button>
                  {mobileOtpRequested ? (
                    <div className="otp-panel">
                      <label className="verify-label">
                        Enter Mobile OTP
                        <input value={mobileToken} onChange={(event) => setMobileToken(event.target.value)} placeholder="6-digit OTP" />
                      </label>
                      <button className="button auth-submit" type="button" onClick={() => handleConfirm('MOBILE')} disabled={loadingMobile}>
                        {loadingMobile ? 'Verifying...' : 'Verify Mobile'}
                      </button>
                    </div>
                  ) : (
                    <p className="verify-note">Click the button above to receive the OTP and continue verification.</p>
                  )}
                  {mobileMessage ? <p className="status-success">{mobileMessage}</p> : null}
                </div>
              </div>
            ) : null}
          </div>
        </section>
      ) : null}
    </div>
  )
}
